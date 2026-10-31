package com.motogp.simulator;

import com.motogp.simulator.repository.CampeonatoJson;
import com.motogp.simulator.repository.RegistroCarreraTxt;
import com.motogp.simulator.repository.TelemetriaBin;
import com.motogp.simulator.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;


public class InicializarData {
    public static final Path DATOS_DIRECTORY = FileUtil.getPathDatos();

    public static void main(String[] args) {

        Logger LOG = LogManager.getLogger(InicializarData.class);

        try {
            FileUtil.validarDirectorios();

            System.out.println("************* GP: Inicializar data *************");
            LOG.info("Inicialización de data - iniciada");
            inicializarRegistroCarrera();
            inicializarTelemetria();
            inicializarCampeonato();
            LOG.info("Inicialización de data - terminada");
            System.out.println("Inicialización de data exitosa");


            RegistroCarreraTxt registroCarrera = new RegistroCarreraTxt(DATOS_DIRECTORY.resolve(FileUtil.ARCHIVO_CARRERA_TXT));
            TelemetriaBin telemetriaBin = new TelemetriaBin(DATOS_DIRECTORY.resolve(FileUtil.ARCHIVO_TELEMETRIA_DAT));
            CampeonatoJson campeonatoJson = new CampeonatoJson(DATOS_DIRECTORY.resolve(FileUtil.ARCHIVO_CAMPEONATO_JSON));

            System.out.println("Registros TXT: " + registroCarrera.size());
            System.out.println("Registros BIN: " + telemetriaBin.size());

        } catch (Exception e) {
            LOG.error("No se pudo inicializar la data", e);
            System.err.println("No se pudo inicializar la data: " + e.getMessage());
        }
    }

    private static void inicializarCampeonato() {
    }

    private static void inicializarTelemetria() {
    }

    private static void inicializarRegistroCarrera() {
    }
}
