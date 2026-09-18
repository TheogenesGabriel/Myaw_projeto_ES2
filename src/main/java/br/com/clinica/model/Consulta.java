package br.com.clinica.model;

public class Consulta {
    private int id;
    private int pacienteId;
    private String pacienteNome;
    private String tutorNome;
    private String veterinario;
    private String dataConsulta;
    private String horario;
    private String motivo;
    private String status; // AGENDADA, REALIZADA, CANCELADA
    private String observacoes;
    private String motivoCancelamento;

    public Consulta() {}

    public Consulta(int id, int pacienteId, String veterinario, String dataConsulta, String horario, String motivo, String status, String observacoes) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.veterinario = veterinario;
        this.dataConsulta = dataConsulta;
        this.horario = horario;
        this.motivo = motivo;
        this.status = status;
        this.observacoes = observacoes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }

    public String getPacienteNome() { return pacienteNome; }
    public void setPacienteNome(String pacienteNome) { this.pacienteNome = pacienteNome; }

    public String getTutorNome() { return tutorNome; }
    public void setTutorNome(String tutorNome) { this.tutorNome = tutorNome; }

    public String getVeterinario() { return veterinario; }
    public void setVeterinario(String veterinario) { this.veterinario = veterinario; }

    public String getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(String dataConsulta) { this.dataConsulta = dataConsulta; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    @Override
    public String toString() {
        return "Consulta #" + id + " - " + (pacienteNome != null ? pacienteNome : "Paciente " + pacienteId) + " (" + dataConsulta + " às " + horario + ")";
    }
}
