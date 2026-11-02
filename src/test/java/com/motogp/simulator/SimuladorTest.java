package com.motogp.simulator;

import com.motogp.model.enums.TipoIncidente;
import com.motogp.simulator.exception.EventoCriticoException;
import com.motogp.simulator.repository.RegistroCarreraTxt;
import com.motogp.simulator.repository.TelemetriaBin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SimuladorTest {

    @TempDir
    Path datos;

    @Test
    void situacionCriticaLanzaExcepcionConLosDatosYaPersistidos() throws IOException {
        Simulador simulador = new Simulador(datos, 42);

        EventoCriticoException ex = assertThrows(EventoCriticoException.class,
                () -> simulador.ejecutarModoPrueba(TipoIncidente.FALLO_DE_FRENOS));

        assertEquals(Simulador.VUELTA_INCIDENTE_PRUEBA, ex.getVuelta());
        TelemetriaBin bin = new TelemetriaBin(datos.resolve("telemetria.dat"));
        assertEquals(Simulador.VUELTA_INCIDENTE_PRUEBA * 5, bin.size());
        assertTrue(new RegistroCarreraTxt(datos.resolve("carrera.txt")).leerEventos().stream()
                .anyMatch(e -> e.tipo().equals("AVISO_FRENOS")));
    }

    @Test
    void carreraSinIncidenteTerminaSinExcepcion() throws IOException {
        Simulador simulador = new Simulador(datos, 7);
        assertDoesNotThrow(simulador::ejecutarSinIncidente);
        assertEquals(Simulador.TOTAL_VUELTAS * 5, new TelemetriaBin(datos.resolve("telemetria.dat")).size());
    }
}
