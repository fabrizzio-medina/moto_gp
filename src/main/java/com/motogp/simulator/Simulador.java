package com.motogp.simulator;

import com.motogp.model.Campeonato;
import com.motogp.model.Circuito;
import com.motogp.model.EventoCarrera;
import com.motogp.model.LecturaTelemetria;
import com.motogp.model.Moto;
import com.motogp.model.enums.EstadoSesion;
import com.motogp.model.enums.TipoIncidente;
import com.motogp.simulator.exception.EventoCriticoException;
import com.motogp.simulator.repository.CampeonatoJson;
import com.motogp.simulator.repository.RegistroCarreraTxt;
import com.motogp.simulator.repository.TelemetriaBin;
import com.motogp.simulator.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * Simula una carrera de MotoGP vuelta a vuelta.
 * TXT  -> histórico de acontecimientos (carrera.txt)
 * BIN  -> telemetría por moto y vuelta con acceso directo (telemetria.dat)
 * JSON -> estado estructurado de circuito y motos (campeonato.json)
 */
public class Simulador {

    private static final Logger LOG = LogManager.getLogger(Simulador.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");

    public static final int TOTAL_VUELTAS = 20;
    public static final int VUELTA_INCIDENTE_PRUEBA = 8;

    private final Random random;
    private final RegistroCarreraTxt txt;
    private final TelemetriaBin bin;
    private final CampeonatoJson json;
    private Campeonato campeonato;

    public Simulador(Path datosDir, long semilla) throws IOException {
        FileUtil.validarDirectorio(datosDir);
        Path txtPath = datosDir.resolve(FileUtil.ARCHIVO_CARRERA_TXT);
        Path binPath = datosDir.resolve(FileUtil.ARCHIVO_TELEMETRIA_DAT);
        Path jsonPath = datosDir.resolve(FileUtil.ARCHIVO_CAMPEONATO_JSON);
        FileUtil.vaciarArchivo(txtPath);
        FileUtil.vaciarArchivo(binPath);
        FileUtil.vaciarArchivo(jsonPath);
        this.random = new Random(semilla);
        this.txt = new RegistroCarreraTxt(txtPath);
        this.bin = new TelemetriaBin(binPath);
        this.json = new CampeonatoJson(jsonPath);
    }

    /** Modo normal: el incidente (o su ausencia) y la vuelta se eligen al azar. */
    public void ejecutarModoNormal() throws IOException {
        TipoIncidente incidente = null;
        int vuelta = -1;
        if (random.nextDouble() < 0.8) {
            TipoIncidente[] tipos = TipoIncidente.values();
            incidente = tipos[random.nextInt(tipos.length)];
            vuelta = 3 + random.nextInt(TOTAL_VUELTAS - 5);
        }
        ejecutar(incidente, vuelta);
    }

    /** Modo prueba: provoca el incidente indicado en una vuelta fija. */
    public void ejecutarModoPrueba(TipoIncidente incidente) throws IOException {
        ejecutar(incidente, VUELTA_INCIDENTE_PRUEBA);
    }

    /** Carrera completa sin incidentes. */
    public void ejecutarSinIncidente() throws IOException {
        ejecutar(null, -1);
    }

    private void ejecutar(TipoIncidente incidente, int vueltaIncidente) throws IOException {
        inicializarCarrera();
        LOG.info("Carrera {} iniciada en {} con {} motos", campeonato.getIdCarrera(),
                campeonato.getCircuito().getNombre(), campeonato.getMotos().size());

        for (int vuelta = 1; vuelta <= TOTAL_VUELTAS; vuelta++) {
            campeonato.setVueltaActual(vuelta);
            if (incidente != null && vuelta == vueltaIncidente) {
                LOG.debug("Vuelta {}: aparece una anomalía", vuelta);
                producirIncidente(incidente, vuelta);
            } else {
                simularVueltaNormal(vuelta);
            }
        }
        campeonato.setEstadoSesion(EstadoSesion.FINALIZADA);
        json.guardar(campeonato);
        registrar(TOTAL_VUELTAS, 0, "BANDERA_A_CUADROS", "Fin de la carrera");
        LOG.info("Carrera finalizada sin eventos críticos. Registros de telemetría: {}", bin.size());
    }

    private void inicializarCarrera() throws IOException {
        campeonato = new Campeonato("MotoGP 2026", "GP-ITA-2026", new Circuito("Mugello", "Italia", "SECA"), TOTAL_VUELTAS);
        campeonato.getMotos().add(new Moto(93, "Marc Márquez", "Ducati Lenovo"));
        campeonato.getMotos().add(new Moto(1, "Francesco Bagnaia", "Ducati Lenovo"));
        campeonato.getMotos().add(new Moto(89, "Jorge Martín", "Aprilia Racing"));
        campeonato.getMotos().add(new Moto(73, "Álex Márquez", "Gresini Racing"));
        campeonato.getMotos().add(new Moto(20, "Fabio Quartararo", "Monster Yamaha"));
        campeonato.setEstadoSesion(EstadoSesion.EN_CURSO);
        json.guardar(campeonato);
        registrar(0, 0, "SALIDA", "Semáforo apagado, comienza la carrera en " + campeonato.getCircuito().getNombre());
    }

    private void simularVueltaNormal(int vuelta) throws IOException {
        for (Moto moto : campeonato.getMotos()) {
            LecturaTelemetria lectura = lecturaNormal(moto.getId(), vuelta);
            int registro = bin.anadir(lectura);
            LOG.debug("Telemetría #{} -> {}", registro, lectura);
        }
        List<Moto> motos = campeonato.getMotos();
        Moto lider = motos.get(random.nextInt(motos.size()));
        registrar(vuelta, lider.getId(), "PASO_META", "Lidera la vuelta " + lider.getNombrePiloto());
        if (random.nextDouble() < 0.4) {
            Moto a = motos.get(random.nextInt(motos.size()));
            registrar(vuelta, a.getId(), "ADELANTAMIENTO", a.getNombrePiloto() + " gana una posición");
        }
    }

    private LecturaTelemetria lecturaNormal(int idMoto, int vuelta) {
        return new LecturaTelemetria(idMoto, vuelta,
                rango(290, 350), (int) rango(14000, 17500), rango(95, 112),
                rango(450, 750), rango(1.9, 2.2), rango(40, 62));
    }

    /**
     * El incidente se refleja en los tres ficheros con los mismos mecanismos que la operativa normal.
     * Después se relee la telemetría persistida y, si es crítica, se lanza la excepción.
     */
    private void producirIncidente(TipoIncidente incidente, int vuelta) throws IOException {
        List<Moto> motos = campeonato.getMotos();
        Moto afectada = motos.get(random.nextInt(motos.size()));
        int primerRegistro = bin.size();

        // 1. Telemetría habitual de la vuelta
        for (Moto moto : motos) {
            if (incidente == TipoIncidente.LLUVIA) {
                bin.anadir(new LecturaTelemetria(moto.getId(), vuelta, rango(110, 140), (int) rango(9000, 11000),
                        rango(85, 95), rango(180, 260), rango(1.9, 2.1), rango(15, 30)));
            } else {
                bin.anadir(lecturaNormal(moto.getId(), vuelta));
            }
        }

        // 2. El sensor de la moto afectada actualiza su registro (acceso directo con RandomAccessFile)
        int registroAfectado = primerRegistro + motos.indexOf(afectada);
        LecturaTelemetria original = bin.leer(registroAfectado);
        LecturaTelemetria anomala = switch (incidente) {
            case CAIDA_EN_CURVA -> new LecturaTelemetria(afectada.getId(), vuelta, rango(0, 3), 0,
                    original.tempMotor(), original.tempFreno(), original.presionNeumatico(), rango(75, 90));
            case ROTURA_DE_MOTOR -> new LecturaTelemetria(afectada.getId(), vuelta, rango(40, 80), 0,
                    rango(140, 160), original.tempFreno(), original.presionNeumatico(), original.inclinacion());
            case FALLO_DE_FRENOS -> new LecturaTelemetria(afectada.getId(), vuelta, original.velocidad(), original.rpm(),
                    original.tempMotor(), rango(950, 1150), original.presionNeumatico(), original.inclinacion());
            case PINCHAZO_DE_NEUMATICO -> new LecturaTelemetria(afectada.getId(), vuelta, rango(60, 100), original.rpm(),
                    original.tempMotor(), original.tempFreno(), rango(0.2, 0.7), original.inclinacion());
            case PRESION_MINIMA_NEUMATICO -> new LecturaTelemetria(afectada.getId(), vuelta, original.velocidad(), original.rpm(),
                    original.tempMotor(), original.tempFreno(), rango(1.3, 1.6), original.inclinacion());
            case LLUVIA -> original;
        };
        if (incidente != TipoIncidente.LLUVIA) {
            bin.modificar(registroAfectado, anomala);
            LOG.warn("Telemetría #{} fuera de rango: {}", registroAfectado, anomala);
        }

        // 3. Histórico de texto (síntoma observado, no el nombre del incidente)
        switch (incidente) {
            case CAIDA_EN_CURVA -> registrar(vuelta, afectada.getId(), "BANDERA_AMARILLA",
                    afectada.getNombrePiloto() + " se va al suelo en la curva Arrabbiata");
            case ROTURA_DE_MOTOR -> registrar(vuelta, afectada.getId(), "HUMO",
                    "Sale humo del escape de la moto de " + afectada.getNombrePiloto());
            case FALLO_DE_FRENOS -> registrar(vuelta, afectada.getId(), "AVISO_FRENOS",
                    afectada.getNombrePiloto() + " se pasa de frenada en San Donato, disco al rojo vivo");
            case PINCHAZO_DE_NEUMATICO -> registrar(vuelta, afectada.getId(), "PERDIDA_RITMO",
                    afectada.getNombrePiloto() + " pierde ritmo de golpe y la rueda trasera se deforma");
            case PRESION_MINIMA_NEUMATICO -> registrar(vuelta, afectada.getId(), "AVISO_TPMS",
                    "Dirección de carrera revisa la presión del neumático delantero de " + afectada.getNombrePiloto());
            case LLUVIA -> registrar(vuelta, 0, "BANDERA_BLANCA", "Empiezan a caer gotas, pista cambiante en todo el trazado");
        }

        // 4. Estado estructurado (JSON)
        switch (incidente) {
            case CAIDA_EN_CURVA -> { afectada.setEstado("ACCIDENTADA"); afectada.setNeumaticos("DANADOS"); }
            case ROTURA_DE_MOTOR -> { afectada.setEstado("RETIRADA"); afectada.setMotor("ROTO"); }
            case FALLO_DE_FRENOS -> { afectada.setEstado("RETIRADA"); afectada.setFrenos("SOBRECALENTADOS"); }
            case PINCHAZO_DE_NEUMATICO -> { afectada.setEstado("EN_BOXES"); afectada.setNeumaticos("PINCHADO"); }
            case PRESION_MINIMA_NEUMATICO -> { afectada.setEstado("PENALIZADA"); afectada.setNeumaticos("PRESION_BAJA"); }
            case LLUVIA -> {
                campeonato.getCircuito().setCondicionPista("MOJADA");
                campeonato.setEstadoSesion(EstadoSesion.SUSPENDIDA);
                motos.forEach(m -> m.setNeumaticos("SLICK_EN_MOJADO"));
            }
        }
        json.guardar(campeonato);

        // 5. Detección sobre los datos ya persistidos
        detectarSituacionCritica(primerRegistro, motos.size(), vuelta);
    }

    private void detectarSituacionCritica(int primerRegistro, int numMotos, int vuelta) throws IOException {
        int lentas = 0;
        for (int i = primerRegistro; i < primerRegistro + numMotos; i++) {
            LecturaTelemetria l = bin.leer(i);
            if (l.inclinacion() > 70 || l.tempMotor() > 130 || l.tempFreno() > 900 || l.presionNeumatico() < 1.7) {
                throw new EventoCriticoException("Valores críticos en la telemetría: " + l, l.idMoto(), vuelta);
            }
            if (l.velocidad() < 150) lentas++;
        }
        if (lentas == numMotos) {
            throw new EventoCriticoException("Todas las motos por debajo de 150 km/h: bandera roja", 0, vuelta);
        }
    }

    private void registrar(int vuelta, int idMoto, String tipo, String detalle) throws IOException {
        txt.registrar(new EventoCarrera(LocalDateTime.now().format(TS), vuelta, idMoto, tipo, detalle));
    }

    private double rango(double min, double max) {
        return Math.round((min + random.nextDouble() * (max - min)) * 100.0) / 100.0;
    }
}
