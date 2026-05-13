package estructuras;

/**
 * ColaSimple<T> - Cola FIFO personalizada (First In, First Out).
 *
 * Propósito: Almacenar notificaciones pendientes por usuario.
 * La primera notificación generada es la primera en entregarse.
 *
 * Implementación: Lista enlazada simple (frente y final).
 * Complejidad:
 *   - encolar():   O(1)
 *   - desencolar(): O(1)
 *   - frente():    O(1)
 *   - tamaño():    O(1)
 *   - contarConFiltro(): O(n) recursivo
 *
 * @param <T> Tipo de elemento a almacenar
 */
public class ColaSimple<T> {

    // ─── Nodo interno ────────────────────────────────────────────────────────
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    // ─── Atributos ────────────────────────────────────────────────────────────
    private Nodo<T> frente;   // Primer elemento (siguiente a desencolar)
    private Nodo<T> final_;   // Último elemento (donde se encola)
    private int tamaño;

    // ─── Interfaz funcional para filtros ─────────────────────────────────────
    /**
     * Interfaz para filtrar elementos al contar recursivamente.
     */
    public interface Filtro<T> {
        boolean cumple(T elemento);
    }

    // ─── Constructor ──────────────────────────────────────────────────────────
    public ColaSimple() {
        frente = null;
        final_ = null;
        tamaño = 0;
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Agrega un elemento al final de la cola.
     * O(1) - acceso directo al nodo final.
     *
     * @param dato Elemento a encolar
     */
    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            final_.siguiente = nuevo;
        }
        final_ = nuevo;
        tamaño++;
    }

    /**
     * Extrae y devuelve el primer elemento de la cola.
     * O(1)
     *
     * @return Elemento al frente, o null si vacía
     */
    public T desencolar() {
        if (estaVacia()) return null;
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) final_ = null;
        tamaño--;
        return dato;
    }

    /**
     * Consulta el primer elemento SIN extraerlo.
     * O(1)
     *
     * @return Primer elemento, o null si vacía
     */
    public T frente() {
        if (estaVacia()) return null;
        return frente.dato;
    }

    /** @return true si la cola no tiene elementos */
    public boolean estaVacia() { return tamaño == 0; }

    /** @return Cantidad de elementos en la cola */
    public int tamaño() { return tamaño; }

    /**
     * Cuenta recursivamente cuántos elementos cumplen el filtro dado.
     * O(n) - recorre todos los nodos.
     * Recursión: caso base = nodo null, caso recursivo = procesar + continuar.
     *
     * @param filtro Condición a evaluar
     * @return Cantidad de elementos que cumplen el filtro
     */
    public int contarConFiltro(Filtro<T> filtro) {
        return contarRecursivo(frente, filtro);
    }

    /**
     * Devuelve todos los elementos como lista (sin extraerlos).
     * O(n) - recorre recursivamente todos los nodos.
     *
     * @return Lista con todos los elementos, del frente al final
     */
    public java.util.LinkedList<T> toList() {
        java.util.LinkedList<T> lista = new java.util.LinkedList<>();
        llenarListaRecursivo(frente, lista);
        return lista;
    }

    // ─── Métodos recursivos ───────────────────────────────────────────────────

    /**
     * Conteo recursivo con filtro.
     * Caso base: nodo == null → retorna 0.
     * Caso recursivo: evalúa el nodo actual y continúa con el siguiente.
     */
    private int contarRecursivo(Nodo<T> nodo, Filtro<T> filtro) {
        if (nodo == null) return 0;                         // Caso base
        int cuenta = filtro.cumple(nodo.dato) ? 1 : 0;
        return cuenta + contarRecursivo(nodo.siguiente, filtro); // Recursión
    }

    /**
     * Llenado de lista recursivo.
     * Caso base: nodo == null.
     * Caso recursivo: agrega dato y avanza.
     */
    private void llenarListaRecursivo(Nodo<T> nodo, java.util.LinkedList<T> lista) {
        if (nodo == null) return;                           // Caso base
        lista.add(nodo.dato);
        llenarListaRecursivo(nodo.siguiente, lista);        // Recursion
    }

    @Override
    public String toString() {
        if (estaVacia()) return "ColaSimple[]";
        StringBuilder sb = new StringBuilder("ColaSimple[");
        Nodo<T> actual = frente;
        while (actual != null) {
            sb.append(actual.dato);
            if (actual.siguiente != null) sb.append(" -> ");
            actual = actual.siguiente;
        }
        return sb.append("]").toString();
    }
}
