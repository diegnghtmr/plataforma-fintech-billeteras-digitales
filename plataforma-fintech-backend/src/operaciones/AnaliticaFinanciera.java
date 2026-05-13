package operaciones;

import estructuras.GrafoTransferencias;
import modelos.Transaccion;
import modelos.Usuario;
import modelos.Billetera;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * AnaliticaFinanciera - Motor de análisis e inteligencia del sistema fintech.
 *
 * Responsabilidades:
 *   1. Construir y mantener el GrafoTransferencias a partir del historial.
 *   2. Responder consultas analíticas sobre billeteras, usuarios y transacciones.
 *   3. Detectar rutas frecuentes y ciclos en el grafo.
 *   4. Generar reportes e indicadores del sistema.
 *
 * Consultas disponibles (mínimo 5 según entregables):
 *   1. billetersaConMayorUso()         → billeteras con más transacciones
 *   2. usuariosMasActivos()            → usuarios con más movimientos
 *   3. montoMovilizadoPorRango()       → monto total en un rango de tiempo
 *   4. frecuenciaTransaccionesPorTipo() → conteo por tipo (RECARGA, RETIRO, etc.)
 *   5. topTransaccionesPorMonto()      → las N transacciones más grandes
 *   6. usuarioConMayorActividad()      → el usuario más activo en un período
 *   7. rutasFrecuentesEnGrafo()        → rutas con más transferencias
 *   8. ciclosEnGrafo()                 → ciclos de transferencias detectados
 *
 * Integración: Recibe referencia a OperacionesFinancieras para leer
 *              el historial global de transacciones.
 */
public class AnaliticaFinanciera {

    // ─── Interfaz de integración con Compañero 1 ─────────────────────────────

    /**
     * Interfaz para acceder a los datos del sistema del Compañero 1
     * sin acoplarse directamente a OperacionesFinancieras.
     */
    public interface FuenteDatos {
        LinkedList<Transaccion> obtenerHistorialGlobal();
        Usuario obtenerUsuario(String idUsuario);
        Billetera obtenerBilletera(String idUsuario, String codigoBilletera);
    }

    // ─── Atributos ────────────────────────────────────────────────────────────

    /** Grafo de transferencias entre usuarios. */
    private final GrafoTransferencias grafo;

    /** Fuente de datos del Compañero 1. */
    private final FuenteDatos fuente;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Crea el motor de analítica conectado al sistema del Compañero 1.
     *
     * @param fuente Implementación de acceso a datos del Comp.1
     */
    public AnaliticaFinanciera(FuenteDatos fuente) {
        this.fuente = fuente;
        this.grafo  = new GrafoTransferencias();
    }

    // ─── Construcción del grafo ───────────────────────────────────────────────

    /**
     * Reconstruye el grafo de transferencias a partir del historial global.
     * Debe llamarse antes de cualquier consulta sobre el grafo.
     * O(n) donde n = cantidad de transacciones.
     */
    public void reconstruirGrafo() {
        LinkedList<Transaccion> historial = fuente.obtenerHistorialGlobal();
        for (Transaccion t : historial) {
            if ("TRANSFERENCIA".equals(t.getTipo())) {
                // Solo agregar si es una transferencia externa (usuarios distintos)
                if (!t.getUsuarioOrigen().equals(t.getUsuarioDestino())) {
                    grafo.agregarArista(t.getUsuarioOrigen(), t.getUsuarioDestino(), t.getMonto());
                }
            }
        }
        System.out.println("[ANALITICA] Grafo reconstruido: " + grafo.cantidadNodos() +
                " nodos, " + grafo.cantidadAristas() + " aristas.");
    }

    // ─── CONSULTA 1: Billeteras con mayor uso ────────────────────────────────

