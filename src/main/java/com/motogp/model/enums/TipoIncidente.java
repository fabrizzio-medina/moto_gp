package com.motogp.model.enums;

public enum TipoIncidente {

    CAIDA_EN_CURVA(1, "Caída en curva"),
    ROTURA_DE_MOTOR(2, "Rotura de motor"),
    FALLO_DE_FRENOS(3, "Fallo de frenos"),
    PINCHAZO_DE_NEUMATICO(4, "pinchazo de neumático"),
    LLUVIA(5, "Lluvia"),
    PRESION_MINIMA__NEUMATICO(6, "Presion minina de neumatico");

    private final int id;
    private final String descripcion;

    TipoIncidente(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }


}
