package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConnection;
import br.com.clinica.model.Consulta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDAO {

    // UC04 - Agendar Consulta
    public void agendar(Consulta consulta) throws SQLException {
        String sql = """
            INSERT INTO consultas (paciente_id, veterinario, data_consulta, horario, motivo, status, observacoes)
            VALUES (?, ?, ?, ?, ?, 'AGENDADA', ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, consulta.getPacienteId());
            stmt.setString(2, consulta.getVeterinario());
            stmt.setString(3, consulta.getDataConsulta());
            stmt.setString(4, consulta.getHorario());
            stmt.setString(5, consulta.getMotivo());
            stmt.setString(6, consulta.getObservacoes());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    consulta.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // UC05 - Alterar Consulta
    public void alterar(int id, String novoVeterinario, String novaData, String novoHorario, String novoMotivo) throws SQLException {
        String sql = """
            UPDATE consultas 
            SET veterinario = ?, data_consulta = ?, horario = ?, motivo = ?
            WHERE id = ? AND status = 'AGENDADA'
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoVeterinario);
            stmt.setString(2, novaData);
            stmt.setString(3, novoHorario);
            stmt.setString(4, novoMotivo);
            stmt.setInt(5, id);
            stmt.executeUpdate();
        }
    }

    // UC06 - Cancelar Consulta
    public void cancelar(int id, String motivoCancelamento) throws SQLException {
        String sql = """
            UPDATE consultas 
            SET status = 'CANCELADA', motivo_cancelamento = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, motivoCancelamento);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    // Listar Consultas
    public List<Consulta> listar(String statusFiltro) throws SQLException {
        List<Consulta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT c.*, p.nome as paciente_nome, t.nome as tutor_nome
            FROM consultas c
            JOIN pacientes p ON c.paciente_id = p.id
            JOIN tutores t ON p.tutor_id = t.id
        """);

        if (statusFiltro != null && !statusFiltro.isEmpty()) {
            sql.append(" WHERE c.status = ?");
        }
        sql.append(" ORDER BY c.data_consulta ASC, c.horario ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (statusFiltro != null && !statusFiltro.isEmpty()) {
                stmt.setString(1, statusFiltro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Consulta c = new Consulta(
                            rs.getInt("id"),
                            rs.getInt("paciente_id"),
                            rs.getString("veterinario"),
                            rs.getString("data_consulta"),
                            rs.getString("horario"),
                            rs.getString("motivo"),
                            rs.getString("status"),
                            rs.getString("observacoes")
                    );
                    c.setPacienteNome(rs.getString("paciente_nome"));
                    c.setTutorNome(rs.getString("tutor_nome"));
                    c.setMotivoCancelamento(rs.getString("motivo_cancelamento"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    public Consulta buscarPorId(int id) throws SQLException {
        String sql = """
            SELECT c.*, p.nome as paciente_nome, t.nome as tutor_nome
            FROM consultas c
            JOIN pacientes p ON c.paciente_id = p.id
            JOIN tutores t ON p.tutor_id = t.id
            WHERE c.id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Consulta c = new Consulta(
                            rs.getInt("id"),
                            rs.getInt("paciente_id"),
                            rs.getString("veterinario"),
                            rs.getString("data_consulta"),
                            rs.getString("horario"),
                            rs.getString("motivo"),
                            rs.getString("status"),
                            rs.getString("observacoes")
                    );
                    c.setPacienteNome(rs.getString("paciente_nome"));
                    c.setTutorNome(rs.getString("tutor_nome"));
                    c.setMotivoCancelamento(rs.getString("motivo_cancelamento"));
                    return c;
                }
            }
        }
        return null;
    }
}
