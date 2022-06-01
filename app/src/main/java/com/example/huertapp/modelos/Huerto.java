package com.example.huertapp.modelos;

import java.io.Serializable;

public class Huerto implements Serializable {
    String nombre, descripcion, foto;
    String idHuerto, idUsuario;

    public Huerto() {}

    public Huerto(String nombre, String descripcion, String foto, String idHuerto, String idUsuario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.idHuerto = idHuerto;
        this.idUsuario = idUsuario;
    }

    public String getidUsuario() {
        return idUsuario;
    }

    public void setidUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getIdHuerto() { return idHuerto; }

    public void setIdHuerto(String idHuerto) { this.idHuerto = idHuerto; }
}
