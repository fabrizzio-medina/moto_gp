package com.motogp.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Lectura de telemetría de una moto en una vuelta.
 * Registro binario de longitud fija: 52 bytes.
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

    public static final int TAMANIO_REGISTRO = Integer.BYTES + Integer.BYTES + Double.BYTES + Integer.BYTES
            + Double.BYTES + Double.BYTES + Double.BYTES + Double.BYTES;

    public void escribir(DataOutput out) throws IOException {
        out.writeInt(idMoto);
        out.writeInt(vuelta);
        out.writeDouble(velocidad);
        out.writeInt(rpm);
        out.writeDouble(tempMotor);
        out.writeDouble(tempFreno);
        out.writeDouble(presionNeumatico);
        out.writeDouble(inclinacion);
    }

    public static LecturaTelemetria leer(DataInput in) throws IOException {
        return new LecturaTelemetria(in.readInt(), in.readInt(), in.readDouble(), in.readInt(),
                in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ROOT,
                "moto=%d vuelta=%d vel=%.1fkm/h rpm=%d tMotor=%.1fC tFreno=%.1fC presion=%.2fbar incl=%.1fº",
                idMoto, vuelta, velocidad, rpm, tempMotor, tempFreno, presionNeumatico, inclinacion);
    }
}
