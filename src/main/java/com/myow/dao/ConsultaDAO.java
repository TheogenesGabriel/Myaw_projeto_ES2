package com.myow.dao;

import com.myow.database.DatabaseManager;
import com.myow.model.Consulta;
import com.myow.model.StatusConsulta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConsultaDAO {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Consulta> buscarPorId(String id) {
        String sql = "SELECT * FROM consultas WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar consulta por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Consulta> listarTodas() {
        List<Consulta> lista = new ArrayList<>();
        String sql = "SELECT * FROM consultas ORDER BY data DESC, horario ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar consultas: " + e.getMessage());
        }
        return lista;
    }

    public List<Consulta> listarPorData(String data) {
        List<Consulta> lista = new ArrayList<>();
        String sql = "SELECT * FROM consultas WHERE data = ? ORDER BY horario ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar consultas por data: " + e.getMessage());
        }
        return lista;
    }

    public List<Consulta> buscarConflitoHorario(String veterinarioId, String data, String horario, String consultaIdIgnorar) {
        List<Consulta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM consultas WHERE veterinario_id = ? AND data = ? AND horario = ? AND status != 'CANCELADA'");
        if (consultaIdIgnorar != null && !consultaIdIgnorar.trim().isEmpty()) {
            sql.append(" AND id != ?");
        }

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, veterinarioId);
            stmt.setString(2, data);
            stmt.setString(3, horario);
            if (consultaIdIgnorar != null && !consultaIdIgnorar.trim().isEmpty()) {
                stmt.setString(4, consultaIdIgnorar);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar conflito de horário: " + e.getMessage());
        }
        return lista;
    }

    public boolean salvar(Consulta c) {
        String sql = "INSERT INTO consultas (id, data, horario, paciente_id, tutor_id, veterinario_id, status, motivo, observacoes, data_agendamento) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getId());
            stmt.setString(2, c.getData());
            stmt.setString(3, c.getHorario());
            stmt.setString(4, c.getPacienteId());
            stmt.setString(5, c.getTutorId());
            stmt.setString(6, c.getVeterinarioId());
            stmt.setString(7, c.getStatus().name());
            stmt.setString(8, c.getMotivo());
            stmt.setString(9, c.getObservacoes());
            stmt.setString(10, c.getDataAgendamento());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar consulta: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizar(Consulta c) {
        String sql = "UPDATE consultas SET data = ?, horario = ?, veterinario_id = ?, status = ?, motivo = ?, observacoes = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getData());
            stmt.setString(2, c.getHorario());
            stmt.setString(3, c.getVeterinarioId());
            stmt.setString(4, c.getStatus().name());
            stmt.setString(5, c.getMotivo());
            stmt.setString(6, c.getObservacoes());
            stmt.setString(7, c.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar consulta: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarStatus(String id, StatusConsulta status) {
        String sql = "UPDATE consultas SET status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status da consulta: " + e.getMessage());
            return false;
        }
    }

    private Consulta mapResultSet(ResultSet rs) throws SQLException {
        return new Consulta(
                rs.getString("id"),
                rs.getString("data"),
                rs.getString("horario"),
                rs.getString("paciente_id"),
                rs.getString("tutor_id"),
                rs.getString("veterinario_id"),
                StatusConsulta.valueOf(rs.getString("status")),
                rs.getString("motivo"),
                rs.getString("observacoes"),
                rs.getString("data_agendamento")
        );
    }
}
