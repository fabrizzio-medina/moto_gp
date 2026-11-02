package com.motogp.simulator.repository;

import com.motogp.model.EventoCarrera;
import com.motogp.simulator.util.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Histórico secuencial de la carrera (Java I/O clásico: FileWriter/PrintWriter y FileReader/BufferedReader).
 */
public class RegistroCarreraTxt {

    private final Path archivoPath;

    public RegistroCarreraTxt(Path archivoPath) throws IOException {
        this.archivoPath = archivoPath;
        FileUtil.crearArchivo(archivoPath);
    }

    public void registrar(EventoCarrera evento) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(archivoPath.toFile(), StandardCharsets.UTF_8, true))) {
            out.println(evento.toLinea());
        }
    }

    public List<EventoCarrera> leerEventos() throws IOException {
        List<EventoCarrera> eventos = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(archivoPath.toFile(), StandardCharsets.UTF_8))) {
            String linea;
            int numero = 0;
            while ((linea = in.readLine()) != null) {
                numero++;
                if (linea.isBlank()) continue;
                try {
                    eventos.add(EventoCarrera.parse(linea));
                } catch (IllegalArgumentException e) {
                    throw new IOException("Formato incorrecto en " + archivoPath.getFileName() + ", línea " + numero, e);
                }
            }
        }
        return eventos;
    }

    public int size() throws IOException {
        return leerEventos().size();
    }

    public Path getArchivoPath() {
        return archivoPath;
    }
}
