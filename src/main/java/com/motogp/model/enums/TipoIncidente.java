package com.motogp.model.enums;

/**
 * Incidentes que puede producir el simulador y que el analizador forense sabe reconocer.
 */
public enum TipoIncidente {

    CAIDA_EN_CURVA(1, "Caída en curva"),
    ROTURA_DE_MOTOR(2, "Rotura de motor"),
    FALLO_DE_FRENOS(3, "Fallo de frenos"),
    PINCHAZO_DE_NEUMATICO(4, "Pinchazo de neumático"),
    LLUVIA(5, "Lluvia en pista"),
    PRESION_MINIMA_NEUMATICO(6, "Presión mínima de neumático");

    private final int id;
    private final String descripcion;

    TipoIncidente(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
