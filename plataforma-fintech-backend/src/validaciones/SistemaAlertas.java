package validaciones;

import estructuras.ColaSimple;
import modelos.Notificacion;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * SistemaAlertas - Motor de alertas automáticas y gestión de notificaciones.
 *
 * Responsabilidades:
 *   1. Detectar saldo bajo en billeteras y generar alerta.
 *   2. Gestionar la Cola de notificaciones por usuario (ColaSimple<Notificacion>).
 *   3. Entregar notificaciones pendientes a un usuario (desencolar).
 *   4. Consultar notificaciones sin consumirlas (peek).
 *   5. Contar notificaciones no leídas por usuario.
 *
 * Umbral de saldo bajo: configurable (default $50.0).
 *
 * Integración:
 *   - Comparte el mapa de colas con SistemaPuntos (mismo objeto HashMap).
 *   - MotorOperaciones2 llama a verificarSaldoBajo() tras cada retiro o transferencia.
 */
public class SistemaAlertas {

    // ─── Constantes ───────────────────────────────────────────────────────────
    public static final double UMBRAL_SALDO_BAJO_DEFAULT = 50.0;

    // ─── Atributos ────────────────────────────────────────────────────────────

    /**
     * Mapa principal: idUsuario → ColaSimple<Notificacion>.
     * Compartido con SistemaPuntos para que ambos encolen en la misma cola.
     */
    private final HashMap<String, ColaSimple<Notificacion>> colasNotificaciones;

    /**
     * Umbral de saldo bajo por billetera (configurable).
     */
    private double umbralSaldoBajo;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Crea el sistema de alertas con el mapa de colas compartido.
     *
     * @param colasNotificaciones Mapa compartido con SistemaPuntos
     */
    public SistemaAlertas(HashMap<String, ColaSimple<Notificacion>> colasNotificaciones) {
        this.colasNotificaciones = colasNotificaciones;
        this.umbralSaldoBajo = UMBRAL_SALDO_BAJO_DEFAULT;
    }

    // ─── API pública: Generación de alertas ───────────────────────────────────

    /**
     * Verifica si el saldo de una billetera está bajo el umbral y, de ser así,
     * genera y encola una alerta de saldo bajo.
     *
     * Debe llamarse tras cada retiro o transferencia saliente.
     *
     * @param idUsuario       ID del usuario dueño de la billetera
     * @param nombreBilletera Nombre descriptivo de la billetera
     * @param saldoActual     Saldo actual después de la operación
     */
    public void verificarSaldoBajo(String idUsuario, String nombreBilletera, double saldoActual) {
        if (saldoActual < umbralSaldoBajo) {
            Notificacion alerta = Notificacion.saldoBajo(idUsuario, nombreBilletera, saldoActual);
            encolarNotificacion(idUsuario, alerta);
            System.out.println("[ALERTA] Saldo bajo en billetera '" + nombreBilletera +
                    "' de usuario " + idUsuario + ": $" + String.format("%.2f", saldoActual));
        }
    }

    /**
     * Genera una notificación de operación programada ejecutada.
     *
     * @param idUsuario      ID del usuario
     * @param descripcionOp  Descripción de la operación
     * @param monto          Monto ejecutado
     */
    public void notificarOpEjecutada(String idUsuario, String descripcionOp, double monto) {
        Notificacion notif = Notificacion.opEjecutada(idUsuario, descripcionOp, monto);
        encolarNotificacion(idUsuario, notif);
        System.out.println("[NOTIF] Operacion ejecutada para " + idUsuario + ": " + descripcionOp);
    }

    /**
     * Genera una notificación de operación programada fallida.
     *
     * @param idUsuario      ID del usuario
     * @param descripcionOp  Descripción de la operación
     * @param razon          Razón del fallo
     */
    public void notificarOpFallida(String idUsuario, String descripcionOp, String razon) {
        Notificacion notif = Notificacion.opFallida(idUsuario, descripcionOp, razon);
        encolarNotificacion(idUsuario, notif);
        System.out.println("[NOTIF] Operacion fallida para " + idUsuario + ": " + razon);
    }

    /**
     * Genera una notificación de operación programada cancelada.
     *
     * @param idUsuario     ID del usuario
     * @param descripcionOp Descripción de la operación cancelada
     */
    public void notificarOpCancelada(String idUsuario, String descripcionOp) {
        Notificacion notif = Notificacion.opCancelada(idUsuario, descripcionOp);
        encolarNotificacion(idUsuario, notif);
    }

    /**
     * Agrega manualmente una notificación para un usuario.
     * Útil para SistemaPuntos u otros módulos.
     *
     * @param idUsuario ID del usuario destinatario
     * @param notif     Notificación a encolar
     */
    public void agregar(String idUsuario, Notificacion notif) {
        encolarNotificacion(idUsuario, notif);
    }

