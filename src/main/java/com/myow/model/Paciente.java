package com.myow.model;

public class Paciente {
    private String id;
    private String nome;
    private String especie;
    private String raca;
    private int idade;
    private String sexo; // "Macho" ou "Fêmea"
    private String tutorId;
    private String historicoMedico;
    private String dataCadastro;

    public Paciente() {}

    public Paciente(String id, String nome, String especie, String raca, int idade, String sexo, String tutorId, String historicoMedico, String dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.idade = idade;
        this.sexo = sexo;
        this.tutorId = tutorId;
        this.historicoMedico = historicoMedico;
        this.dataCadastro = dataCadastro;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public String getHistoricoMedico() { return historicoMedico; }
    public void setHistoricoMedico(String historicoMedico) { this.historicoMedico = historicoMedico; }

    public String getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(String dataCadastro) { this.dataCadastro = dataCadastro; }

    @Override
    public String toString() {
        return nome + " (" + especie + ", " + raca + ")";
    }
}
