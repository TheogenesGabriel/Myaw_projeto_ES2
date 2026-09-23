package com.myow;

import com.myow.dao.*;
import com.myow.database.DatabaseManager;
import com.myow.model.*;
import com.myow.service.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MyowSystemIntegrationTest {

    private static DatabaseManager dbManager;
    private final AuthService authService = new AuthService();
    private final TutorService tutorService = new TutorService();
    private final PacienteService pacienteService = new PacienteService();
    private final AgendamentoService agendamentoService = new AgendamentoService();
    private final EstoqueService estoqueService = new EstoqueService();
    private final AtendimentoService atendimentoService = new AtendimentoService();
    private final FaturamentoService faturamentoService = new FaturamentoService();
    private final FuncionarioService funcionarioService = new FuncionarioService();
    private final RelatorioService relatorioService = new RelatorioService();

    @BeforeAll
    static void setup() {
        dbManager = DatabaseManager.getInstance();
        assertNotNull(dbManager);
    }

    @Test
    @Order(1)
    @DisplayName("Teste 1: Verificar se o banco SQLite foi criado e possui as 7 tabelas e dados seed")
    void testBancoDeDadosEInicializacao() throws Exception {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Verifica se as tabelas existem no sqlite_master
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name IN ('usuarios', 'tutores', 'pacientes', 'consultas', 'atendimentos', 'atendimento_itens', 'itens_estoque', 'faturamentos')");
            assertTrue(rs.next());
            assertEquals(8, rs.getInt(1), "Todas as 8 tabelas do modelo relacional devem existir no SQLite");

            // Verifica integridade referencial ativa
            ResultSet pragmaRs = stmt.executeQuery("PRAGMA foreign_keys");
            assertTrue(pragmaRs.next());
            assertEquals(1, pragmaRs.getInt(1), "Chaves estrangeiras devem estar ativas");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Teste 2 (UC01): Login com credenciais válidas e contador de tentativas")
    void testLoginValidoEInvalido() {
        // Login com sucesso
        OperationResult<Usuario> resOk = authService.login("admin", "admin123");
        assertTrue(resOk.isSuccess());
        assertNotNull(resOk.getData());
        assertEquals("admin", resOk.getData().getLogin());
        assertEquals(0, resOk.getData().getTentativasLogin());

        // Login com senha incorreta - tentativa 1
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Optional<Usuario> userOpt = usuarioDAO.buscarPorLogin("admin");
        assertTrue(userOpt.isPresent());

        OperationResult<Usuario> err1 = authService.login("admin", "senha_errada_1");
        assertFalse(err1.isSuccess());
        assertTrue(err1.getMessage().contains("mais 2 tentativa(s)"));

        OperationResult<Usuario> err2 = authService.login("admin", "senha_errada_2");
        assertFalse(err2.isSuccess());
        assertTrue(err2.getMessage().contains("mais 1 tentativa(s)"));

        // Tentativa 3: Deve bloquear por 5 minutos
        OperationResult<Usuario> err3 = authService.login("admin", "senha_errada_3");
        assertFalse(err3.isSuccess());
        assertTrue(err3.getMessage().contains("temporariamente bloqueada por 5 minutos"));

        // Tentativa subsequente durante o bloqueio
        OperationResult<Usuario> errBloqueado = authService.login("admin", "admin123");
        assertFalse(errBloqueado.isSuccess());
        assertTrue(errBloqueado.getMessage().contains("temporariamente bloqueado"));

        // Desbloqueia manualmente para continuar os testes
        usuarioDAO.atualizarTentativasEBloqueio("usr_1", 0, null);
    }

    @Test
    @Order(3)
    @DisplayName("Teste 3 (UC01): Recuperação local de senha 'Esqueci minha senha'")
    void testRecuperacaoSenha() {
        Optional<String> pergunta = authService.obterPerguntaSeguranca("admin");
        assertTrue(pergunta.isPresent());

        // Resposta errada
        OperationResult<Void> falha = authService.redefinirSenha("admin", "resposta_incorreta", "nova123", "nova123");
        assertFalse(falha.isSuccess());
        assertEquals("Resposta de segurança incorreta.", falha.getMessage());

        // Resposta correta
        OperationResult<Void> ok = authService.redefinirSenha("admin", "myow", "admin123", "admin123");
        assertTrue(ok.isSuccess());

        // Login com a senha
        OperationResult<Usuario> login = authService.login("admin", "admin123");
        assertTrue(login.isSuccess());
    }

    @Test
    @Order(4)
    @DisplayName("Teste 4 (UC02/UC03): Cadastro de Tutor e Paciente com verificação de duplicidade")
    void testCadastroTutorEPaciente() {
        String cpfTeste = "987.654.321-00";
        OperationResult<Tutor> resTutor = tutorService.cadastrar("Tutor Teste Automatizado", cpfTeste, "(11) 98888-7777", "teste@tutor.com", "Rua dos Testes, 123");
        assertTrue(resTutor.isSuccess() || resTutor.getMessage().contains("Já existe"));

        String tutorId = "tut_1";
        OperationResult<Paciente> resPac = pacienteService.cadastrar("Rex Teste", "Cão", "Vira-lata", 3, "Macho", tutorId, "Sem histórico anterior", false);
        assertTrue(resPac.isSuccess() || resPac.getMessage().contains("DUPLICADO"));

        // Tentativa de duplicado com forcar=false deve acusar
        boolean duplicado = pacienteService.verificarDuplicidade(tutorId, "Rex Teste");
        assertTrue(duplicado);
    }

    @Test
    @Order(5)
    @DisplayName("Teste 5 (UC04): Agendamento de Consulta e Conflito de Horário")
    void testAgendamentoEConflito() {
        String data = "2026-12-28";
        String hora = "16:45";
        String vetId = "usr_1"; // Dra. Camila Silveira

        // Remove agendamento anterior se houver para garantir idempotência
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM consultas WHERE data = '" + data + "' AND horario = '" + hora + "'");
        } catch (Exception ignored) {}

        OperationResult<Consulta> c1 = agendamentoService.agendar(data, hora, "pac_1", "tut_1", vetId, "Consulta preventiva", "Rotina");
        assertTrue(c1.isSuccess(), "Agendamento inicial deve ter sucesso: " + c1.getMessage());

        // Tentativa de marcar no mesmo dia, hora e veterinário deve conflitar
        OperationResult<Consulta> c2 = agendamentoService.agendar(data, hora, "pac_2", "tut_2", vetId, "Segunda consulta no mesmo horario", "Conflito");
        assertFalse(c2.isSuccess(), "Agendamento no mesmo horário deve ser recusado");
        assertTrue(c2.getMessage().contains("Horário indisponível"));
    }

    @Test
    @Order(6)
    @DisplayName("Teste 6 (UC05/UC08): Estoque - Entrada, Ajuste e Baixa Automática")
    void testEstoqueEBaixa() {
        String loteUnico = "LT-" + System.currentTimeMillis();
        OperationResult<ItemEstoque> resItem = estoqueService.cadastrarItem("Vacina Raiva Teste", loteUnico, "Vacinas", 20, 5, "2027-12-31", 60.00, "frasco", false);
        assertTrue(resItem.isSuccess(), "Cadastro de item de estoque deve ter sucesso");
        String itemId = resItem.getData().getId();

        // Baixa
        OperationResult<Void> baixa = estoqueService.darBaixa(itemId, 5);
        assertTrue(baixa.isSuccess());

        Optional<ItemEstoque> itemAtual = estoqueService.buscarPorId(itemId);
        assertTrue(itemAtual.isPresent());
        assertEquals(15, itemAtual.get().getQuantidade());
    }

    @Test
    @Order(7)
    @DisplayName("Teste 7 (UC11): Geração e Exportação de Relatórios em PDF e Excel")
    void testExportacaoRelatoriosPdfEExcel() {
        File dirTest = new File("target/test-reports");
        dirTest.mkdirs();

        File filePdf = new File(dirTest, "relatorio_teste.pdf");
        File fileExcel = new File(dirTest, "relatorio_teste.xlsx");

        // Simula login de administrador para autorização de relatório
        authService.login("admin", "admin123");

        OperationResult<File> resPdf = relatorioService.exportarPdf(filePdf, "2026-01-01", "2026-12-31");
        assertTrue(resPdf.isSuccess(), "Exportação para PDF deve ser bem sucedida");
        assertTrue(filePdf.exists(), "Arquivo PDF deve ter sido gerado em disco");
        assertTrue(filePdf.length() > 0, "Arquivo PDF não deve ser vazio");

        OperationResult<File> resExcel = relatorioService.exportarExcel(fileExcel, "2026-01-01", "2026-12-31");
        assertTrue(resExcel.isSuccess(), "Exportação para Excel deve ser bem sucedida");
        assertTrue(fileExcel.exists(), "Arquivo Excel deve ter sido gerado em disco");
        assertTrue(fileExcel.length() > 0, "Arquivo Excel não deve ser vazio");
    }

    @Test
    @Order(8)
    @DisplayName("Teste 8: Validação de Controle de Acesso Baseado em Perfis (RBAC)")
    void testControleDeAcessoRBAC() {
        // 1. Veterinário tenta cadastrar funcionário -> Deve ser bloqueado
        authService.login("camila.vet", "123456");
        OperationResult<Usuario> resFunc = funcionarioService.cadastrar("Tentativa", "111.222.333-44", "(11) 9999-8888", "Auxiliar", Perfil.FUNCIONARIO, "tentativa", "123");
        assertFalse(resFunc.isSuccess());
        assertTrue(resFunc.getMessage().contains("Acesso negado"));

        // 2. Veterinário tenta registrar pagamento -> Deve ser bloqueado
        OperationResult<Faturamento> resFat = faturamentoService.registrarPagamento("atend_1", FormaPagamento.PIX, 150.0);
        assertFalse(resFat.isSuccess());
        assertTrue(resFat.getMessage().contains("Acesso negado"));

        // 3. Funcionário comum tenta registrar atendimento clínico -> Deve ser bloqueado
        authService.login("bia.recepcao", "123456");
        OperationResult<Atendimento> resAtend = atendimentoService.registrarAtendimento("cons_1", "Diag", "Proc", "Presc", "Obs", List.of(), 100.0);
        assertFalse(resAtend.isSuccess());
        assertTrue(resAtend.getMessage().contains("Acesso negado"));

        // 4. Funcionário comum tenta exportar relatório -> Deve ser bloqueado
        File filePdf = new File("target/test-reports/proibido.pdf");
        OperationResult<File> resRel = relatorioService.exportarPdf(filePdf, "2026-01-01", "2026-12-31");
        assertFalse(resRel.isSuccess());
        assertTrue(resRel.getMessage().contains("Acesso negado"));
    }

    @Test
    @Order(9)
    @DisplayName("Teste 9: Validação do SessionManager (ciclo de vida de sessão)")
    void testSessionManager() {
        SessionManager sm = SessionManager.getInstance();
        sm.logout();
        assertNull(sm.getUsuarioLogado());

        Usuario u = new Usuario("test_1", "Test User", "000.000.000-00", "Admin", Perfil.ADMINISTRADOR, "test", "pass", "Ativo", "", 0, null, "", "");
        sm.setUsuarioLogado(u);
        assertNotNull(sm.getUsuarioLogado());
        assertEquals(u, sm.getUsuarioLogado());
        assertEquals("test_1", sm.getUsuarioLogado().getId());

        sm.logout();
        assertNull(sm.getUsuarioLogado());
    }

    @Test
    @Order(10)
    @DisplayName("Teste 10: Validação do IdGenerator (formato e unicidade)")
    void testIdGenerator() {
        String id1 = IdGenerator.nextId("cons");
        String id2 = IdGenerator.nextId("cons");
        String id3 = IdGenerator.nextId("pac");

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotNull(id3);
        assertTrue(id1.startsWith("cons_"));
        assertTrue(id2.startsWith("cons_"));
        assertTrue(id3.startsWith("pac_"));
        assertNotEquals(id1, id2, "IDs gerados sucessivamente devem ser distintos");
    }
}