    /**
     * Devuelve las billeteras con mayor cantidad de transacciones.
     * Cuenta cuántas veces aparece cada código de billetera en el historial.
     * O(n) donde n = cantidad de transacciones.
     *
     * @param top Cantidad de billeteras a retornar
     * @return Lista de strings "codigoBilletera: N transacciones", ordenada descendente
     */
    public LinkedList<String> billeterasConMayorUso(int top) {
        LinkedList<Transaccion> historial = fuente.obtenerHistorialGlobal();
        HashMap<String, Integer> conteo = new HashMap<>();

        for (Transaccion t : historial) {
            String bil = t.getBilleteraOrigen();
            conteo.put(bil, conteo.getOrDefault(bil, 0) + 1);
        }

        // Ordenar por conteo descendente (inserción simple — válido académicamente)
        LinkedList<Map.Entry<String, Integer>> lista = new LinkedList<>(conteo.entrySet());
        ordenarDescendente(lista);

        LinkedList<String> resultado = new LinkedList<>();
        int cuenta = 0;
        for (Map.Entry<String, Integer> e : lista) {
            if (cuenta >= top) break;
            resultado.add(e.getKey() + ": " + e.getValue() + " transacciones");
            cuenta++;
        }
        return resultado;
    }

    // ─── CONSULTA 2: Usuarios más activos ────────────────────────────────────

    /**
     * Devuelve los usuarios con más transacciones realizadas.
     * O(n)
     *
     * @param top Cantidad de usuarios a retornar
     * @return Lista de strings "idUsuario: N transacciones", ordenada descendente
     */
    public LinkedList<String> usuariosMasActivos(int top) {
        LinkedList<Transaccion> historial = fuente.obtenerHistorialGlobal();
        HashMap<String, Integer> conteo = new HashMap<>();

        for (Transaccion t : historial) {
            String usr = t.getUsuarioOrigen();
            conteo.put(usr, conteo.getOrDefault(usr, 0) + 1);
        }

        LinkedList<Map.Entry<String, Integer>> lista = new LinkedList<>(conteo.entrySet());
        ordenarDescendente(lista);

        LinkedList<String> resultado = new LinkedList<>();
        int cuenta = 0;
        for (Map.Entry<String, Integer> e : lista) {
            if (cuenta >= top) break;
            resultado.add(e.getKey() + ": " + e.getValue() + " transacciones");
            cuenta++;
        }
        return resultado;
    }

    // ─── CONSULTA 3: Monto movilizado por rango de tiempo ────────────────────

    /**
     * Calcula el monto total movilizado en un rango de tiempo dado.
     * O(n)
     *
     * @param timestampInicio Inicio del rango (Unix ms)
     * @param timestampFin    Fin del rango (Unix ms)
     * @return Monto total de todas las transacciones en el rango
     */
    public double montoMovilizadoPorRango(long timestampInicio, long timestampFin) {
        LinkedList<Transaccion> historial = fuente.obtenerHistorialGlobal();
        double total = 0;
        int cantidad = 0;

        for (Transaccion t : historial) {
            if (t.getTimestamp() >= timestampInicio && t.getTimestamp() <= timestampFin) {
                if (!"REVERTIDA".equals(t.getEstado())) {
                    total += t.getMonto();
                    cantidad++;
                }
            }
        }

        System.out.println("[ANALITICA] Rango: " + cantidad + " transacciones, $" +
                String.format("%.2f", total) + " movilizados.");
        return total;
    }

