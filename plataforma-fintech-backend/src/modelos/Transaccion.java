package modelos;

import java.util.*;

/**
 * Clase que representa una Transacción en la plataforma fintech.
 * Registra detalles de cada operación financiera realizada.
 * 
 * @author Compañero 1
 * @version 1.0
 */
public class Transaccion {
    private String id;
    private long timestamp;
    private String tipo; // RECARGA, RETIRO, TRANSFERENCIA, TRANSFERENCIA_RECIBIDA
    private double monto;
    private String billeteraOrigen;
    private String billeteraDestino;
    private String usuarioOrigen;
    private String usuarioDestino;
    private String estado; // EXITOSA, REVERTIDA, PENDIENTE, RECHAZADA
    private double puntosGenerados;
    private String descripcion;
    private boolean reversible;
    
    /**
     * Constructor para crear una transacción.
     * 
     * @param tipo Tipo de transacción
     * @param monto Cantidad de dinero
     * @param billeteraOrigen Código de billetera origen
     * @param billeteraDestino Código de billetera destino
     * @param usuarioOrigen ID del usuario origen
     * @param usuarioDestino ID del usuario destino
     * @param descripcion Descripción adicional
     */
    public Transaccion(String tipo, double monto, String billeteraOrigen, 
                      String billeteraDestino, String usuarioOrigen, 
                      String usuarioDestino, String descripcion) {
        this.id = generarId();
        this.timestamp = System.currentTimeMillis();
        this.tipo = tipo;
        this.monto = monto;
        this.billeteraOrigen = billeteraOrigen;
        this.billeteraDestino = billeteraDestino;
        this.usuarioOrigen = usuarioOrigen;
        this.usuarioDestino = usuarioDestino;
        this.estado = "EXITOSA";
        this.puntosGenerados = 0;
        this.descripcion = descripcion;
        this.reversible = true;
    }
    
    // ==================== Generación de ID ====================
    
    /**
     * Genera un identificador único para la transacción.
     * 
     * @return ID único
     */
    private String generarId() {
        return "TXN" + System.nanoTime();
    }
    
    // ==================== Getters ====================
    
    public String getId() {
        return id;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public double getMonto() {
        return monto;
    }
    
    public String getBilleteraOrigen() {
        return billeteraOrigen;
    }
    
    public String getBilleteraDestino() {
        return billeteraDestino;
    }
    
    public String getUsuarioOrigen() {
        return usuarioOrigen;
    }
    
    public String getUsuarioDestino() {
        return usuarioDestino;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public double getPuntosGenerados() {
        return puntosGenerados;
    }
    
    public void setPuntosGenerados(double puntos) {
        this.puntosGenerados = puntos;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public boolean isReversible() {
        return reversible;
    }
    
    public void setReversible(boolean reversible) {
        this.reversible = reversible;
    }
    
    // ==================== Métodos de Validación ====================
    
    /**
     * Valida que la transacción sea reversible.
     * Las transacciones revertidas no pueden revertirse nuevamente.
     * 
     * @return true si es reversible
     */
    public boolean puedeRevertirse() {
        return reversible && !estado.equals("REVERTIDA");
    }
    
    /**
     * Marca la transacción como revertida.
     * 
     * @return true si se pudo revertir
     */
    public boolean marcarComoRevertida() {
        if (puedeRevertirse()) {
            this.estado = "REVERTIDA";
            this.reversible = false;
            return true;
        }
        return false;
    }
    
    // ==================== Métodos de Información ====================
    
    /**
     * Obtiene una descripción completa de la transacción.
     * 
     * @return String con los detalles
     */
    public String obtenerDetalles() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(id).append("\n");
        sb.append("Tipo: ").append(tipo).append("\n");
        sb.append("Monto: $").append(String.format("%.2f", monto)).append("\n");
        sb.append("Billetera origen: ").append(billeteraOrigen).append("\n");
        sb.append("Billetera destino: ").append(billeteraDestino).append("\n");
        sb.append("Usuario origen: ").append(usuarioOrigen).append("\n");
        sb.append("Usuario destino: ").append(usuarioDestino).append("\n");
        sb.append("Estado: ").append(estado).append("\n");
        sb.append("Puntos generados: ").append(puntosGenerados).append("\n");
        sb.append("Descripción: ").append(descripcion).append("\n");
        sb.append("Fecha: ").append(new java.util.Date(timestamp)).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Crea una copia de la transacción para propósitos de auditoría.
     * 
     * @return Copia profunda de la transacción
     */
    public Transaccion crearCopia() {
        Transaccion copia = new Transaccion(
            this.tipo, this.monto, this.billeteraOrigen,
            this.billeteraDestino, this.usuarioOrigen,
            this.usuarioDestino, this.descripcion
        );
        copia.id = this.id;
        copia.timestamp = this.timestamp;
        copia.estado = this.estado;
        copia.puntosGenerados = this.puntosGenerados;
        copia.reversible = this.reversible;
        return copia;
    }
    
    @Override
    public String toString() {
        return "Transaccion{" +
                "id='" + id + '\'' +
                ", tipo='" + tipo + '\'' +
                ", monto=" + monto +
                ", estado='" + estado + '\'' +
                ", puntosGenerados=" + puntosGenerados +
                ", fecha=" + new Date(timestamp) +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaccion that = (Transaccion) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
