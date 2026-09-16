package com.myow.dao;

import com.myow.database.DatabaseManager;
import com.myow.model.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PacienteDAO {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Paciente> buscarPorId(String id) {
        String sql = "SELECT * FROM pacientes WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar paciente por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Paciente> listarTodos() {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes ORDER BY nome ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pacientes: " + e.getMessage());
        }
        return lista;
    }

    public List<Paciente> listarPorTutor(String tutorId) {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE tutor_id = ? ORDER BY nome ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tutorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar pacientes por tutor: " + e.getMessage());
        }
        return lista;
    }

    public boolean salvar(Paciente p) {
        String sql = "INSERT INTO pacientes (id, nome, especie, raca, idade, sexo, tutor_id, historico_medico, data_cadastro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getId());
            stmt.setString(2, p.getNome());
            stmt.setString(3, p.getEspecie());
            stmt.setString(4, p.getRaca());
            stmt.setInt(5, p.getIdade());
            stmt.setString(6, p.getSexo());
            stmt.setString(7, p.getTutorId());
            stmt.setString(8, p.getHistoricoMedico());
            stmt.setString(9, p.getDataCadastro());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar paciente: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizar(Paciente p) {
        String sql = "UPDATE pacientes SET nome = ?, especie = ?, raca = ?, idade = ?, sexo = ?, historico_medico = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNome());
            stmt.setString(2, p.getEspecie());
            stmt.setString(3, p.getRaca());
            stmt.setInt(4, p.getIdade());
            stmt.setString(5, p.getSexo());
            stmt.setString(6, p.getHistoricoMedico());
            stmt.setString(7, p.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar paciente: " + e.getMessage());
            return false;
        }
    }

    private Paciente mapResultSet(ResultSet rs) throws SQLException {
        return new Paciente(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("especie"),
                rs.getString("raca"),
                rs.getInt("idade"),
                rs.getString("sexo"),
                rs.getString("tutor_id"),
                rs.getString("historico_medico"),
                rs.getString("data_cadastro")
        );
    }
}
