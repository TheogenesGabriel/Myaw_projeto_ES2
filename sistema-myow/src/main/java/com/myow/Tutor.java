package com.myow;

public class Tutor {
    
    // Atributos privados (Encapsulamento - conceito de POO)
    // O documento exige: Nome Completo, CPF, Telefone, E-mail e Endereço
    private String nomeCompleto;
    private String cpf;
    private String telefone;
    private String email;
    private String endereco;

    // Construtor vazio
    public Tutor() {
    }

    // Construtor com todos os dados
    public Tutor(String nomeCompleto, String cpf, String telefone, String email, String endereco) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
        this.endereco = endereco;
    }

    // Getters e Setters (Para acessar e modificar os dados)
    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}