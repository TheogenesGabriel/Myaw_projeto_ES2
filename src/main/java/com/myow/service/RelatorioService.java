package com.myow.service;

import com.myow.dao.AtendimentoDAO;
import com.myow.dao.FaturamentoDAO;
import com.myow.dao.ItemEstoqueDAO;
import com.myow.model.Atendimento;
import com.myow.model.Faturamento;
import com.myow.model.FormaPagamento;
import com.myow.model.ItemEstoque;
import com.myow.model.Perfil;
import com.myow.model.Usuario;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RelatorioService {
    private final FaturamentoDAO faturamentoDAO = new FaturamentoDAO();
    private final AtendimentoDAO atendimentoDAO = new AtendimentoDAO();
    private final ItemEstoqueDAO itemEstoqueDAO = new ItemEstoqueDAO();
    private final RelatorioExportService exportService = new RelatorioExportService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public boolean isPeriodoValido(String dataInicio, String dataFim) {
        if (dataInicio == null || dataFim == null || dataInicio.isEmpty() || dataFim.isEmpty()) {
            return true;
        }
        return dataInicio.compareTo(dataFim) <= 0;
    }

    public List<Faturamento> filtrarFaturamento(String dataInicio, String dataFim) {
        return faturamentoDAO.listarPorPeriodo(dataInicio, dataFim);
    }

    public Map<String, Object> gerarResumoFinanceiro(String dataInicio, String dataFim) {
        List<Faturamento> lista = filtrarFaturamento(dataInicio, dataFim);
        double total = lista.stream().mapToDouble(Faturamento::getValorTotal).sum();
        
        // Mantém o ticket médio em zero quando não há faturamentos no período.
        double ticketMedio = lista.isEmpty() ? 0.0 : total / lista.size();

        double totalDinheiro = lista.stream().filter(f -> f.getFormaPagamento() == FormaPagamento.DINHEIRO).mapToDouble(Faturamento::getValorTotal).sum();
        double totalCartao = lista.stream().filter(f -> f.getFormaPagamento() == FormaPagamento.CARTAO).mapToDouble(Faturamento::getValorTotal).sum();
        double totalPix = lista.stream().filter(f -> f.getFormaPagamento() == FormaPagamento.PIX).mapToDouble(Faturamento::getValorTotal).sum();

        Map<String, Object> resumo = new HashMap<>();
        resumo.put("total", total);
        resumo.put("ticketMedio", ticketMedio);
        resumo.put("quantidade", lista.size());
        resumo.put("totalDinheiro", totalDinheiro);
        resumo.put("totalCartao", totalCartao);
        resumo.put("totalPix", totalPix);
        resumo.put("registros", lista);
        return resumo;
    }

    public List<Atendimento> filtrarAtendimentos(String dataInicio, String dataFim) {
        return atendimentoDAO.listarTodos().stream().filter(a -> {
            if (dataInicio != null && !dataInicio.isEmpty() && a.getData().compareTo(dataInicio) < 0) return false;
            if (dataFim != null && !dataFim.isEmpty() && a.getData().compareTo(dataFim) > 0) return false;
            return true;
        }).collect(Collectors.toList());
    }

    public List<ItemEstoque> obterItensEstoqueCritico() {
        return itemEstoqueDAO.listarTodos().stream().filter(ItemEstoque::isEstoqueBaixo).collect(Collectors.toList());
    }

    // Exportação em PDF
    public OperationResult<File> exportarPdf(File destino, String dataInicio, String dataFim) {
        // Validação de controle de acesso (RBAC)
        Usuario logado = sessionManager.getUsuarioLogado();
        if (logado != null && logado.getPerfil() != Perfil.ADMINISTRADOR) {
            return OperationResult.error("Acesso negado: Apenas Administradores possuem permissão para exportar relatórios.");
        }

        if (!isPeriodoValido(dataInicio, dataFim)) {
            return OperationResult.error("Período inválido: A data inicial não pode ser posterior à data final.");
        }

        Map<String, Object> resumo = gerarResumoFinanceiro(dataInicio, dataFim);
        @SuppressWarnings("unchecked")
        List<Faturamento> registros = (List<Faturamento>) resumo.get("registros");
        return exportService.exportarParaPdf(destino, dataInicio, dataFim, resumo, registros);
    }

    // Exportação em Excel
    public OperationResult<File> exportarExcel(File destino, String dataInicio, String dataFim) {
        // Validação de controle de acesso (RBAC)
        Usuario logado = sessionManager.getUsuarioLogado();
        if (logado != null && logado.getPerfil() != Perfil.ADMINISTRADOR) {
            return OperationResult.error("Acesso negado: Apenas Administradores possuem permissão para exportar relatórios.");
        }

        if (!isPeriodoValido(dataInicio, dataFim)) {
            return OperationResult.error("Período inválido: A data inicial não pode ser posterior à data final.");
        }

        Map<String, Object> resumo = gerarResumoFinanceiro(dataInicio, dataFim);
        @SuppressWarnings("unchecked")
        List<Faturamento> registros = (List<Faturamento>) resumo.get("registros");
        return exportService.exportarParaExcel(destino, dataInicio, dataFim, resumo, registros);
    }
}
