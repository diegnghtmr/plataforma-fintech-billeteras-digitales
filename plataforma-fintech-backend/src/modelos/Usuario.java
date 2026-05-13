package modelos;

import java.util.*;

/**
 * Clase que representa un Usuario en la plataforma fintech.
 * Un usuario puede tener múltiples billeteras y acumula puntos según sus transacciones.
 * 
 * @author Compañero 1
 * @version 1.0
 */
public class Usuario {
    private String id;
    private String nombre;
    private String email;
    private long fechaRegistro;
    private double puntosAcumulados;
    private String nivelFidelizacion;
    private HashMap<String, Billetera> billeteras;
    private LinkedList<Transaccion> historialTransacciones;
    
    /**
     * Constructor para crear un nuevo usuario.
     * 
     * @param id Identificador único del usuario
     * @param nombre Nombre del usuario
     * @param email Email del usuario
     */
    public Usuario(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.fechaRegistro = System.currentTimeMillis();
        this.puntosAcumulados = 0;
        this.nivelFidelizacion = "Bronce";
        this.billeteras = new HashMap<>();
        this.historialTransacciones = new LinkedList<>();
    }
    
    // ==================== Getters y Setters ====================
    
    public String getId() {
        return id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public long getFechaRegistro() {
        return fechaRegistro;
    }
    
    public double getPuntosAcumulados() {
        return puntosAcumulados;
    }
    
    public void setPuntosAcumulados(double puntos) {
        this.puntosAcumulados = Math.max(0, puntos);
        actualizarNivelFidelizacion();
    }
    
    public void agregarPuntos(double puntos) {
        this.puntosAcumulados += puntos;
        actualizarNivelFidelizacion();
    }
    
    public void descontarPuntos(double puntos) {
        this.puntosAcumulados = Math.max(0, this.puntosAcumulados - puntos);
        actualizarNivelFidelizacion();
    }
    
    public String getNivelFidelizacion() {
        return nivelFidelizacion;
    }
    
    public HashMap<String, Billetera> getBilleteras() {
        return billeteras;
    }
    
    public LinkedList<Transaccion> getHistorialTransacciones() {
        return historialTransacciones;
    }
    
    // ==================== Gestión de Niveles ====================
    
    /**
     * Actualiza el nivel de fidelización según los puntos acumulados.
     * Bronce: 0 a 500 puntos
     * Plata: 501 a 1000 puntos
     * Oro: 1001 a 5000 puntos
     * Platino: más de 5000 puntos
     */
    private void actualizarNivelFidelizacion() {
        String nivelAnterior = this.nivelFidelizacion;
        
        if (puntosAcumulados <= 500) {
            this.nivelFidelizacion = "Bronce";
        } else if (puntosAcumulados <= 1000) {
            this.nivelFidelizacion = "Plata";
        } else if (puntosAcumulados <= 5000) {
            this.nivelFidelizacion = "Oro";
        } else {
            this.nivelFidelizacion = "Platino";
        }
        
        // Notificar cambio de nivel
        if (!nivelAnterior.equals(this.nivelFidelizacion)) {
            System.out.println("[ALERTA] Usuario " + nombre + " ascendió al nivel: " + this.nivelFidelizacion);
        }
    }
    
    // ==================== Gestión de Billeteras ====================
    
    /**
     * Crea una nueva billetera para el usuario.
     * 
     * @param codigoBilletera Código único de la billetera
     * @param nombre Nombre descriptivo
     * @param tipo Tipo de billetera (Ahorro, Gastos diarios, etc.)
     * @return true si se creó exitosamente, false si ya existe
     */
    public boolean crearBilletera(String codigoBilletera, String nombre, String tipo) {
        if (billeteras.containsKey(codigoBilletera)) {
            System.out.println("[ERROR] La billetera " + codigoBilletera + " ya existe para el usuario.");
            return false;
        }
        
        Billetera billetera = new Billetera(codigoBilletera, nombre, tipo, this.id);
        billeteras.put(codigoBilletera, billetera);
        System.out.println("[ÉXITO] Billetera " + codigoBilletera + " creada para " + nombre);
        return true;
    }
    
    /**
     * Obtiene una billetera por su código.
     * 
     * @param codigoBilletera Código de la billetera
     * @return La billetera si existe, null en caso contrario
     */
    public Billetera obtenerBilletera(String codigoBilletera) {
        return billeteras.get(codigoBilletera);
    }
    
    /**
     * Verifica si el usuario tiene una billetera específica.
     * 
     * @param codigoBilletera Código de la billetera
     * @return true si existe, false en caso contrario
     */
    public boolean tieneBilletera(String codigoBilletera) {
        return billeteras.containsKey(codigoBilletera);
    }
    
    /**
     * Obtiene el número total de billeteras del usuario.
     * 
     * @return Cantidad de billeteras
     */
    public int cantidadBilleteras() {
        return billeteras.size();
    }
    
    /**
     * Calcula el saldo total del usuario sumando todas sus billeteras.
     * 
     * @return Saldo total
     */
    public double calcularSaldoTotal() {
        double total = 0;
        for (Billetera billetera : billeteras.values()) {
            total += billetera.getSaldo();
        }
        return total;
    }
    
    // ==================== Gestión de Historial ====================
    
    /**
     * Añade una transacción al historial del usuario.
     * 
     * @param transaccion La transacción a registrar
     */
    public void registrarTransaccion(Transaccion transaccion) {
        historialTransacciones.add(transaccion);
    }
    
    /**
     * Obtiene el historial de transacciones del usuario.
     * 
     * @return Lista de transacciones
     */
    public LinkedList<Transaccion> obtenerHistorial() {
        return new LinkedList<>(historialTransacciones);
    }
    
    /**
     * Obtiene el número de transacciones realizadas.
     * 
     * @return Cantidad de transacciones
     */
    public int cantidadTransacciones() {
        return historialTransacciones.size();
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", puntosAcumulados=" + puntosAcumulados +
                ", nivelFidelizacion='" + nivelFidelizacion + '\'' +
                ", billeteras=" + billeteras.size() +
                ", saldoTotal=" + calcularSaldoTotal() +
                '}';
    }
}
