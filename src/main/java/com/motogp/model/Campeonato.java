package com.motogp.model;

import com.motogp.model.enums.EstadoSesion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Estado estructurado de la carrera que se persiste en campeonato.json (Data Binding con Jackson).
 */
public class Campeonato {

    private String nombre;
    private String idCarrera;
    private Circuito circuito;
    private int totalVueltas;
    private int vueltaActual;
    private EstadoSesion estadoSesion;
    private List<Moto> motos = new ArrayList<>();

    public Campeonato() {
    }

    public Campeonato(String nombre, String idCarrera, Circuito circuito, int totalVueltas) {
        this.nombre = nombre;
        this.idCarrera = idCarrera;
        this.circuito = circuito;
        this.totalVueltas = totalVueltas;
        this.vueltaActual = 0;
        this.estadoSesion = EstadoSesion.PROGRAMADA;
    }

    public Optional<Moto> buscarMoto(int idMoto) {
        return motos.stream().filter(m -> m.getId() == idMoto).findFirst();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getIdCarrera() { return idCarrera; }
    public void setIdCarrera(String idCarrera) { this.idCarrera = idCarrera; }
    public Circuito getCircuito() { return circuito; }
    public void setCircuito(Circuito circuito) { this.circuito = circuito; }
    public int getTotalVueltas() { return totalVueltas; }
    public void setTotalVueltas(int totalVueltas) { this.totalVueltas = totalVueltas; }
    public int getVueltaActual() { return vueltaActual; }
    public void setVueltaActual(int vueltaActual) { this.vueltaActual = vueltaActual; }
    public EstadoSesion getEstadoSesion() { return estadoSesion; }
    public void setEstadoSesion(EstadoSesion estadoSesion) { this.estadoSesion = estadoSesion; }
    public List<Moto> getMotos() { return motos; }
    public void setMotos(List<Moto> motos) { this.motos = motos != null ? motos : new ArrayList<>(); }
}
