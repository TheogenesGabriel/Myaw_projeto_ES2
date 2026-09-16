package com.myow.service;

import com.myow.dao.UsuarioDAO;
import com.myow.model.Perfil;
import com.myow.model.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class AuthService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public static final int MAX_TENTATIVAS = 3;
    public static final int MINUTOS_BLOQUEIO = 5;

    public OperationResult<Usuario> login(String login, String senha) {
        if (login == null || login.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
            return OperationResult.error("Por favor, preencha os campos de login e senha.");
        }

        Optional<Usuario> userOpt = usuarioDAO.buscarPorLogin(login.trim());
        if (userOpt.isEmpty()) {
            return OperationResult.error("Credenciais inválidas: Usuário não cadastrado.");
        }

        Usuario user = userOpt.get();

        // Verificar se usuário está bloqueado temporariamente por excesso de tentativas
        if (user.isBloqueadoTemporariamente()) {
            LocalDateTime ate = LocalDateTime.parse(user.getBloqueadoAte());
            long minutosRestantes = ChronoUnit.MINUTES.between(LocalDateTime.now(), ate) + 1;
            return OperationResult.error("Usuário temporariamente bloqueado após " + MAX_TENTATIVAS + 
                    " tentativas incorretas consecutivas. Tente novamente em aproximadamente " + 
                    minutosRestantes + " minuto(s).");
        }

        // Verificar se usuário está inativo
        if (!user.isAtivo()) {
            return OperationResult.error("Acesso bloqueado: Este colaborador está inativo no sistema.");
        }

        // Verificar senha
        if (!user.getSenha().equals(senha)) {
            int novasTentativas = user.getTentativasLogin() + 1;
            if (novasTentativas >= MAX_TENTATIVAS) {
                LocalDateTime bloqueio = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO);
                usuarioDAO.atualizarTentativasEBloqueio(user.getId(), novasTentativas, bloqueio.toString());
                return OperationResult.error("Limite de " + MAX_TENTATIVAS + " tentativas incorretas atingido! " +
                        "A conta foi temporariamente bloqueada por " + MINUTOS_BLOQUEIO + " minutos por segurança.");
            } else {
                usuarioDAO.atualizarTentativasEBloqueio(user.getId(), novasTentativas, null);
                int restantes = MAX_TENTATIVAS - novasTentativas;
                return OperationResult.error("Senha incorreta. Você possui mais " + restantes + " tentativa(s) antes do bloqueio temporário.");
            }
        }

        // Login bem sucedido: zera as tentativas e limpa bloqueio
        usuarioDAO.atualizarTentativasEBloqueio(user.getId(), 0, null);
        user.setTentativasLogin(0);
        user.setBloqueadoAte(null);

        sessionManager.setUsuarioLogado(user);
        return OperationResult.ok("Login realizado com sucesso!", user);
    }

    public void logout() {
        sessionManager.setUsuarioLogado(null);
    }

    public Usuario getUsuarioLogado() {
        return sessionManager.getUsuarioLogado();
    }

    // Funcionalidade "Esqueci minha senha"
    public Optional<String> obterPerguntaSeguranca(String loginOuCpf) {
        if (loginOuCpf == null || loginOuCpf.trim().isEmpty()) {
            return Optional.empty();
        }
        String clean = loginOuCpf.trim();
        Optional<Usuario> userOpt = usuarioDAO.buscarPorLogin(clean);
        if (userOpt.isEmpty()) {
            userOpt = usuarioDAO.buscarPorCpf(clean);
        }
        return userOpt.map(u -> u.getPerguntaSeguranca() != null ? u.getPerguntaSeguranca() : "Qual o nome da clínica?");
    }

    public OperationResult<Void> redefinirSenha(String loginOuCpf, String respostaSeguranca, String novaSenha, String confirmacaoSenha) {
        if (loginOuCpf == null || loginOuCpf.trim().isEmpty()) {
            return OperationResult.error("Informe seu Login ou CPF.");
        }
        if (respostaSeguranca == null || respostaSeguranca.trim().isEmpty()) {
            return OperationResult.error("Informe a resposta de segurança.");
        }
        if (novaSenha == null || novaSenha.trim().isEmpty() || novaSenha.length() < 4) {
            return OperationResult.error("A nova senha deve possuir pelo menos 4 caracteres.");
        }
        if (!novaSenha.equals(confirmacaoSenha)) {
            return OperationResult.error("A nova senha e a confirmação não coincidem.");
        }

        String clean = loginOuCpf.trim();
        Optional<Usuario> userOpt = usuarioDAO.buscarPorLogin(clean);
        if (userOpt.isEmpty()) {
            userOpt = usuarioDAO.buscarPorCpf(clean);
        }

        if (userOpt.isEmpty()) {
            return OperationResult.error("Colaborador não encontrado com o Login ou CPF fornecido.");
        }

        Usuario user = userOpt.get();
        String respSalva = user.getRespostaSeguranca() != null ? user.getRespostaSeguranca().trim() : "myow";
        if (!respSalva.equalsIgnoreCase(respostaSeguranca.trim())) {
            return OperationResult.error("Resposta de segurança incorreta.");
        }

        boolean ok = usuarioDAO.redefinirSenha(user.getId(), novaSenha.trim());
        if (ok) {
            return OperationResult.ok("Senha redefinida com sucesso! Agora você já pode fazer login.");
        } else {
            return OperationResult.error("Erro interno ao persistir a nova senha no banco de dados.");
        }
    }

    // Validações de controle de acesso (RBAC)
    public boolean podeAcessarAtendimento(Usuario u) {
        return u != null && (u.getPerfil() == Perfil.VETERINARIO || u.getPerfil() == Perfil.ADMINISTRADOR);
    }

    public boolean podeAcessarProntuario(Usuario u) {
        return u != null && (u.getPerfil() == Perfil.VETERINARIO || u.getPerfil() == Perfil.ADMINISTRADOR);
    }

    public boolean podeGerenciarFuncionarios(Usuario u) {
        return u != null && u.getPerfil() == Perfil.ADMINISTRADOR;
    }

    public boolean podeAcessarRelatorios(Usuario u) {
        return u != null && u.getPerfil() == Perfil.ADMINISTRADOR;
    }

    public boolean podeFaturar(Usuario u) {
        return u != null && (u.getPerfil() == Perfil.FUNCIONARIO || u.getPerfil() == Perfil.ADMINISTRADOR);
    }
}
