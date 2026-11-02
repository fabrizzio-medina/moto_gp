package com.motogp.simulator;

import com.motogp.model.Campeonato;
import com.motogp.model.Circuito;
import com.motogp.model.EventoCarrera;
import com.motogp.model.LecturaTelemetria;
import com.motogp.model.Moto;
import com.motogp.simulator.repository.CampeonatoJson;
import com.motogp.simulator.repository.RegistroCarreraTxt;
import com.motogp.simulator.repository.TelemetriaBin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoriosTest {

    @TempDir
    Path tmp;

    @Test
    void escrituraYRecuperacionDeTexto() throws IOException {
        RegistroCarreraTxt txt = new RegistroCarreraTxt(tmp.resolve("carrera.txt"));
        EventoCarrera e1 = new EventoCarrera("01-11-2026 10:00:00.000", 1, 93, "PASO_META", "Lidera Marc Márquez");
        EventoCarrera e2 = new EventoCarrera("01-11-2026 10:01:40.000", 2, 0, "BANDERA_BLANCA", "Gotas en pista");

        txt.registrar(e1);
        txt.registrar(e2);

        assertEquals(List.of(e1, e2), txt.leerEventos());
    }

    @Test
    void textoConFormatoIncorrectoLanzaIOException() throws IOException {
        Path p = tmp.resolve("carrera.txt");
        Files.writeString(p, "linea|rota\n");
        assertThrows(IOException.class, () -> new RegistroCarreraTxt(p).leerEventos());
    }

    @Test
    void randomAccessFileAltaLecturaDirectaYModificacion() throws IOException {
        TelemetriaBin bin = new TelemetriaBin(tmp.resolve("telemetria.dat"));
        LecturaTelemetria l0 = new LecturaTelemetria(93, 1, 320, 16000, 100, 600, 2.0, 50);
        LecturaTelemetria l1 = new LecturaTelemetria(1, 1, 318, 15800, 102, 610, 2.1, 52);
        LecturaTelemetria l2 = new LecturaTelemetria(89, 1, 315, 15500, 101, 590, 2.0, 48);

        assertEquals(0, bin.anadir(l0));
        assertEquals(1, bin.anadir(l1));
        assertEquals(2, bin.anadir(l2));
        assertEquals(3, bin.size());
        assertEquals(3L * LecturaTelemetria.TAMANIO_REGISTRO, Files.size(tmp.resolve("telemetria.dat")));

        assertEquals(l1, bin.leer(1));

        LecturaTelemetria modificada = new LecturaTelemetria(1, 1, 318, 15800, 102, 1050, 2.1, 52);
        bin.modificar(1, modificada);

        assertEquals(modificada, bin.leer(1));
        assertEquals(l0, bin.leer(0));
        assertEquals(l2, bin.leer(2));
        assertEquals(3, bin.size());
        assertThrows(IOException.class, () -> bin.leer(5));
    }

    @Test
    void procesamientoDelJson() throws IOException {
        CampeonatoJson json = new CampeonatoJson(tmp.resolve("campeonato.json"));
        Campeonato c = new Campeonato("MotoGP 2026", "GP-1", new Circuito("Mugello", "Italia", "SECA"), 20);
        Moto moto = new Moto(93, "Marc Márquez", "Ducati Lenovo");
        moto.setFrenos("SOBRECALENTADOS");
        c.getMotos().add(moto);

        json.guardar(c);
        Campeonato leido = json.cargar();

        assertEquals("GP-1", leido.getIdCarrera());
        assertEquals(c.getCircuito(), leido.getCircuito());
        assertEquals(List.of(moto), leido.getMotos());
        assertTrue(Files.readString(tmp.resolve("campeonato.json")).contains("\"frenos\" : \"SOBRECALENTADOS\""));
    }
}
