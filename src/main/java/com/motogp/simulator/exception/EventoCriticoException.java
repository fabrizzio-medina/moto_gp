package com.motogp.simulator.exception;

/**
 * Indica que el sistema ha detectado que una anomalía ha alcanzado una situación crítica.
 * No crea el incidente ni sus evidencias: estas ya están persistidas cuando se lanza.
 */
public class EventoCriticoException extends RuntimeException {

    private final int idMoto;
    private final int vuelta;

    public EventoCriticoException(String mensaje, int idMoto, int vuelta) {
        super(mensaje);
        this.idMoto = idMoto;
        this.vuelta = vuelta;
    }

    public int getIdMoto() { return idMoto; }
    public int getVuelta() { return vuelta; }
}
