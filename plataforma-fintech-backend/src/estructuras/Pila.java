package estructuras;

import java.util.*;

/**
 * Implementación personalizada de una Pila (Stack) para gestionar operaciones reversibles.
 * Propósito: Permitir deshacer transacciones y restaurar estados anteriores.
 * Estructura: LIFO (Last In, First Out)
 * 
 * @author Compañero 1
 * @version 1.0
 * @param <T> Tipo de elemento en la pila
 */
public class Pila<T> {
    private Nodo<T> cima;
    private int tamaño;
    
    /**
     * Clase interna para representar los nodos de la pila.
     */
    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;
        
        Nodo(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }
    
    /**
     * Constructor que inicializa la pila.
     */
    public Pila() {
        this.cima = null;
        this.tamaño = 0;
    }
    
    /**
     * Inserta un elemento en la cima de la pila.
     * Tiempo: O(1)
     * 
     * @param dato Elemento a insertar
     */
    public void apilar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        nuevoNodo.siguiente = cima;
        cima = nuevoNodo;
        tamaño++;
    }
    
    /**
     * Extrae el elemento de la cima de la pila.
     * Tiempo: O(1)
     * 
     * @return Elemento en la cima, null si está vacía
     */
    public T desapilar() {
        if (estaVacia()) {
            return null;
        }
        
        T dato = cima.dato;
        cima = cima.siguiente;
        tamaño--;
        return dato;
    }
    
    /**
     * Consulta el elemento de la cima sin extraerlo.
     * Tiempo: O(1)
     * 
     * @return Elemento en la cima, null si está vacía
     */
    public T cima() {
        if (estaVacia()) {
            return null;
        }
        return cima.dato;
    }
    
    /**
     * Verifica si la pila está vacía.
     * Tiempo: O(1)
     * 
     * @return true si no hay elementos
     */
    public boolean estaVacia() {
        return cima == null;
    }
    
    /**
     * Obtiene el número de elementos en la pila.
     * Tiempo: O(1)
     * 
     * @return Cantidad de elementos
     */
    public int tamaño() {
        return tamaño;
    }
    
    /**
     * Vacía completamente la pila.
     * Tiempo: O(1) - Solo se reasignan referencias
     */
    public void limpiar() {
        cima = null;
        tamaño = 0;
    }
    
    /**
     * Obtiene todos los elementos de la pila en una lista.
     * Los elementos están ordenados de cima a base.
     * Tiempo: O(n)
     * 
     * @return LinkedList con los elementos
     */
    public LinkedList<T> obtenerElementos() {
        LinkedList<T> elementos = new LinkedList<>();
        Nodo<T> actual = cima;
        
        while (actual != null) {
            elementos.add(actual.dato);
            actual = actual.siguiente;
        }
        
        return elementos;
    }
    
    /**
     * Cuenta el número de elementos que cumplen una condición.
     * Usa recursión para demostrar su aplicación.
     * 
     * @param filtro Condición a cumplir
     * @return Cantidad de elementos que cumplen
     */
    public int contarConFiltro(Filtro<T> filtro) {
        return contarRecursivo(cima, filtro);
    }
    
    /**
     * Método auxiliar recursivo para contar elementos.
     * 
     * @param nodo Nodo actual
     * @param filtro Condición a cumplir
     * @return Cantidad de elementos que cumplen
     */
    private int contarRecursivo(Nodo<T> nodo, Filtro<T> filtro) {
        if (nodo == null) {
            return 0;
        }
        int cuenta = filtro.cumple(nodo.dato) ? 1 : 0;
        return cuenta + contarRecursivo(nodo.siguiente, filtro);
    }
    
    /**
     * Interfaz funcional para filtrar elementos.
     */
    public interface Filtro<T> {
        boolean cumple(T elemento);
    }
    
    /**
     * Convierte la pila a una representación en string.
     * Muestra los elementos de cima a base.
     * 
     * @return Representación string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Pila[");
        Nodo<T> actual = cima;
        boolean primero = true;
        
        while (actual != null) {
            if (!primero) {
                sb.append(" <- ");
            }
            sb.append(actual.dato.toString());
            actual = actual.siguiente;
            primero = false;
        }
        
        sb.append("]");
        return sb.toString();
    }
}
