package com.example.huertapp.modelo;

import java.io.Serializable;

public class Planta implements Serializable {
    String nombre, descripcion, foto;
    String idPlanta, idHuerto;
    String fecha;
    String idUsuario;

    public Planta() {}

    public Planta(String nombre, String descripcion, String foto, String idPlanta, String idHuerto, String fecha,
                  String idUsuario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.idPlanta = idPlanta;
        this.idHuerto = idHuerto;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
    }

    public String getIdUsuario() { return idUsuario; }

    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getFecha() { return fecha; }

    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getIdHuerto() {
        return idHuerto;
    }

    public void setIdHuerto(String idHuerto) {
        this.idHuerto = idHuerto;
    }

    public String getIdPlanta() {
        return idPlanta;
    }

    public void setIdPlanta(String idPlanta) {
        this.idPlanta = idPlanta;
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
}
