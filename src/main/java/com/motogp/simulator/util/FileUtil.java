package com.motogp.simulator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtil {
    public static final String LOG_DIRECTORY = "logs";
    public static final String DATOS_DIRECTORY = "datos";
    public static final String SALIDA_DIRECTORY = "salida";

    public static final String ARCHIVO_CARRERA_TXT = "carrera.txt";
    public static final String ARCHIVO_TELEMETRIA_DAT = "telemetria.dat";
    public static final String ARCHIVO_CAMPEONATO_JSON = "campeonato.json";

    private FileUtil() {
    }

    public static void validarDirectorios() {
        validarDirectorio(Path.of(LOG_DIRECTORY, "evidencias"));
        validarDirectorio(Path.of(LOG_DIRECTORY, "runtime"));
        validarDirectorio(Path.of(DATOS_DIRECTORY));
        validarDirectorio(Path.of(SALIDA_DIRECTORY));
    }

    //crear o comprobar los directorios necesarios antes de utilizarlos;
    public static void validarDirectorio(Path path) {
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error crítico: No se pudo crear el directorio en la ruta: " + path.toAbsolutePath(), e);
        }
    }

    public static void crearArchivo(Path pathArchivo) throws IOException {
        Path padre = pathArchivo.toAbsolutePath().getParent();
        if (padre != null) {
            Files.createDirectories(padre);
        }
        if (Files.notExists(pathArchivo)) {
            Files.createFile(pathArchivo);
        }
    }


    /*
    public static void validarArchivo(Path path) {
        if (Files.exists(path)) {
            LOG.info("El archivo ya existe: " + path.toAbsolutePath());
        } else {
            LOG.warn("El archivo NO existe todavía: " + path.toAbsolutePath());
        }
    }*/

    public static Path getPathDatos() {
        return Path.of(DATOS_DIRECTORY);
    }

    public static Path getPathDatos(String nombreArchivo) {

        return getPathDatos().resolve(nombreArchivo);
    }

    public static Path getPathSalida() {
        return Path.of(SALIDA_DIRECTORY);
    }

    public static Path getPathSalida(String nombreArchivo) {

        return getPathSalida().resolve(nombreArchivo);
    }


}