    /**
     * Devuelve todas las transacciones en un rango de tiempo.
     * O(n)
     *
     * @param timestampInicio Inicio del rango (Unix ms)
     * @param timestampFin    Fin del rango (Unix ms)
     * @return Lista de transacciones en el rango
     */
    public LinkedList<Transaccion> transaccionesEnRango(long timestampInicio, long timestampFin) {
        LinkedList<Transaccion> historial = fuente.obtenerHistorialGlobal();
        LinkedList<Transaccion> resultado = new LinkedList<>();

        for (Transaccion t : historial) {
            if (t.getTimestamp() >= timestampInicio && t.getTimestamp() <= timestampFin) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    // ─── CONSULTA 4: Frecuencia de transacciones por tipo ────────────────────

    /**
     * Cuenta cuántas transacciones hay de cada tipo.
     * O(n)
     *
     * @return Mapa tipo → cantidad
     */
    public HashMap<String, Integer> frecuenciaTransaccionesPorTipo() {
        LinkedList<Transaccion> historial = fuente.obtenerHistorialGlobal();
        HashMap<String, Integer> frecuencia = new HashMap<>();

        for (Transaccion t : historial) {
            String tipo = t.getTipo();
            frecuencia.put(tipo, frecuencia.getOrDefault(tipo, 0) + 1);
        }
        return frecuencia;
    }

    // ─── CONSULTA 5: Top transacciones por monto ─────────────────────────────

    /**
     * Devuelve las N transacciones de mayor valor en el sistema.
     * Usa ordenamiento por inserción (O(n²)) — válido académicamente para demostrar
     * el uso de estructuras ordenadas.
     * O(n²)
     *
     * @param top Cantidad de transacciones a retornar
     * @return Lista de transacciones ordenadas de mayor a menor monto
     */
    public LinkedList<Transaccion> topTransaccionesPorMonto(int top) {
        LinkedList<Transaccion> historial = new LinkedList<>(fuente.obtenerHistorialGlobal());

        // Ordenamiento por inserción descendente (por monto)
        ordenarTransaccionesPorMontoDesc(historial);

        LinkedList<Transaccion> resultado = new LinkedList<>();
        int cuenta = 0;
        for (Transaccion t : historial) {
            if (cuenta >= top) break;
            resultado.add(t);
            cuenta++;
        }
        return resultado;
    }

    // ─── CONSULTA 6: Usuario con mayor actividad en un período ───────────────

    /**
     * Encuentra el usuario con más transacciones en un período de tiempo.
     * O(n)
     *
     * @param timestampInicio Inicio del período (Unix ms)
     * @param timestampFin    Fin del período (Unix ms)
     * @return ID del usuario más activo, o null si no hay transacciones
     */
    public String usuarioConMayorActividad(long timestampInicio, long timestampFin) {
        LinkedList<Transaccion> rango = transaccionesEnRango(timestampInicio, timestampFin);
        HashMap<String, Integer> conteo = new HashMap<>();

        for (Transaccion t : rango) {
            String usr = t.getUsuarioOrigen();
            conteo.put(usr, conteo.getOrDefault(usr, 0) + 1);
        }

        String masActivo = null;
        int maxConteo = 0;
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            if (e.getValue() > maxConteo) {
                maxConteo = e.getValue();
                masActivo = e.getKey();
            }
        }
        return masActivo;
    }

    // ─── CONSULTA 7: Rutas frecuentes en el grafo ────────────────────────────

    /**
     * Devuelve las rutas de transferencia más frecuentes entre usuarios.
     * Delega en el GrafoTransferencias.
     * O(V + E)
     *
     * @param minTransferencias Mínimo de transferencias para ser "frecuente"
     * @return Lista de rutas frecuentes con sus estadísticas
     */
    public LinkedList<String> rutasFrecuentesEnGrafo(int minTransferencias) {
        return grafo.rutasFrecuentes(minTransferencias);
    }

    // ─── CONSULTA 8: Ciclos en el grafo ──────────────────────────────────────

    /**
     * Detecta y devuelve todos los ciclos de transferencias en el grafo.
     * Un ciclo indica que el dinero circula entre un grupo de usuarios.
     * O(V + E)
     *
     * @return Lista de ciclos detectados; cada ciclo es una lista de IDs de usuario
     */
    public LinkedList<LinkedList<String>> ciclosEnGrafo() {
        return grafo.encontrarCiclos();
    }

    /**
     * Verifica si existe algún ciclo en el grafo.
     * O(V + E)
     *
     * @return true si hay al menos un ciclo
     */
    public boolean hayCirculacionDinero() {
        return grafo.tieneCiclos();
    }

    // ─── Reporte general ─────────────────────────────────────────────────────

    /**
     * Genera un reporte completo del sistema.
     * Combina todas las consultas analíticas en un texto legible.
     *
     * @return String con el reporte completo
     */
    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE ANALITICO DEL SISTEMA ===\n\n");

        // Total de transacciones
        LinkedList<Transaccion> historial = fuente.obtenerHistorialGlobal();
        sb.append("Total de transacciones: ").append(historial.size()).append("\n");

        // Frecuencia por tipo
        sb.append("\nTransacciones por tipo:\n");
        HashMap<String, Integer> freq = frecuenciaTransaccionesPorTipo();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }

