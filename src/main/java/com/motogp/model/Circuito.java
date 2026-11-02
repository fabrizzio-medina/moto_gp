package com.motogp.model;

import java.util.Objects;

public class Circuito {
    private String nombre;
    private String pais;
    private String condicionPista;

    public Circuito() {
    }

    public Circuito(String nombre) {
        this(nombre, "", "SECA");
    }

    public Circuito(String nombre, String pais, String condicionPista) {
        this.nombre = nombre;
        this.pais = pais;
        this.condicionPista = condicionPista;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCondicionPista() { return condicionPista; }
    public void setCondicionPista(String condicionPista) { this.condicionPista = condicionPista; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Circuito c)) return false;
        return Objects.equals(nombre, c.nombre) && Objects.equals(pais, c.pais)
                && Objects.equals(condicionPista, c.condicionPista);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, pais, condicionPista);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
