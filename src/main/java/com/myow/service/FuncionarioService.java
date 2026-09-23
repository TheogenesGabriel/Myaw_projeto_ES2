package com.myow.service;

import com.myow.dao.UsuarioDAO;
import com.myow.model.Perfil;
import com.myow.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FuncionarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioDAO.buscarPorId(id);
    }

    public List<Usuario> filtrar(String termo, String perfilFiltro, String statusFiltro) {
        return usuarioDAO.listarTodos().stream().filter(u -> {
            boolean matchTermo = true;
            if (termo != null && !termo.trim().isEmpty()) {
                String t = termo.trim().toLowerCase();
                matchTermo = u.getNome().toLowerCase().contains(t) ||
                             u.getCpf().replaceAll("\\D", "").contains(t.replaceAll("\\D", "")) ||
                             u.getLogin().toLowerCase().contains(t);
            }

            boolean matchPerfil = true;
            if (perfilFiltro != null && !perfilFiltro.isEmpty() && !perfilFiltro.equalsIgnoreCase("TODOS")) {
                matchPerfil = u.getPerfil().name().equalsIgnoreCase(perfilFiltro);
            }

            boolean matchStatus = true;
            if (statusFiltro != null && !statusFiltro.isEmpty() && !statusFiltro.equalsIgnoreCase("TODOS")) {
                matchStatus = u.getStatus().equalsIgnoreCase(statusFiltro);
            }

            return matchTermo && matchPerfil && matchStatus;
        }).collect(Collectors.toList());
    }

    public OperationResult<Usuario> cadastrar(String nome, String cpf, String telefone, String cargo, Perfil perfil, String login, String senha) {
        // Validação de controle de acesso (RBAC)
        Usuario logado = sessionManager.getUsuarioLogado();
        if (logado != null && logado.getPerfil() != Perfil.ADMINISTRADOR) {
            return OperationResult.error("Acesso negado: Apenas Administradores possuem permissão para cadastrar colaboradores.");
        }

        if (nome == null || nome.trim().isEmpty() ||
            cpf == null || cpf.trim().isEmpty() ||
            cargo == null || cargo.trim().isEmpty() ||
            perfil == null ||
            login == null || login.trim().isEmpty() ||
            senha == null || senha.trim().isEmpty()) {
            return OperationResult.error("Todos os campos obrigatórios (Nome, CPF, Cargo, Perfil, Login e Senha) devem ser preenchidos.");
        }

        String cpfLimpo = cpf.replaceAll("\\D", "");
        boolean cpfOuLoginExiste = usuarioDAO.listarTodos().stream().anyMatch(u ->
                u.getCpf().replaceAll("\\D", "").equals(cpfLimpo) ||
                u.getLogin().equalsIgnoreCase(login.trim())
        );

        if (cpfOuLoginExiste) {
            return OperationResult.error("Já existe um colaborador cadastrado com este mesmo CPF ou Login de acesso.");
        }

        String id = IdGenerator.nextId("usr");
        Usuario novo = new Usuario(id, nome.trim(), cpf.trim(), cargo.trim(), perfil, login.trim(), senha.trim(), "Ativo", telefone != null ? telefone.trim() : "",
                0, null, "Qual o nome da clínica?", "myow");

        boolean salvo = usuarioDAO.salvar(novo);
        if (!salvo) {
            return OperationResult.error("Erro ao persistir colaborador no banco de dados.");
        }

        return OperationResult.ok("Colaborador cadastrado com sucesso!", novo);
    }

    public OperationResult<Usuario> editar(String id, String nome, String cpf, String telefone, String cargo, Perfil perfil, String login, String senha) {
        // Validação de controle de acesso (RBAC)
        Usuario logado = sessionManager.getUsuarioLogado();
        if (logado != null && logado.getPerfil() != Perfil.ADMINISTRADOR) {
            return OperationResult.error("Acesso negado: Apenas Administradores possuem permissão para editar colaboradores.");
        }

        Optional<Usuario> opt = buscarPorId(id);
        if (opt.isEmpty()) return OperationResult.error("Colaborador não encontrado.");

        if (nome == null || nome.trim().isEmpty() ||
            cpf == null || cpf.trim().isEmpty() ||
            cargo == null || cargo.trim().isEmpty() ||
            perfil == null ||
            login == null || login.trim().isEmpty()) {
            return OperationResult.error("Todos os campos obrigatórios devem ser preenchidos.");
        }

        String cpfLimpo = cpf.replaceAll("\\D", "");
        boolean duplicadoOutro = usuarioDAO.listarTodos().stream()
                .filter(u -> !u.getId().equals(id))
                .anyMatch(u -> u.getCpf().replaceAll("\\D", "").equals(cpfLimpo) || u.getLogin().equalsIgnoreCase(login.trim()));

        if (duplicadoOutro) {
            return OperationResult.error("Outro usuário já está utilizando este CPF ou Login.");
        }

        Usuario u = opt.get();
        u.setNome(nome.trim());
        u.setCpf(cpf.trim());
        u.setTelefone(telefone != null ? telefone.trim() : "");
        u.setCargo(cargo.trim());
        u.setPerfil(perfil);
        u.setLogin(login.trim());
        if (senha != null && !senha.trim().isEmpty()) {
            u.setSenha(senha.trim());
        }

        boolean atualizado = usuarioDAO.atualizar(u);
        if (!atualizado) {
            return OperationResult.error("Erro ao atualizar dados do colaborador no banco de dados.");
        }

        return OperationResult.ok("Colaborador atualizado com sucesso!", u);
    }

    public OperationResult<Usuario> alternarStatus(String id) {
        // Validação de controle de acesso (RBAC)
        Usuario logado = sessionManager.getUsuarioLogado();
        if (logado != null && logado.getPerfil() != Perfil.ADMINISTRADOR) {
            return OperationResult.error("Acesso negado: Apenas Administradores possuem permissão para alterar o status de colaboradores.");
        }

        Optional<Usuario> opt = buscarPorId(id);
        if (opt.isEmpty()) return OperationResult.error("Colaborador não encontrado.");

        Usuario user = opt.get();

        if (logado != null && logado.getId().equals(user.getId())) {
            return OperationResult.error("Operação bloqueada: Não é permitido inativar a própria conta de administrador atualmente em uso.");
        }

        String novoStatus = "Ativo".equalsIgnoreCase(user.getStatus()) ? "Inativo" : "Ativo";
        user.setStatus(novoStatus);
        usuarioDAO.atualizarStatus(user.getId(), novoStatus);

        return OperationResult.ok("Status do colaborador alterado para " + user.getStatus() + ".", user);
    }
}
