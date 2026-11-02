package com.motogp.model;

/**
 * Línea del histórico de carrera (carrera.txt).
 * Formato: timestamp|vuelta|idMoto|tipo|detalle   (idMoto = 0 -> afecta a toda la carrera)
 */
public record EventoCarrera(String timestamp, int vuelta, int idMoto, String tipo, String detalle) {

    private static final String SEP = "|";
    private static final int CAMPOS = 5;

    public String toLinea() {
        return String.join(SEP, timestamp, String.valueOf(vuelta), String.valueOf(idMoto), tipo,
                detalle.replace(SEP, "/"));
    }

    public static EventoCarrera parse(String linea) {
        if (linea == null || linea.isBlank()) {
            throw new IllegalArgumentException("Línea vacía");
        }
        String[] c = linea.split("\\|", -1);
        if (c.length != CAMPOS) {
            throw new IllegalArgumentException("Se esperaban " + CAMPOS + " campos y hay " + c.length + ": " + linea);
        }
        try {
            return new EventoCarrera(c[0].trim(), Integer.parseInt(c[1].trim()), Integer.parseInt(c[2].trim()),
                    c[3].trim(), c[4].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vuelta o idMoto no numérico: " + linea, e);
        }
    }
}
