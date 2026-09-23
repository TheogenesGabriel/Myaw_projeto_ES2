package com.myow.model;

public class Consulta {
    private String id;
    private String data; // YYYY-MM-DD
    private String horario; // HH:mm
    private String pacienteId;
    private String tutorId;
    private String veterinarioId;
    private StatusConsulta status;
    private String motivo;
    private String observacoes;
    private String dataAgendamento;

    public Consulta() {}

    public Consulta(String id, String data, String horario, String pacienteId, String tutorId, String veterinarioId, StatusConsulta status, String motivo, String observacoes, String dataAgendamento) {
        this.id = id;
        this.data = data;
        this.horario = horario;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.veterinarioId = veterinarioId;
        this.status = status;
        this.motivo = motivo;
        this.observacoes = observacoes;
        this.dataAgendamento = dataAgendamento;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(String veterinarioId) { this.veterinarioId = veterinarioId; }

    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(String dataAgendamento) { this.dataAgendamento = dataAgendamento; }
}
