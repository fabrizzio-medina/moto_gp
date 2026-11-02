package com.motogp.forense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.motogp.model.Campeonato;
import com.motogp.model.EventoCarrera;
import com.motogp.model.LecturaTelemetria;
import com.motogp.model.Moto;
import com.motogp.model.enums.TipoIncidente;
import com.motogp.simulator.repository.CampeonatoJson;
import com.motogp.simulator.repository.RegistroCarreraTxt;
import com.motogp.simulator.repository.TelemetriaBin;
import com.motogp.simulator.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analizador forense: trabaja únicamente con los ficheros de datos persistidos.
 * Relaciona TXT (síntomas), BIN (mediciones fuera de rango) y JSON (estado de los elementos)
 * mediante el id de moto y la vuelta.
 */
public class AnalizadorForense {

    private static final Logger LOG = LogManager.getLogger(AnalizadorForense.class);

    private final Path datosDir;
    private final Path salidaDir;

    public AnalizadorForense(Path datosDir, Path salidaDir) {
        this.datosDir = datosDir;
        this.salidaDir = salidaDir;
    }

    public ResultadoAnalisis analizar() throws IOException {
        Path txtPath = datosDir.resolve(FileUtil.ARCHIVO_CARRERA_TXT);
        Path binPath = datosDir.resolve(FileUtil.ARCHIVO_TELEMETRIA_DAT);
        Path jsonPath = datosDir.resolve(FileUtil.ARCHIVO_CAMPEONATO_JSON);
        for (Path p : List.of(txtPath, binPath, jsonPath)) {
            if (Files.notExists(p)) {
                throw new IOException("No existe el fichero de datos: " + p.toAbsolutePath());
            }
        }

        List<EventoCarrera> eventos = new RegistroCarreraTxt(txtPath).leerEventos();
        List<LecturaTelemetria> lecturas = new TelemetriaBin(binPath).leerTodas();
        Campeonato campeonato = new CampeonatoJson(jsonPath).cargar();
        LOG.info("Datos leídos: {} eventos TXT, {} registros BIN, {} motos JSON",
                eventos.size(), lecturas.size(), campeonato.getMotos().size());

        ResultadoAnalisis resultado = Optional.ofNullable(detectarLluvia(eventos, lecturas, campeonato))
                .orElseGet(() -> detectarIncidenteIndividual(eventos, lecturas, campeonato));
        LOG.info("Incidente identificado: {}", resultado.incidente());
        return resultado;
    }

    private ResultadoAnalisis detectarLluvia(List<EventoCarrera> eventos, List<LecturaTelemetria> lecturas,
                                             Campeonato c) {
        boolean pistaMojada = "MOJADA".equals(c.getCircuito().getCondicionPista());
        Optional<EventoCarrera> aviso = eventos.stream()
                .filter(e -> e.idMoto() == 0 && e.tipo().equals("BANDERA_BLANCA")).findFirst();
        if (!pistaMojada || aviso.isEmpty()) {
            return null;
        }
        int vuelta = aviso.get().vuelta();
        List<LecturaTelemetria> lentas = lecturas.stream()
                .filter(l -> l.vuelta() == vuelta && l.velocidad() < 150).toList();
        if (lentas.size() < c.getMotos().size()) {
            return null;
        }
        LOG.warn("{} lecturas con velocidad anómala en la vuelta {}", lentas.size(), vuelta);
        List<String> efectos = new ArrayList<>(List.of(
                "pista del circuito " + c.getCircuito().getNombre() + " en estado MOJADA",
                "todas las motos (" + lentas.size() + ") por debajo de 150 km/h en la vuelta " + vuelta,
                "motos rodando con neumáticos slick en mojado",
                "sesión en estado " + c.getEstadoSesion()));
        return new ResultadoAnalisis(TipoIncidente.LLUVIA.name(), vuelta, efectos,
                "La lluvia ha dejado la pista sin adherencia para neumáticos de seco; la carrera debe neutralizarse con bandera roja.",
                lentas, c.getMotos(), c.getCircuito());
    }

