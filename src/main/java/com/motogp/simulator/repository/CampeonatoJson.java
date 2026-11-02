package com.motogp.simulator.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.motogp.model.Campeonato;
import com.motogp.simulator.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Estado estructurado de la carrera en JSON (Data Binding con Jackson ObjectMapper).
 */
public class CampeonatoJson {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final Path archivoPath;

    public CampeonatoJson(Path archivoPath) throws IOException {
        this.archivoPath = archivoPath;
        FileUtil.crearArchivo(archivoPath);
    }

    public void guardar(Campeonato campeonato) throws IOException {
        MAPPER.writeValue(archivoPath.toFile(), campeonato);
    }

    public Campeonato cargar() throws IOException {
        if (Files.size(archivoPath) == 0) {
            throw new IOException("El fichero " + archivoPath.getFileName() + " está vacío");
        }
        return MAPPER.readValue(archivoPath.toFile(), Campeonato.class);
    }
}
