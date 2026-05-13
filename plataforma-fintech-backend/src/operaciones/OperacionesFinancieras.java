package operaciones;

import modelos.*;
import estructuras.*;
import validaciones.*;

/**
 * Clase que implementa todas las operaciones financieras básicas del sistema.
 * Propósito: Centralizar la lógica de transacciones y usar las estructuras de datos.
 * 
 * @author Compañero 1
 * @version 1.0
 */
public class OperacionesFinancieras {
    
    private TablaHash<String, Usuario> usuarios;
    private Pila<Transaccion> pilaDeshecho;
    private java.util.LinkedList<Transaccion> historialGlobal;
    
    /**
     * Constructor que inicializa el sistema de operaciones financieras.
     */
    public OperacionesFinancieras() {
        this.usuarios = new TablaHash<>();
        this.pilaDeshecho = new Pila<>();
        this.historialGlobal = new java.util.LinkedList<>();
    }
    
    // ==================== Gestión de Usuarios ====================
    
    /**
     * Registra un nuevo usuario en el sistema.
     * Tiempo: O(1) en promedio (Tabla Hash)
     * 
     * @param id ID del usuario
     * @param nombre Nombre del usuario
     * @param email Email del usuario
     * @return true si se registró, false si ya existe
     */
    public boolean registrarUsuario(String id, String nombre, String email) {
        if (id == null || id.trim().isEmpty()) {
            System.out.println("[ERROR] ID de usuario inválido.");
            return false;
        }
        
        if (usuarios.contiene(id)) {
            System.out.println("[ERROR] El usuario " + id + " ya está registrado.");
            return false;
        }
        
        Usuario usuario = new Usuario(id, nombre, email);
        usuarios.insertar(id, usuario);
        System.out.println("[ÉXITO] Usuario " + nombre + " registrado exitosamente.");
        return true;
    }
    
    /**
     * Busca un usuario por su ID.
     * Tiempo: O(1) en promedio (Tabla Hash)
     * 
     * @param id ID del usuario
     * @return Usuario si existe, null en caso contrario
     */
    public Usuario obtenerUsuario(String id) {
        Usuario usuario = usuarios.obtener(id);
        if (usuario == null) {
            System.out.println("[ERROR] El usuario " + id + " no existe.");
        }
        return usuario;
    }
    
    /**
     * Verifica si un usuario existe.
     * Tiempo: O(1) en promedio
     * 
     * @param id ID del usuario
     * @return true si existe
     */
    public boolean usuarioExiste(String id) {
        return usuarios.contiene(id);
    }
    
    /**
     * Obtiene el número de usuarios registrados.
     * Tiempo: O(1)
     * 
     * @return Cantidad de usuarios
     */
    public int cantidadUsuarios() {
        return usuarios.tamaño();
    }
    
    // ==================== Gestión de Billeteras ====================
    
