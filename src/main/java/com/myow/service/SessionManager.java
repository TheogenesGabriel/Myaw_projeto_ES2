package com.myow.service;

import com.myow.model.Usuario;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private Usuario usuarioLogado;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(Usuario usuario) {
        this.usuarioLogado = usuario;
    }

    public void logout() {
        this.usuarioLogado = null;
    }
}
