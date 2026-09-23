package com.myow.service;

import com.myow.dao.AtendimentoDAO;
import com.myow.dao.ConsultaDAO;
import com.myow.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AtendimentoService {
    private final AtendimentoDAO atendimentoDAO = new AtendimentoDAO();
    private final ConsultaDAO consultaDAO = new ConsultaDAO();
    private final EstoqueService estoqueService = new EstoqueService();
    private final AgendamentoService agendamentoService = new AgendamentoService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public List<Atendimento> listarTodos() {
        return atendimentoDAO.listarTodos();
    }

    public Optional<Atendimento> buscarPorId(String id) {
        return atendimentoDAO.buscarPorId(id);
    }

    public List<Atendimento> listarPorPaciente(String pacienteId) {
        return atendimentoDAO.listarPorPaciente(pacienteId);
    }

    public List<Consulta> listarConsultasAguardando() {
        return consultaDAO.listarTodas().stream()
                .filter(c -> c.getStatus() == StatusConsulta.AGUARDANDO_ATENDIMENTO || 
                             c.getStatus() == StatusConsulta.AGENDADA || 
                             c.getStatus() == StatusConsulta.EM_ANDAMENTO)
                .collect(Collectors.toList());
    }

    public OperationResult<Atendimento> registrarAtendimento(
            String consultaId,
            String diagnostico,
            String procedimentos,
            String prescricoes,
            String obsClinicas,
            List<ItemUtilizado> itensUtilizados,
            double valorProcedimentos
    ) {
        // Validação de controle de acesso (RBAC)
        Usuario logado = sessionManager.getUsuarioLogado();
        if (logado != null && logado.getPerfil() != Perfil.VETERINARIO && logado.getPerfil() != Perfil.ADMINISTRADOR) {
            return OperationResult.error("Acesso negado: Apenas Médicos Veterinários ou Administradores possuem permissão para registrar atendimentos clínicos.");
        }

        if (consultaId == null || diagnostico == null || diagnostico.trim().isEmpty() || procedimentos == null || procedimentos.trim().isEmpty()) {
            return OperationResult.error("Preencha todos os campos obrigatórios do atendimento (Diagnóstico e Procedimentos).");
        }

        Optional<Consulta> consultaOpt = agendamentoService.buscarPorId(consultaId);
        if (consultaOpt.isEmpty()) {
            return OperationResult.error("Consulta vinculada não encontrada.");
        }
        Consulta consulta = consultaOpt.get();

        // Verificar disponibilidade no estoque para todos os itens antes de dar baixa
        if (itensUtilizados != null) {
            for (ItemUtilizado item : itensUtilizados) {
                Optional<ItemEstoque> estoqueOpt = estoqueService.buscarPorId(item.getItemId());
                if (estoqueOpt.isEmpty() || estoqueOpt.get().getQuantidade() < item.getQuantidade()) {
                    int disp = estoqueOpt.map(ItemEstoque::getQuantidade).orElse(0);
                    return OperationResult.error("Saldo insuficiente para o item '" + item.getNome() + "'. Disponível: " + disp + ", Solicitado: " + item.getQuantidade());
                }
            }
        }

        // Realizar as baixas de estoque
        List<String> alertasEstoque = new ArrayList<>();
        if (itensUtilizados != null) {
            for (ItemUtilizado item : itensUtilizados) {
                estoqueService.darBaixa(item.getItemId(), item.getQuantidade());
                estoqueService.buscarPorId(item.getItemId()).ifPresent(i -> {
                    if (i.isEstoqueBaixo()) {
                        alertasEstoque.add("Alerta de estoque: '" + i.getNome() + "' atingiu nível crítico (" + i.getQuantidade() + " " + i.getUnidade() + ").");
                    }
                });
            }
        }

        double totalItens = (itensUtilizados != null) ? itensUtilizados.stream().mapToDouble(ItemUtilizado::getSubtotal).sum() : 0.0;
        double valorTotal = valorProcedimentos + totalItens;

        String id = IdGenerator.nextId("atend");
        String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String horaAgora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Atendimento atendimento = new Atendimento(
                id, consultaId, consulta.getPacienteId(), consulta.getTutorId(), consulta.getVeterinarioId(),
                dataHoje, horaAgora, diagnostico.trim(), procedimentos.trim(),
                prescricoes != null ? prescricoes.trim() : "",
                obsClinicas != null ? obsClinicas.trim() : "",
                itensUtilizados != null ? itensUtilizados : new ArrayList<>(),
                valorProcedimentos, valorTotal, "Concluído"
        );

        boolean salvo = atendimentoDAO.salvar(atendimento);
        if (!salvo) {
            return OperationResult.error("Erro ao persistir atendimento no banco de dados.");
        }

        // Atualizar status da consulta para REALIZADA no banco de dados
        consultaDAO.atualizarStatus(consultaId, StatusConsulta.REALIZADA);

        return new OperationResult<>(true, "Atendimento registrado e concluído com sucesso!", atendimento, alertasEstoque);
    }
}