    /**
     * Crea una nueva billetera para un usuario.
     * Tiempo: O(1) en promedio
     * 
     * @param idUsuario ID del usuario
     * @param codigoBilletera Código de la billetera
     * @param nombreBilletera Nombre de la billetera
     * @param tipo Tipo de billetera
     * @return true si se creó, false en caso contrario
     */
    public boolean crearBilletera(String idUsuario, String codigoBilletera, 
                                 String nombreBilletera, String tipo) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return false;
        }
        
        return usuario.crearBilletera(codigoBilletera, nombreBilletera, tipo);
    }
    
    /**
     * Obtiene una billetera de un usuario.
     * Tiempo: O(1) en promedio
     * 
     * @param idUsuario ID del usuario
     * @param codigoBilletera Código de la billetera
     * @return Billetera si existe, null en caso contrario
     */
    public Billetera obtenerBilletera(String idUsuario, String codigoBilletera) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return null;
        }
        
        return usuario.obtenerBilletera(codigoBilletera);
    }
    
    // ==================== Operaciones Financieras Básicas ====================
    
    /**
     * Recarga dinero en una billetera.
     * Tiempo: O(1) en promedio
     * 
     * @param idUsuario ID del usuario
     * @param codigoBilletera Código de la billetera
     * @param monto Monto a recargar
     * @return true si se realizó, false en caso contrario
     */
    public boolean recargar(String idUsuario, String codigoBilletera, double monto) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return false;
        }
        
        if (!ValidadorFinanciero.validarRecarga(usuario, codigoBilletera, monto)) {
            return false;
        }
        
        Billetera billetera = usuario.obtenerBilletera(codigoBilletera);
        
        if (!billetera.aumentarSaldo(monto)) {
            return false;
        }
        
        // Crear transacción
        Transaccion transaccion = new Transaccion(
            "RECARGA", monto, codigoBilletera, codigoBilletera,
            idUsuario, idUsuario, "Recarga de saldo"
        );
        
        // Calcular puntos (1 punto por cada 100 unidades)
        double puntos = (monto / 100.0);
        transaccion.setPuntosGenerados(puntos);
        
        // Registrar operación
        usuario.registrarTransaccion(transaccion);
        billetera.registrarTransaccion(transaccion);
        historialGlobal.add(transaccion);
        
        // Acumular puntos
        usuario.agregarPuntos(puntos);
        
        // Guardar en pila para posible reversión
        pilaDeshecho.apilar(transaccion);
        
        System.out.println("[ÉXITO] Recarga de $" + String.format("%.2f", monto) + 
                         " realizada en billetera " + codigoBilletera);
        
        return true;
    }
    
    /**
     * Retira dinero de una billetera.
     * Tiempo: O(1) en promedio
     * 
     * @param idUsuario ID del usuario
     * @param codigoBilletera Código de la billetera
     * @param monto Monto a retirar
     * @return true si se realizó, false en caso contrario
     */
    public boolean retirar(String idUsuario, String codigoBilletera, double monto) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return false;
        }
        
        if (!ValidadorFinanciero.validarRetiro(usuario, codigoBilletera, monto)) {
            return false;
        }
        
        Billetera billetera = usuario.obtenerBilletera(codigoBilletera);
        
        if (!billetera.disminuirSaldo(monto)) {
            return false;
        }
        
        // Crear transacción
        Transaccion transaccion = new Transaccion(
            "RETIRO", monto, codigoBilletera, codigoBilletera,
            idUsuario, idUsuario, "Retiro de saldo"
        );
        
        // Calcular puntos (2 puntos por cada 100 unidades)
        double puntos = (monto / 100.0) * 2;
        transaccion.setPuntosGenerados(puntos);
        
        // Registrar operación
        usuario.registrarTransaccion(transaccion);
        billetera.registrarTransaccion(transaccion);
        historialGlobal.add(transaccion);
        
        // Acumular puntos
        usuario.agregarPuntos(puntos);
        
        // Guardar en pila para posible reversión
        pilaDeshecho.apilar(transaccion);
        
        System.out.println("[ÉXITO] Retiro de $" + String.format("%.2f", monto) + 
                         " realizado de billetera " + codigoBilletera);
        
        return true;
    }
    
    /**
     * Transfiere dinero entre billeteras del mismo usuario.
     * Tiempo: O(1) en promedio
     * 
     * @param idUsuario ID del usuario
     * @param codigoOrigen Código billetera origen
     * @param codigoDestino Código billetera destino
     * @param monto Monto a transferir
     * @return true si se realizó, false en caso contrario
     */
    public boolean transferirInterno(String idUsuario, String codigoOrigen, 
                                     String codigoDestino, double monto) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return false;
        }
        
        if (!ValidadorFinanciero.validarTransferenciaInterna(usuario, codigoOrigen, 
                                                            codigoDestino, monto)) {
            return false;
        }
        
        Billetera billeteraOrigen = usuario.obtenerBilletera(codigoOrigen);
        Billetera billeteraDestino = usuario.obtenerBilletera(codigoDestino);
        
        // Descontar de origen
        if (!billeteraOrigen.disminuirSaldo(monto)) {
            return false;
        }
        
        // Agregar a destino
        if (!billeteraDestino.aumentarSaldo(monto)) {
            // Revertir si hay error
            billeteraOrigen.aumentarSaldo(monto);
            return false;
        }
        
        // Crear transacción
        Transaccion transaccion = new Transaccion(
            "TRANSFERENCIA", monto, codigoOrigen, codigoDestino,
            idUsuario, idUsuario, "Transferencia interna"
        );
        
        // Calcular puntos (3 puntos por cada 100 unidades)
        double puntos = (monto / 100.0) * 3;
        transaccion.setPuntosGenerados(puntos);
        
        // Registrar operación
        usuario.registrarTransaccion(transaccion);
        billeteraOrigen.registrarTransaccion(transaccion);
        billeteraDestino.registrarTransaccion(transaccion);
        historialGlobal.add(transaccion);
        
        // Acumular puntos
        usuario.agregarPuntos(puntos);
        
        // Guardar en pila para posible reversión
        pilaDeshecho.apilar(transaccion);
        
        System.out.println("[ÉXITO] Transferencia de $" + String.format("%.2f", monto) + 
                         " realizada de " + codigoOrigen + " a " + codigoDestino);
        
        return true;
    }
    
    /**
     * Transfiere dinero entre billeteras de diferentes usuarios.
     * Tiempo: O(1) en promedio
     * 
     * @param idUsuarioOrigen ID del usuario origen
     * @param codigoOrigen Código billetera origen
     * @param idUsuarioDestino ID del usuario destino
     * @param codigoDestino Código billetera destino
     * @param monto Monto a transferir
     * @return true si se realizó, false en caso contrario
     */
    public boolean transferirExterno(String idUsuarioOrigen, String codigoOrigen,
                                     String idUsuarioDestino, String codigoDestino,
                                     double monto) {
        Usuario usuarioOrigen = obtenerUsuario(idUsuarioOrigen);
        Usuario usuarioDestino = obtenerUsuario(idUsuarioDestino);
        
        if (usuarioOrigen == null || usuarioDestino == null) {
            return false;
        }
        
        if (!ValidadorFinanciero.validarTransferenciaExterna(usuarioOrigen, codigoOrigen,
                                                            usuarioDestino, codigoDestino,
                                                            monto)) {
            return false;
        }
        
        Billetera billeteraOrigen = usuarioOrigen.obtenerBilletera(codigoOrigen);
        Billetera billeteraDestino = usuarioDestino.obtenerBilletera(codigoDestino);
        
        // Descontar de origen
        if (!billeteraOrigen.disminuirSaldo(monto)) {
            return false;
        }
        
        // Agregar a destino
        if (!billeteraDestino.aumentarSaldo(monto)) {
            // Revertir si hay error
            billeteraOrigen.aumentarSaldo(monto);
            return false;
        }
        
        // Crear transacción de envío (origen)
        Transaccion transaccionEnvio = new Transaccion(
            "TRANSFERENCIA", monto, codigoOrigen, codigoDestino,
            idUsuarioOrigen, idUsuarioDestino, "Transferencia a otro usuario"
        );
        
        // Calcular puntos (3 puntos por cada 100 unidades)
        double puntos = (monto / 100.0) * 3;
        transaccionEnvio.setPuntosGenerados(puntos);
        
        // Crear transacción de recepción (destino)
        Transaccion transaccionRecepcion = new Transaccion(
            "TRANSFERENCIA_RECIBIDA", monto, codigoOrigen, codigoDestino,
            idUsuarioOrigen, idUsuarioDestino, "Transferencia recibida"
        );
        transaccionRecepcion.setPuntosGenerados(0); // No se dan puntos por recibir
        
        // Registrar operaciones
        usuarioOrigen.registrarTransaccion(transaccionEnvio);
        billeteraOrigen.registrarTransaccion(transaccionEnvio);
        historialGlobal.add(transaccionEnvio);
        
        usuarioDestino.registrarTransaccion(transaccionRecepcion);
        billeteraDestino.registrarTransaccion(transaccionRecepcion);
        historialGlobal.add(transaccionRecepcion);
        
        // Acumular puntos solo al que envía
        usuarioOrigen.agregarPuntos(puntos);
        
        // Guardar en pila para posible reversión
        pilaDeshecho.apilar(transaccionEnvio);
        pilaDeshecho.apilar(transaccionRecepcion);
        
        System.out.println("[ÉXITO] Transferencia de $" + String.format("%.2f", monto) + 
                         " realizada de " + idUsuarioOrigen + " a " + idUsuarioDestino);
        
        return true;
    }
    
    // ==================== Reversión de Operaciones ====================
    
    /**
     * Revierte la última operación realizada.
     * Utiliza la Pila (LIFO) para obtener la última transacción.
     * Tiempo: O(1) en promedio
     * 
     * @return true si se revertió, false en caso contrario
     */
    public boolean deshacerOperacion() {
        if (pilaDeshecho.estaVacia()) {
            System.out.println("[ERROR] No hay operaciones para deshacer.");
            return false;
        }
        
        Transaccion transaccion = pilaDeshecho.desapilar();
        
        if (!ValidadorFinanciero.validarReversibilidad(transaccion)) {
            pilaDeshecho.apilar(transaccion); // Volver a apilar
            return false;
        }
        
        Usuario usuarioOrigen = obtenerUsuario(transaccion.getUsuarioOrigen());
        if (usuarioOrigen == null) {
            return false;
        }
        
        Billetera billeteraOrigen = usuarioOrigen.obtenerBilletera(transaccion.getBilleteraOrigen());
        if (billeteraOrigen == null) {
            return false;
        }
        
        // Revertir según tipo de transacción
        String tipo = transaccion.getTipo();
        
        if ("RECARGA".equals(tipo) || "TRANSFERENCIA_RECIBIDA".equals(tipo)) {
            // Se debe restar
            billeteraOrigen.disminuirSaldo(transaccion.getMonto());
        } else if ("RETIRO".equals(tipo) || "TRANSFERENCIA".equals(tipo)) {
            // Se debe sumar
            billeteraOrigen.aumentarSaldo(transaccion.getMonto());
            
            // Si es transferencia externa, también revertir destino
            if ("TRANSFERENCIA".equals(tipo)) {
                Usuario usuarioDestino = obtenerUsuario(transaccion.getUsuarioDestino());
                if (usuarioDestino != null) {
                    Billetera billeteraDestino = usuarioDestino.obtenerBilletera(
                        transaccion.getBilleteraDestino()
                    );
                    if (billeteraDestino != null) {
                        billeteraDestino.disminuirSaldo(transaccion.getMonto());
                    }
                }
            }
        }
        
        // Descontar puntos
        usuarioOrigen.descontarPuntos(transaccion.getPuntosGenerados());
        
        // Marcar como revertida
        transaccion.marcarComoRevertida();
        
        System.out.println("[ÉXITO] Operación revertida: " + transaccion.getId());
        
        return true;
    }
    
    /**
     * Obtiene el tamaño de la pila de deshacer.
     * Indica cuántas operaciones se pueden revertir.
     * 
     * @return Cantidad de operaciones en pila
     */
    public int cantidadOperacionesReversibles() {
        return pilaDeshecho.tamaño();
    }
    
    // ==================== Consultas ====================
    
    /**
     * Obtiene el saldo total de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @return Saldo total en todas sus billeteras
     */
    public double obtenerSaldoTotal(String idUsuario) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return 0.0;
        }
        return usuario.calcularSaldoTotal();
    }
    
    /**
     * Obtiene el saldo de una billetera específica.
     * 
     * @param idUsuario ID del usuario
     * @param codigoBilletera Código de la billetera
     * @return Saldo de la billetera
     */
    public double obtenerSaldo(String idUsuario, String codigoBilletera) {
        Billetera billetera = obtenerBilletera(idUsuario, codigoBilletera);
        if (billetera == null) {
            return 0.0;
        }
        return billetera.getSaldo();
    }
    
    /**
     * Obtiene el historial de transacciones de un usuario.
     * Utiliza LinkedList internamente.
     * Tiempo: O(n)
     * 
     * @param idUsuario ID del usuario
     * @return Historial de transacciones
     */
    public java.util.LinkedList<Transaccion> obtenerHistorialUsuario(String idUsuario) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return new java.util.LinkedList<>();
        }
        return usuario.obtenerHistorial();
    }
    
    /**
     * Obtiene el historial global de todas las transacciones.
     * 
     * @return Lista de transacciones globales
     */
    public java.util.LinkedList<Transaccion> obtenerHistorialGlobal() {
        return new java.util.LinkedList<>(historialGlobal);
    }
    
    /**
     * Obtiene información completa de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @return String con detalles del usuario
     */
    public String obtenerDetallesUsuario(String idUsuario) {
        Usuario usuario = obtenerUsuario(idUsuario);
        if (usuario == null) {
            return "[ERROR] usuario no encontrado.";
        }
        return usuario.toString();
    }
}
