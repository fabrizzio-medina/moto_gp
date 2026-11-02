# MotoGP · Simulador y Analizador Forense

Práctica UD1 (Acceso a Datos · DAM) — Manejo de ficheros.

Repositorio: https://github.com/fabrizzio-medina/moto_gp

## Temática

Se simula un Gran Premio de MotoGP en el circuito de Mugello (20 vueltas, 5 motos). En cada vuelta se registra
la telemetría de cada moto, los acontecimientos de carrera y el estado de circuito y motos. En algún momento puede
producirse un incidente que deja huella en los tres ficheros. Después, un analizador forense independiente lee
esos ficheros y determina qué ocurrió.

## Incidentes posibles

| Incidente | TXT (síntoma) | BIN (telemetría) | JSON (estado) |
|---|---|---|---|
| `CAIDA_EN_CURVA` | `BANDERA_AMARILLA` | inclinación > 70º, velocidad ≈ 0 | moto `ACCIDENTADA` |
| `ROTURA_DE_MOTOR` | `HUMO` | temp. motor > 130 ºC, 0 rpm | motor `ROTO`, moto `RETIRADA` |
| `FALLO_DE_FRENOS` | `AVISO_FRENOS` | temp. frenos > 900 ºC | frenos `SOBRECALENTADOS` |
| `PINCHAZO_DE_NEUMATICO` | `PERDIDA_RITMO` | presión < 1.0 bar | neumáticos `PINCHADO`, moto `EN_BOXES` |
| `PRESION_MINIMA_NEUMATICO` | `AVISO_TPMS` | presión entre 1.0 y 1.7 bar | neumáticos `PRESION_BAJA`, moto `PENALIZADA` |
| `LLUVIA` | `BANDERA_BLANCA` (idMoto 0) | todas las motos < 150 km/h | pista `MOJADA`, sesión `SUSPENDIDA` |

## Ficheros de datos (`datos/`)

- **`carrera.txt`** — histórico de acontecimientos. Una línea por evento: `timestamp|vuelta|idMoto|tipo|detalle`
  (Java I/O: `FileWriter`/`PrintWriter`, `FileReader`/`BufferedReader`).
- **`telemetria.dat`** — telemetría binaria con registros de longitud fija de **52 bytes**
  (`idMoto, vuelta, velocidad, rpm, tempMotor, tempFreno, presionNeumatico, inclinacion`).
  Se usa `RandomAccessFile` para **añadir**, **leer un registro concreto** (`seek(n * 52)`) y **modificar**
  un registro existente (el sensor de la moto afectada actualiza su lectura cuando aparece la anomalía).
- **`campeonato.json`** — estado estructurado: circuito (condición de pista), sesión y motos con el estado de
  motor, frenos y neumáticos (Jackson `ObjectMapper`, Data Binding).

Los tres se relacionan por **`idMoto`** y **`vuelta`**.

## Salidas del analizador (`salida/`)

- **`informe_forense.txt`** — incidente identificado, efectos detectados y conclusión (NIO.2 `Files.newBufferedWriter`).
- **`anomalias.dat`** — solo las lecturas de telemetría anómalas usadas en el análisis (`DataOutputStream`, 52 bytes por registro).
- **`afectados.json`** — solo el circuito y las motos implicadas (Jackson, modelo de árbol `ObjectNode`).

Si no se reconoce ningún incidente el resultado es `INDETERMINADO`.

## Ejecución

Requisitos: JDK 21 o superior y Maven (o IntelliJ IDEA). Dependencias en `pom.xml`: Log4j 2, Jackson Databind, JUnit 5/6.

Desde IntelliJ: ejecutar la clase con ▶ (Run → Edit Configurations para añadir argumentos).

