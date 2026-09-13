package com.myow;

public class Paciente {
    
    private int id;
    private String nome;
    private int idade;
    private String raca;
    private String especie;
    private String sexo;
    private String historicoMedico;
    private Tutor tutor;

    // Construtor vazio
    public Paciente() {
    }

    // Construtor completo
    public Paciente(int id, String nome, int idade, String raca, String especie, String sexo, String historicoMedico, Tutor tutor) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
        this.raca = raca;
        this.especie = especie;
        this.sexo = sexo;
        this.historicoMedico = historicoMedico;
        this.tutor = tutor;
    }

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getHistoricoMedico() {
        return historicoMedico;
    }

    public void setHistoricoMedico(String historicoMedico) {
        this.historicoMedico = historicoMedico;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }
}