package operaciones;

import modelos.EventoSospechoso;
import modelos.Transaccion;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * DetectorFraude - Motor de detección de comportamiento financiero inusual.
 *
 * Responsabilidades:
 *   1. Detectar múltiples transferencias consecutivas en tiempo muy corto.
 *   2. Detectar montos inusualmente altos respecto al promedio del usuario.
 *   3. Detectar movimientos repetitivos hacia el mismo destino.
 *   4. Detectar fragmentación de montos en varias billeteras.
 *   5. Detectar actividad en horarios inusuales.
 *   6. Mantener historial de auditoría de eventos sospechosos.
 *   7. Marcar transacciones con nivel de riesgo.
 *
 * Umbrales configurables:
 *   - Ventana de velocidad: 60 segundos (default)
 *   - Max transacciones en ventana: 3 (default)
 *   - Factor de monto atípico: 3x el promedio (default)
 *   - Min repeticiones al mismo destino: 3 (default)
 *   - Horario inusual: 00:00 - 06:00 (default)
 */
public class DetectorFraude {

    // ─── Configuración ────────────────────────────────────────────────────────
    private long   ventanaVelocidadMs;
    private int    maxTransaccionesEnVentana;
    private double factorMontoAtipico;
    private int    minRepeticionesDestino;
    private int    horaSospechosaInicio;
    private int    horaSospechosaFin;

