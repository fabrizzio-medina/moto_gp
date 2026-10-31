package com.motogp.model;

public class Moto {

    private int id;

    private String nombrePiloto;

    private String motor;

    private String neumaticos;

    private String estado;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombrePiloto() {
        return nombrePiloto;
    }

    public void setNombrePiloto(String nombrePiloto) {
        this.nombrePiloto = nombrePiloto;
    }

    public String getMotor() {
        return motor;
    }

    public void setMotor(String motor) {
        this.motor = motor;
    }

    public String getNeumaticos() {
        return neumaticos;
    }

    public void setNeumaticos(String neumaticos) {
        this.neumaticos = neumaticos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
