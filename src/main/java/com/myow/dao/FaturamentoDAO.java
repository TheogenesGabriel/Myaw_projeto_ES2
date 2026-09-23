package com.myow.dao;

import com.myow.database.DatabaseManager;
import com.myow.model.Faturamento;
import com.myow.model.FormaPagamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FaturamentoDAO {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Faturamento> buscarPorId(String id) {
        String sql = "SELECT * FROM faturamentos WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar faturamento por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Faturamento> buscarPorAtendimentoId(String atendimentoId) {
        String sql = "SELECT * FROM faturamentos WHERE atendimento_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, atendimentoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar faturamento por atendimento id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Faturamento> listarTodos() {
        List<Faturamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM faturamentos ORDER BY data_pagamento DESC, hora_pagamento DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar faturamentos: " + e.getMessage());
        }
        return lista;
    }

    public List<Faturamento> listarPorPeriodo(String dataInicio, String dataFim) {
        List<Faturamento> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM faturamentos WHERE 1=1");
        if (dataInicio != null && !dataInicio.trim().isEmpty()) {
            sql.append(" AND data_pagamento >= '").append(dataInicio).append("'");
        }
        if (dataFim != null && !dataFim.trim().isEmpty()) {
            sql.append(" AND data_pagamento <= '").append(dataFim).append("'");
        }
        sql.append(" ORDER BY data_pagamento DESC, hora_pagamento DESC");

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar faturamentos por período: " + e.getMessage());
        }
        return lista;
    }

    public boolean salvar(Faturamento f) {
        String sql = "INSERT INTO faturamentos (id, atendimento_id, consulta_id, paciente_id, tutor_id, valor_total, forma_pagamento, valor_recebido, troco, data_pagamento, hora_pagamento, operador_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, f.getId());
            stmt.setString(2, f.getAtendimentoId());
            stmt.setString(3, f.getConsultaId());
            stmt.setString(4, f.getPacienteId());
            stmt.setString(5, f.getTutorId());
            stmt.setDouble(6, f.getValorTotal());
            stmt.setString(7, f.getFormaPagamento().name());
            if (f.getValorRecebido() != null) {
                stmt.setDouble(8, f.getValorRecebido());
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            if (f.getTroco() != null) {
                stmt.setDouble(9, f.getTroco());
            } else {
                stmt.setNull(9, Types.DOUBLE);
            }
            stmt.setString(10, f.getDataPagamento());
            stmt.setString(11, f.getHoraPagamento());
            stmt.setString(12, f.getOperadorId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar faturamento: " + e.getMessage());
            return false;
        }
    }

    private Faturamento mapResultSet(ResultSet rs) throws SQLException {
        Double valorRecebido = rs.getObject("valor_recebido") != null ? rs.getDouble("valor_recebido") : null;
        Double troco = rs.getObject("troco") != null ? rs.getDouble("troco") : null;

        return new Faturamento(
                rs.getString("id"),
                rs.getString("atendimento_id"),
                rs.getString("consulta_id"),
                rs.getString("paciente_id"),
                rs.getString("tutor_id"),
                rs.getDouble("valor_total"),
                FormaPagamento.valueOf(rs.getString("forma_pagamento")),
                valorRecebido,
                troco,
                rs.getString("data_pagamento"),
                rs.getString("hora_pagamento"),
                rs.getString("operador_id")
        );
    }
}
