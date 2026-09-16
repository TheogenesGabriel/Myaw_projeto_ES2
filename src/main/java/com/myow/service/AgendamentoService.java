package com.myow.service;

import com.myow.dao.ConsultaDAO;
import com.myow.model.Consulta;
import com.myow.model.StatusConsulta;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AgendamentoService {
    private final ConsultaDAO consultaDAO = new ConsultaDAO();

    public List<Consulta> listarTodas() {
        return consultaDAO.listarTodas();
    }

    public Optional<Consulta> buscarPorId(String id) {
        return consultaDAO.buscarPorId(id);
    }

    public boolean verificarConflito(String data, String horario, String veterinarioId, String ignorarConsultaId) {
        List<Consulta> conflitos = consultaDAO.buscarConflitoHorario(veterinarioId, data, horario, ignorarConsultaId);
        return !conflitos.isEmpty();
    }

    public OperationResult<Consulta> agendar(String data, String horario, String pacienteId, String tutorId, String veterinarioId, String motivo, String obs) {
        if (data == null || data.trim().isEmpty() ||
            horario == null || horario.trim().isEmpty() ||
            pacienteId == null || pacienteId.trim().isEmpty() ||
            tutorId == null || tutorId.trim().isEmpty() ||
            veterinarioId == null || veterinarioId.trim().isEmpty() ||
            motivo == null || motivo.trim().isEmpty()) {
            return OperationResult.error("Preencha todos os campos obrigatórios para o agendamento.");
        }

        if (verificarConflito(data, horario, veterinarioId, null)) {
            return OperationResult.error("Horário indisponível: O veterinário selecionado já possui uma consulta marcada para este mesmo dia e horário.");
        }

        String id = IdGenerator.nextId("cons");
        String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Consulta consulta = new Consulta(id, data, horario, pacienteId, tutorId, veterinarioId, StatusConsulta.AGENDADA, motivo.trim(), obs != null ? obs.trim() : "", dataHoje);

        boolean salvo = consultaDAO.salvar(consulta);
        if (!salvo) {
            return OperationResult.error("Erro ao salvar agendamento no banco de dados.");
        }

        return OperationResult.ok("Consulta agendada com sucesso!", consulta);
    }

    public OperationResult<Consulta> alterar(String consultaId, String novaData, String novoHorario, String novoVeterinarioId, String novoMotivo, String novaObs) {
        Optional<Consulta> opt = buscarPorId(consultaId);
        if (opt.isEmpty()) {
            return OperationResult.error("Consulta não encontrada.");
        }
        Consulta c = opt.get();

        if (c.getStatus() == StatusConsulta.REALIZADA || c.getStatus() == StatusConsulta.CANCELADA) {
            return OperationResult.error("Consultas já realizadas ou canceladas não podem ser alteradas.");
        }

        if (verificarConflito(novaData, novoHorario, novoVeterinarioId, consultaId)) {
            return OperationResult.error("Horário indisponível: O veterinário já possui um agendamento neste horário.");
        }

        c.setData(novaData);
        c.setHorario(novoHorario);
        c.setVeterinarioId(novoVeterinarioId);
        if (novoMotivo != null && !novoMotivo.trim().isEmpty()) c.setMotivo(novoMotivo.trim());
        if (novaObs != null) c.setObservacoes(novaObs.trim());

        boolean atualizado = consultaDAO.atualizar(c);
        if (!atualizado) {
            return OperationResult.error("Erro ao atualizar consulta no banco de dados.");
        }

        return OperationResult.ok("Consulta remarcada com sucesso!", c);
    }

    public OperationResult<Consulta> cancelar(String consultaId, String motivoCancelamento) {
        if (motivoCancelamento == null || motivoCancelamento.trim().isEmpty()) {
            return OperationResult.error("Informe a justificativa/motivo do cancelamento.");
        }

        Optional<Consulta> opt = buscarPorId(consultaId);
        if (opt.isEmpty()) {
            return OperationResult.error("Consulta não encontrada.");
        }
        Consulta c = opt.get();

        if (c.getStatus() == StatusConsulta.REALIZADA) {
            return OperationResult.error("Não é possível cancelar uma consulta que já foi concluída.");
        }

        c.setStatus(StatusConsulta.CANCELADA);
        String obsAtual = c.getObservacoes() != null ? c.getObservacoes() : "";
        c.setObservacoes(obsAtual + " [CANCELADA: " + motivoCancelamento.trim() + "]");

        boolean atualizado = consultaDAO.atualizar(c);
        if (!atualizado) {
            return OperationResult.error("Erro ao registrar cancelamento no banco de dados.");
        }

        return OperationResult.ok("Consulta cancelada com sucesso!", c);
    }

    public void atualizarStatus(String consultaId, StatusConsulta novoStatus) {
        consultaDAO.atualizarStatus(consultaId, novoStatus);
    }
}
