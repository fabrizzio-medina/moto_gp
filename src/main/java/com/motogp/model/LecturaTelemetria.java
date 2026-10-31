package com.motogp.model;


/**
 * Telemtria moto.
 * El formato binario tiene un peso de 52 bytes por registro.
 */
public record LecturaTelemetria(
        int idMoto,
        int vuelta,
        double velocidad,
        int rpm,
        double tempMotor,
        double tempFreno,
        double presionNeumatico,
        double inclinacion
) {

    // TAMANIO = 52 bytes
    public static final int TAMANIO_REGISTRO = Integer.BYTES + Integer.BYTES + Double.BYTES + Integer.BYTES
            + Double.BYTES + Double.BYTES + Double.BYTES + Double.BYTES;


}