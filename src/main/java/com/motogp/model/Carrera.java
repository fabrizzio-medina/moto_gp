package com.motogp.model;

import com.motogp.model.enums.EstadoSesion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Carrera {
    private static final int NUMERO_CAMPOS = 6;
    private static final String DELIMITADOR_REGEX = "\\|";
    private static final String DELIMITADOR_TEXTO = "|";

    private String id;
    private Circuito circuito;
    private int totalVueltas;
    private int vueltaActual;
    private EstadoSesion estado;
    private List<Moto> motos;

    public Carrera() {
        this.motos = new ArrayList<>();
        this.estado = EstadoSesion.PROGRAMADA;
    }

    public Carrera(String id, Circuito circuito, int totalVueltas) {
        this();
        if (totalVueltas <= 0) {
            throw new IllegalArgumentException("El número de vueltas debe ser positivo");
        }
        this.id = id;
        this.circuito = circuito;
        this.totalVueltas = totalVueltas;
        this.vueltaActual = 0;
    }

    public Carrera(String id, Circuito circuito, int totalVueltas, int vueltaActual, EstadoSesion estado, List<Moto> motos) {
        this.id = id;
        this.circuito = circuito;
        this.totalVueltas = totalVueltas;
        this.vueltaActual = vueltaActual;
        this.estado = estado;
        this.motos = motos;
    }

    public void iniciar() {
        if (estado != EstadoSesion.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede iniciar una carrera programada");
        }
        estado = EstadoSesion.EN_CURSO;
        vueltaActual = 1;
    }

    /**
     * Avanza una vuelta; si se completa la última, la carrera finaliza.
     */
    public void avanzarVuelta() {
        if (estado != EstadoSesion.EN_CURSO) {
            throw new IllegalStateException("La carrera no está en curso: " + estado);
        }
        if (vueltaActual >= totalVueltas) {
            finalizar();
        } else {
            vueltaActual++;
        }
    }

    /**
     * Bandera roja: la sesión se detiene (lluvia, accidente grave...).
     */
    public void suspender() {
        if (estado == EstadoSesion.EN_CURSO) {
            estado = EstadoSesion.SUSPENDIDA;
        }
    }

    public void reanudar() {
        if (estado != EstadoSesion.SUSPENDIDA) {
            throw new IllegalStateException("Solo se puede reanudar una carrera suspendida");
        }
        estado = EstadoSesion.EN_CURSO;
    }

    public void finalizar() {
        estado = EstadoSesion.FINALIZADA;
    }

    /**
     * No sigue la convención is/get para que Jackson no lo serialice.
     */
    public boolean haTerminado() {
        return estado == EstadoSesion.FINALIZADA;
    }

    public void agregarMoto(Moto moto) {
        if (moto != null && !motos.contains(moto)) {
            motos.add(moto);
        }
    }

    /**
     * Busca una moto por su ID (la clave que relaciona JSON, TXT y BIN).
     */
    public Optional<Moto> buscarMoto(int idMoto) {
        return motos.stream()
                .filter(moto -> moto.getId() == idMoto)
                .findFirst();
    }


    public String toWriteTxt() {
        return String.join(DELIMITADOR_TEXTO, id, circuito.toString(), String.valueOf(totalVueltas), String.valueOf(vueltaActual), estado.name(), motos.toString());
    }

    public static Carrera parse(String registro) {
        // 1. Validar que la línea no sea nula ni esté vacía
        if (registro == null || registro.isBlank()) {
            throw new IllegalArgumentException("El registro no puede ser nulo o estar en blanco.");
        }

        String[] campos = registro.split(DELIMITADOR_REGEX, -1);

        // 2. Validar cantidad de campos
        if (campos.length != NUMERO_CAMPOS) {
            throw new IllegalArgumentException(String.format("El registro debe contener %d campos (encontrados: %d): %s",
                    NUMERO_CAMPOS, campos.length, registro));
        }

        try {
            // 3. Aplicar .trim() a cada campo por seguridad ante espacios accidentales
            return new Carrera(campos[0].trim(),
                    new Circuito(campos[1].trim()),
                    Integer.parseInt(campos[2]),
                    Integer.parseInt(campos[3]),
                    EstadoSesion.EN_CURSO,
                    new ArrayList<>());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error al convertir las vueltas: " + campos[1], e);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Circuito getCircuito() {
        return circuito;
    }

    public void setCircuito(Circuito circuito) {
        this.circuito = circuito;
    }

    public int getTotalVueltas() {
        return totalVueltas;
    }

    public void setTotalVueltas(int totalVueltas) {
        this.totalVueltas = totalVueltas;
    }

    public int getVueltaActual() {
        return vueltaActual;
    }

    public void setVueltaActual(int vueltaActual) {
        this.vueltaActual = vueltaActual;
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }

    public List<Moto> getMotos() {
        return motos;
    }

    public void setMotos(List<Moto> motos) {
        this.motos = (motos != null) ? motos : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Carrera{id='" + id + "', vuelta=" + vueltaActual + "/" + totalVueltas
                + ", estado=" + estado + ", motos=" + motos.size() + "}";
    }
}
