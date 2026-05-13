package estructuras;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * GrafoTransferencias - Grafo dirigido de transferencias entre usuarios.
 *
 * Propósito:
 *   - Representar quién le transfirió dinero a quién.
 *   - Detectar rutas frecuentes de movimiento de dinero.
 *   - Encontrar ciclos (transferencias circulares sospechosas).
 *   - Analizar relaciones entre billeteras y usuarios.
 *
 * Implementación: Lista de adyacencia con HashMap.
 *   - Cada nodo es un ID de usuario (String).
 *   - Cada arista dirigida (origen → destino) tiene peso (monto acumulado)
 *     y conteo de transferencias.
 *
 * Complejidad:
 *   - agregarArista():     O(1) promedio
 *   - BFS():               O(V + E)
 *   - DFS():               O(V + E)
 *   - detectarCiclos():    O(V + E)
 *   - rutasFrecuentes():   O(V + E)
 */
public class GrafoTransferencias {

    // ─── Arista interna ───────────────────────────────────────────────────────
    /**
     * Representa una transferencia dirigida entre dos usuarios.
     * Acumula el monto total y el conteo de transferencias.
     */
    public static class Arista {
        private final String destino;
        private double montoTotal;
        private int conteoTransferencias;

        public Arista(String destino, double montoInicial) {
            this.destino = destino;
            this.montoTotal = montoInicial;
            this.conteoTransferencias = 1;
        }

        /** Agrega otra transferencia a esta arista. */
        public void agregarTransferencia(double monto) {
            this.montoTotal += monto;
            this.conteoTransferencias++;
        }

        public String getDestino()             { return destino; }
        public double getMontoTotal()          { return montoTotal; }
        public int    getConteoTransferencias(){ return conteoTransferencias; }

        @Override
        public String toString() {
            return String.format("→%s ($%.2f, %dx)", destino, montoTotal, conteoTransferencias);
        }
    }

    // ─── Atributos ────────────────────────────────────────────────────────────

    /**
     * Lista de adyacencia: idUsuario → lista de aristas salientes.
     * Estructura principal del grafo.
     */
    private final HashMap<String, LinkedList<Arista>> listaAdyacencia;

