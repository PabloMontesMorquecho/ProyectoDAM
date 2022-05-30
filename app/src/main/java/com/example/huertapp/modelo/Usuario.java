package com.example.huertapp.modelo;

import java.io.Serializable;

public class Usuario implements Serializable {
    String email, nombre;

    public Usuario() {
    }
    public Usuario(String email, String nombre) {
        this.email = email;
        this.nombre = nombre;
    }

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
