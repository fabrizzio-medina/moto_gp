package com.motogp.simulator.repository;

import com.motogp.simulator.util.FileUtil;

import java.io.IOException;
import java.nio.file.Path;

public class CampeonatoJson {

    private final Path archivoPath;

    public CampeonatoJson(Path archivoPath) throws IOException {
        this.archivoPath = archivoPath;
        FileUtil.crearArchivo(archivoPath);
    }
}