    // ─── Historial de auditoría ───────────────────────────────────────────────
    private final LinkedList<EventoSospechoso> historialAuditoria;
    private final HashMap<String, String>      riesgoTransacciones;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public DetectorFraude() {
        this.ventanaVelocidadMs        = 60_000;
        this.maxTransaccionesEnVentana = 3;
        this.factorMontoAtipico        = 3.0;
        this.minRepeticionesDestino    = 3;
        this.horaSospechosaInicio      = 0;
        this.horaSospechosaFin         = 6;
        this.historialAuditoria        = new LinkedList<>();
        this.riesgoTransacciones       = new HashMap<>();
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Analiza el historial de un usuario y detecta patrones sospechosos.
     * Ejecuta los 5 detectores y acumula los eventos en el historial de auditoría.
     *
     * @param idUsuario ID del usuario a analizar
     * @param historial Lista de transacciones del usuario
     * @return Lista de eventos sospechosos detectados para este usuario
     */
    public LinkedList<EventoSospechoso> analizarUsuario(String idUsuario,
                                                         LinkedList<Transaccion> historial) {
        LinkedList<EventoSospechoso> eventosUsuario = new LinkedList<>();
        if (historial == null || historial.isEmpty()) return eventosUsuario;

        eventosUsuario.addAll(detectarVelocidadAlta(idUsuario, historial));
        eventosUsuario.addAll(detectarMontoAtipico(idUsuario, historial));
        eventosUsuario.addAll(detectarDestinoRepetitivo(idUsuario, historial));
        eventosUsuario.addAll(detectarFragmentacion(idUsuario, historial));
        eventosUsuario.addAll(detectarHorarioInusual(idUsuario, historial));

        historialAuditoria.addAll(eventosUsuario);
        return eventosUsuario;
    }

    /**
     * Analiza el historial global del sistema completo.
     * Agrupa transacciones por usuario y llama a analizarUsuario().
     * O(n) donde n = cantidad total de transacciones.
     *
     * @param historialGlobal Todas las transacciones del sistema
     * @return Lista de todos los eventos sospechosos detectados
     */
    public LinkedList<EventoSospechoso> analizarSistema(LinkedList<Transaccion> historialGlobal) {
        HashMap<String, LinkedList<Transaccion>> porUsuario = new HashMap<>();
        for (Transaccion t : historialGlobal) {
            String usr = t.getUsuarioOrigen();
            porUsuario.computeIfAbsent(usr, k -> new LinkedList<>()).add(t);
        }

        LinkedList<EventoSospechoso> todos = new LinkedList<>();
        for (Map.Entry<String, LinkedList<Transaccion>> entry : porUsuario.entrySet()) {
            todos.addAll(analizarUsuario(entry.getKey(), entry.getValue()));
        }

        System.out.println("[FRAUDE] Analisis completo: " + todos.size() +
                " eventos sospechosos detectados.");
        return todos;
    }

    /** @return Historial completo de auditoría. */
    public LinkedList<EventoSospechoso> getHistorialAuditoria() {
        return new LinkedList<>(historialAuditoria);
    }

    /** @return Solo los eventos de riesgo alto. */
    public LinkedList<EventoSospechoso> getEventosRiesgoAlto() {
        LinkedList<EventoSospechoso> altos = new LinkedList<>();
        for (EventoSospechoso e : historialAuditoria)
            if (e.esRiesgoAlto()) altos.add(e);
        return altos;
    }

    /** @return Eventos sospechosos de un usuario específico. */
    public LinkedList<EventoSospechoso> getEventosPorUsuario(String idUsuario) {
        LinkedList<EventoSospechoso> resultado = new LinkedList<>();
        for (EventoSospechoso e : historialAuditoria)
            if (e.getIdUsuario().equals(idUsuario)) resultado.add(e);
        return resultado;
    }

    /**
     * Consulta el nivel de riesgo asignado a una transacción.
     *
     * @param idTransaccion ID de la transacción
     * @return "BAJO", "MEDIO", "ALTO" o "NORMAL" si no fue marcada
     */
    public String getRiesgoTransaccion(String idTransaccion) {
        return riesgoTransacciones.getOrDefault(idTransaccion, "NORMAL");
    }

    /** Muestra un resumen del historial de auditoría en consola. */
    public void mostrarResumenAuditoria() {
        System.out.println("\n=== HISTORIAL DE AUDITORIA ===");
        System.out.println("Total eventos: " + historialAuditoria.size());
        int bajos = 0, medios = 0, altos = 0;
        for (EventoSospechoso e : historialAuditoria) {
            switch (e.getNivelRiesgo()) {
                case EventoSospechoso.RIESGO_BAJO:  bajos++;  break;
                case EventoSospechoso.RIESGO_MEDIO: medios++; break;
                case EventoSospechoso.RIESGO_ALTO:  altos++;  break;
            }
        }
        System.out.println("  Riesgo BAJO:  " + bajos);
        System.out.println("  Riesgo MEDIO: " + medios);
        System.out.println("  Riesgo ALTO:  " + altos);

        if (!historialAuditoria.isEmpty()) {
            System.out.println("\nUltimos eventos:");
            java.util.List<EventoSospechoso> copia = new java.util.ArrayList<>(historialAuditoria);
            java.util.Collections.reverse(copia);
            int i = 0;
            for (EventoSospechoso e : copia) {
                if (i++ >= 5) break;
                System.out.println("  " + e);
            }
        }
    }

    // ─── Detectores individuales ──────────────────────────────────────────────

    /**
     * DETECTOR 1: Velocidad alta.
     * Detecta múltiples transferencias en una ventana de tiempo muy corta.
     * Usa recursión para contar transacciones dentro de la ventana. O(n²)
     */
    private LinkedList<EventoSospechoso> detectarVelocidadAlta(String idUsuario,
                                                                 LinkedList<Transaccion> historial) {
        LinkedList<EventoSospechoso> eventos = new LinkedList<>();
        LinkedList<Transaccion> transferencias = filtrarPorTipo(historial, "TRANSFERENCIA");
        if (transferencias.size() < maxTransaccionesEnVentana) return eventos;

        Transaccion[] arr = transferencias.toArray(new Transaccion[0]);
        for (int i = 0; i < arr.length; i++) {
            int count = contarEnVentanaRecursivo(arr, i, i + 1); // Recursión
            if (count >= maxTransaccionesEnVentana) {
                EventoSospechoso e = new EventoSospechoso(
                        EventoSospechoso.VELOCIDAD_ALTA, idUsuario,
                        EventoSospechoso.RIESGO_ALTO,
                        "Transferencias rapidas consecutivas",
                        count + " transferencias en " + (ventanaVelocidadMs / 1000) + "s");
                eventos.add(e);
                marcarTransaccion(arr[i].getId(), EventoSospechoso.RIESGO_ALTO);
                break;
            }
        }
        return eventos;
    }

    /**
     * DETECTOR 2: Monto atípico.
     * Detecta transacciones que superan N veces el promedio del usuario. O(n)
     */
    private LinkedList<EventoSospechoso> detectarMontoAtipico(String idUsuario,
                                                               LinkedList<Transaccion> historial) {
        LinkedList<EventoSospechoso> eventos = new LinkedList<>();
        if (historial.size() < 2) return eventos;

        double promedio = calcularPromedio(historial);
        double umbral   = promedio * factorMontoAtipico;

        for (Transaccion t : historial) {
            if (t.getMonto() > umbral && promedio > 0) {
                EventoSospechoso e = new EventoSospechoso(
                        EventoSospechoso.MONTO_ATIPICO, idUsuario,
                        EventoSospechoso.RIESGO_MEDIO,
                        "Monto inusualmente alto",
                        String.format("$%.2f vs promedio $%.2f (%.1fx)",
                                t.getMonto(), promedio, t.getMonto() / promedio));
                eventos.add(e);
                marcarTransaccion(t.getId(), EventoSospechoso.RIESGO_MEDIO);
            }
        }
        return eventos;
    }

    /**
     * DETECTOR 3: Destino repetitivo.
     * Detecta múltiples transferencias al mismo usuario destino. O(n)
     */
    private LinkedList<EventoSospechoso> detectarDestinoRepetitivo(String idUsuario,
                                                                     LinkedList<Transaccion> historial) {
        LinkedList<EventoSospechoso> eventos = new LinkedList<>();
        HashMap<String, Integer> conteoDestino = new HashMap<>();

        for (Transaccion t : historial) {
            if ("TRANSFERENCIA".equals(t.getTipo()) &&
                !t.getUsuarioDestino().equals(idUsuario)) {
                String dest = t.getUsuarioDestino();
                conteoDestino.put(dest, conteoDestino.getOrDefault(dest, 0) + 1);
            }
        }

        for (Map.Entry<String, Integer> e : conteoDestino.entrySet()) {
            if (e.getValue() >= minRepeticionesDestino) {
                eventos.add(new EventoSospechoso(
                        EventoSospechoso.DESTINO_REPETITIVO, idUsuario,
                        EventoSospechoso.RIESGO_MEDIO,
                        "Multiples transferencias al mismo destino",
                        e.getValue() + " transferencias a " + e.getKey()));
            }
        }
        return eventos;
    }

    /**
     * DETECTOR 4: Fragmentación.
     * Detecta uso simultáneo de múltiples billeteras en poco tiempo. O(n²)
     */
    private LinkedList<EventoSospechoso> detectarFragmentacion(String idUsuario,
                                                                LinkedList<Transaccion> historial) {
        LinkedList<EventoSospechoso> eventos = new LinkedList<>();
        if (historial.size() < 3) return eventos;

        Transaccion[] arr = historial.toArray(new Transaccion[0]);
        for (int i = 0; i < arr.length; i++) {
            HashMap<String, Integer> billeterasEnVentana = new HashMap<>();
            for (int j = i; j < arr.length; j++) {
                if (arr[j].getTimestamp() - arr[i].getTimestamp() > ventanaVelocidadMs * 5) break;
                billeterasEnVentana.put(arr[j].getBilleteraOrigen(),
                        billeterasEnVentana.getOrDefault(arr[j].getBilleteraOrigen(), 0) + 1);
            }
            if (billeterasEnVentana.size() >= 3) {
                eventos.add(new EventoSospechoso(
                        EventoSospechoso.FRAGMENTACION, idUsuario,
                        EventoSospechoso.RIESGO_MEDIO,
                        "Uso simultaneo de multiples billeteras",
                        billeterasEnVentana.size() + " billeteras distintas en ventana de tiempo"));
                break;
            }
        }
        return eventos;
    }

    /**
     * DETECTOR 5: Horario inusual.
     * Detecta transacciones en horarios fuera del patrón habitual. O(n)
     */
    private LinkedList<EventoSospechoso> detectarHorarioInusual(String idUsuario,
                                                                  LinkedList<Transaccion> historial) {
        LinkedList<EventoSospechoso> eventos = new LinkedList<>();
        int countInusual = 0;

        for (Transaccion t : historial) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(t.getTimestamp());
            int hora = cal.get(java.util.Calendar.HOUR_OF_DAY);
            if (hora >= horaSospechosaInicio && hora < horaSospechosaFin) {
                countInusual++;
                marcarTransaccion(t.getId(), EventoSospechoso.RIESGO_BAJO);
            }
        }

        if (countInusual > 0) {
            eventos.add(new EventoSospechoso(
                    EventoSospechoso.HORARIO_INUSUAL, idUsuario,
                    EventoSospechoso.RIESGO_BAJO,
                    "Actividad en horario inusual",
                    countInusual + " transacciones entre " +
                            horaSospechosaInicio + ":00 y " + horaSospechosaFin + ":00"));
        }
        return eventos;
    }

