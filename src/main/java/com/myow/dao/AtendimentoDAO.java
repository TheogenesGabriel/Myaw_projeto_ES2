package com.myow.dao;

import com.myow.database.DatabaseManager;
import com.myow.model.Atendimento;
import com.myow.model.ItemUtilizado;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtendimentoDAO {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Atendimento> buscarPorId(String id) {
        String sql = "SELECT * FROM atendimentos WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Atendimento a = mapResultSet(rs);
                    a.setItensUtilizados(listarItensPorAtendimento(a.getId()));
                    return Optional.of(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar atendimento por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Atendimento> buscarPorConsultaId(String consultaId) {
        String sql = "SELECT * FROM atendimentos WHERE consulta_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, consultaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Atendimento a = mapResultSet(rs);
                    a.setItensUtilizados(listarItensPorAtendimento(a.getId()));
                    return Optional.of(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar atendimento por consulta id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Atendimento> listarTodos() {
        List<Atendimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM atendimentos ORDER BY data DESC, hora DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Atendimento a = mapResultSet(rs);
                a.setItensUtilizados(listarItensPorAtendimento(a.getId()));
                lista.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar atendimentos: " + e.getMessage());
        }
        return lista;
    }

    public List<Atendimento> listarPorPaciente(String pacienteId) {
        List<Atendimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM atendimentos WHERE paciente_id = ? ORDER BY data DESC, hora DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pacienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Atendimento a = mapResultSet(rs);
                    a.setItensUtilizados(listarItensPorAtendimento(a.getId()));
                    lista.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar atendimentos por paciente: " + e.getMessage());
        }
        return lista;
    }

    public boolean salvar(Atendimento a) {
        String sqlAtend = "INSERT INTO atendimentos (id, consulta_id, paciente_id, tutor_id, veterinario_id, data, hora, " +
                          "diagnostico, procedimentos, prescricoes, observacoes_clinicas, valor_procedimentos, valor_total, status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlItem = "INSERT INTO atendimento_itens (id, atendimento_id, item_estoque_id, nome_item, quantidade, valor_unitario, subtotal) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sqlAtend)) {
                stmt.setString(1, a.getId());
                stmt.setString(2, a.getConsultaId());
                stmt.setString(3, a.getPacienteId());
                stmt.setString(4, a.getTutorId());
                stmt.setString(5, a.getVeterinarioId());
                stmt.setString(6, a.getData());
                stmt.setString(7, a.getHora());
                stmt.setString(8, a.getDiagnostico());
                stmt.setString(9, a.getProcedimentos());
                stmt.setString(10, a.getPrescricoes());
                stmt.setString(11, a.getObservacoesClinicas());
                stmt.setDouble(12, a.getValorProcedimentos());
                stmt.setDouble(13, a.getValorTotal());
                stmt.setString(14, a.getStatus());
                stmt.executeUpdate();
            }

            if (a.getItensUtilizados() != null && !a.getItensUtilizados().isEmpty()) {
                try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem)) {
                    for (ItemUtilizado item : a.getItensUtilizados()) {
                        stmtItem.setString(1, "ait_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000));
                        stmtItem.setString(2, a.getId());
                        stmtItem.setString(3, item.getItemId());
                        stmtItem.setString(4, item.getNome());
                        stmtItem.setInt(5, item.getQuantidade());
                        stmtItem.setDouble(6, item.getValorUnitario());
                        stmtItem.setDouble(7, item.getSubtotal());
                        stmtItem.addBatch();
                    }
                    stmtItem.executeBatch();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar atendimento com itens: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarStatus(String id, String status) {
        String sql = "UPDATE atendimentos SET status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status do atendimento: " + e.getMessage());
            return false;
        }
    }

    public List<ItemUtilizado> listarItensPorAtendimento(String atendimentoId) {
        List<ItemUtilizado> lista = new ArrayList<>();
        String sql = "SELECT * FROM atendimento_itens WHERE atendimento_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, atendimentoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ItemUtilizado(
                            rs.getString("item_estoque_id"),
                            rs.getString("nome_item"),
                            rs.getInt("quantidade"),
                            rs.getDouble("valor_unitario")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar itens do atendimento: " + e.getMessage());
        }
        return lista;
    }

    private Atendimento mapResultSet(ResultSet rs) throws SQLException {
        return new Atendimento(
                rs.getString("id"),
                rs.getString("consulta_id"),
                rs.getString("paciente_id"),
                rs.getString("tutor_id"),
                rs.getString("veterinario_id"),
                rs.getString("data"),
                rs.getString("hora"),
                rs.getString("diagnostico"),
                rs.getString("procedimentos"),
                rs.getString("prescricoes"),
                rs.getString("observacoes_clinicas"),
                new ArrayList<>(),
                rs.getDouble("valor_procedimentos"),
                rs.getDouble("valor_total"),
                rs.getString("status")
        );
    }
}
