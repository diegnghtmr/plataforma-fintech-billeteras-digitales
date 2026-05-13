package operaciones;

import estructuras.ColaPrioridad;
import estructuras.ColaSimple;
import modelos.Notificacion;
import modelos.OperacionProgramada;
import validaciones.SistemaAlertas;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * MotorOperaciones2 - Orquestador principal del Módulo 2.
 *
 * Es el punto de entrada de este módulo. Integra:
 *   - ColaPrioridad<OperacionProgramada>: ordena y ejecuta ops. por fecha.
 *   - SistemaPuntos: acumula, descuenta y recalcula puntos/niveles.
 *   - SistemaAlertas: genera y entrega notificaciones al usuario.
 *
 * INTEGRACIÓN CON COMPAÑERO 1:
 *   Este motor NO duplica la lógica financiera del Compañero 1.
 *   Para las transacciones programadas delega en el sistema del Comp.1
 *   a través de la interfaz IntegracionComp1 (inyección de dependencia).
 *   Si no hay integración real disponible (p.ej. en pruebas), usa el
 *   adaptador SimuladorComp1 que simula las respuestas.
 *
 * FLUJO DE UNA OPERACIÓN PROGRAMADA:
 *   1. Usuario llama a programarOperacion() → va a ColaPrioridad.
 *   2. llamarEjecucionAutomatica() extrae ops. vencidas de la Cola.
 *   3. Para cada op. vencida: delega en Comp.1, actualiza puntos,
 *      genera notificaciones.
 *
 * FLUJO DE PUNTOS MANUAL (sin operación programada):
 *   MotorOperaciones2.registrarTransaccionManual() permite que el Comp.1
 *   notifique a este módulo de transacciones ya ejecutadas para que
 *   se actualicen puntos y alertas.
 */
public class GestorOperaciones {

    // ─── Interfaz de integración con Compañero 1 ─────────────────────────────

    /**
     * Interfaz que el Compañero 1 debe implementar para que este motor
     * pueda ejecutar operaciones financieras reales.
     * En pruebas se usa SimuladorComp1.
     */
    public interface IntegracionComp1 {
        boolean recargar(String idUsuario, String codigoBilletera, double monto);
        boolean retirar(String idUsuario, String codigoBilletera, double monto);
        boolean transferirInterno(String idUsuario, String origen, String destino, double monto);
        boolean transferirExterno(String idOrigen, String billOrigen,
                                  String idDestino, String billDestino, double monto);
        /** @return Saldo actual de la billetera, o -1 si no existe */
        double getSaldo(String idUsuario, String codigoBilletera);
        /** @return Nombre de la billetera, o "?" si no existe */
        String getNombreBilletera(String idUsuario, String codigoBilletera);
    }

    // ─── Estructuras principales ──────────────────────────────────────────────

    /**
     * Cola de Prioridad: almacena operaciones programadas ordenadas por
     * fecha de ejecución (menor timestamp = más urgente).
     */
    private final ColaPrioridad<OperacionProgramada> colaOperaciones;

    /**
     * Motor de puntos y niveles.
     */
    private final SistemaPuntos sistemaPuntos;

    /**
     * Motor de alertas y notificaciones.
     */
    private final SistemaAlertas sistemaAlertas;

    /**
     * Mapa compartido de colas de notificaciones.
     */
    private final HashMap<String, ColaSimple<Notificacion>> colasNotificaciones;

    /**
     * Integración con el sistema del Compañero 1.
     */
    private final IntegracionComp1 comp1;

    /**
     * Historial de operaciones ejecutadas (todas, exitosas o fallidas).
     */
    private final LinkedList<OperacionProgramada> historialEjecuciones;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Construye el motor con una implementación de IntegracionComp1.
     *
     * @param comp1 Implementación real o simulada de la API del Compañero 1
     */
    public GestorOperaciones(IntegracionComp1 comp1) {
        this.colasNotificaciones = new HashMap<>();
        this.sistemaPuntos       = new SistemaPuntos(colasNotificaciones);
        this.sistemaAlertas      = new SistemaAlertas(colasNotificaciones);
        this.colaOperaciones     = new ColaPrioridad<>();
        this.historialEjecuciones = new LinkedList<>();
        this.comp1               = comp1;
    }

    // ─── API: Gestión de usuarios ─────────────────────────────────────────────

