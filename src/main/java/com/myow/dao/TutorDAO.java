package com.myow.dao;

import com.myow.database.DatabaseManager;
import com.myow.model.Tutor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TutorDAO {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Tutor> buscarPorId(String id) {
        String sql = "SELECT * FROM tutores WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar tutor por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Tutor> buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM tutores WHERE cpf = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar tutor por CPF: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Tutor> listarTodos() {
        List<Tutor> lista = new ArrayList<>();
        String sql = "SELECT * FROM tutores ORDER BY nome_completo ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar tutores: " + e.getMessage());
        }
        return lista;
    }

    public boolean salvar(Tutor t) {
        String sql = "INSERT INTO tutores (id, nome_completo, cpf, telefone, email, endereco, data_cadastro) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getId());
            stmt.setString(2, t.getNomeCompleto());
            stmt.setString(3, t.getCpf());
            stmt.setString(4, t.getTelefone());
            stmt.setString(5, t.getEmail());
            stmt.setString(6, t.getEndereco());
            stmt.setString(7, t.getDataCadastro());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar tutor: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizar(Tutor t) {
        String sql = "UPDATE tutores SET nome_completo = ?, cpf = ?, telefone = ?, email = ?, endereco = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getNomeCompleto());
            stmt.setString(2, t.getCpf());
            stmt.setString(3, t.getTelefone());
            stmt.setString(4, t.getEmail());
            stmt.setString(5, t.getEndereco());
            stmt.setString(6, t.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar tutor: " + e.getMessage());
            return false;
        }
    }

    private Tutor mapResultSet(ResultSet rs) throws SQLException {
        return new Tutor(
                rs.getString("id"),
                rs.getString("nome_completo"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                rs.getString("email"),
                rs.getString("endereco"),
                rs.getString("data_cadastro")
        );
    }
}
