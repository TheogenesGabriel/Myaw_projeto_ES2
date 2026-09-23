package com.myow.service;

import com.myow.dao.ItemEstoqueDAO;
import com.myow.model.ItemEstoque;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EstoqueService {
    private final ItemEstoqueDAO itemEstoqueDAO = new ItemEstoqueDAO();

    public List<ItemEstoque> listarTodos() {
        return itemEstoqueDAO.listarTodos();
    }

    public Optional<ItemEstoque> buscarPorId(String id) {
        return itemEstoqueDAO.buscarPorId(id);
    }

    public List<ItemEstoque> listarItensCriticos() {
        return itemEstoqueDAO.listarTodos().stream()
                .filter(ItemEstoque::isEstoqueBaixo)
                .collect(Collectors.toList());
    }

    public OperationResult<ItemEstoque> cadastrarItem(String nome, String lote, String categoria, int quantidade, int estoqueMinimo, String validade, double valorUnitario, String unidade, boolean somarSeExistir) {
        if (nome == null || nome.trim().isEmpty() ||
            lote == null || lote.trim().isEmpty() ||
            categoria == null || categoria.trim().isEmpty() ||
            validade == null || validade.trim().isEmpty() ||
            quantidade < 0 || estoqueMinimo < 0 || valorUnitario <= 0) {
            return OperationResult.error("Preencha todos os dados obrigatórios do item de estoque.");
        }

        Optional<ItemEstoque> duplicado = itemEstoqueDAO.buscarPorNomeELote(nome.trim(), lote.trim());

        if (duplicado.isPresent()) {
            if (somarSeExistir) {
                ItemEstoque itemExistente = duplicado.get();
                int novaQtd = itemExistente.getQuantidade() + quantidade;
                itemEstoqueDAO.atualizarQuantidade(itemExistente.getId(), novaQtd);
                itemExistente.setQuantidade(novaQtd);
                return OperationResult.ok("Item já existente com mesmo lote: Quantidade somada ao estoque atual!", itemExistente);
            } else {
                return OperationResult.error("DUPLICADO: Já existe um item cadastrado com este mesmo nome e lote.");
            }
        }

        String id = IdGenerator.nextId("est");
        ItemEstoque novo = new ItemEstoque(id, nome.trim(), lote.trim(), categoria.trim(), quantidade, estoqueMinimo, validade.trim(), valorUnitario, unidade != null ? unidade.trim() : "unidade");
        boolean salvo = itemEstoqueDAO.salvar(novo);
        if (!salvo) {
            return OperationResult.error("Erro ao persistir item no estoque.");
        }

        List<String> alertas = new ArrayList<>();
        if (novo.isEstoqueBaixo()) {
            alertas.add("Atenção: A quantidade cadastrada está igual ou abaixo do estoque mínimo definido (" + estoqueMinimo + ").");
        }

        return new OperationResult<>(true, "Item cadastrado com sucesso no estoque!", novo, alertas);
    }

    public OperationResult<ItemEstoque> editarItem(String id, String nome, String categoria, int estoqueMinimo, double valorUnitario) {
        Optional<ItemEstoque> opt = buscarPorId(id);
        if (opt.isEmpty()) return OperationResult.error("Item não encontrado.");

        ItemEstoque i = opt.get();
        i.setNome(nome.trim());
        i.setCategoria(categoria.trim());
        i.setEstoqueMinimo(estoqueMinimo);
        i.setValorUnitario(valorUnitario);

        boolean atualizado = itemEstoqueDAO.atualizar(i);
        if (!atualizado) {
            return OperationResult.error("Erro ao atualizar item de estoque no banco de dados.");
        }

        return OperationResult.ok("Item atualizado com sucesso!", i);
    }

    public OperationResult<ItemEstoque> ajustarQuantidade(String id, int delta) {
        Optional<ItemEstoque> opt = buscarPorId(id);
        if (opt.isEmpty()) return OperationResult.error("Item não encontrado.");

        ItemEstoque i = opt.get();
        int novaQtd = i.getQuantidade() + delta;
        if (novaQtd < 0) {
            return OperationResult.error("Quantidade em estoque não pode ser negativa.");
        }

        boolean atualizado = itemEstoqueDAO.atualizarQuantidade(id, novaQtd);
        if (!atualizado) {
            return OperationResult.error("Erro ao registrar ajuste de estoque.");
        }
        i.setQuantidade(novaQtd);

        List<String> alertas = new ArrayList<>();
        if (i.isEstoqueBaixo()) {
            alertas.add("Alerta: O item '" + i.getNome() + "' está com saldo crítico de " + i.getQuantidade() + " " + i.getUnidade() + "!");
        }

        return new OperationResult<>(true, "Quantidade ajustada com sucesso!", i, alertas);
    }

    public OperationResult<Void> darBaixa(String id, int quantidadeUsada) {
        Optional<ItemEstoque> opt = buscarPorId(id);
        if (opt.isEmpty()) return OperationResult.error("Item não encontrado.");

        ItemEstoque i = opt.get();
        if (i.getQuantidade() < quantidadeUsada) {
            return OperationResult.error("Saldo insuficiente de '" + i.getNome() + "'. Disponível: " + i.getQuantidade() + ", Solicitado: " + quantidadeUsada);
        }
        int novaQtd = i.getQuantidade() - quantidadeUsada;
        boolean atualizado = itemEstoqueDAO.atualizarQuantidade(id, novaQtd);
        if (!atualizado) {
            return OperationResult.error("Erro ao debitar estoque.");
        }
        i.setQuantidade(novaQtd);
        return OperationResult.ok("Baixa realizada com sucesso!");
    }
}
