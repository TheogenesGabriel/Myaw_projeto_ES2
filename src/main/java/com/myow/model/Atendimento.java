package com.myow.model;

import java.util.ArrayList;
import java.util.List;

public class Atendimento {
    private String id;
    private String consultaId;
    private String pacienteId;
    private String tutorId;
    private String veterinarioId;
    private String data;
    private String hora;
    private String diagnostico;
    private String procedimentos;
    private String prescricoes;
    private String observacoesClinicas;
    private List<ItemUtilizado> itensUtilizados = new ArrayList<>();
    private double valorProcedimentos;
    private double valorTotal;
    private String status; // "Concluído" ou "Faturado"

    public Atendimento() {}

    public Atendimento(String id, String consultaId, String pacienteId, String tutorId, String veterinarioId, String data, String hora, String diagnostico, String procedimentos, String prescricoes, String observacoesClinicas, List<ItemUtilizado> itensUtilizados, double valorProcedimentos, double valorTotal, String status) {
        this.id = id;
        this.consultaId = consultaId;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.veterinarioId = veterinarioId;
        this.data = data;
        this.hora = hora;
        this.diagnostico = diagnostico;
        this.procedimentos = procedimentos;
        this.prescricoes = prescricoes;
        this.observacoesClinicas = observacoesClinicas;
        this.itensUtilizados = itensUtilizados != null ? itensUtilizados : new ArrayList<>();
        this.valorProcedimentos = valorProcedimentos;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConsultaId() { return consultaId; }
    public void setConsultaId(String consultaId) { this.consultaId = consultaId; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(String veterinarioId) { this.veterinarioId = veterinarioId; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getProcedimentos() { return procedimentos; }
    public void setProcedimentos(String procedimentos) { this.procedimentos = procedimentos; }

    public String getPrescricoes() { return prescricoes; }
    public void setPrescricoes(String prescricoes) { this.prescricoes = prescricoes; }

    public String getObservacoesClinicas() { return observacoesClinicas; }
    public void setObservacoesClinicas(String observacoesClinicas) { this.observacoesClinicas = observacoesClinicas; }

    public List<ItemUtilizado> getItensUtilizados() { return itensUtilizados; }
    public void setItensUtilizados(List<ItemUtilizado> itensUtilizados) { this.itensUtilizados = itensUtilizados; }

    public double getValorProcedimentos() { return valorProcedimentos; }
    public void setValorProcedimentos(double valorProcedimentos) { this.valorProcedimentos = valorProcedimentos; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