    /** Cantidad de aristas (transferencias únicas origen→destino). */
    private int cantidadAristas;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /** Crea un grafo de transferencias vacío. */
    public GrafoTransferencias() {
        this.listaAdyacencia = new HashMap<>();
        this.cantidadAristas = 0;
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Agrega un nodo (usuario) al grafo si no existe.
     * O(1) promedio.
     *
     * @param idUsuario ID del usuario
     */
    public void agregarNodo(String idUsuario) {
        listaAdyacencia.putIfAbsent(idUsuario, new LinkedList<>());
    }

    /**
     * Registra una transferencia dirigida origen → destino.
     * Si ya existe la arista, acumula el monto y el conteo.
     * O(grado del nodo origen) — normalmente pequeño.
     *
     * @param origen   ID del usuario que envía
     * @param destino  ID del usuario que recibe
     * @param monto    Monto de la transferencia
     */
    public void agregarArista(String origen, String destino, double monto) {
        agregarNodo(origen);
        agregarNodo(destino);

        LinkedList<Arista> aristas = listaAdyacencia.get(origen);

        // Buscar si ya existe arista hacia ese destino
        for (Arista a : aristas) {
            if (a.getDestino().equals(destino)) {
                a.agregarTransferencia(monto);
                return;
            }
        }

        // Nueva arista
        aristas.add(new Arista(destino, monto));
        cantidadAristas++;
    }

    /**
     * Busca la arista entre origen y destino.
     * O(grado del nodo origen).
     *
     * @return Arista si existe, null si no
     */
    public Arista obtenerArista(String origen, String destino) {
        LinkedList<Arista> aristas = listaAdyacencia.get(origen);
        if (aristas == null) return null;
        for (Arista a : aristas) {
            if (a.getDestino().equals(destino)) return a;
        }
        return null;
    }

    /**
     * Recorrido BFS (Breadth-First Search) desde un nodo origen.
     * Recorre por niveles: primero los vecinos directos, luego los de segundo nivel, etc.
     * O(V + E)
     *
     * Uso: encontrar todos los usuarios alcanzables desde un origen,
     *      detectar rutas de propagación de fondos.
     *
     * @param origen ID del usuario de inicio
     * @return Lista de IDs en orden BFS (sin incluir el origen)
     */
    public LinkedList<String> bfs(String origen) {
        LinkedList<String> resultado = new LinkedList<>();
        if (!listaAdyacencia.containsKey(origen)) return resultado;

        HashSet<String> visitados = new HashSet<>();
        LinkedList<String> cola = new LinkedList<>(); // Cola FIFO para BFS

        visitados.add(origen);
        cola.add(origen);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            LinkedList<Arista> vecinos = listaAdyacencia.get(actual);
            if (vecinos == null) continue;

            for (Arista arista : vecinos) {
                String vecino = arista.getDestino();
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    cola.add(vecino);
                    resultado.add(vecino);
                }
            }
        }
        return resultado;
    }

    /**
     * Recorrido DFS (Depth-First Search) desde un nodo origen.
     * Recorre siguiendo cada camino hasta el fondo antes de retroceder.
     * O(V + E) — implementado recursivamente.
     *
     * Uso: explorar rutas completas de transferencias, base para detección de ciclos.
     *
     * @param origen ID del usuario de inicio
     * @return Lista de IDs en orden DFS (sin incluir el origen)
     */
    public LinkedList<String> dfs(String origen) {
        LinkedList<String> resultado = new LinkedList<>();
        HashSet<String> visitados = new HashSet<>();
        visitados.add(origen);
        dfsRecursivo(origen, visitados, resultado);
        return resultado;
    }

    /**
     * Detecta si existe algún ciclo en el grafo.
     * Un ciclo significa que hay una cadena de transferencias que forma un circuito cerrado.
     * O(V + E) — DFS con coloración de nodos (blanco/gris/negro).
     *
     * Uso: detectar transferencias circulares que pueden indicar lavado de dinero.
     *
     * @return true si existe al menos un ciclo
     */
    public boolean tieneCiclos() {
        HashSet<String> visitados  = new HashSet<>();
        HashSet<String> enRecurso  = new HashSet<>(); // Nodos en la pila de recursión actual

        for (String nodo : listaAdyacencia.keySet()) {
            if (!visitados.contains(nodo)) {
                if (detectarCicloRecursivo(nodo, visitados, enRecurso)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Encuentra todos los ciclos del grafo y los devuelve como listas de nodos.
     * O(V + E)
     *
     * @return Lista de ciclos, cada ciclo es una lista de IDs de usuario
     */
    public LinkedList<LinkedList<String>> encontrarCiclos() {
        LinkedList<LinkedList<String>> ciclos = new LinkedList<>();
        HashSet<String> visitados = new HashSet<>();
        HashSet<String> enRecurso = new HashSet<>();
        LinkedList<String> caminoActual = new LinkedList<>();

        for (String nodo : listaAdyacencia.keySet()) {
            if (!visitados.contains(nodo)) {
                encontrarCiclosRecursivo(nodo, visitados, enRecurso, caminoActual, ciclos);
            }
        }
        return ciclos;
    }

    /**
     * Encuentra las rutas frecuentes de dinero: aristas con más transferencias.
     * O(V + E)
     *
     * @param minTransferencias Mínimo de transferencias para considerar "frecuente"
     * @return Lista de aristas frecuentes ordenadas de mayor a menor conteo
     */
    public LinkedList<String> rutasFrecuentes(int minTransferencias) {
        LinkedList<String> rutas = new LinkedList<>();

        for (String origen : listaAdyacencia.keySet()) {
            for (Arista arista : listaAdyacencia.get(origen)) {
                if (arista.getConteoTransferencias() >= minTransferencias) {
                    rutas.add(String.format("%s → %s (%dx, $%.2f total)",
                            origen, arista.getDestino(),
                            arista.getConteoTransferencias(),
                            arista.getMontoTotal()));
                }
            }
        }
        return rutas;
    }

    /**
     * Encuentra todas las rutas entre dos usuarios usando DFS.
     * O(V! en el peor caso) — solo usar con grafos pequeños.
     *
     * @param origen  ID del usuario origen
     * @param destino ID del usuario destino
     * @return Lista de rutas, cada ruta es una lista de IDs
     */
    public LinkedList<LinkedList<String>> encontrarRutas(String origen, String destino) {
        LinkedList<LinkedList<String>> todasRutas = new LinkedList<>();
        LinkedList<String> rutaActual = new LinkedList<>();
        HashSet<String> visitados = new HashSet<>();

        rutaActual.add(origen);
        visitados.add(origen);
        encontrarRutasRecursivo(origen, destino, visitados, rutaActual, todasRutas);
        return todasRutas;
    }

    /**
     * Devuelve los vecinos directos (destinos) de un nodo.
     * O(grado del nodo)
     *
     * @param idUsuario ID del usuario
     * @return Lista de IDs de usuarios a los que ha transferido
     */
    public LinkedList<String> vecinosSalientes(String idUsuario) {
        LinkedList<String> vecinos = new LinkedList<>();
        LinkedList<Arista> aristas = listaAdyacencia.get(idUsuario);
        if (aristas == null) return vecinos;
        for (Arista a : aristas) vecinos.add(a.getDestino());
        return vecinos;
    }

    /**
     * Devuelve los usuarios que han transferido a un nodo dado (vecinos entrantes).
     * O(V + E) — requiere recorrer todo el grafo.
     *
     * @param idUsuario ID del usuario destino
     * @return Lista de IDs de usuarios que le han transferido
     */
    public LinkedList<String> vecinosEntrantes(String idUsuario) {
        LinkedList<String> entrantes = new LinkedList<>();
        for (String origen : listaAdyacencia.keySet()) {
            for (Arista a : listaAdyacencia.get(origen)) {
                if (a.getDestino().equals(idUsuario)) {
                    entrantes.add(origen);
                    break;
                }
            }
        }
        return entrantes;
    }

    /**
     * Calcula el grado de salida de un nodo (cuántos usuarios distintos recibieron de él).
     * O(1)
     *
     * @param idUsuario ID del usuario
     * @return Grado de salida
     */
    public int gradoSalida(String idUsuario) {
        LinkedList<Arista> aristas = listaAdyacencia.get(idUsuario);
        return aristas == null ? 0 : aristas.size();
    }

    /**
     * Calcula el grado de entrada de un nodo (cuántos usuarios distintos le enviaron).
     * O(V + E)
     *
     * @param idUsuario ID del usuario
     * @return Grado de entrada
     */
    public int gradoEntrada(String idUsuario) {
        return vecinosEntrantes(idUsuario).size();
    }

    /** @return Cantidad de nodos (usuarios) en el grafo */
    public int cantidadNodos() { return listaAdyacencia.size(); }

    /** @return Cantidad de aristas únicas (pares origen→destino) */
    public int cantidadAristas() { return cantidadAristas; }

    /** @return true si el grafo no tiene nodos */
    public boolean estaVacio() { return listaAdyacencia.isEmpty(); }

    /** @return true si el nodo existe en el grafo */
    public boolean contieneNodo(String idUsuario) { return listaAdyacencia.containsKey(idUsuario); }

    /** @return Conjunto de todos los IDs de usuario en el grafo */
    public HashSet<String> obtenerNodos() { return new HashSet<>(listaAdyacencia.keySet()); }

    /** @return Lista de aristas salientes de un nodo */
    public LinkedList<Arista> obtenerAristas(String idUsuario) {
        LinkedList<Arista> aristas = listaAdyacencia.get(idUsuario);
        return aristas != null ? new LinkedList<>(aristas) : new LinkedList<>();
    }

    // ─── Métodos recursivos ───────────────────────────────────────────────────

    /**
     * DFS recursivo auxiliar.
     * Caso base: nodo sin vecinos no visitados.
     * Caso recursivo: visitar cada vecino no visitado.
     */
    private void dfsRecursivo(String nodo, HashSet<String> visitados, LinkedList<String> resultado) {
        LinkedList<Arista> aristas = listaAdyacencia.get(nodo);
        if (aristas == null) return;                        // Caso base

        for (Arista arista : aristas) {
            String vecino = arista.getDestino();
            if (!visitados.contains(vecino)) {
                visitados.add(vecino);
                resultado.add(vecino);
                dfsRecursivo(vecino, visitados, resultado); // Recursión
            }
        }
    }

    /**
     * Detección de ciclos recursiva (coloración DFS).
     * Caso base: nodo ya visitado completamente (negro) → no hay ciclo desde él.
     * Caso recursivo: si encontramos un nodo en la pila actual (gris) → ciclo.
     */
    private boolean detectarCicloRecursivo(String nodo, HashSet<String> visitados,
                                            HashSet<String> enRecurso) {
        visitados.add(nodo);
        enRecurso.add(nodo);

        LinkedList<Arista> aristas = listaAdyacencia.get(nodo);
        if (aristas != null) {
            for (Arista arista : aristas) {
                String vecino = arista.getDestino();
                if (!visitados.contains(vecino)) {
                    if (detectarCicloRecursivo(vecino, visitados, enRecurso)) // Recursión
                        return true;
                } else if (enRecurso.contains(vecino)) {
                    return true;                            // Ciclo encontrado
                }
            }
        }

        enRecurso.remove(nodo);                            // Nodo procesado completamente
        return false;
    }

    /**
     * Encontrar ciclos completos recursivamente.
     * Registra el camino actual y cuando detecta un nodo en la pila, extrae el ciclo.
     */
    private void encontrarCiclosRecursivo(String nodo, HashSet<String> visitados,
                                           HashSet<String> enRecurso,
                                           LinkedList<String> caminoActual,
                                           LinkedList<LinkedList<String>> ciclos) {
        visitados.add(nodo);
        enRecurso.add(nodo);
        caminoActual.add(nodo);

        LinkedList<Arista> aristas = listaAdyacencia.get(nodo);
        if (aristas != null) {
            for (Arista arista : aristas) {
                String vecino = arista.getDestino();
                if (!visitados.contains(vecino)) {
                    encontrarCiclosRecursivo(vecino, visitados, enRecurso, caminoActual, ciclos);
                } else if (enRecurso.contains(vecino)) {
                    // Extraer el ciclo del camino actual
                    LinkedList<String> ciclo = new LinkedList<>();
                    boolean enCiclo = false;
                    for (String n : caminoActual) {
                        if (n.equals(vecino)) enCiclo = true;
                        if (enCiclo) ciclo.add(n);
                    }
                    ciclo.add(vecino); // Cerrar el ciclo
                    ciclos.add(ciclo);
                }
            }
        }

        caminoActual.removeLast();
        enRecurso.remove(nodo);
    }

    /**
     * Encontrar todas las rutas entre dos nodos recursivamente.
     * Caso base: llegamos al destino → guardar ruta.
     * Caso recursivo: explorar cada vecino no visitado.
     */
    private void encontrarRutasRecursivo(String actual, String destino,
                                          HashSet<String> visitados,
                                          LinkedList<String> rutaActual,
                                          LinkedList<LinkedList<String>> todasRutas) {
        if (actual.equals(destino) && rutaActual.size() > 1) {
            todasRutas.add(new LinkedList<>(rutaActual)); // Caso base: ruta completa
            return;
        }

        LinkedList<Arista> aristas = listaAdyacencia.get(actual);
        if (aristas == null) return;

        for (Arista arista : aristas) {
            String vecino = arista.getDestino();
            if (!visitados.contains(vecino) || vecino.equals(destino)) {
                visitados.add(vecino);
                rutaActual.add(vecino);
                encontrarRutasRecursivo(vecino, destino, visitados, rutaActual, todasRutas); // Recursión
                rutaActual.removeLast();
                visitados.remove(vecino);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GrafoTransferencias{\n");
        for (String nodo : listaAdyacencia.keySet()) {
            sb.append("  ").append(nodo).append(": ");
            sb.append(listaAdyacencia.get(nodo)).append("\n");
        }
        return sb.append("}").toString();
    }
}
