package estructuras;

/**
 * ColaPrioridad<T> - Cola de Prioridad personalizada (Min-Heap).
 *
 * Propósito: Ordenar operaciones programadas por fecha de ejecución.
 * La operación con fecha más próxima (timestamp menor) siempre
 * queda en la cima, lista para ejecutarse primero.
 *
 * Implementación: Min-Heap con arreglo dinámico.
 * Complejidad:
 *   - insertar():    O(log n)
 *   - extraerMin():  O(log n)
 *   - verMin():      O(1)
 *   - tamaño():      O(1)
 *
 * @param <T> Tipo de elemento que implementa Comparable
 */
public class ColaPrioridad<T extends Comparable<T>> {

    // ─── Nodo interno ────────────────────────────────────────────────────────
    private static class Nodo<T> {
        T dato;
        long prioridad; // timestamp de ejecución (menor = más urgente)

        Nodo(T dato, long prioridad) {
            this.dato = dato;
            this.prioridad = prioridad;
        }
    }

    // ─── Atributos ────────────────────────────────────────────────────────────
    private Object[] heap;          // Arreglo subyacente del heap
    private int tamaño;             // Cantidad de elementos actuales
    private static final int CAPACIDAD_INICIAL = 16;

    // ─── Constructor ──────────────────────────────────────────────────────────
    /**
     * Crea una cola de prioridad vacía.
     */
    public ColaPrioridad() {
        this.heap = new Object[CAPACIDAD_INICIAL];
        this.tamaño = 0;
    }

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Inserta un elemento con su prioridad (timestamp).
     * O(log n) - sube el elemento hasta su posición correcta (heapify-up).
     *
     * @param dato      Elemento a insertar
     * @param prioridad Timestamp Unix en ms (menor = más urgente)
     */
    public void insertar(T dato, long prioridad) {
        if (tamaño == heap.length) redimensionar();
        heap[tamaño] = new Nodo<>(dato, prioridad);
        subirRecursivo(tamaño);   // Recursión: reordenar hacia arriba
        tamaño++;
    }

    /**
     * Extrae y devuelve el elemento con menor prioridad (más urgente).
     * O(log n) - reorganiza el heap tras la extracción (heapify-down).
     *
     * @return Elemento con menor timestamp, o null si está vacía
     */
    @SuppressWarnings("unchecked")
    public T extraerMin() {
        if (estaVacia()) return null;
        T min = ((Nodo<T>) heap[0]).dato;
        tamaño--;
        heap[0] = heap[tamaño];
        heap[tamaño] = null;
        if (tamaño > 0) bajarRecursivo(0);   // Recursión: reordenar hacia abajo
        return min;
    }

    /**
     * Consulta el elemento más urgente SIN extraerlo.
     * O(1)
     *
     * @return Elemento en la cima, o null si vacía
     */
    @SuppressWarnings("unchecked")
    public T verMin() {
        if (estaVacia()) return null;
        return ((Nodo<T>) heap[0]).dato;
    }

    /**
     * Consulta la prioridad (timestamp) del elemento más urgente.
     * O(1)
     *
     * @return Timestamp en ms, o -1 si vacía
     */
    @SuppressWarnings("unchecked")
    public long verPrioridadMin() {
        if (estaVacia()) return -1;
        return ((Nodo<T>) heap[0]).prioridad;
    }

    /** @return true si no hay elementos */
    public boolean estaVacia() { return tamaño == 0; }

    /** @return Cantidad de elementos en la cola */
    public int tamaño() { return tamaño; }

    // ─── Métodos recursivos (requisito académico) ─────────────────────────────

    /**
     * Heapify-up recursivo.
     * Sube el nodo en índice i hasta su posición correcta.
     * Caso base: i == 0 (llegamos a la raíz) o padre <= hijo.
     *
     * @param i Índice del nodo recién insertado
     */
    @SuppressWarnings("unchecked")
    private void subirRecursivo(int i) {
        if (i == 0) return;                          // Caso base: raíz
        int padre = (i - 1) / 2;
        Nodo<T> nodoPadre = (Nodo<T>) heap[padre];
        Nodo<T> nodoHijo  = (Nodo<T>) heap[i];
        if (nodoPadre.prioridad > nodoHijo.prioridad) {
            intercambiar(i, padre);
            subirRecursivo(padre);                   // Recursión hacia arriba
        }
        // Caso base implícito: padre correcto, no hay swap
    }

    /**
     * Heapify-down recursivo.
     * Baja el nodo en índice i hasta su posición correcta.
     * Caso base: no tiene hijos o ya es menor que ambos hijos.
     *
     * @param i Índice del nodo a bajar
     */
    @SuppressWarnings("unchecked")
    private void bajarRecursivo(int i) {
        int izq = 2 * i + 1;
        int der = 2 * i + 2;
        int menor = i;

        if (izq < tamaño && prioridad(izq) < prioridad(menor)) menor = izq;
        if (der < tamaño && prioridad(der) < prioridad(menor)) menor = der;

        if (menor != i) {                            // Caso base: ya en posición
            intercambiar(i, menor);
            bajarRecursivo(menor);                   // Recursión hacia abajo
        }
    }

    // ─── Auxiliares ───────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private long prioridad(int i) {
        return ((Nodo<T>) heap[i]).prioridad;
    }

    private void intercambiar(int a, int b) {
        Object tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    private void redimensionar() {
        Object[] nuevo = new Object[heap.length * 2];
        System.arraycopy(heap, 0, nuevo, 0, heap.length);
        heap = nuevo;
    }

    @Override
    public String toString() {
        if (estaVacia()) return "ColaPrioridad[]";
        StringBuilder sb = new StringBuilder("ColaPrioridad[");
        for (int i = 0; i < tamaño; i++) {
            if (i > 0) sb.append(", ");
            sb.append(((Nodo<?>) heap[i]).dato);
        }
        return sb.append("]").toString();
    }
}