```bash
mvn compile

# Simulador, modo normal (incidente y vuelta aleatorios; a veces no hay incidente)
mvn exec:java -Dexec.mainClass=com.motogp.simulator.SimuladorApp
# Modo normal reproducible
mvn exec:java -Dexec.mainClass=com.motogp.simulator.SimuladorApp -Dexec.args="--seed=42"
# Modo prueba: provocar un incidente concreto (en la vuelta 8)
mvn exec:java -Dexec.mainClass=com.motogp.simulator.SimuladorApp -Dexec.args="FALLO_DE_FRENOS"

# Analizador forense (independiente; por defecto lee datos/ y escribe en salida/)
mvn exec:java -Dexec.mainClass=com.motogp.forense.AnalizadorForenseApp
mvn exec:java -Dexec.mainClass=com.motogp.forense.AnalizadorForenseApp -Dexec.args="datos salida"
```

El directorio de trabajo debe ser la raíz del proyecto (rutas relativas).

## Flujo del simulador

funcionamiento normal → escritura en TXT/BIN/JSON → aparece la anomalía → los mismos ficheros la reflejan →
se relee la telemetría persistida y se detecta la situación crítica → `throw EventoCriticoException` →
`catch` en `SimuladorApp`: registro con Log4j y fin controlado.

## Pruebas

```bash
mvn test
```

En IntelliJ: clic derecho en `src/test/java` → *Run 'All Tests'*. Usan `@TempDir`:

- `RepositoriosTest`: escritura/lectura TXT, `RandomAccessFile` (alta, lectura directa, modificación), JSON.
- `SimuladorTest`: provocación de la situación crítica y `EventoCriticoException`.
- `AnalizadorForenseTest`: prueba parametrizada que identifica los 6 incidentes + caso `INDETERMINADO`.
- `IntegracionTest`: flujo completo datos → incidente → persistencia → análisis → salidas.

## Logs (Log4j 2, `src/main/resources/log4j2.properties`)

- `logs/evidencias/ejecucion_dd-MM-yyyy_HH-mm-ss.log`: un fichero por ejecución.
- `logs/runtime/aplicacion.log`: `RollingFileAppender` con rotación por tamaño (10 KB), históricos
  `aplicacion-yyyy-MM-dd-N.log.gz` comprimidos y máximo 5 conservados.

Preguntas del Anexo I:
- **Condición de rollover:** cuando `aplicacion.log` supera 10 KB (`SizeBasedTriggeringPolicy`).
- **Nombre de los históricos:** `aplicacion-<fecha>-<índice>.log.gz` (`filePattern` con `%d{yyyy-MM-dd}-%i`).
- **Históricos conservados:** 5 (`DefaultRolloverStrategy max=5`); los más antiguos se descartan para no llenar el disco.
- **Ventaja de comprimir:** los logs de texto ocupan mucho menos en `.gz` y siguen pudiendo consultarse con `zcat`/`zgrep`.
- **Log activo vs rotado:** la aplicación solo escribe en `aplicacion.log`; los `.gz` son copias cerradas de solo lectura.

## Observabilidad en producción (Anexo II): Grafana Alloy + Loki + Grafana

Si el simulador se desplegara en un servidor, los logs locales no bastarían:

1. **Recogida:** Grafana Alloy actúa como agente en la máquina, lee `logs/runtime/aplicacion.log`
   (sigue el fichero aunque rote) y añade etiquetas como `app=motogp` o `nivel=ERROR`.
2. **Almacenamiento:** Alloy envía las líneas a Loki, que las guarda indexando solo las etiquetas, no el texto completo.
3. **Consulta:** desde Grafana se consultan con LogQL, por ejemplo `{app="motogp"} |= "Evento crítico"`.
4. **Paneles y alertas:** Grafana permite crear dashboards (errores por minuto) y alertas
   (aviso cuando aparece un `ERROR` del simulador).
5. **Logs, métricas y trazas:** Alloy también puede recoger métricas (Prometheus/Mimir) y trazas (Tempo),
   y es compatible con OpenTelemetry. Promtail, el agente anterior, alcanzó fin de vida en 2026.

Ventaja frente a abrir `aplicacion.log`: búsqueda centralizada de varias ejecuciones y máquinas, histórico más
largo que los 5 ficheros rotados, y alertas automáticas sin que nadie tenga que mirar el fichero.
