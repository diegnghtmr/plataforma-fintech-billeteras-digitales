package modelos;

/**
 * Notificacion - Representa una alerta o aviso del sistema para un usuario.
 *
 * Las notificaciones son generadas automáticamente por el motor de lógica
 * en respuesta a eventos: saldo bajo, cambio de nivel, operaciones programadas, etc.
 *
 * Tipos de notificación:
 *   - SALDO_BAJO:            El saldo de una billetera cayó bajo el umbral configurado.
 *   - ASCENSO_NIVEL:         El usuario subió de nivel de fidelización.
 *   - DESCENSO_NIVEL:        El usuario bajó de nivel (por reversión de puntos).
 *   - OP_EJECUTADA:          Una operación programada se ejecutó exitosamente.
 *   - OP_FALLIDA:            Una operación programada falló al ejecutarse.
 *   - OP_CANCELADA:          Una operación programada fue cancelada.
 *   - PUNTOS_ACUMULADOS:     El usuario acumuló un hito de puntos.
 *   - BIENVENIDA:            Nuevo usuario registrado en el sistema.
 */
public class Notificacion {

    // ─── Tipos ────────────────────────────────────────────────────────────────
    public static final String SALDO_BAJO         = "SALDO_BAJO";
    public static final String ASCENSO_NIVEL      = "ASCENSO_NIVEL";
    public static final String DESCENSO_NIVEL     = "DESCENSO_NIVEL";
    public static final String OP_EJECUTADA       = "OP_EJECUTADA";
    public static final String OP_FALLIDA         = "OP_FALLIDA";
    public static final String OP_CANCELADA       = "OP_CANCELADA";
    public static final String PUNTOS_ACUMULADOS  = "PUNTOS_ACUMULADOS";
    public static final String BIENVENIDA         = "BIENVENIDA";

    // ─── Atributos ────────────────────────────────────────────────────────────
    private final String id;
    private final String idUsuario;
    private final String tipo;
    private final String titulo;
    private final String mensaje;
    private final long   timestamp;
    private boolean leida;

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea una notificación nueva (no leída).
     *
     * @param idUsuario ID del usuario destinatario
     * @param tipo      Tipo de notificación (usar constantes de clase)
     * @param titulo    Título corto de la notificación
     * @param mensaje   Mensaje detallado
     */
    public Notificacion(String idUsuario, String tipo, String titulo, String mensaje) {
        this.id        = "NTF" + System.nanoTime();
        this.idUsuario = idUsuario;
        this.tipo      = tipo;
        this.titulo    = titulo;
        this.mensaje   = mensaje;
        this.timestamp = System.currentTimeMillis();
        this.leida     = false;
    }

    // ─── Fábricas convenientes ────────────────────────────────────────────────

    /** Crea notificación de saldo bajo. */
    public static Notificacion saldoBajo(String idUsuario, String nombreBilletera, double saldo) {
        return new Notificacion(idUsuario, SALDO_BAJO,
                "⚠️ Saldo bajo",
                String.format("Tu billetera '%s' tiene saldo bajo: $%.2f. Considera recargarla.", nombreBilletera, saldo));
    }

    /** Crea notificación de ascenso de nivel. */
    public static Notificacion ascensoNivel(String idUsuario, String nivelAnterior, String nivelNuevo, double puntos) {
        return new Notificacion(idUsuario, ASCENSO_NIVEL,
                "🎉 ¡Subiste de nivel!",
                String.format("Felicitaciones! Pasaste de %s a %s con %.0f puntos. Nuevos beneficios disponibles.",
                        nivelAnterior, nivelNuevo, puntos));
    }

    /** Crea notificación de descenso de nivel. */
    public static Notificacion descensoNivel(String idUsuario, String nivelAnterior, String nivelNuevo, double puntos) {
        return new Notificacion(idUsuario, DESCENSO_NIVEL,
                "📉 Cambio de nivel",
                String.format("Tu nivel cambió de %s a %s. Puntos actuales: %.0f.",
                        nivelAnterior, nivelNuevo, puntos));
    }

    /** Crea notificación de operación programada ejecutada. */
    public static Notificacion opEjecutada(String idUsuario, String descripcionOp, double monto) {
        return new Notificacion(idUsuario, OP_EJECUTADA,
                "✅ Operación ejecutada",
                String.format("Se ejecutó automáticamente: '%s' por $%.2f.", descripcionOp, monto));
    }

    /** Crea notificación de operación programada fallida. */
    public static Notificacion opFallida(String idUsuario, String descripcionOp, String razon) {
        return new Notificacion(idUsuario, OP_FALLIDA,
                "❌ Operación fallida",
                String.format("No se pudo ejecutar: '%s'. Razón: %s.", descripcionOp, razon));
    }

    /** Crea notificación de operación cancelada. */
    public static Notificacion opCancelada(String idUsuario, String descripcionOp) {
        return new Notificacion(idUsuario, OP_CANCELADA,
                "🚫 Operación cancelada",
                String.format("Se canceló la operación programada: '%s'.", descripcionOp));
    }

    /** Crea notificación de hito de puntos. */
    public static Notificacion hitoPuntos(String idUsuario, double puntos) {
        return new Notificacion(idUsuario, PUNTOS_ACUMULADOS,
                "🏆 Hito de puntos",
                String.format("¡Alcanzaste %.0f puntos acumulados! Sigue usando la plataforma.", puntos));
    }

    /** Crea notificación de bienvenida. */
    public static Notificacion bienvenida(String idUsuario, String nombreUsuario) {
        return new Notificacion(idUsuario, BIENVENIDA,
                "👋 ¡Bienvenido!",
                String.format("Hola %s, tu cuenta ha sido creada. Empieza recargando tu primera billetera.",
                        nombreUsuario));
    }

    // ─── Métodos ──────────────────────────────────────────────────────────────

    /** Marca la notificación como leída. */
    public void marcarLeida() { this.leida = true; }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getId()        { return id; }
    public String getIdUsuario() { return idUsuario; }
    public String getTipo()      { return tipo; }
    public String getTitulo()    { return titulo; }
    public String getMensaje()   { return mensaje; }
    public long   getTimestamp() { return timestamp; }
    public boolean isLeida()     { return leida; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s%s",
                tipo, titulo, mensaje, leida ? " (leída)" : "");
    }
}
