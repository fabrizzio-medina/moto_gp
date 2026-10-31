package com.motogp.simulator.repository;

import com.motogp.simulator.util.FileUtil;

import java.io.IOException;
import java.nio.file.Path;

public class TelemetriaBin {

    private final Path archivoPath;

    public TelemetriaBin(Path archivoPath) throws IOException {
        this.archivoPath = archivoPath;
        FileUtil.crearArchivo(archivoPath);
    }

    public int size(){
        return 0;
    }
}
