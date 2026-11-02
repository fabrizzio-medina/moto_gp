package com.motogp.simulator;

import com.motogp.simulator.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Prepara los directorios y ficheros de datos vacíos.
 */
public class InicializarData {

    private static final Logger LOG = LogManager.getLogger(InicializarData.class);

    public static void main(String[] args) {
        try {
            FileUtil.validarDirectorios();
            FileUtil.vaciarArchivo(FileUtil.getPathDatos().resolve(FileUtil.ARCHIVO_CARRERA_TXT));
            FileUtil.vaciarArchivo(FileUtil.getPathDatos().resolve(FileUtil.ARCHIVO_TELEMETRIA_DAT));
            FileUtil.vaciarArchivo(FileUtil.getPathDatos().resolve(FileUtil.ARCHIVO_CAMPEONATO_JSON));
            LOG.info("Directorios y ficheros de datos inicializados");
        } catch (IOException e) {
            LOG.error("No se pudo inicializar la data", e);
        }
    }
}