    // ─── API pública: Consumo de notificaciones ───────────────────────────────

    /**
     * Entrega la siguiente notificación pendiente del usuario (FIFO).
     * La notificación es extraída de la cola (consumida) y marcada como leída.
     *
     * @param idUsuario ID del usuario
     * @return Próxima notificación, o null si no hay
     */
    public Notificacion obtenerSiguiente(String idUsuario) {
        ColaSimple<Notificacion> cola = colasNotificaciones.get(idUsuario);
        if (cola == null || cola.estaVacia()) return null;
        Notificacion notif = cola.desencolar();
        if (notif != null) notif.marcarLeida();
        return notif;
    }

    /**
     * Consulta la siguiente notificación SIN extraerla de la cola.
     *
     * @param idUsuario ID del usuario
     * @return Primera notificación, o null si no hay
     */
    public Notificacion verSiguiente(String idUsuario) {
        ColaSimple<Notificacion> cola = colasNotificaciones.get(idUsuario);
        if (cola == null || cola.estaVacia()) return null;
        return cola.frente();
    }

    /**
     * Entrega TODAS las notificaciones pendientes del usuario de una vez.
     * Las notificaciones son extraídas (consumidas) y marcadas como leídas.
     *
     * @param idUsuario ID del usuario
     * @return Lista con todas las notificaciones pendientes (puede estar vacía)
     */
    public LinkedList<Notificacion> obtenerTodas(String idUsuario) {
        LinkedList<Notificacion> resultado = new LinkedList<>();
        ColaSimple<Notificacion> cola = colasNotificaciones.get(idUsuario);
        if (cola == null) return resultado;
        while (!cola.estaVacia()) {
            Notificacion notif = cola.desencolar();
            notif.marcarLeida();
            resultado.add(notif);
        }
        return resultado;
    }

    /**
     * Cuenta notificaciones no leídas de un usuario usando el filtro recursivo
     * de ColaSimple.
     *
     * @param idUsuario ID del usuario
     * @return Cantidad de notificaciones no leídas
     */
    public int contarNoLeidas(String idUsuario) {
        ColaSimple<Notificacion> cola = colasNotificaciones.get(idUsuario);
        if (cola == null) return 0;
        return cola.contarConFiltro(n -> !n.isLeida());  // Filtro recursivo
    }

    /**
     * Cuenta el total de notificaciones pendientes de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Cantidad total en la cola
     */
    public int contarPendientes(String idUsuario) {
        ColaSimple<Notificacion> cola = colasNotificaciones.get(idUsuario);
        if (cola == null) return 0;
        return cola.tamaño();
    }

    /**
     * Muestra todas las notificaciones de un usuario sin consumirlas.
     * Usa el método toArray() con recursión de ColaSimple.
     *
     * @param idUsuario ID del usuario
     */
    public void mostrarNotificaciones(String idUsuario) {
        ColaSimple<Notificacion> cola = colasNotificaciones.get(idUsuario);
        if (cola == null || cola.estaVacia()) {
            System.out.println("  (Sin notificaciones pendientes para " + idUsuario + ")");
            return;
        }
        java.util.LinkedList<Notificacion> notifs = cola.toList();
        System.out.println("  Notificaciones de " + idUsuario + " (" + notifs.size() + "):");
        int idx = 1;
        for (Notificacion n : notifs) {
            System.out.println("    " + idx++ + ". " + n.getTitulo() + " - " + n.getMensaje());
        }
    }

    // ─── Configuración ────────────────────────────────────────────────────────

    /**
     * Cambia el umbral de saldo bajo.
     *
     * @param nuevoUmbral Nuevo umbral (debe ser positivo)
     */
    public void setUmbralSaldoBajo(double nuevoUmbral) {
        if (nuevoUmbral > 0) this.umbralSaldoBajo = nuevoUmbral;
    }

    /** @return Umbral actual de saldo bajo */
    public double getUmbralSaldoBajo() { return umbralSaldoBajo; }

    /** @return Mapa completo de colas (para inspección/pruebas) */
    public HashMap<String, ColaSimple<Notificacion>> getColasNotificaciones() {
        return colasNotificaciones;
    }

    // ─── Interno ──────────────────────────────────────────────────────────────

    /** Agrega una notificación a la cola del usuario, creando la cola si no existe. */
    private void encolarNotificacion(String idUsuario, Notificacion notif) {
        colasNotificaciones.computeIfAbsent(idUsuario, k -> new ColaSimple<>()).encolar(notif);
    }
}
