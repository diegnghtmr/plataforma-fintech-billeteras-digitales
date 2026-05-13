package operaciones;

import estructuras.ArbolBST;
import modelos.Notificacion;
import estructuras.ColaSimple;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * SistemaPuntos - Motor de gestión de puntos y niveles de fidelización.
 *
 * Responsabilidades:
 *   1. Acumular puntos por operaciones financieras.
 *   2. Descontar puntos al revertir operaciones.
 *   3. Recalcular el nivel del usuario automáticamente.
 *   4. Mantener un ArbolBST de usuarios clasificados por puntos.
 *   5. Generar notificaciones de ascenso/descenso de nivel.
 *   6. Permitir búsqueda por rango de puntos (via BST).
 *
 * Integración con Compañero 1:
 *   - Recibe llamadas desde MotorOperaciones2 tras cada transacción.
 *   - La actualización de puntos en Usuario.java (del Comp.1) la hace
 *     MotorOperaciones2 directamente; SistemaPuntos mantiene el BST
 *     sincronizado y emite notificaciones.
 *
 * Política de puntos (coherente con Compañero 1):
 *   - Recarga:              monto / 100          → 1 punto cada 100
 *   - Retiro:               (monto / 100) * 2    → 2 puntos cada 100
 *   - Transferencia:        (monto / 100) * 3    → 3 puntos cada 100
 *   - Recepción de fondos:  0 puntos
 *
 * Niveles:
 *   - Bronce:  0    – 500  puntos
 *   - Plata:   501  – 1000 puntos
 *   - Oro:     1001 – 5000 puntos
 *   - Platino: 5001+        puntos
 *
 * Hitos de puntos (generan notificación):
 *   100, 500, 1000, 2000, 5000, 10000 puntos.
 */
public class SistemaPuntos {

    // ─── Constantes de negocio ────────────────────────────────────────────────
    public static final String NIVEL_BRONCE  = "Bronce";
    public static final String NIVEL_PLATA   = "Plata";
    public static final String NIVEL_ORO     = "Oro";
    public static final String NIVEL_PLATINO = "Platino";

    private static final double UMBRAL_PLATA   =   501.0;
    private static final double UMBRAL_ORO     =  1001.0;
    private static final double UMBRAL_PLATINO =  5001.0;

    private static final double[] HITOS = {100, 500, 1000, 2000, 5000, 10000};

    // ─── Estructuras de datos ─────────────────────────────────────────────────

    /**
     * ArbolBST clasificado por puntos (Double → idUsuario).
     * Permite búsqueda por rango O(k + log n).
     * Al actualizar puntos de un usuario, se elimina la entrada anterior
     * y se inserta la nueva.
     */
    private ArbolBST<Double, String> arbolPorPuntos;

    /**
     * Mapa auxiliar: idUsuario → puntos actuales.
     * Necesario para poder eliminar del BST la clave anterior antes de insertar.
     */
    private HashMap<String, Double> puntosActuales;

    /**
     * Mapa auxiliar: idUsuario → nivel actual.
     * Para detectar cambios de nivel y generar notificaciones.
     */
    private HashMap<String, String> nivelesActuales;

    /**
     * Mapa auxiliar: idUsuario → hito más alto ya notificado.
     * Para no re-notificar el mismo hito.
     */
    private HashMap<String, Double> hitoNotificado;

