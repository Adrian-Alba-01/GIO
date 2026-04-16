package com.examp.gio.models;

public class Incidencia {
    public int idIncidencia;
    public String descripcion;
    public String estado;
    public String fecha;
    public String nombreUsuario;

    public Incidencia(int idIncidencia, String descripcion, String estado,
                      String fecha, String nombreUsuario) {
        this.idIncidencia = idIncidencia;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fecha = fecha;
        this.nombreUsuario = nombreUsuario;
    }
}