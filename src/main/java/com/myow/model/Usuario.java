package com.myow.model;

public class Usuario {
    private String id;
    private String nome;
    private String cpf;
    private String cargo;
    private Perfil perfil;
    private String login;
    private String senha;
    private String status; // "Ativo" ou "Inativo"
    private String telefone;
    private int tentativasLogin;
    private String bloqueadoAte; // Timestamp ISO se bloqueado temporariamente
    private String perguntaSeguranca;
    private String respostaSeguranca;

    public Usuario() {}

    public Usuario(String id, String nome, String cpf, String cargo, Perfil perfil, String login, String senha, String status, String telefone) {
        this(id, nome, cpf, cargo, perfil, login, senha, status, telefone, 0, null, "Qual o nome da clínica?", "myow");
    }

    public Usuario(String id, String nome, String cpf, String cargo, Perfil perfil, String login, String senha, String status, String telefone,
                   int tentativasLogin, String bloqueadoAte, String perguntaSeguranca, String respostaSeguranca) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.cargo = cargo;
        this.perfil = perfil;
        this.login = login;
        this.senha = senha;
        this.status = status;
        this.telefone = telefone;
        this.tentativasLogin = tentativasLogin;
        this.bloqueadoAte = bloqueadoAte;
        this.perguntaSeguranca = perguntaSeguranca;
        this.respostaSeguranca = respostaSeguranca;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public boolean isAtivo() {
        return "Ativo".equalsIgnoreCase(this.status);
    }

    public int getTentativasLogin() { return tentativasLogin; }
    public void setTentativasLogin(int tentativasLogin) { this.tentativasLogin = tentativasLogin; }

    public String getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(String bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }

    public String getPerguntaSeguranca() { return perguntaSeguranca; }
    public void setPerguntaSeguranca(String perguntaSeguranca) { this.perguntaSeguranca = perguntaSeguranca; }

    public String getRespostaSeguranca() { return respostaSeguranca; }
    public void setRespostaSeguranca(String respostaSeguranca) { this.respostaSeguranca = respostaSeguranca; }

    public boolean isBloqueadoTemporariamente() {
        if (bloqueadoAte == null || bloqueadoAte.trim().isEmpty()) {
            return false;
        }
        try {
            java.time.LocalDateTime ate = java.time.LocalDateTime.parse(bloqueadoAte);
            return java.time.LocalDateTime.now().isBefore(ate);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return nome + " (" + cargo + ")";
    }
}