    /**
     * Registra un usuario en el sistema de puntos y genera bienvenida.
     * Debe llamarse cuando el Compañero 1 crea un usuario nuevo.
     *
     * @param idUsuario ID del usuario
     * @param nombre    Nombre del usuario
     */
    public void registrarUsuario(String idUsuario, String nombre) {
        sistemaPuntos.registrarUsuario(idUsuario, nombre);
        System.out.println("[MOTOR2] Usuario " + idUsuario + " (" + nombre + ") registrado en sistema de puntos.");
    }

    // ─── API: Operaciones programadas ─────────────────────────────────────────

    /**
     * Programa una operación para ejecución futura.
     * La operación se inserta en la ColaPrioridad según su fecha de ejecución.
     * O(log n)
     *
     * @param op Operación a programar
     */
    public void programarOperacion(OperacionProgramada op) {
        colaOperaciones.insertar(op, op.getFechaEjecucion());
        System.out.println("[MOTOR2] Operacion programada: " + op.getDescripcion() +
                " para timestamp " + op.getFechaEjecucion() +
                " | Cola: " + colaOperaciones.tamaño() + " pendientes");
    }

    /**
     * Cancela la primera operación pendiente con el ID dado.
     * Nota: no elimina del heap (lazy deletion); al llegar al frente
     * se detecta como CANCELADA y se descarta.
     *
     * @param idOperacion ID de la operación a cancelar
     * @return true si se encontró y canceló, false si no existe
     */
    public boolean cancelarOperacion(String idOperacion) {
        // Buscar en el historial de pendientes (la cola no permite acceso directo)
        // Se marca como CANCELADA; al salir del heap se descarta
        return buscarYCancelarEnCola(idOperacion);
    }

    /**
     * Ejecuta automáticamente todas las operaciones vencidas.
     * Extrae de la ColaPrioridad todas las ops. cuyo timestamp <= ahora.
     * Para cada una, delega en Comp.1 y actualiza puntos/alertas.
     *
     * Llamar periódicamente (simulador, scheduler, o en pruebas manual).
     *
     * @return Cantidad de operaciones procesadas en esta llamada
     */
    public int ejecutarOperacionesVencidas() {
        int procesadas = 0;
        long ahora = System.currentTimeMillis();

        System.out.println("\n[MOTOR2] Verificando operaciones vencidas... (t=" + ahora + ")");

        while (!colaOperaciones.estaVacia() &&
               colaOperaciones.verPrioridadMin() <= ahora) {

            OperacionProgramada op = colaOperaciones.extraerMin();

            // Lazy deletion: si ya fue cancelada, descartar
            if (OperacionProgramada.CANCELADA.equals(op.getEstado())) {
                System.out.println("[MOTOR2] Operacion cancelada descartada: " + op.getId());
                continue;
            }

            ejecutarOperacion(op);
            historialEjecuciones.add(op);
            procesadas++;
        }

        if (procesadas == 0) {
            System.out.println("[MOTOR2] No hay operaciones vencidas por ejecutar.");
        }
        return procesadas;
    }

    /**
     * Simulación de ejecución automática con tiempo artificial.
     * Permite probar operaciones "a futuro" avanzando el timestamp simulado.
     * Ejecuta operaciones cuyo timestamp sea menor o igual al dado.
     *
     * @param timestampSimulado Timestamp hasta el cual ejecutar operaciones
     * @return Cantidad de operaciones procesadas
     */
    public int ejecutarHasta(long timestampSimulado) {
        int procesadas = 0;
        System.out.println("\n[MOTOR2] Simulacion: ejecutando hasta t=" + timestampSimulado);

        while (!colaOperaciones.estaVacia() &&
               colaOperaciones.verPrioridadMin() <= timestampSimulado) {

            OperacionProgramada op = colaOperaciones.extraerMin();

            if (OperacionProgramada.CANCELADA.equals(op.getEstado())) {
                System.out.println("[MOTOR2] Cancelada descartada: " + op.getId());
                continue;
            }

            ejecutarOperacion(op);
            historialEjecuciones.add(op);
            procesadas++;
        }
        return procesadas;
    }

    // ─── API: Registro de transacciones manuales (desde Comp.1) ──────────────