    // ─── Auxiliares recursivos ────────────────────────────────────────────────

    /**
     * Cuenta recursivamente cuántas transacciones hay en la ventana desde arr[inicio].
     * Caso base: j >= arr.length o timestamp fuera de la ventana → retorna 1.
     * Caso recursivo: 1 + contar desde j+1.
     *
     * @param arr    Arreglo de transacciones ordenadas por timestamp
     * @param inicio Índice de referencia
     * @param j      Índice actual (avanza en la recursión)
     * @return Cantidad de transacciones en la ventana
     */
    private int contarEnVentanaRecursivo(Transaccion[] arr, int inicio, int j) {
        if (j >= arr.length) return 1;                              // Caso base: fin del arreglo
        if (arr[j].getTimestamp() - arr[inicio].getTimestamp() > ventanaVelocidadMs)
            return 1;                                               // Caso base: fuera de ventana
        return 1 + contarEnVentanaRecursivo(arr, inicio, j + 1);   // Recursión
    }

    /** Calcula el promedio de montos. O(n) */
    private double calcularPromedio(LinkedList<Transaccion> historial) {
        if (historial.isEmpty()) return 0;
        double suma = 0;
        for (Transaccion t : historial) suma += t.getMonto();
        return suma / historial.size();
    }

    /** Filtra transacciones por tipo. O(n) */
    private LinkedList<Transaccion> filtrarPorTipo(LinkedList<Transaccion> historial, String tipo) {
        LinkedList<Transaccion> resultado = new LinkedList<>();
        for (Transaccion t : historial)
            if (tipo.equals(t.getTipo())) resultado.add(t);
        return resultado;
    }

    /** Marca una transacción con su nivel de riesgo. */
    private void marcarTransaccion(String idTransaccion, String nivelRiesgo) {
        riesgoTransacciones.put(idTransaccion, nivelRiesgo);
    }

    // ─── Configuración ────────────────────────────────────────────────────────
    public void setVentanaVelocidadMs(long ms)            { this.ventanaVelocidadMs = ms; }
    public void setMaxTransaccionesEnVentana(int max)     { this.maxTransaccionesEnVentana = max; }
    public void setFactorMontoAtipico(double factor)      { this.factorMontoAtipico = factor; }
    public void setMinRepeticionesDestino(int min)        { this.minRepeticionesDestino = min; }
    public void setHorarioSospechoso(int inicio, int fin) { this.horaSospechosaInicio = inicio; this.horaSospechosaFin = fin; }
}
