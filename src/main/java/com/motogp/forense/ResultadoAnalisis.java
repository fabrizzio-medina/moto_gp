package com.motogp.forense;

import com.motogp.model.Circuito;
import com.motogp.model.LecturaTelemetria;
import com.motogp.model.Moto;

import java.util.List;

/**
 * Resultado del análisis. incidente = nombre de TipoIncidente o "INDETERMINADO".
 */
public record ResultadoAnalisis(
        String incidente,
        int vuelta,
        List<String> efectos,
        String conclusion,
        List<LecturaTelemetria> lecturasAnomalas,
        List<Moto> motosAfectadas,
        Circuito circuito
) {
    public static final String INDETERMINADO = "INDETERMINADO";
}
