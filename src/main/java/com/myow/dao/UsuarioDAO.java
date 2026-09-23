package com.myow.dao;

import com.myow.database.DatabaseManager;
import com.myow.model.Perfil;
import com.myow.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Usuario> buscarPorId(String id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorLogin(String login) {
        String sql = "SELECT * FROM usuarios WHERE LOWER(login) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por login: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM usuarios WHERE cpf = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por CPF: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nome ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }
        return lista;
    }

    public boolean salvar(Usuario u) {
        String sql = "INSERT INTO usuarios (id, nome, cpf, cargo, perfil, login, senha, status, telefone, tentativas_login, bloqueado_ate, pergunta_seguranca, resposta_seguranca) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getId());
            stmt.setString(2, u.getNome());
            stmt.setString(3, u.getCpf());
            stmt.setString(4, u.getCargo());
            stmt.setString(5, u.getPerfil().name());
            stmt.setString(6, u.getLogin());
            stmt.setString(7, u.getSenha());
            stmt.setString(8, u.getStatus());
            stmt.setString(9, u.getTelefone());
            stmt.setInt(10, u.getTentativasLogin());
            stmt.setString(11, u.getBloqueadoAte());
            stmt.setString(12, u.getPerguntaSeguranca());
            stmt.setString(13, u.getRespostaSeguranca());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar usuário: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizar(Usuario u) {
        String sql = "UPDATE usuarios SET nome = ?, cpf = ?, cargo = ?, perfil = ?, login = ?, senha = ?, status = ?, telefone = ?, " +
                     "pergunta_seguranca = ?, resposta_seguranca = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getCpf());
            stmt.setString(3, u.getCargo());
            stmt.setString(4, u.getPerfil().name());
            stmt.setString(5, u.getLogin());
            stmt.setString(6, u.getSenha());
            stmt.setString(7, u.getStatus());
            stmt.setString(8, u.getTelefone());
            stmt.setString(9, u.getPerguntaSeguranca());
            stmt.setString(10, u.getRespostaSeguranca());
            stmt.setString(11, u.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }

    public void atualizarTentativasEBloqueio(String id, int tentativas, String bloqueadoAte) {
        String sql = "UPDATE usuarios SET tentativas_login = ?, bloqueado_ate = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tentativas);
            stmt.setString(2, bloqueadoAte);
            stmt.setString(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar bloqueio: " + e.getMessage());
        }
    }

    public boolean redefinirSenha(String id, String novaSenha) {
        String sql = "UPDATE usuarios SET senha = ?, tentativas_login = 0, bloqueado_ate = NULL WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novaSenha);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao redefinir senha: " + e.getMessage());
            return false;
        }
    }

    public void atualizarStatus(String id, String novoStatus) {
        String sql = "UPDATE usuarios SET status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setString(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status do usuário: " + e.getMessage());
        }
    }

    private Usuario mapResultSet(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("cargo"),
                Perfil.valueOf(rs.getString("perfil")),
                rs.getString("login"),
                rs.getString("senha"),
                rs.getString("status"),
                rs.getString("telefone"),
                rs.getInt("tentativas_login"),
                rs.getString("bloqueado_ate"),
                rs.getString("pergunta_seguranca"),
                rs.getString("resposta_seguranca")
        );
    }
}
