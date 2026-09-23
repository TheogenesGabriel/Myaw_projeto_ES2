package com.myow.dao;

import com.myow.database.DatabaseManager;
import com.myow.model.ItemEstoque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemEstoqueDAO {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<ItemEstoque> buscarPorId(String id) {
        String sql = "SELECT * FROM itens_estoque WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar item de estoque por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<ItemEstoque> buscarPorNomeELote(String nome, String lote) {
        String sql = "SELECT * FROM itens_estoque WHERE LOWER(nome) = LOWER(?) AND LOWER(lote) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, lote);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar item por nome e lote: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<ItemEstoque> listarTodos() {
        List<ItemEstoque> lista = new ArrayList<>();
        String sql = "SELECT * FROM itens_estoque ORDER BY nome ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar estoque: " + e.getMessage());
        }
        return lista;
    }

    public boolean salvar(ItemEstoque item) {
        String sql = "INSERT INTO itens_estoque (id, nome, lote, categoria, quantidade, estoque_minimo, validade, valor_unitario, unidade) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getNome());
            stmt.setString(3, item.getLote());
            stmt.setString(4, item.getCategoria());
            stmt.setInt(5, item.getQuantidade());
            stmt.setInt(6, item.getEstoqueMinimo());
            stmt.setString(7, item.getValidade());
            stmt.setDouble(8, item.getValorUnitario());
            stmt.setString(9, item.getUnidade());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar item de estoque: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizar(ItemEstoque item) {
        String sql = "UPDATE itens_estoque SET nome = ?, categoria = ?, quantidade = ?, estoque_minimo = ?, validade = ?, valor_unitario = ?, unidade = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getNome());
            stmt.setString(2, item.getCategoria());
            stmt.setInt(3, item.getQuantidade());
            stmt.setInt(4, item.getEstoqueMinimo());
            stmt.setString(5, item.getValidade());
            stmt.setDouble(6, item.getValorUnitario());
            stmt.setString(7, item.getUnidade());
            stmt.setString(8, item.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar item de estoque: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarQuantidade(String id, int novaQuantidade) {
        if (novaQuantidade < 0) {
            return false;
        }
        String sql = "UPDATE itens_estoque SET quantidade = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novaQuantidade);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar quantidade do item: " + e.getMessage());
            return false;
        }
    }

    private ItemEstoque mapResultSet(ResultSet rs) throws SQLException {
        return new ItemEstoque(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("lote"),
                rs.getString("categoria"),
                rs.getInt("quantidade"),
                rs.getInt("estoque_minimo"),
                rs.getString("validade"),
                rs.getDouble("valor_unitario"),
                rs.getString("unidade")
        );
    }
}
