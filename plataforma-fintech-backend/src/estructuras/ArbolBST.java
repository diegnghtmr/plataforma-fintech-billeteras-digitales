package estructuras;

import java.util.LinkedList;

/**
 * ArbolBST<K extends Comparable<K>, V> - Árbol Binario de Búsqueda.
 *
 * Propósito:
 *   - Clasificar usuarios por puntos acumulados.
 *   - Asignar nivel (Bronce/Plata/Oro/Platino) según posición.
 *   - Búsqueda eficiente por rango de puntos.
 *
 * Implementación: BST estándar (sin balanceo).
 * Complejidad promedio (árbol balanceado):
 *   - insertar():          O(log n)
 *   - buscar():            O(log n)
 *   - buscarPorRango():    O(k + log n) donde k = resultados en rango
 *   - eliminar():          O(log n)
 *   - enOrden():           O(n)
 *
 * Todos los recorridos y búsquedas son recursivos (requisito académico).
 *
 * @param <K> Tipo de clave (Comparable). En el sistema: Double (puntos)
 * @param <V> Tipo de valor. En el sistema: String (ID de usuario)
 */
public class ArbolBST<K extends Comparable<K>, V> {

    // ─── Nodo interno ────────────────────────────────────────────────────────
    private static class Nodo<K, V> {
        K clave;
        V valor;
        Nodo<K, V> izquierdo;
        Nodo<K, V> derecho;

        Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            izquierdo = null;
            derecho = null;
        }

