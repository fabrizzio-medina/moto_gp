package com.motogp.model;

import com.motogp.model.enums.EstadoSesion;

import java.util.Objects;

/**
 * Resumen de una carrera en una línea de texto: id|circuito|totalVueltas|vueltaActual|estado
 */
public class Carrera {
    private static final int NUMERO_CAMPOS = 5;

    private final String id;
    private final Circuito circuito;
    private final int totalVueltas;
    private final int vueltaActual;
    private final EstadoSesion estado;

    public Carrera(String id, Circuito circuito, int totalVueltas, int vueltaActual, EstadoSesion estado) {
        if (totalVueltas <= 0) {
            throw new IllegalArgumentException("El número de vueltas debe ser positivo");
        }
        this.id = id;
        this.circuito = circuito;
        this.totalVueltas = totalVueltas;
        this.vueltaActual = vueltaActual;
        this.estado = estado;
    }

    public String toWriteTxt() {
        return String.join("|", id, circuito.getNombre(), String.valueOf(totalVueltas),
                String.valueOf(vueltaActual), estado.name());
    }

    public static Carrera parse(String registro) {
        if (registro == null || registro.isBlank()) {
            throw new IllegalArgumentException("El registro no puede ser nulo o estar en blanco.");
        }
        String[] c = registro.split("\\|", -1);
        if (c.length != NUMERO_CAMPOS) {
            throw new IllegalArgumentException("El registro debe contener " + NUMERO_CAMPOS + " campos: " + registro);
        }
        try {
            return new Carrera(c[0].trim(), new Circuito(c[1].trim()), Integer.parseInt(c[2].trim()),
                    Integer.parseInt(c[3].trim()), EstadoSesion.valueOf(c[4].trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Registro de carrera incorrecto: " + registro, e);
        }
    }

    public String getId() { return id; }
    public Circuito getCircuito() { return circuito; }
    public int getTotalVueltas() { return totalVueltas; }
    public int getVueltaActual() { return vueltaActual; }
    public EstadoSesion getEstado() { return estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carrera c)) return false;
        return totalVueltas == c.totalVueltas && vueltaActual == c.vueltaActual && Objects.equals(id, c.id)
                && Objects.equals(circuito.getNombre(), c.circuito.getNombre()) && estado == c.estado;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, circuito.getNombre(), totalVueltas, vueltaActual, estado);
    }
}
