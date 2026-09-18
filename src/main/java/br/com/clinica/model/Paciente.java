package br.com.clinica.model;

public class Paciente {
    private int id;
    private int tutorId;
    private String tutorNome;
    private String nome;
    private String especie;
    private String raca;
    private int idade;
    private String sexo;
    private String historicoMedico;

    public Paciente() {}

    public Paciente(int id, int tutorId, String nome, String especie, String raca, int idade, String sexo, String historicoMedico) {
        this.id = id;
        this.tutorId = tutorId;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.idade = idade;
        this.sexo = sexo;
        this.historicoMedico = historicoMedico;
    }

    public Paciente(int tutorId, String nome, String especie, String raca, int idade, String sexo, String historicoMedico) {
        this.tutorId = tutorId;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.idade = idade;
        this.sexo = sexo;
        this.historicoMedico = historicoMedico;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTutorId() { return tutorId; }
    public void setTutorId(int tutorId) { this.tutorId = tutorId; }

    public String getTutorNome() { return tutorNome; }
    public void setTutorNome(String tutorNome) { this.tutorNome = tutorNome; }

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

    public String getHistoricoMedico() { return historicoMedico; }
    public void setHistoricoMedico(String historicoMedico) { this.historicoMedico = historicoMedico; }

    @Override
    public String toString() {
        return nome + " (" + especie + " - " + raca + ")";
    }
}
