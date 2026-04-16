package com.examp.gio.models;

public class Tarea {
    public int idTarea;
    public String nombre;
    public String descripcion;
    public String estado;
    public String fechaInicio;
    public String fechaFin;

    public Tarea(int idTarea, String nombre, String descripcion,
                 String estado, String fechaInicio, String fechaFin) {
        this.idTarea = idTarea;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
}