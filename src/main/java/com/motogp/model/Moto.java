package com.motogp.model;

import java.util.Objects;

/**
 * Moto participante. Su id es la clave que relaciona JSON, TXT y BIN.
 */
public class Moto {

    private int id;
    private String nombrePiloto;
    private String equipo;
    private String motor;
    private String frenos;
    private String neumaticos;
    private String estado;

    public Moto() {
    }

    public Moto(int id, String nombrePiloto, String equipo) {
        this.id = id;
        this.nombrePiloto = nombrePiloto;
        this.equipo = equipo;
        this.motor = "OPERATIVO";
        this.frenos = "OPERATIVOS";
        this.neumaticos = "OPERATIVOS";
        this.estado = "EN_PISTA";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombrePiloto() { return nombrePiloto; }
    public void setNombrePiloto(String nombrePiloto) { this.nombrePiloto = nombrePiloto; }
    public String getEquipo() { return equipo; }
    public void setEquipo(String equipo) { this.equipo = equipo; }
    public String getMotor() { return motor; }
    public void setMotor(String motor) { this.motor = motor; }
    public String getFrenos() { return frenos; }
    public void setFrenos(String frenos) { this.frenos = frenos; }
    public String getNeumaticos() { return neumaticos; }
    public void setNeumaticos(String neumaticos) { this.neumaticos = neumaticos; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Moto moto)) return false;
        return id == moto.id && Objects.equals(nombrePiloto, moto.nombrePiloto)
                && Objects.equals(equipo, moto.equipo) && Objects.equals(motor, moto.motor)
                && Objects.equals(frenos, moto.frenos) && Objects.equals(neumaticos, moto.neumaticos)
                && Objects.equals(estado, moto.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombrePiloto, equipo, motor, frenos, neumaticos, estado);
    }

    @Override
    public String toString() {
        return "Moto{id=" + id + ", piloto='" + nombrePiloto + "', estado=" + estado + "}";
    }
}
