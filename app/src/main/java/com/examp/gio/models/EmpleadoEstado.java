package com.examp.gio.models;

public class EmpleadoEstado {
    public int idUsuario;
    public String nombre;
    public String estado; // "activo", "descansando", "ausente"

    public EmpleadoEstado(int idUsuario, String nombre, String estado) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.estado = estado;
    }
}