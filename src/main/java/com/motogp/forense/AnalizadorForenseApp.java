package com.motogp.forense;

import com.motogp.simulator.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Punto de entrada independiente del analizador.
 * Uso: AnalizadorForenseApp [directorioDatos] [directorioSalida]   (por defecto: datos salida)
 */
public class AnalizadorForenseApp {

    private static final Logger LOG = LogManager.getLogger(AnalizadorForenseApp.class);

    public static void main(String[] args) {
        Path datos = args.length > 0 ? Path.of(args[0]) : FileUtil.getPathDatos();
        Path salida = args.length > 1 ? Path.of(args[1]) : FileUtil.getPathSalida();
        LOG.info("Analizador forense iniciado. Datos: {} Salida: {}", datos, salida);
        try {
            AnalizadorForense analizador = new AnalizadorForense(datos, salida);
            ResultadoAnalisis r = analizador.analizar();
            analizador.generarSalidas(r);
            System.out.println("Incidente detectado: " + r.incidente());
            System.out.println("Conclusión: " + r.conclusion());
        } catch (IOException e) {
            LOG.error("No se pudo completar el análisis: {}", e.getMessage(), e);
            System.err.println("Error en el análisis: " + e.getMessage());
        } finally {
            LOG.info("Analizador forense finalizado");
        }
    }
}
