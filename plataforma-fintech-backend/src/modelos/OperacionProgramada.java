package modelos;

/**
 * OperacionProgramada - Representa una transacción financiera programada.
 *
 * Una operación programada es una instrucción de pago o movimiento que
 * el usuario define para ejecutarse automáticamente en un momento futuro.
 *
 * Tipos soportados: RECARGA, RETIRO, TRANSFERENCIA_INTERNA, TRANSFERENCIA_EXTERNA.
 *
 * Estado posibles:
 *   - PENDIENTE:  Aún no ha llegado su fecha de ejecución.
 *   - EJECUTADA:  Se ejecutó exitosamente.
 *   - FALLIDA:    Intentó ejecutarse pero falló (saldo insuficiente, etc.).
 *   - CANCELADA:  Fue cancelada manualmente antes de ejecutarse.
 */
public class OperacionProgramada implements Comparable<OperacionProgramada> {

    // ─── Tipos de operación ───────────────────────────────────────────────────
    public static final String RECARGA                = "RECARGA";
    public static final String RETIRO                 = "RETIRO";
    public static final String TRANSFERENCIA_INTERNA  = "TRANSFERENCIA_INTERNA";
    public static final String TRANSFERENCIA_EXTERNA  = "TRANSFERENCIA_EXTERNA";

    // ─── Estados ─────────────────────────────────────────────────────────────
    public static final String PENDIENTE  = "PENDIENTE";
    public static final String EJECUTADA  = "EJECUTADA";
    public static final String FALLIDA    = "FALLIDA";
    public static final String CANCELADA  = "CANCELADA";

    // ─── Atributos ────────────────────────────────────────────────────────────
    private final String id;
    private final String tipo;
    private final String idUsuarioOrigen;
    private final String codigoBilleteraOrigen;
    private final String idUsuarioDestino;    // null si no aplica
    private final String codigoBilleteraDestino; // null si no aplica
    private final double monto;
    private final long   fechaEjecucion;      // Timestamp Unix en ms
    private final String descripcion;
    private String estado;
    private String mensajeError;              // Razón de fallo, si aplica
    private long   fechaEjecucionReal;        // Cuándo se ejecutó realmente

    // ─── Constructor completo ─────────────────────────────────────────────────
    /**
     * Crea una operación programada.
     *
     * @param tipo                     Tipo de operación (usar constantes de clase)
     * @param idUsuarioOrigen          ID del usuario que origina la operación
     * @param codigoBilleteraOrigen    Código de la billetera origen
     * @param idUsuarioDestino         ID del usuario destino (null si no aplica)
     * @param codigoBilleteraDestino   Código de la billetera destino (null si no aplica)
     * @param monto                    Monto a mover
     * @param fechaEjecucion           Timestamp Unix en ms de cuándo ejecutar
     * @param descripcion              Descripción legible de la operación
     */
    public OperacionProgramada(String tipo,
                                String idUsuarioOrigen,
                                String codigoBilleteraOrigen,
                                String idUsuarioDestino,
                                String codigoBilleteraDestino,
                                double monto,
                                long   fechaEjecucion,
                                String descripcion) {
        this.id                      = "OP" + System.nanoTime();
        this.tipo                    = tipo;
        this.idUsuarioOrigen         = idUsuarioOrigen;
        this.codigoBilleteraOrigen   = codigoBilleteraOrigen;
        this.idUsuarioDestino        = idUsuarioDestino;
        this.codigoBilleteraDestino  = codigoBilleteraDestino;
        this.monto                   = monto;
        this.fechaEjecucion          = fechaEjecucion;
        this.descripcion             = descripcion;
        this.estado                  = PENDIENTE;
        this.mensajeError            = null;
        this.fechaEjecucionReal      = -1;
    }

    // ─── Constructores convenientes ───────────────────────────────────────────

