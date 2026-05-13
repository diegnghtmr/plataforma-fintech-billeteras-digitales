package modelos;

import java.util.*;

/**
 * Clase que representa una Billetera Digital en la plataforma fintech.
 * Cada billetera tiene un saldo y mantiene un historial de transacciones.
 * 
 * @author Compañero 1
 * @version 1.0
 */
public class Billetera {
    private String codigo;
    private String nombre;
    private String tipo;
    private String idPropietario;
    private double saldo;
    private boolean activa;
    private long fechaCreacion;
    private LinkedList<Transaccion> historialLocal;
    
    private static final double SALDO_MINIMO = 0.0;
    private static final double SALDO_MAXIMO = 1000000.0;
    
    /**
     * Constructor para crear una nueva billetera.
     * 
     * @param codigo Identificador único de la billetera
     * @param nombre Nombre descriptivo
     * @param tipo Tipo de billetera (Ahorro, Gastos diarios, etc.)
     * @param idPropietario ID del usuario propietario
     */
    public Billetera(String codigo, String nombre, String tipo, String idPropietario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.idPropietario = idPropietario;
        this.saldo = 0.0;
        this.activa = true;
        this.fechaCreacion = System.currentTimeMillis();
        this.historialLocal = new LinkedList<>();
    }
    
    // ==================== Getters y Setters ====================
    
    public String getCodigo() {
        return codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public String getIdPropietario() {
        return idPropietario;
    }
    
    public double getSaldo() {
        return saldo;
    }
    
    public boolean isActiva() {
        return activa;
    }
    
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    public long getFechaCreacion() {
        return fechaCreacion;
    }
    
    public LinkedList<Transaccion> getHistorialLocal() {
        return new LinkedList<>(historialLocal);
    }
    
    // ==================== Operaciones de Saldo ====================
    
    /**
     * Aumenta el saldo de la billetera.
     * 
     * @param monto Cantidad a agregar
     * @return true si se realizó exitosamente
     */
    public boolean aumentarSaldo(double monto) {
        if (monto < 0) {
            System.out.println("[ERROR] No se puede agregar un monto negativo.");
            return false;
        }
        
        if (saldo + monto > SALDO_MAXIMO) {
            System.out.println("[ERROR] Saldo excedería el límite máximo permitido.");
            return false;
        }
        
        this.saldo += monto;
        return true;
    }
    
    /**
     * Disminuye el saldo de la billetera.
     * 
     * @param monto Cantidad a restar
     * @return true si se realizó exitosamente
     */
    public boolean disminuirSaldo(double monto) {
        if (monto < 0) {
            System.out.println("[ERROR] No se puede restar un monto negativo.");
            return false;
        }
        
        if (saldo - monto < SALDO_MINIMO) {
            System.out.println("[ERROR] Saldo insuficiente. Saldo actual: " + saldo);
            return false;
        }
        
        this.saldo -= monto;
        return true;
    }
    
    /**
     * Verifica si hay saldo suficiente.
     * 
     * @param monto Cantidad a verificar
     * @return true si hay saldo suficiente
     */
    public boolean tieneSaldoSuficiente(double monto) {
        return saldo >= monto && monto >= 0;
    }
    
    // ==================== Gestión de Historial ====================
    
    /**
     * Registra una transacción en el historial local de la billetera.
     * 
     * @param transaccion Transacción a registrar
     */
    public void registrarTransaccion(Transaccion transaccion) {
        historialLocal.add(transaccion);
    }
    
    /**
     * Obtiene el número de transacciones de esta billetera.
     * 
     * @return Cantidad de transacciones
     */
    public int cantidadTransacciones() {
        return historialLocal.size();
    }
    
    /**
     * Obtiene las últimas N transacciones.
     * Utiliza recursividad para demostrar su aplicación.
     * 
     * @param n Número de transacciones a obtener
     * @return Lista de las últimas n transacciones
     */
    public LinkedList<Transaccion> obtenerUltimas(int n) {
        LinkedList<Transaccion> resultado = new LinkedList<>();
        int inicio = Math.max(0, historialLocal.size() - n);
        
        // Recursión para copiar elementos
        copiarDesde(resultado, inicio);
        return resultado;
    }
    
    /**
     * Método auxiliar recursivo para copiar transacciones desde un índice.
     * 
     * @param resultado Lista donde acumular resultados
     * @param indice Índice actual
     */
    private void copiarDesde(LinkedList<Transaccion> resultado, int indice) {
        if (indice >= historialLocal.size()) {
            return;
        }
        resultado.add(historialLocal.get(indice));
        copiarDesde(resultado, indice + 1);
    }
    
    /**
     * Calcula el monto total movido en esta billetera.
     * 
     * @return Suma de todos los montos de transacciones
     */
    public double calcularMontoTotalMovido() {
        double total = 0;
        for (Transaccion t : historialLocal) {
            total += t.getMonto();
        }
        return total;
    }
    
    @Override
    public String toString() {
        return "Billetera{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", saldo=" + saldo +
                ", activa=" + activa +
                ", transacciones=" + historialLocal.size() +
                '}';
    }
}
