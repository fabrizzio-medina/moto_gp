package com.motogp.simulator;

import com.motogp.model.enums.TipoIncidente;
import com.motogp.simulator.exception.EventoCriticoException;
import com.motogp.simulator.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Punto de entrada del simulador.
 * Uso:
 *   SimuladorApp                      -> modo normal (aleatorio)
 *   SimuladorApp FALLO_DE_FRENOS      -> modo prueba: provoca ese incidente
 *   SimuladorApp --seed=42            -> modo normal reproducible
 */
public class SimuladorApp {

    private static final Logger LOG = LogManager.getLogger(SimuladorApp.class);

    public static void main(String[] args) {
        LOG.info("Simulador iniciado");
        FileUtil.validarDirectorios();

        TipoIncidente forzado = null;
        long semilla = System.currentTimeMillis();
        for (String arg : args) {
            if (arg.startsWith("--seed=")) {
                try {
                    semilla = Long.parseLong(arg.substring(7));
                } catch (NumberFormatException e) {
                    LOG.error("Semilla no válida '{}': debe ser un número entero (ej. --seed=42)", arg.substring(7));
                    return;
                }
            } else {
                try {
                    forzado = TipoIncidente.valueOf(arg.toUpperCase());
                } catch (IllegalArgumentException e) {
                    LOG.error("Incidente desconocido '{}'. Valores válidos: {}", arg, java.util.Arrays.toString(TipoIncidente.values()));
                    return;
                }
            }
        }

        try {
            Simulador simulador = new Simulador(FileUtil.getPathDatos(), semilla);
            if (forzado != null) {
                LOG.info("Modo prueba: se provocará {}", forzado);
                simulador.ejecutarModoPrueba(forzado);
            } else {
                LOG.info("Modo normal con semilla {}", semilla);
                simulador.ejecutarModoNormal();
            }
            System.out.println("Carrera terminada sin eventos críticos.");
        } catch (EventoCriticoException e) {
            LOG.error("Evento crítico capturado en vuelta {} (moto {}): {}", e.getVuelta(), e.getIdMoto(), e.getMessage());
            System.out.println("¡Evento crítico! Carrera detenida. Ejecuta AnalizadorForenseApp para analizar los datos.");
        } catch (IOException e) {
            LOG.error("Error de E/S en el simulador", e);
        } finally {
            LOG.info("Simulador finalizado");
        }
    }
}
