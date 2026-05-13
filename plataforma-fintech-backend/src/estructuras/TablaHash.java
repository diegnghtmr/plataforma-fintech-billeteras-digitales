package estructuras;

/**
 * Implementación personalizada de una Tabla Hash (HashMap) para búsqueda rápida.
 * Utiliza encadenamiento para resolver colisiones.
 * Propósito: Demostrar el funcionamiento de tablas hash y su eficiencia O(1) en promedio.
 * 
 * @author Compañero 1
 * @version 1.0
 * @param <K> Tipo de clave
 * @param <V> Tipo de valor
 */
public class TablaHash<K, V> {
    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA = 0.75;
    
    private Entrada<K, V>[] tabla;
    private int tamaño;
    
    /**
     * Clase interna para representar una entrada en la tabla hash.
     * Utiliza encadenamiento para resolver colisiones.
     */
    private static class Entrada<K, V> {
        K clave;
        V valor;
        Entrada<K, V> siguiente;
        
        Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }
    
    /**
     * Constructor que inicializa la tabla hash.
     */
    @SuppressWarnings("unchecked")
    public TablaHash() {
        tabla = new Entrada[CAPACIDAD_INICIAL];
        tamaño = 0;
    }
    
    /**
     * Calcula el hash de una clave.
     * 
     * @param clave Clave a hashear
     * @return Índice en la tabla
     */
    private int hash(K clave) {
        if (clave == null) return 0;
        return Math.abs(clave.hashCode() % tabla.length);
    }
    
    /**
     * Inserta o actualiza un valor en la tabla hash.
     * Tiempo: O(1) en promedio, O(n) en el peor caso.
     * 
     * @param clave Clave
     * @param valor Valor
     */
    public void insertar(K clave, V valor) {
        if (tamaño >= tabla.length * FACTOR_CARGA) {
            redimensionar();
        }
        
        int indice = hash(clave);
        Entrada<K, V> entrada = tabla[indice];
        
        // Recorrer la cadena para buscar la clave o encontrar el final
        while (entrada != null) {
            if ((clave == null && entrada.clave == null) || 
                (clave != null && clave.equals(entrada.clave))) {
                entrada.valor = valor; // Actualizar valor existente
                return;
            }
            entrada = entrada.siguiente;
        }
        
        // Insertar al principio de la cadena
        Entrada<K, V> nuevaEntrada = new Entrada<>(clave, valor);
        nuevaEntrada.siguiente = tabla[indice];
        tabla[indice] = nuevaEntrada;
        tamaño++;
    }
    
    /**
     * Obtiene un valor de la tabla hash.
     * Tiempo: O(1) en promedio, O(n) en el peor caso.
     * 
     * @param clave Clave
     * @return Valor asociado, null si no existe
     */
    public V obtener(K clave) {
        int indice = hash(clave);
        Entrada<K, V> entrada = tabla[indice];
        
        while (entrada != null) {
            if ((clave == null && entrada.clave == null) || 
                (clave != null && clave.equals(entrada.clave))) {
                return entrada.valor;
            }
            entrada = entrada.siguiente;
        }
        
        return null;
    }
    
    /**
     * Verifica si la tabla contiene una clave.
     * Tiempo: O(1) en promedio.
     * 
     * @param clave Clave a buscar
     * @return true si contiene la clave
     */
    public boolean contiene(K clave) {
        return obtener(clave) != null;
    }
    
    /**
     * Elimina un valor de la tabla hash.
     * Tiempo: O(1) en promedio, O(n) en el peor caso.
     * 
     * @param clave Clave a eliminar
     * @return Valor eliminado, null si no existía
     */
    public V eliminar(K clave) {
        int indice = hash(clave);
        Entrada<K, V> entrada = tabla[indice];
        Entrada<K, V> anterior = null;
        
        while (entrada != null) {
            if ((clave == null && entrada.clave == null) || 
                (clave != null && clave.equals(entrada.clave))) {
                
                if (anterior == null) {
                    tabla[indice] = entrada.siguiente;
                } else {
                    anterior.siguiente = entrada.siguiente;
                }
                
                tamaño--;
                return entrada.valor;
            }
            
            anterior = entrada;
            entrada = entrada.siguiente;
        }
        
        return null;
    }
    
    /**
     * Obtiene el número de elementos en la tabla.
     * Tiempo: O(1)
     * 
     * @return Cantidad de pares clave-valor
     */
    public int tamaño() {
        return tamaño;
    }
    
    /**
     * Verifica si la tabla está vacía.
     * 
     * @return true si no hay elementos
     */
    public boolean estaVacia() {
        return tamaño == 0;
    }
    
    /**
     * Redimensiona la tabla cuando se alcanza el factor de carga.
     * Tiempo: O(n)
     */
    @SuppressWarnings("unchecked")
    private void redimensionar() {
        Entrada<K, V>[] tablaAntigua = tabla;
        tabla = new Entrada[tablaAntigua.length * 2];
        tamaño = 0;
        
        // Reinsertar todos los elementos
        for (Entrada<K, V> entrada : tablaAntigua) {
            while (entrada != null) {
                insertar(entrada.clave, entrada.valor);
                entrada = entrada.siguiente;
            }
        }
    }
    
    /**
     * Vacia completamente la tabla hash.
     * Tiempo: O(n)
     */
    @SuppressWarnings("unchecked")
    public void limpiar() {
        tabla = new Entrada[CAPACIDAD_INICIAL];
        tamaño = 0;
    }
    
    /**
     * Obtiene información sobre la tabla.
     * 
     * @return String con estadísticas
     */
    public String obtenerEstadisticas() {
        int cadenasOcupadas = 0;
        int longitudMaximaCadena = 0;
        
        for (Entrada<K, V> entrada : tabla) {
            if (entrada != null) {
                cadenasOcupadas++;
                int longitud = 0;
                Entrada<K, V> actual = entrada;
                while (actual != null) {
                    longitud++;
                    actual = actual.siguiente;
                }
                longitudMaximaCadena = Math.max(longitudMaximaCadena, longitud);
            }
        }
        
        return "TablaHash{" +
                "tamaño=" + tamaño +
                ", capacidad=" + tabla.length +
                ", factorCarga=" + (double) tamaño / tabla.length +
                ", cadenasOcupadas=" + cadenasOcupadas +
                ", longitudMaximaCadena=" + longitudMaximaCadena +
                '}';
    }
}