    private ResultadoAnalisis detectarIncidenteIndividual(List<EventoCarrera> eventos, List<LecturaTelemetria> lecturas,
                                                          Campeonato c) {
        for (LecturaTelemetria l : lecturas) {
            Optional<Moto> motoOpt = c.buscarMoto(l.idMoto());
            if (motoOpt.isEmpty()) continue;
            Moto m = motoOpt.get();
            TipoIncidente tipo = null;
            String tipoEvento = null;
            String conclusion = null;
            List<String> efectos = new ArrayList<>();

            if (l.inclinacion() > 70 && l.velocidad() < 5 && "ACCIDENTADA".equals(m.getEstado())) {
                tipo = TipoIncidente.CAIDA_EN_CURVA; tipoEvento = "BANDERA_AMARILLA";
                efectos.add("inclinación de " + l.inclinacion() + "º y velocidad " + l.velocidad() + " km/h");
                efectos.add("neumáticos " + m.getNeumaticos());
                conclusion = "El piloto ha perdido el control en curva; la moto queda fuera de carrera y se requiere asistencia médica.";
            } else if (l.tempMotor() > 130 && l.rpm() == 0 && "ROTO".equals(m.getMotor())) {
                tipo = TipoIncidente.ROTURA_DE_MOTOR; tipoEvento = "HUMO";
                efectos.add("temperatura de motor de " + l.tempMotor() + " ºC con 0 rpm");
                efectos.add("motor en estado ROTO");
                conclusion = "El motor ha sufrido una avería mecánica grave; la moto debe abandonar y hay riesgo de aceite en pista.";
            } else if (l.tempFreno() > 900 && "SOBRECALENTADOS".equals(m.getFrenos())) {
                tipo = TipoIncidente.FALLO_DE_FRENOS; tipoEvento = "AVISO_FRENOS";
                efectos.add("temperatura de frenos de " + l.tempFreno() + " ºC");
                efectos.add("frenos en estado SOBRECALENTADOS");
                conclusion = "Los frenos han superado su temperatura máxima de trabajo; el piloto no puede detener la moto con seguridad.";
            } else if (l.presionNeumatico() < 1.0 && "PINCHADO".equals(m.getNeumaticos())) {
                tipo = TipoIncidente.PINCHAZO_DE_NEUMATICO; tipoEvento = "PERDIDA_RITMO";
                efectos.add("presión de neumático de " + l.presionNeumatico() + " bar y velocidad " + l.velocidad() + " km/h");
                efectos.add("neumático en estado PINCHADO");
                conclusion = "El neumático ha perdido el aire; la moto debe entrar en boxes inmediatamente.";
            } else if (l.presionNeumatico() >= 1.0 && l.presionNeumatico() < 1.7 && "PRESION_BAJA".equals(m.getNeumaticos())) {
                tipo = TipoIncidente.PRESION_MINIMA_NEUMATICO; tipoEvento = "AVISO_TPMS";
                efectos.add("presión de neumático de " + l.presionNeumatico() + " bar (mínimo reglamentario 1.7)");
                efectos.add("neumático en estado PRESION_BAJA");
                conclusion = "La moto incumple la presión mínima reglamentaria; corresponde una penalización de dirección de carrera.";
            }

            if (tipo != null) {
                final String te = tipoEvento;
                boolean confirmadoTxt = eventos.stream()
                        .anyMatch(e -> e.idMoto() == l.idMoto() && e.vuelta() == l.vuelta() && e.tipo().equals(te));
                if (confirmadoTxt) {
                    LOG.warn("Lectura anómala confirmada por TXT y JSON: {}", l);
                    efectos.add(0, "aviso '" + te + "' en el histórico para la moto " + m.getId() + " (" + m.getNombrePiloto() + ")");
                    efectos.add("moto en estado " + m.getEstado());
                    return new ResultadoAnalisis(tipo.name(), l.vuelta(), efectos, conclusion,
                            List.of(l), List.of(m), c.getCircuito());
                }
                LOG.debug("Lectura anómala sin confirmación en TXT: {}", l);
            }
        }
        return new ResultadoAnalisis(ResultadoAnalisis.INDETERMINADO, 0, List.of(),
                "Los datos no permiten identificar ningún incidente conocido.", List.of(), List.of(), c.getCircuito());
    }

    /** Genera los tres ficheros de salida, cada uno con un papel distinto. */
    public void generarSalidas(ResultadoAnalisis r) throws IOException {
        FileUtil.validarDirectorio(salidaDir);
        escribirInforme(r, salidaDir.resolve(FileUtil.SALIDA_INFORME_TXT));
        escribirAnomalias(r, salidaDir.resolve(FileUtil.SALIDA_ANOMALIAS_DAT));
        escribirAfectados(r, salidaDir.resolve(FileUtil.SALIDA_AFECTADOS_JSON));
        LOG.info("Salidas generadas en {}", salidaDir.toAbsolutePath());
    }

    /** TXT: informe legible (NIO.2 Files.newBufferedWriter). */
    private void escribirInforme(ResultadoAnalisis r, Path destino) throws IOException {
        try (var w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            w.write("INFORME FORENSE - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
            w.newLine();
            w.write("Circuito: " + r.circuito().getNombre());
            w.newLine();
            w.write("Incidente detectado: " + r.incidente() + (r.vuelta() > 0 ? " (vuelta " + r.vuelta() + ")" : ""));
            w.newLine();
            w.newLine();
            w.write("Efectos detectados:");
            w.newLine();
            for (String efecto : r.efectos()) {
                w.write("- " + efecto + ";");
                w.newLine();
            }
            w.newLine();
            w.write("Conclusión:");
            w.newLine();
            w.write(r.conclusion());
            w.newLine();
        }
    }

    /** BIN: solo las lecturas anómalas usadas en el análisis (DataOutputStream secuencial). */
    private void escribirAnomalias(ResultadoAnalisis r, Path destino) throws IOException {
        try (OutputStream os = Files.newOutputStream(destino);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(os))) {
            for (LecturaTelemetria l : r.lecturasAnomalas()) {
                l.escribir(out);
            }
        }
    }

    /** JSON: solo los elementos de la estructura afectados (modelo de árbol con JsonNode). */
    private void escribirAfectados(ResultadoAnalisis r, Path destino) throws IOException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode raiz = mapper.createObjectNode();
        raiz.put("incidente", r.incidente());
        raiz.put("vuelta", r.vuelta());
        ObjectNode circuito = raiz.putObject("circuito");
        circuito.put("nombre", r.circuito().getNombre());
        circuito.put("condicionPista", r.circuito().getCondicionPista());
        ArrayNode motos = raiz.putArray("motosAfectadas");
        for (Moto m : r.motosAfectadas()) {
            ObjectNode n = motos.addObject();
            n.put("id", m.getId());
            n.put("piloto", m.getNombrePiloto());
            n.put("estado", m.getEstado());
            n.put("motor", m.getMotor());
            n.put("frenos", m.getFrenos());
            n.put("neumaticos", m.getNeumaticos());
        }
        mapper.writeValue(destino.toFile(), raiz);
    }
}
