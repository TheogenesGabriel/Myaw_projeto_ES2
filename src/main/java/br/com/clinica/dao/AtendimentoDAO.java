package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConnection;
import br.com.clinica.model.Atendimento;

import java.sql.*;

public class AtendimentoDAO {

    // UC07 - Registrar Atendimento Médico
    public void registrar(Atendimento atendimento) throws SQLException {
        String sql = """
            INSERT INTO atendimentos (
                consulta_id, peso_kg, temperatura_c, frequencia_cardiaca,
                diagnostico, procedimentos_realizados, prescricao_medicamentosa, recomendacoes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String sqlAtualizaConsulta = "UPDATE consultas SET status = 'REALIZADA' WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transação atômica

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement stmtConsulta = conn.prepareStatement(sqlAtualizaConsulta)) {

                stmt.setInt(1, atendimento.getConsultaId());
                stmt.setDouble(2, atendimento.getPesoKg());
                stmt.setDouble(3, atendimento.getTemperaturaC());
                stmt.setInt(4, atendimento.getFrequenciaCardiaca());
                stmt.setString(5, atendimento.getDiagnostico());
                stmt.setString(6, atendimento.getProcedimentosRealizados());
                stmt.setString(7, atendimento.getPrescricaoMedicamentosa());
                stmt.setString(8, atendimento.getRecomendacoes());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        atendimento.setId(generatedKeys.getInt(1));
                    }
                }

                // Atualizar consulta para REALIZADA
                stmtConsulta.setInt(1, atendimento.getConsultaId());
                stmtConsulta.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public Atendimento buscarPorConsulta(int consultaId) throws SQLException {
        String sql = "SELECT * FROM atendimentos WHERE consulta_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, consultaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Atendimento a = new Atendimento();
                    a.setId(rs.getInt("id"));
                    a.setConsultaId(rs.getInt("consulta_id"));
                    a.setPesoKg(rs.getDouble("peso_kg"));
                    a.setTemperaturaC(rs.getDouble("temperatura_c"));
                    a.setFrequenciaCardiaca(rs.getInt("frequencia_cardiaca"));
                    a.setDiagnostico(rs.getString("diagnostico"));
                    a.setProcedimentosRealizados(rs.getString("procedimentos_realizados"));
                    a.setPrescricaoMedicamentosa(rs.getString("prescricao_medicamentosa"));
                    a.setRecomendacoes(rs.getString("recomendacoes"));
                    a.setDataAtendimento(rs.getString("data_atendimento"));
                    return a;
                }
            }
        }
        return null;
    }
}
