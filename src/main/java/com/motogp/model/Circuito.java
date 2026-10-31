package com.motogp.model;

public class Circuito {
    private String nombre;

    public Circuito(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Circuito{" +
                "nombre='" + nombre + '\'' +
                '}';
    }
}
