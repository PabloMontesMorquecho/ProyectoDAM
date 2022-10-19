package com.example.huertapp.modelo;

import java.io.Serializable;

public class Usuario implements Serializable {
    String idUsuario, email, nombre, password;

    public Usuario() {
    }
    public Usuario(String idUsuario, String email, String nombre, String password) {
        this.idUsuario = idUsuario;
        this.email = email;
        this.nombre = nombre;
        this.password = password;
    }

    public String getIdUsuario() { return idUsuario; }

    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
