package com.motogp.simulator;

import com.motogp.model.Carrera;
import com.motogp.model.Circuito;
import com.motogp.model.enums.EstadoSesion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestGPSimulator {

    @Test
    void testCarreraSeConvierteATextoYSeRecupera() {
        Carrera carrera = new Carrera("GP-ITA-2026", new Circuito("Mugello"), 20, 8, EstadoSesion.EN_CURSO);

        Carrera carreraParse = Carrera.parse(carrera.toWriteTxt());

        assertEquals(carrera, carreraParse);
    }

    @Test
    void testCarreraConRegistroIncompletoLanzaError() {
        assertThrows(IllegalArgumentException.class, () -> Carrera.parse("GP|Mugello|20"));
    }
}
