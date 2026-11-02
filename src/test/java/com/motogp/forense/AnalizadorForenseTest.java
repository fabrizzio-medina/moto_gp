package com.motogp.forense;

import com.motogp.model.enums.TipoIncidente;
import com.motogp.simulator.Simulador;
import com.motogp.simulator.exception.EventoCriticoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnalizadorForenseTest {

    @TempDir
    Path tmp;

    @ParameterizedTest
    @EnumSource(TipoIncidente.class)
    void identificaCadaTipoDeIncidente(TipoIncidente incidente) throws IOException {
        Path datos = tmp.resolve("datos");
        Simulador simulador = new Simulador(datos, 2026);
        assertThrows(EventoCriticoException.class, () -> simulador.ejecutarModoPrueba(incidente));

        ResultadoAnalisis r = new AnalizadorForense(datos, tmp.resolve("salida")).analizar();

        assertEquals(incidente.name(), r.incidente());
        assertEquals(Simulador.VUELTA_INCIDENTE_PRUEBA, r.vuelta());
        assertFalse(r.lecturasAnomalas().isEmpty());
        assertFalse(r.motosAfectadas().isEmpty());
    }

    @Test
    void carreraLimpiaDaIndeterminado() throws IOException {
        Path datos = tmp.resolve("datos");
        new Simulador(datos, 11).ejecutarSinIncidente();

        ResultadoAnalisis r = new AnalizadorForense(datos, tmp.resolve("salida")).analizar();

        assertEquals(ResultadoAnalisis.INDETERMINADO, r.incidente());
    }

    @Test
    void ficherosInexistentesLanzanIOException() {
        assertThrows(IOException.class, () -> new AnalizadorForense(tmp.resolve("nada"), tmp).analizar());
    }
}
