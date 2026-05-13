package estructuras;

import java.util.Iterator;

/**
 * Implementación personalizada de una Lista Enlazada para almacenar históricos.
 * Propósito: Demostrar el funcionamiento de listas enlazadas y su flexibilidad.
 * Estructura: Doubly-Linked List (Lista doblemente enlazada)
 * 
 * @author Compañero 1
 * @version 1.0
 * @param <T> Tipo de elemento en la lista
 */
public class MiLista<T> implements Iterable<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamaño;
    
    /**
     * Clase interna para representar los nodos de la lista.
     */
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;
        Nodo<T> anterior;
        
        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }
    }
    
    /**
     * Constructor que inicializa la lista vacía.
     */
    public MiLista() {
        this.cabeza = null;
        this.cola = null;
        this.tamaño = 0;
    }
    
    /**
     * Añade un elemento al final de la lista.
     * Tiempo: O(1)
     * 
     * @param dato Elemento a añadir
     */
    public void agregar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        
        if (estaVacia()) {
            cabeza = nuevoNodo;
            cola = nuevoNodo;
        } else {
            cola.siguiente = nuevoNodo;
            nuevoNodo.anterior = cola;
            cola = nuevoNodo;
        }
        
        tamaño++;
    }
    
    /**
     * Inserta un elemento en una posición específica.
     * Tiempo: O(n)
     * 
     * @param indice Posición (0 basada)
     * @param dato Elemento a insertar
     * @return true si se insertó, false si índice inválido
     */
    public boolean insertar(int indice, T dato) {
        if (indice < 0 || indice > tamaño) {
            return false;
        }
        
        if (indice == tamaño) {
            agregar(dato);
            return true;
        }
        
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        Nodo<T> nodoActual = obtenerNodo(indice);
        
        if (nodoActual == null) return false;
        
        nuevoNodo.siguiente = nodoActual;
        nuevoNodo.anterior = nodoActual.anterior;
        
        if (nodoActual.anterior != null) {
            nodoActual.anterior.siguiente = nuevoNodo;
        } else {
            cabeza = nuevoNodo;
        }
        
        nodoActual.anterior = nuevoNodo;
        tamaño++;
        return true;
    }
    
    /**
     * Obtiene un elemento por su índice.
     * Tiempo: O(n)
     * 
     * @param indice Posición (0 basada)
     * @return Elemento en la posición, null si índice inválido
     */
    public T obtener(int indice) {
        Nodo<T> nodo = obtenerNodo(indice);
        return nodo != null ? nodo.dato : null;
    }
    
    /**
     * Obtiene el nodo en una posición específica.
     * Utiliza búsqueda desde el extremo más cercano (optimización).
     * Tiempo: O(n)
     * 
     * @param indice Posición (0 basada)
     * @return Nodo en la posición, null si índice inválido
     */
    private Nodo<T> obtenerNodo(int indice) {
        if (indice < 0 || indice >= tamaño) {
            return null;
        }
        
        // Optimización: buscar desde el extremo más cercano
        if (indice < tamaño / 2) {
            Nodo<T> nodo = cabeza;
            for (int i = 0; i < indice; i++) {
                nodo = nodo.siguiente;
            }
            return nodo;
        } else {
            Nodo<T> nodo = cola;
            for (int i = tamaño - 1; i > indice; i--) {
                nodo = nodo.anterior;
            }
            return nodo;
        }
    }
    
    /**
     * Elimina el elemento en una posición específica.
     * Tiempo: O(n)
     * 
     * @param indice Posición (0 basada)
     * @return Elemento eliminado, null si índice inválido
     */
    public T eliminar(int indice) {
        Nodo<T> nodo = obtenerNodo(indice);
        
        if (nodo == null) return null;
        
        if (nodo.anterior != null) {
            nodo.anterior.siguiente = nodo.siguiente;
        } else {
            cabeza = nodo.siguiente;
        }
        
        if (nodo.siguiente != null) {
            nodo.siguiente.anterior = nodo.anterior;
        } else {
            cola = nodo.anterior;
        }
        
        tamaño--;
        return nodo.dato;
    }
    
    /**
     * Busca la primera ocurrencia de un elemento.
     * Tiempo: O(n)
     * 
     * @param dato Elemento a buscar
     * @return Índice del elemento, -1 si no existe
     */
    public int buscar(T dato) {
        Nodo<T> actual = cabeza;
        int indice = 0;
        
        while (actual != null) {
            if (actual.dato.equals(dato)) {
                return indice;
            }
            actual = actual.siguiente;
            indice++;
        }
        
        return -1;
    }
    
    /**
     * Verifica si la lista contiene un elemento.
     * Tiempo: O(n)
     * 
     * @param dato Elemento a verificar
     * @return true si contiene elemento
     */
    public boolean contiene(T dato) {
        return buscar(dato) != -1;
    }
    
    /**
     * Obtiene el número de elementos en la lista.
     * Tiempo: O(1)
     * 
     * @return Cantidad de elementos
     */
    public int tamaño() {
        return tamaño;
    }
    
    /**
     * Verifica si la lista está vacía.
     * Tiempo: O(1)
     * 
     * @return true si no hay elementos
     */
    public boolean estaVacia() {
        return tamaño == 0;
    }
    
    /**
     * Vacía completamente la lista.
     * Tiempo: O(1)
     */
    public void limpiar() {
        cabeza = null;
        cola = null;
        tamaño = 0;
    }
    
    /**
     * Invierte el orden de los elementos.
     * Utilizando recursión.
     * Tiempo: O(n)
     */
    public void invertir() {
        if (tamaño <= 1) return;
        invertirRecursivo(cabeza);
        
        // Intercambiar cabeza y cola
        Nodo<T> temp = cabeza;
        cabeza = cola;
        cola = temp;
    }
    
    /**
     * Método auxiliar recursivo para invertir.
     */
    private void invertirRecursivo(Nodo<T> nodo) {
        if (nodo == null) return;
        
        invertirRecursivo(nodo.siguiente);
        
        // Invertir los punteros
        Nodo<T> tempSiguiente = nodo.siguiente;
        nodo.siguiente = nodo.anterior;
        nodo.anterior = tempSiguiente;
    }
    
    /**
     * Obtiene el primer elemento de la lista.
     * Tiempo: O(1)
     * 
     * @return Primer elemento, null si vacía
     */
    public T obtenerPrimero() {
        return cabeza != null ? cabeza.dato : null;
    }
    
    /**
     * Obtiene el último elemento de la lista.
     * Tiempo: O(1)
     * 
     * @return Último elemento, null si vacía
     */
    public T obtenerUltimo() {
        return cola != null ? cola.dato : null;
    }
    
    /**
     * Cinta un iterador para recorrer la lista.
     * 
     * @return Iterador de la lista
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorLista();
    }
    
    /**
     * Clase interna para iterar sobre la lista.
     */
    private class IteradorLista implements Iterator<T> {
        private Nodo<T> actual = cabeza;
        
        @Override
        public boolean hasNext() {
            return actual != null;
        }
        
        @Override
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            T dato = actual.dato;
            actual = actual.siguiente;
            return dato;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MiLista[");
        Nodo<T> actual = cabeza;
        boolean primero = true;
        
        while (actual != null) {
            if (!primero) {
                sb.append(", ");
            }
            sb.append(actual.dato.toString());
            actual = actual.siguiente;
            primero = false;
        }
        
        sb.append("]");
        return sb.toString();
    }
}
