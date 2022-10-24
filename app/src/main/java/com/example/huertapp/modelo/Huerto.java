package com.example.huertapp.modelo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Huerto implements Serializable {
    String nombre, descripcion, foto;
    String idHuerto, idUsuario;
    String fecha;
    public Map<String, Boolean> miembros = new HashMap<>();

    public Huerto() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Huerto(String nombre, String descripcion, String foto, String idHuerto, String idUsuario, String fecha) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.foto = foto;
        this.idHuerto = idHuerto;
        this.idUsuario = idUsuario;
        this.fecha = fecha;
    }

    public String getFecha() { return fecha; }

    public void setFecha(String fecha) { this.fecha = fecha; }

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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fecha", fecha);
        result.put("idHuerto", idHuerto);
        result.put("idUsuario", idUsuario);
        result.put("nombre", nombre);
        result.put("descripcion", descripcion);
        result.put("foto", foto);
        result.put("miembros", miembros);

        return result;
    }
}