    /**
     * Cola de notificaciones por usuario: idUsuario → ColaSimple<Notificacion>.
     * Compartida con SistemaAlertas (referencia inyectada).
     */
    private HashMap<String, ColaSimple<Notificacion>> colasNotificaciones;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Crea el motor de puntos conectado a las colas de notificaciones.
     *
     * @param colasNotificaciones Mapa compartido con SistemaAlertas
     */
    public SistemaPuntos(HashMap<String, ColaSimple<Notificacion>> colasNotificaciones) {
        this.arbolPorPuntos       = new ArbolBST<>();
        this.puntosActuales       = new HashMap<>();
        this.nivelesActuales      = new HashMap<>();
        this.hitoNotificado       = new HashMap<>();
        this.colasNotificaciones  = colasNotificaciones;
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Registra un usuario en el sistema de puntos (puntos iniciales = 0).
     * Debe llamarse al crear un usuario nuevo.
     *
     * @param idUsuario   ID del usuario
     * @param nombre      Nombre para notificación de bienvenida
     */
    public void registrarUsuario(String idUsuario, String nombre) {
        puntosActuales.put(idUsuario, 0.0);
        nivelesActuales.put(idUsuario, NIVEL_BRONCE);
        hitoNotificado.put(idUsuario, 0.0);
        arbolPorPuntos.insertar(0.0 + offsetUnico(idUsuario), idUsuario);
        encolarNotificacion(idUsuario, Notificacion.bienvenida(idUsuario, nombre));
    }

    /**
     * Calcula los puntos correspondientes a una operación y los acumula.
     *
     * @param idUsuario ID del usuario que recibe los puntos
     * @param tipoOp    Tipo de operación: "RECARGA", "RETIRO", "TRANSFERENCIA_INTERNA",
     *                  "TRANSFERENCIA_EXTERNA", "RECIBIDA"
     * @param monto     Monto de la operación
     * @return Puntos acumulados en esta operación
     */
    public double acumularPuntos(String idUsuario, String tipoOp, double monto) {
        double puntos = calcularPuntos(tipoOp, monto);
        if (puntos <= 0) return 0;
        actualizarPuntos(idUsuario, puntos);
        return puntos;
    }

    /**
     * Descuenta puntos al revertir una operación.
     * Los puntos no pueden quedar en negativo (mínimo 0).
     *
     * @param idUsuario ID del usuario
     * @param tipoOp    Tipo de operación revertida
     * @param monto     Monto de la operación revertida
     * @return Puntos descontados (valor positivo)
     */
    public double descontarPuntos(String idUsuario, String tipoOp, double monto) {
        double puntos = calcularPuntos(tipoOp, monto);
        if (puntos <= 0) return 0;
        double actuales = getPuntos(idUsuario);
        double descuento = Math.min(puntos, actuales); // No bajar de 0
        actualizarPuntos(idUsuario, -descuento);
        return descuento;
    }

    /**
     * Recalcula y sincroniza los puntos de un usuario (útil tras cargar datos externos).
     * Equivale a poner exactamente los puntos indicados.
     *
     * @param idUsuario    ID del usuario
     * @param puntosTotales Puntos totales a establecer
     */
    public void sincronizarPuntos(String idUsuario, double puntosTotales) {
        double anteriores = getPuntos(idUsuario);
        double delta = puntosTotales - anteriores;
        actualizarPuntos(idUsuario, delta);
    }

    /**
     * Devuelve los puntos actuales de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Puntos acumulados, o 0 si no registrado
     */
    public double getPuntos(String idUsuario) {
        return puntosActuales.getOrDefault(idUsuario, 0.0);
    }

    /**
     * Devuelve el nivel actual de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return Nivel ("Bronce", "Plata", "Oro", "Platino"), o "Bronce" si no registrado
     */
    public String getNivel(String idUsuario) {
        return nivelesActuales.getOrDefault(idUsuario, NIVEL_BRONCE);
    }

    /**
     * Busca usuarios cuyo puntaje esté entre min y max (inclusive).
     * Usa el ArbolBST para búsqueda eficiente por rango.
     * O(k + log n)
     *
     * @param min Puntos mínimos
     * @param max Puntos máximos
     * @return Lista de IDs de usuarios en ese rango
     */
    public LinkedList<String> buscarUsuariosPorRangoPuntos(double min, double max) {
        return arbolPorPuntos.buscarPorRango(min, max);
    }

    /**
     * Devuelve todos los usuarios ordenados de menor a mayor puntaje.
     * Usa recorrido en orden del BST. O(n)
     *
     * @return Lista de IDs de usuarios, de menor a mayor puntaje
     */
    public LinkedList<String> rankingAscendente() {
        return arbolPorPuntos.enOrden();
    }

    /**
     * Devuelve todos los usuarios ordenados de mayor a menor puntaje.
     * Usa recorrido en orden inverso del BST. O(n)
     *
     * @return Lista de IDs de usuarios, de mayor a menor puntaje
     */
    public LinkedList<String> rankingDescendente() {
        return arbolPorPuntos.enOrdenInverso();
    }

    /**
     * Calcula la cantidad de puntos necesarios para subir de nivel.
     *
     * @param idUsuario ID del usuario
     * @return Puntos faltantes para el siguiente nivel, o 0 si ya es Platino
     */
    public double puntosParaSiguienteNivel(String idUsuario) {
        double puntos = getPuntos(idUsuario);
        if (puntos < UMBRAL_PLATA)   return UMBRAL_PLATA - puntos;
        if (puntos < UMBRAL_ORO)     return UMBRAL_ORO - puntos;
        if (puntos < UMBRAL_PLATINO) return UMBRAL_PLATINO - puntos;
        return 0; // Ya es Platino
    }

    /**
     * Calcula cuántos puntos genera una operación según tipo y monto.
     * Fórmula coherente con Compañero 1.
     *
     * @param tipoOp Tipo de operación
     * @param monto  Monto de la operación
     * @return Puntos a generar (puede ser 0)
     */
    public static double calcularPuntos(String tipoOp, double monto) {
        switch (tipoOp.toUpperCase()) {
            case "RECARGA":                return Math.floor(monto / 100.0);
            case "RETIRO":                 return Math.floor(monto / 100.0) * 2;
            case "TRANSFERENCIA_INTERNA":
            case "TRANSFERENCIA_EXTERNA":
            case "TRANSFERENCIA":          return Math.floor(monto / 100.0) * 3;
            default:                       return 0; // RECIBIDA, etc.
        }
    }

    /**
     * Calcula el nivel correspondiente a una cantidad de puntos.
     *
     * @param puntos Puntos acumulados
     * @return Nivel de fidelización
     */
    public static String calcularNivel(double puntos) {
        if (puntos >= UMBRAL_PLATINO) return NIVEL_PLATINO;
        if (puntos >= UMBRAL_ORO)     return NIVEL_ORO;
        if (puntos >= UMBRAL_PLATA)   return NIVEL_PLATA;
        return NIVEL_BRONCE;
    }

    /** @return Referencia al ArbolBST (para inspección externa/pruebas) */
    public ArbolBST<Double, String> getArbol() { return arbolPorPuntos; }

    // ─── Lógica interna ───────────────────────────────────────────────────────

    /**
     * Actualiza los puntos de un usuario en delta (positivo o negativo),
     * sincroniza el BST y verifica cambios de nivel.
     */
    private void actualizarPuntos(String idUsuario, double delta) {
        if (!puntosActuales.containsKey(idUsuario)) return;

        double anterior = puntosActuales.get(idUsuario);
        double nuevo    = Math.max(0, anterior + delta);
        puntosActuales.put(idUsuario, nuevo);

        // Actualizar BST: eliminar clave anterior, insertar nueva
        arbolPorPuntos.eliminar(anterior + offsetUnico(idUsuario));
        arbolPorPuntos.insertar(nuevo    + offsetUnico(idUsuario), idUsuario);

        // Verificar cambio de nivel
        verificarCambioNivel(idUsuario, anterior, nuevo);

        // Verificar hitos
        verificarHitos(idUsuario, anterior, nuevo);
    }

    /**
     * Detecta si el usuario cambió de nivel y emite notificación.
     */
    private void verificarCambioNivel(String idUsuario, double puntosAnteriores, double puntosNuevos) {
        String nivelAnterior = calcularNivel(puntosAnteriores);
        String nivelNuevo    = calcularNivel(puntosNuevos);

        if (!nivelAnterior.equals(nivelNuevo)) {
            nivelesActuales.put(idUsuario, nivelNuevo);
            Notificacion notif;
            if (esAscenso(nivelAnterior, nivelNuevo)) {
                notif = Notificacion.ascensoNivel(idUsuario, nivelAnterior, nivelNuevo, puntosNuevos);
                System.out.println("[NIVEL] " + idUsuario + " ascendio de " + nivelAnterior + " a " + nivelNuevo);
            } else {
                notif = Notificacion.descensoNivel(idUsuario, nivelAnterior, nivelNuevo, puntosNuevos);
                System.out.println("[NIVEL] " + idUsuario + " descendio de " + nivelAnterior + " a " + nivelNuevo);
            }
            encolarNotificacion(idUsuario, notif);
        }
    }

    /**
     * Detecta si el usuario superó un hito de puntos y emite notificación.
     */
    private void verificarHitos(String idUsuario, double anterior, double nuevo) {
        double ultimoHito = hitoNotificado.getOrDefault(idUsuario, 0.0);
        for (double hito : HITOS) {
            if (nuevo >= hito && anterior < hito && hito > ultimoHito) {
                hitoNotificado.put(idUsuario, hito);
                encolarNotificacion(idUsuario, Notificacion.hitoPuntos(idUsuario, nuevo));
                System.out.println("[HITO] " + idUsuario + " alcanzo " + hito + " puntos");
            }
        }
    }

    /** Determina si el cambio de nivel es un ascenso. */
    private boolean esAscenso(String anterior, String nuevo) {
        return nivelANumero(nuevo) > nivelANumero(anterior);
    }

    private int nivelANumero(String nivel) {
        switch (nivel) {
            case NIVEL_BRONCE:  return 1;
            case NIVEL_PLATA:   return 2;
            case NIVEL_ORO:     return 3;
            case NIVEL_PLATINO: return 4;
            default:            return 0;
        }
    }

    /**
     * Pequeño offset único por usuario para evitar colisiones en el BST
     * cuando dos usuarios tienen exactamente los mismos puntos.
     * Se basa en el hashCode del ID (muy pequeño, no afecta el nivel).
     */
    private double offsetUnico(String idUsuario) {
        return (idUsuario.hashCode() % 1000) * 0.000001;
    }

    /** Agrega una notificación a la cola del usuario. */
    private void encolarNotificacion(String idUsuario, Notificacion notif) {
        colasNotificaciones.computeIfAbsent(idUsuario, k -> new ColaSimple<>()).encolar(notif);
    }
}
