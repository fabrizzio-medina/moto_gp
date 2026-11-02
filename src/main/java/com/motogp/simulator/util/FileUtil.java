package com.motogp.simulator.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Rutas relativas y portables del proyecto (NIO.2).
 */
public final class FileUtil {
    public static final String LOG_DIRECTORY = "logs";
    public static final String DATOS_DIRECTORY = "datos";
    public static final String SALIDA_DIRECTORY = "salida";

    public static final String ARCHIVO_CARRERA_TXT = "carrera.txt";
    public static final String ARCHIVO_TELEMETRIA_DAT = "telemetria.dat";
    public static final String ARCHIVO_CAMPEONATO_JSON = "campeonato.json";

    public static final String SALIDA_INFORME_TXT = "informe_forense.txt";
    public static final String SALIDA_ANOMALIAS_DAT = "anomalias.dat";
    public static final String SALIDA_AFECTADOS_JSON = "afectados.json";

    private FileUtil() {
    }

    public static void validarDirectorios() {
        validarDirectorio(Path.of(LOG_DIRECTORY, "evidencias"));
        validarDirectorio(Path.of(LOG_DIRECTORY, "runtime"));
        validarDirectorio(Path.of(DATOS_DIRECTORY));
        validarDirectorio(Path.of(SALIDA_DIRECTORY));
    }

    /** Crea o comprueba un directorio antes de utilizarlo. */
    public static void validarDirectorio(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio: " + path.toAbsolutePath(), e);
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

    /** Vacía el fichero (o lo crea) para empezar una ejecución limpia. */
    public static void vaciarArchivo(Path pathArchivo) throws IOException {
        crearArchivo(pathArchivo);
        Files.write(pathArchivo, new byte[0]);
    }

    public static Path getPathDatos() {
        return Path.of(DATOS_DIRECTORY);
    }

    public static Path getPathSalida() {
        return Path.of(SALIDA_DIRECTORY);
    }
}