        @Override
        public String toString() {
            return "[" + clave + " -> " + valor + "]";
        }
    }

    // ─── Atributos ────────────────────────────────────────────────────────────
    private Nodo<K, V> raiz;
    private int tamaño;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public ArbolBST() {
        raiz = null;
        tamaño = 0;
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Inserta un par (clave, valor) en el BST.
     * Si la clave ya existe, actualiza el valor.
     * O(log n) promedio.
     *
     * @param clave Clave de ordenamiento (puntos del usuario)
     * @param valor Valor asociado (ID del usuario)
     */
    public void insertar(K clave, V valor) {
        raiz = insertarRecursivo(raiz, clave, valor);
    }

    /**
     * Busca un valor por su clave exacta.
     * O(log n) promedio.
     *
     * @param clave Clave a buscar
     * @return Valor asociado, o null si no existe
     */
    public V buscar(K clave) {
        return buscarRecursivo(raiz, clave);
    }

    /**
     * Elimina un nodo por clave (usa sucesor en orden para reemplazar).
     * O(log n) promedio.
     *
     * @param clave Clave del nodo a eliminar
     */
    public void eliminar(K clave) {
        raiz = eliminarRecursivo(raiz, clave);
    }

    /**
     * Devuelve todos los valores cuyas claves están en [min, max].
     * Recorrido en orden que poda ramas fuera del rango.
     * O(k + log n) donde k = cantidad de resultados.
     *
     * Uso en el sistema: buscar usuarios en rango de puntos para
     * verificar ascenso de nivel o generar reportes.
     *
     * @param min Límite inferior (inclusive)
     * @param max Límite superior (inclusive)
     * @return Lista de valores en el rango
     */
    public LinkedList<V> buscarPorRango(K min, K max) {
        LinkedList<V> resultado = new LinkedList<>();
        buscarRangoRecursivo(raiz, min, max, resultado);
        return resultado;
    }

    /**
     * Recorre el árbol en orden (izq → raíz → der).
     * Produce una lista de valores ordenados de menor a mayor clave.
     * O(n)
     *
     * Uso: listar usuarios ordenados de menor a mayor puntaje.
     *
     * @return Lista ordenada de valores
     */
    public LinkedList<V> enOrden() {
        LinkedList<V> resultado = new LinkedList<>();
        enOrdenRecursivo(raiz, resultado);
        return resultado;
    }

    /**
     * Recorre el árbol en orden inverso (der → raíz → izq).
     * Produce una lista de valores de mayor a menor clave.
     * O(n)
     *
     * Uso: ranking de usuarios de mayor a menor puntaje.
     *
     * @return Lista ordenada de mayor a menor
     */
    public LinkedList<V> enOrdenInverso() {
        LinkedList<V> resultado = new LinkedList<>();
        enOrdenInversoRecursivo(raiz, resultado);
        return resultado;
    }

    /**
     * Encuentra la clave mínima del árbol.
     * O(log n)
     *
     * @return Clave mínima, o null si vacío
     */
    public K minimo() {
        if (raiz == null) return null;
        return minimoNodo(raiz).clave;
    }

    /**
     * Encuentra la clave máxima del árbol.
     * O(log n)
     *
     * @return Clave máxima, o null si vacío
     */
    public K maximo() {
        if (raiz == null) return null;
        return maximoNodo(raiz).clave;
    }

    /**
     * Calcula la altura del árbol recursivamente.
     * O(n)
     *
     * @return Altura del árbol (0 si vacío)
     */
    public int altura() {
        return alturaRecursiva(raiz);
    }

    /** @return Cantidad de nodos en el árbol */
    public int tamaño() { return tamaño; }

    /** @return true si el árbol no tiene nodos */
    public boolean estaVacio() { return raiz == null; }

    // ─── Métodos recursivos (requisito académico) ─────────────────────────────

    /**
     * Inserción recursiva.
     * Caso base: nodo null → crear nodo nuevo.
     * Caso recursivo: comparar clave y decidir subárbol.
     */
    private Nodo<K, V> insertarRecursivo(Nodo<K, V> nodo, K clave, V valor) {
        if (nodo == null) {                               // Caso base
            tamaño++;
            return new Nodo<>(clave, valor);
        }
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, clave, valor);  // Recursión izq
        } else if (cmp > 0) {
            nodo.derecho = insertarRecursivo(nodo.derecho, clave, valor);      // Recursión der
        } else {
            nodo.valor = valor;  // Actualizar valor si clave existe
        }
        return nodo;
    }

    /**
     * Búsqueda recursiva.
     * Caso base: nodo null (no encontrado) o clave igual.
     * Caso recursivo: ir a subárbol correspondiente.
     */
    private V buscarRecursivo(Nodo<K, V> nodo, K clave) {
        if (nodo == null) return null;                    // Caso base: no encontrado
        int cmp = clave.compareTo(nodo.clave);
        if (cmp == 0) return nodo.valor;                 // Caso base: encontrado
        if (cmp < 0) return buscarRecursivo(nodo.izquierdo, clave);  // Recursión izq
        return buscarRecursivo(nodo.derecho, clave);     // Recursión der
    }

    /**
     * Eliminación recursiva usando sucesor en orden.
     * Caso base: nodo null o nodo encontrado.
     * Caso recursivo: navegar hacia el nodo objetivo.
     */
    private Nodo<K, V> eliminarRecursivo(Nodo<K, V> nodo, K clave) {
        if (nodo == null) return null;                   // Caso base: no existe
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) {
            nodo.izquierdo = eliminarRecursivo(nodo.izquierdo, clave);  // Recursión izq
        } else if (cmp > 0) {
            nodo.derecho = eliminarRecursivo(nodo.derecho, clave);      // Recursión der
        } else {
            // Encontrado: manejar casos de eliminación
            tamaño--;
            if (nodo.izquierdo == null) return nodo.derecho;
            if (nodo.derecho == null)   return nodo.izquierdo;
            // Nodo con dos hijos: reemplazar con sucesor (mínimo del subárbol derecho)
            Nodo<K, V> sucesor = minimoNodo(nodo.derecho);
            nodo.clave = sucesor.clave;
            nodo.valor = sucesor.valor;
            tamaño++;  // compensar el tamaño-- anterior (el sucesor se elimina abajo)
            nodo.derecho = eliminarRecursivo(nodo.derecho, sucesor.clave);
        }
        return nodo;
    }

    /**
     * Búsqueda por rango recursiva.
     * Poda: si clave del nodo > max, solo ir a izquierdo.
     *       si clave del nodo < min, solo ir a derecho.
     *       si está en rango, agregar y explorar ambos lados.
     */
    private void buscarRangoRecursivo(Nodo<K, V> nodo, K min, K max, LinkedList<V> resultado) {
        if (nodo == null) return;                         // Caso base
        int cmpMin = nodo.clave.compareTo(min);
        int cmpMax = nodo.clave.compareTo(max);

        if (cmpMin > 0) buscarRangoRecursivo(nodo.izquierdo, min, max, resultado); // Poda der
        if (cmpMin >= 0 && cmpMax <= 0) resultado.add(nodo.valor);                 // En rango
        if (cmpMax < 0) buscarRangoRecursivo(nodo.derecho, min, max, resultado);   // Poda izq

        // Si está en rango explorar ambos lados
        if (cmpMin > 0 && cmpMax < 0) {
            buscarRangoRecursivo(nodo.derecho, min, max, resultado);
        } else if (cmpMin <= 0 && cmpMax >= 0) {
            buscarRangoRecursivo(nodo.izquierdo, min, max, resultado);
            buscarRangoRecursivo(nodo.derecho, min, max, resultado);
        }
    }

    /** Recorrido en orden recursivo (izq → raíz → der). */
    private void enOrdenRecursivo(Nodo<K, V> nodo, LinkedList<V> resultado) {
        if (nodo == null) return;                         // Caso base
        enOrdenRecursivo(nodo.izquierdo, resultado);      // Recursión izq
        resultado.add(nodo.valor);
        enOrdenRecursivo(nodo.derecho, resultado);         // Recursión der
    }

    /** Recorrido en orden inverso recursivo (der → raíz → izq). */
    private void enOrdenInversoRecursivo(Nodo<K, V> nodo, LinkedList<V> resultado) {
        if (nodo == null) return;                         // Caso base
        enOrdenInversoRecursivo(nodo.derecho, resultado); // Recursión der
        resultado.add(nodo.valor);
        enOrdenInversoRecursivo(nodo.izquierdo, resultado); // Recursión izq
    }

    /** Nodo con clave mínima (más a la izquierda). */
    private Nodo<K, V> minimoNodo(Nodo<K, V> nodo) {
        if (nodo.izquierdo == null) return nodo;          // Caso base
        return minimoNodo(nodo.izquierdo);                // Recursión
    }

    /** Nodo con clave máxima (más a la derecha). */
    private Nodo<K, V> maximoNodo(Nodo<K, V> nodo) {
        if (nodo.derecho == null) return nodo;            // Caso base
        return maximoNodo(nodo.derecho);                  // Recursión
    }

    /** Altura recursiva. Caso base: null → -1. */
    private int alturaRecursiva(Nodo<K, V> nodo) {
        if (nodo == null) return -1;                      // Caso base
        int altIzq = alturaRecursiva(nodo.izquierdo);    // Recursión izq
        int altDer = alturaRecursiva(nodo.derecho);       // Recursión der
        return 1 + Math.max(altIzq, altDer);
    }

    @Override
    public String toString() {
        if (estaVacio()) return "ArbolBST[vacío]";
        return "ArbolBST[tamaño=" + tamaño + ", altura=" + altura() + ", enOrden=" + enOrden() + "]";
    }
}