        // Top 3 usuarios más activos
        sb.append("\nTop 3 usuarios mas activos:\n");
        for (String u : usuariosMasActivos(3)) {
            sb.append("  ").append(u).append("\n");
        }

        // Top 3 billeteras con mayor uso
        sb.append("\nTop 3 billeteras con mayor uso:\n");
        for (String b : billeterasConMayorUso(3)) {
            sb.append("  ").append(b).append("\n");
        }

        // Top 3 transacciones por monto
        sb.append("\nTop 3 transacciones por monto:\n");
        for (Transaccion t : topTransaccionesPorMonto(3)) {
            sb.append("  ").append(t.getTipo()).append(" $")
              .append(String.format("%.2f", t.getMonto()))
              .append(" | ").append(t.getUsuarioOrigen()).append("\n");
        }

        // Grafo
        sb.append("\nGrafo de transferencias:\n");
        sb.append("  Nodos (usuarios): ").append(grafo.cantidadNodos()).append("\n");
        sb.append("  Aristas (rutas):  ").append(grafo.cantidadAristas()).append("\n");
        sb.append("  Hay circulacion de dinero (ciclos): ").append(hayCirculacionDinero()).append("\n");

        LinkedList<LinkedList<String>> ciclos = ciclosEnGrafo();
        if (!ciclos.isEmpty()) {
            sb.append("  Ciclos detectados: ").append(ciclos.size()).append("\n");
            for (LinkedList<String> ciclo : ciclos) {
                sb.append("    ").append(ciclo).append("\n");
            }
        }

        return sb.toString();
    }

    /** @return Referencia al grafo de transferencias */
    public GrafoTransferencias getGrafo() { return grafo; }

    // ─── Auxiliares ───────────────────────────────────────────────────────────

    /**
     * Ordenamiento por inserción descendente sobre lista de entradas (String, Integer).
     * O(n²) — se usa por requisito académico de estructuras propias.
     */
    private void ordenarDescendente(LinkedList<Map.Entry<String, Integer>> lista) {
        Object[] arr = lista.toArray();
        for (int i = 1; i < arr.length; i++) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, Integer> clave = (Map.Entry<String, Integer>) arr[i];
            int j = i - 1;
            while (j >= 0) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, Integer> prev = (Map.Entry<String, Integer>) arr[j];
                if (prev.getValue() > clave.getValue()) break;
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = clave;
        }
        lista.clear();
        for (Object o : arr) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, Integer> e = (Map.Entry<String, Integer>) o;
            lista.add(e);
        }
    }

    /**
     * Ordenamiento por inserción descendente sobre lista de transacciones (por monto).
     * O(n²)
     */
    private void ordenarTransaccionesPorMontoDesc(LinkedList<Transaccion> lista) {
        Object[] arr = lista.toArray();
        for (int i = 1; i < arr.length; i++) {
            Transaccion clave = (Transaccion) arr[i];
            int j = i - 1;
            while (j >= 0 && ((Transaccion) arr[j]).getMonto() < clave.getMonto()) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = clave;
        }
        lista.clear();
        for (Object o : arr) lista.add((Transaccion) o);
    }
}