    /**
     * Registra una transacción ya ejecutada por el Compañero 1.
     * Actualiza puntos y verifica alertas de saldo.
     *
     * Llamar desde Comp.1 después de cada transacción exitosa.
     *
     * @param idUsuario       ID del usuario
     * @param tipoOp          Tipo de operación ("RECARGA", "RETIRO", etc.)
     * @param monto           Monto de la operación
     * @param nombreBilletera Nombre de la billetera afectada (para alerta de saldo)
     * @param saldoResultante Saldo de la billetera tras la operación
     */
    public void registrarTransaccionManual(String idUsuario, String tipoOp,
                                            double monto, String nombreBilletera,
                                            double saldoResultante) {
        double puntos = sistemaPuntos.acumularPuntos(idUsuario, tipoOp, monto);
        if (puntos > 0) {
            System.out.println("[MOTOR2] +" + puntos + " puntos para " + idUsuario +
                    " por " + tipoOp + " de $" + String.format("%.2f", monto));
        }

        // Verificar saldo bajo tras retiros y transferencias
        if ("RETIRO".equals(tipoOp) || "TRANSFERENCIA_INTERNA".equals(tipoOp) ||
            "TRANSFERENCIA_EXTERNA".equals(tipoOp)) {
            sistemaAlertas.verificarSaldoBajo(idUsuario, nombreBilletera, saldoResultante);
        }
    }

    /**
     * Registra una reversión de transacción.
     * Descuenta los puntos correspondientes.
     *
     * @param idUsuario ID del usuario
     * @param tipoOp    Tipo de la operación revertida
     * @param monto     Monto de la operación revertida
     */
    public void registrarReversion(String idUsuario, String tipoOp, double monto) {
        double descuento = sistemaPuntos.descontarPuntos(idUsuario, tipoOp, monto);
        if (descuento > 0) {
            System.out.println("[MOTOR2] -" + descuento + " puntos para " + idUsuario +
                    " por reversion de " + tipoOp);
        }
    }

    // ─── API: Sistema de puntos (delegación) ──────────────────────────────────

    /** @return Puntos actuales del usuario */
    public double getPuntos(String idUsuario) {
        return sistemaPuntos.getPuntos(idUsuario);
    }

    /** @return Nivel actual del usuario */
    public String getNivel(String idUsuario) {
        return sistemaPuntos.getNivel(idUsuario);
    }

    /** @return Puntos necesarios para subir de nivel */
    public double puntosParaSiguienteNivel(String idUsuario) {
        return sistemaPuntos.puntosParaSiguienteNivel(idUsuario);
    }

    /** @return Usuarios con puntos en el rango [min, max] */
    public LinkedList<String> buscarUsuariosPorRangoPuntos(double min, double max) {
        return sistemaPuntos.buscarUsuariosPorRangoPuntos(min, max);
    }

    /** @return Lista de IDs de usuario, de mayor a menor puntaje */
    public LinkedList<String> rankingDescendente() {
        return sistemaPuntos.rankingDescendente();
    }

    /** @return Lista de IDs de usuario, de menor a mayor puntaje */
    public LinkedList<String> rankingAscendente() {
        return sistemaPuntos.rankingAscendente();
    }

    // ─── API: Sistema de alertas (delegación) ─────────────────────────────────

    /** @return Próxima notificación del usuario (la extrae y marca leída) */
    public Notificacion obtenerNotificacion(String idUsuario) {
        return sistemaAlertas.obtenerSiguiente(idUsuario);
    }

    /** @return Todas las notificaciones pendientes del usuario */
    public LinkedList<Notificacion> obtenerTodasNotificaciones(String idUsuario) {
        return sistemaAlertas.obtenerTodas(idUsuario);
    }

    /** Muestra notificaciones sin consumirlas */
    public void mostrarNotificaciones(String idUsuario) {
        sistemaAlertas.mostrarNotificaciones(idUsuario);
    }

    /** @return Cantidad de notificaciones pendientes */
    public int contarNotificacionesPendientes(String idUsuario) {
        return sistemaAlertas.contarPendientes(idUsuario);
    }

    /** Cambia el umbral de saldo bajo */
    public void setUmbralSaldoBajo(double umbral) {
        sistemaAlertas.setUmbralSaldoBajo(umbral);
    }

    // ─── API: Consultas generales ─────────────────────────────────────────────

    /** @return Cantidad de operaciones pendientes en la cola */
    public int operacionesPendientes() { return colaOperaciones.tamaño(); }

    /** @return true si no hay operaciones programadas */
    public boolean sinOperacionesPendientes() { return colaOperaciones.estaVacia(); }

    /** @return Historial de todas las operaciones ejecutadas (exitosas y fallidas) */
    public LinkedList<OperacionProgramada> getHistorialEjecuciones() {
        return historialEjecuciones;
    }

    /** @return Referencia al SistemaPuntos (para pruebas directas) */
    public SistemaPuntos getSistemaPuntos() { return sistemaPuntos; }

    /** @return Referencia al SistemaAlertas */
    public SistemaAlertas getSistemaAlertas() { return sistemaAlertas; }

    // ─── Ejecución interna de una operación ──────────────────────────────────

