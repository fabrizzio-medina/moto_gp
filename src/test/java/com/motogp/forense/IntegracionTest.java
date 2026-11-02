package com.motogp.forense;

import com.motogp.model.LecturaTelemetria;
import com.motogp.model.enums.TipoIncidente;
import com.motogp.simulator.Simulador;
import com.motogp.simulator.exception.EventoCriticoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Flujo completo: datos -> incidente -> persistencia -> análisis -> salidas.
 */
class IntegracionTest {

    @TempDir
    Path tmp;

    @Test
    void flujoCompletoPinchazo() throws IOException {
        Path datos = tmp.resolve("datos");
        Path salida = tmp.resolve("salida");

        assertThrows(EventoCriticoException.class,
                () -> new Simulador(datos, 99).ejecutarModoPrueba(TipoIncidente.PINCHAZO_DE_NEUMATICO));

        AnalizadorForense analizador = new AnalizadorForense(datos, salida);
        ResultadoAnalisis r = analizador.analizar();
        analizador.generarSalidas(r);

        String informe = Files.readString(salida.resolve("informe_forense.txt"));
        assertTrue(informe.contains("PINCHAZO_DE_NEUMATICO"));
        assertTrue(informe.contains("Conclusión"));

        Path anomalias = salida.resolve("anomalias.dat");
        assertEquals(LecturaTelemetria.TAMANIO_REGISTRO, Files.size(anomalias));
        try (DataInputStream in = new DataInputStream(Files.newInputStream(anomalias))) {
            LecturaTelemetria l = LecturaTelemetria.leer(in);
            assertTrue(l.presionNeumatico() < 1.0);
        }

        String afectados = Files.readString(salida.resolve("afectados.json"));
        assertTrue(afectados.contains("PINCHADO"));
        assertEquals(1, r.motosAfectadas().size());
    }
}
