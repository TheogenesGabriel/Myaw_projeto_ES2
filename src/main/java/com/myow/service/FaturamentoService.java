package com.myow.service;

import com.myow.dao.AtendimentoDAO;
import com.myow.dao.FaturamentoDAO;
import com.myow.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FaturamentoService {
    private final FaturamentoDAO faturamentoDAO = new FaturamentoDAO();
    private final AtendimentoDAO atendimentoDAO = new AtendimentoDAO();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public List<Faturamento> listarTodos() {
        return faturamentoDAO.listarTodos();
    }

    public List<Atendimento> listarPendentesFaturamento() {
        List<String> atendimentosFaturadosIds = faturamentoDAO.listarTodos().stream()
                .map(Faturamento::getAtendimentoId)
                .collect(Collectors.toList());

        return atendimentoDAO.listarTodos().stream()
                .filter(a -> !atendimentosFaturadosIds.contains(a.getId()) && !"Faturado".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
    }

    public OperationResult<Faturamento> registrarPagamento(
            String atendimentoId,
            FormaPagamento formaPagamento,
            Double valorRecebido
    ) {
        // Validação de controle de acesso (RBAC)
        Usuario logado = sessionManager.getUsuarioLogado();
        if (logado != null && logado.getPerfil() == Perfil.VETERINARIO) {
            return OperationResult.error("Acesso negado: O perfil Veterinário não possui permissão para registrar recebimentos no caixa.");
        }

        if (formaPagamento == null) {
            return OperationResult.error("Selecione uma forma de pagamento válida.");
        }

        Optional<Atendimento> atendOpt = atendimentoDAO.buscarPorId(atendimentoId);
        if (atendOpt.isEmpty()) {
            return OperationResult.error("Atendimento não encontrado para faturamento.");
        }
        Atendimento atend = atendOpt.get();

        Optional<Faturamento> jaFaturado = faturamentoDAO.buscarPorAtendimentoId(atendimentoId);
        if (jaFaturado.isPresent()) {
            return OperationResult.error("Este atendimento já foi faturado anteriormente.");
        }

        Double troco = null;
        if (formaPagamento == FormaPagamento.DINHEIRO) {
            if (valorRecebido == null || valorRecebido < atend.getValorTotal()) {
                return OperationResult.error("Valor recebido insuficiente. O valor recebido deve ser maior ou igual a R$ " + String.format("%.2f", atend.getValorTotal()));
            }
            troco = valorRecebido - atend.getValorTotal();
        }

        String id = IdGenerator.nextId("fat");
        String data = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String operadorId = logado != null ? logado.getId() : "usr_2";

        Faturamento fat = new Faturamento(
                id, atend.getId(), atend.getConsultaId(), atend.getPacienteId(), atend.getTutorId(),
                atend.getValorTotal(), formaPagamento, valorRecebido, troco, data, hora, operadorId
        );

        boolean salvo = faturamentoDAO.salvar(fat);
        if (!salvo) {
            return OperationResult.error("Erro ao registrar faturamento no banco de dados.");
        }

        atendimentoDAO.atualizarStatus(atend.getId(), "Faturado");
        atend.setStatus("Faturado");

        return OperationResult.ok("Pagamento registrado com sucesso! Comprovante emitido.", fat);
    }
}