    /**
     * Ejecuta una operación delegando en Comp.1, actualiza puntos y alertas.
     */
    private void ejecutarOperacion(OperacionProgramada op) {
        System.out.println("[MOTOR2] Ejecutando: " + op.getDescripcion() + " ($" +
                String.format("%.2f", op.getMonto()) + ")");
        boolean exito;

        switch (op.getTipo()) {
            case OperacionProgramada.RECARGA:
                exito = comp1.recargar(op.getIdUsuarioOrigen(),
                        op.getCodigoBilleteraOrigen(), op.getMonto());
                break;
            case OperacionProgramada.RETIRO:
                exito = comp1.retirar(op.getIdUsuarioOrigen(),
                        op.getCodigoBilleteraOrigen(), op.getMonto());
                break;
            case OperacionProgramada.TRANSFERENCIA_INTERNA:
                exito = comp1.transferirInterno(op.getIdUsuarioOrigen(),
                        op.getCodigoBilleteraOrigen(),
                        op.getCodigoBilleteraDestino(), op.getMonto());
                break;
            case OperacionProgramada.TRANSFERENCIA_EXTERNA:
                exito = comp1.transferirExterno(op.getIdUsuarioOrigen(),
                        op.getCodigoBilleteraOrigen(),
                        op.getIdUsuarioDestino(),
                        op.getCodigoBilleteraDestino(), op.getMonto());
                break;
            default:
                exito = false;
        }

        if (exito) {
            op.marcarEjecutada();
            // Actualizar puntos
            double puntos = sistemaPuntos.acumularPuntos(op.getIdUsuarioOrigen(), op.getTipo(), op.getMonto());
            // Notificar al usuario
            sistemaAlertas.notificarOpEjecutada(op.getIdUsuarioOrigen(), op.getDescripcion(), op.getMonto());
            // Verificar saldo bajo tras retiros/transferencias
            if (!OperacionProgramada.RECARGA.equals(op.getTipo())) {
                double saldo = comp1.getSaldo(op.getIdUsuarioOrigen(), op.getCodigoBilleteraOrigen());
                String nombre = comp1.getNombreBilletera(op.getIdUsuarioOrigen(), op.getCodigoBilleteraOrigen());
                if (saldo >= 0) sistemaAlertas.verificarSaldoBajo(op.getIdUsuarioOrigen(), nombre, saldo);
            }
            System.out.println("[MOTOR2] [EXITO] " + op.getDescripcion() +
                    " | Puntos ganados: " + puntos);
        } else {
            String razon = determinarRazonFallo(op);
            op.marcarFallida(razon);
            sistemaAlertas.notificarOpFallida(op.getIdUsuarioOrigen(), op.getDescripcion(), razon);
            System.out.println("[MOTOR2] [FALLO] " + op.getDescripcion() + " | Razon: " + razon);
        }
    }

    /** Determina la razón probable del fallo de una operación. */
    private String determinarRazonFallo(OperacionProgramada op) {
        double saldo = comp1.getSaldo(op.getIdUsuarioOrigen(), op.getCodigoBilleteraOrigen());
        if (saldo >= 0 && saldo < op.getMonto()) {
            return "Saldo insuficiente ($" + String.format("%.2f", saldo) + " disponible)";
        }
        return "Error en la operacion financiera";
    }

    /**
     * Búsqueda y cancelación en la cola (implementación simple).
     * Reconstruye la cola excluyendo la operación cancelada.
     * O(n log n) - no óptimo pero académicamente válido.
     */
    private boolean buscarYCancelarEnCola(String idOperacion) {
        // Extraer todas, cancelar si coincide, reinsertar
        LinkedList<OperacionProgramada> temporales = new LinkedList<>();
        boolean encontrado = false;

        while (!colaOperaciones.estaVacia()) {
            OperacionProgramada op = colaOperaciones.extraerMin();
            if (op.getId().equals(idOperacion) && op.estaPendiente()) {
                op.cancelar();
                encontrado = true;
                sistemaAlertas.notificarOpCancelada(op.getIdUsuarioOrigen(), op.getDescripcion());
                historialEjecuciones.add(op);
                System.out.println("[MOTOR2] Operacion cancelada: " + op.getDescripcion());
            }
            temporales.add(op);
        }
        // Reinsertar (incluyendo la cancelada para que quede en historial)
        for (OperacionProgramada op : temporales) {
            if (!OperacionProgramada.CANCELADA.equals(op.getEstado())) {
                colaOperaciones.insertar(op, op.getFechaEjecucion());
            }
        }
        return encontrado;
    }
}
