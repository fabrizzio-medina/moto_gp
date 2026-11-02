package com.motogp.simulator.repository;

import com.motogp.model.LecturaTelemetria;
import com.motogp.simulator.util.FileUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Telemetría en binario con registros de longitud fija (52 bytes) y acceso directo con RandomAccessFile.
 * posición = numeroRegistro * TAMANIO_REGISTRO
 */
public class TelemetriaBin {

    private final Path archivoPath;

    public TelemetriaBin(Path archivoPath) throws IOException {
        this.archivoPath = archivoPath;
        FileUtil.crearArchivo(archivoPath);
    }

    /** Añade un registro al final y devuelve su número de registro. */
    public int anadir(LecturaTelemetria lectura) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(archivoPath.toFile(), "rw")) {
            long longitud = raf.length();
            raf.seek(longitud);
            lectura.escribir(raf);
            return (int) (longitud / LecturaTelemetria.TAMANIO_REGISTRO);
        }
    }

    /** Lee directamente el registro indicado sin recorrer los anteriores. */
    public LecturaTelemetria leer(int numeroRegistro) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(archivoPath.toFile(), "r")) {
            comprobarIndice(raf, numeroRegistro);
            raf.seek((long) numeroRegistro * LecturaTelemetria.TAMANIO_REGISTRO);
            return LecturaTelemetria.leer(raf);
        }
    }

    /** Sobrescribe un registro existente sin reescribir el fichero. */
    public void modificar(int numeroRegistro, LecturaTelemetria lectura) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(archivoPath.toFile(), "rw")) {
            comprobarIndice(raf, numeroRegistro);
            raf.seek((long) numeroRegistro * LecturaTelemetria.TAMANIO_REGISTRO);
            lectura.escribir(raf);
        }
    }

    public int size() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(archivoPath.toFile(), "r")) {
            return (int) (raf.length() / LecturaTelemetria.TAMANIO_REGISTRO);
        }
    }

    /** Lectura completa (la usa el analizador para buscar valores fuera de rango). */
    public List<LecturaTelemetria> leerTodas() throws IOException {
        List<LecturaTelemetria> lecturas = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(archivoPath.toFile(), "r")) {
            if (raf.length() % LecturaTelemetria.TAMANIO_REGISTRO != 0) {
                throw new IOException("Fichero binario con registro incompleto: " + archivoPath.getFileName());
            }
            long total = raf.length() / LecturaTelemetria.TAMANIO_REGISTRO;
            for (int i = 0; i < total; i++) {
                lecturas.add(LecturaTelemetria.leer(raf));
            }
        }
        return lecturas;
    }

    private void comprobarIndice(RandomAccessFile raf, int numeroRegistro) throws IOException {
        long total = raf.length() / LecturaTelemetria.TAMANIO_REGISTRO;
        if (numeroRegistro < 0 || numeroRegistro >= total) {
            throw new IOException("Registro " + numeroRegistro + " fuera de rango (total=" + total + ")");
        }
    }
}
