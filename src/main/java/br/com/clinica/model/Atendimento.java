package br.com.clinica.model;

public class Atendimento {
    private int id;
    private int consultaId;
    private double pesoKg;
    private double temperaturaC;
    private int frequenciaCardiaca;
    private String diagnostico;
    private String procedimentosRealizados;
    private String prescricaoMedicamentosa;
    private String recomendacoes;
    private String dataAtendimento;

    public Atendimento() {}

    public Atendimento(int consultaId, double pesoKg, double temperaturaC, int frequenciaCardiaca,
                       String diagnostico, String procedimentosRealizados,
                       String prescricaoMedicamentosa, String recomendacoes) {
        this.consultaId = consultaId;
        this.pesoKg = pesoKg;
        this.temperaturaC = temperaturaC;
        this.frequenciaCardiaca = frequenciaCardiaca;
        this.diagnostico = diagnostico;
        this.procedimentosRealizados = procedimentosRealizados;
        this.prescricaoMedicamentosa = prescricaoMedicamentosa;
        this.recomendacoes = recomendacoes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConsultaId() { return consultaId; }
    public void setConsultaId(int consultaId) { this.consultaId = consultaId; }

    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }

    public double getTemperaturaC() { return temperaturaC; }
    public void setTemperaturaC(double temperaturaC) { this.temperaturaC = temperaturaC; }

    public int getFrequenciaCardiaca() { return frequenciaCardiaca; }
    public void setFrequenciaCardiaca(int frequenciaCardiaca) { this.frequenciaCardiaca = frequenciaCardiaca; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getProcedimentosRealizados() { return procedimentosRealizados; }
    public void setProcedimentosRealizados(String procedimentosRealizados) { this.procedimentosRealizados = procedimentosRealizados; }

    public String getPrescricaoMedicamentosa() { return prescricaoMedicamentosa; }
    public void setPrescricaoMedicamentosa(String prescricaoMedicamentosa) { this.prescricaoMedicamentosa = prescricaoMedicamentosa; }

    public String getRecomendacoes() { return recomendacoes; }
    public void setRecomendacoes(String recomendacoes) { this.recomendacoes = recomendacoes; }

    public String getDataAtendimento() { return dataAtendimento; }
    public void setDataAtendimento(String dataAtendimento) { this.dataAtendimento = dataAtendimento; }
}
