package modelos;

/**
 * EventoSospechoso - Representa un evento de comportamiento financiero inusual.
 *
 * Generado por DetectorFraude cuando detecta patrones atípicos.
 * Se almacena en el historial de auditoría del sistema.
 *
 * Tipos de evento:
 *   - VELOCIDAD_ALTA:        Múltiples transferencias en tiempo muy corto.
 *   - MONTO_ATIPICO:         Monto inusualmente alto respecto al promedio del usuario.
 *   - DESTINO_REPETITIVO:    Múltiples transferencias al mismo destino en poco tiempo.
 *   - FRAGMENTACION:         Uso simultáneo de varias billeteras para dividir un monto.
 *   - HORARIO_INUSUAL:       Actividad en horario no habitual para el usuario.
 *   - CICLO_TRANSFERENCIAS:  El usuario participa en un ciclo de transferencias circular.
 *
 * Niveles de riesgo:
 *   - BAJO:   Comportamiento levemente inusual, solo para seguimiento.
 *   - MEDIO:  Patrón sospechoso que merece revisión.
 *   - ALTO:   Comportamiento muy atípico, requiere acción inmediata.
 */
public class EventoSospechoso {

    // ─── Tipos ────────────────────────────────────────────────────────────────
    public static final String VELOCIDAD_ALTA      = "VELOCIDAD_ALTA";
    public static final String MONTO_ATIPICO       = "MONTO_ATIPICO";
    public static final String DESTINO_REPETITIVO  = "DESTINO_REPETITIVO";
    public static final String FRAGMENTACION       = "FRAGMENTACION";
    public static final String HORARIO_INUSUAL     = "HORARIO_INUSUAL";
    public static final String CICLO_TRANSFERENCIAS = "CICLO_TRANSFERENCIAS";

    // ─── Niveles de riesgo ────────────────────────────────────────────────────
    public static final String RIESGO_BAJO   = "BAJO";
    public static final String RIESGO_MEDIO  = "MEDIO";
    public static final String RIESGO_ALTO   = "ALTO";

    // ─── Atributos ────────────────────────────────────────────────────────────
    private final String id;
    private final String tipo;
    private final String idUsuario;
    private final String nivelRiesgo;
    private final String descripcion;
    private final String detalles;
    private final long   timestamp;
    private boolean revisado;

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea un evento sospechoso no revisado.
     *
     * @param tipo        Tipo de patrón detectado (usar constantes de clase)
     * @param idUsuario   ID del usuario involucrado
     * @param nivelRiesgo Nivel de riesgo asignado (BAJO/MEDIO/ALTO)
     * @param descripcion Descripción breve del evento
     * @param detalles    Información técnica detallada del patrón detectado
     */
    public EventoSospechoso(String tipo, String idUsuario, String nivelRiesgo,
                             String descripcion, String detalles) {
        this.id          = "EVT" + System.nanoTime();
        this.tipo        = tipo;
        this.idUsuario   = idUsuario;
        this.nivelRiesgo = nivelRiesgo;
        this.descripcion = descripcion;
        this.detalles    = detalles;
        this.timestamp   = System.currentTimeMillis();
        this.revisado    = false;
    }

    // ─── Métodos ──────────────────────────────────────────────────────────────

    /** Marca el evento como revisado por un analista. */
    public void marcarRevisado() { this.revisado = true; }

    /** @return true si el evento es de riesgo alto */
    public boolean esRiesgoAlto() { return RIESGO_ALTO.equals(nivelRiesgo); }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getId()          { return id; }
    public String getTipo()        { return tipo; }
    public String getIdUsuario()   { return idUsuario; }
    public String getNivelRiesgo() { return nivelRiesgo; }
    public String getDescripcion() { return descripcion; }
    public String getDetalles()    { return detalles; }
    public long   getTimestamp()   { return timestamp; }
    public boolean isRevisado()    { return revisado; }

    @Override
    public String toString() {
        return String.format("[%s][%s] %s - %s | Usuario: %s%s",
                nivelRiesgo, tipo, descripcion, detalles, idUsuario,
                revisado ? " (revisado)" : "");
    }
}
