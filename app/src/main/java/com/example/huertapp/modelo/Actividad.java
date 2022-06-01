package com.example.huertapp.modelo;

import java.io.Serializable;
import java.util.Date;

public class Actividad implements Serializable {
    String tipo, observaciones, fecha;
    String idActividad, idPlanta;

    public Actividad() {}

    public Actividad(String tipo, String observaciones, String fecha, String idActividad, String idPlanta) {
        this.tipo = tipo;
        this.observaciones = observaciones;
        this.fecha = fecha;
        this.idActividad = idActividad;
        this.idPlanta = idPlanta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(String idActividad) {
        this.idActividad = idActividad;
    }

    public String getIdPlanta() {
        return idPlanta;
    }

    public void setIdPlanta(String idPlanta) {
        this.idPlanta = idPlanta;
    }
}
