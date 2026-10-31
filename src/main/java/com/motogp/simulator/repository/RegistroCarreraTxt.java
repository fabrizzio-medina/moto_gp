package com.motogp.simulator.repository;

import com.motogp.model.Carrera;
import com.motogp.simulator.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class RegistroCarreraTxt {

    private final Path archivoPath;

    public RegistroCarreraTxt(Path archivoPath) throws IOException {
        this.archivoPath = archivoPath;
        FileUtil.crearArchivo(archivoPath);
    }

    public void guardar(Carrera carrera) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(archivoPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(carrera.toWriteTxt());
            writer.newLine();
        }
    }

    public List<Carrera> getLista() throws IOException {
        List<Carrera> carreras = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(archivoPath, StandardCharsets.UTF_8)) {
            String fila;
            int numeroFila = 0;

            while ((fila = reader.readLine()) != null) {
                numeroFila++;
                if (fila.isBlank()) continue;
                try {
                    carreras.add(Carrera.parse(fila));
                } catch (IllegalArgumentException e) {
                    throw new IOException("Error en carrera.txt, fila " + numeroFila + ": " + e.getMessage(), e);
                }
            }
        }
        return carreras;
    }

    public int size() throws IOException {
        return getLista().size();
    }

}