    /** Crea una recarga programada. */
    public static OperacionProgramada recarga(String idUsuario, String codigoBilletera,
                                               double monto, long fechaEjecucion, String descripcion) {
        return new OperacionProgramada(RECARGA, idUsuario, codigoBilletera,
                null, null, monto, fechaEjecucion, descripcion);
    }

    /** Crea un retiro programado. */
    public static OperacionProgramada retiro(String idUsuario, String codigoBilletera,
                                              double monto, long fechaEjecucion, String descripcion) {
        return new OperacionProgramada(RETIRO, idUsuario, codigoBilletera,
                null, null, monto, fechaEjecucion, descripcion);
    }

    /** Crea una transferencia interna programada. */
    public static OperacionProgramada transferenciaInterna(String idUsuario, String origen,
                                                            String destino, double monto,
                                                            long fechaEjecucion, String descripcion) {
        return new OperacionProgramada(TRANSFERENCIA_INTERNA, idUsuario, origen,
                idUsuario, destino, monto, fechaEjecucion, descripcion);
    }

    /** Crea una transferencia externa programada. */
    public static OperacionProgramada transferenciaExterna(String idOrigen, String billeteraOrigen,
                                                            String idDestino, String billeteraDestino,
                                                            double monto, long fechaEjecucion,
                                                            String descripcion) {
        return new OperacionProgramada(TRANSFERENCIA_EXTERNA, idOrigen, billeteraOrigen,
                idDestino, billeteraDestino, monto, fechaEjecucion, descripcion);
    }

    // ─── Métodos de estado ────────────────────────────────────────────────────

    /** Marca la operación como ejecutada exitosamente. */
    public void marcarEjecutada() {
        this.estado = EJECUTADA;
        this.fechaEjecucionReal = System.currentTimeMillis();
    }

    /** Marca la operación como fallida con una razón. */
    public void marcarFallida(String razon) {
        this.estado = FALLIDA;
        this.mensajeError = razon;
        this.fechaEjecucionReal = System.currentTimeMillis();
    }

    /** Cancela la operación (solo si está PENDIENTE). */
    public boolean cancelar() {
        if (!PENDIENTE.equals(estado)) return false;
        this.estado = CANCELADA;
        return true;
    }

    /** @return true si la operación puede ejecutarse ahora */
    public boolean debeEjecutarse() {
        return PENDIENTE.equals(estado) &&
               System.currentTimeMillis() >= fechaEjecucion;
    }

    /** @return true si la operación está pendiente */
    public boolean estaPendiente() { return PENDIENTE.equals(estado); }

    // ─── Comparable (por fechaEjecucion, para ColaPrioridad) ─────────────────

    /**
     * Compara por fecha de ejecución.
     * Menor timestamp = más urgente = mayor prioridad.
     * Usado por ColaPrioridad<OperacionProgramada>.
     */
    @Override
    public int compareTo(OperacionProgramada otra) {
        return Long.compare(this.fechaEjecucion, otra.fechaEjecucion);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getId()                      { return id; }
    public String getTipo()                    { return tipo; }
    public String getIdUsuarioOrigen()         { return idUsuarioOrigen; }
    public String getCodigoBilleteraOrigen()   { return codigoBilleteraOrigen; }
    public String getIdUsuarioDestino()        { return idUsuarioDestino; }
    public String getCodigoBilleteraDestino()  { return codigoBilleteraDestino; }
    public double getMonto()                   { return monto; }
    public long   getFechaEjecucion()          { return fechaEjecucion; }
    public String getDescripcion()             { return descripcion; }
    public String getEstado()                  { return estado; }
    public String getMensajeError()            { return mensajeError; }
    public long   getFechaEjecucionReal()      { return fechaEjecucionReal; }

    @Override
    public String toString() {
        return String.format("OperacionProgramada{id='%s', tipo='%s', usuario='%s', " +
                        "billetera='%s', monto=%.2f, estado='%s', desc='%s'}",
                id, tipo, idUsuarioOrigen, codigoBilleteraOrigen, monto, estado, descripcion);
    }
}
