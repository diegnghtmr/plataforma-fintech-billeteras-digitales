package validaciones;

import modelos.*;

/**
 * Clase encargada de validar todas las operaciones financieras.
 * Propósito: Garantizar consistencia de datos y validez de transacciones.
 * 
 * @author Compañero 1
 * @version 1.0
 */
public class ValidadorFinanciero {
    
    private static final double MONTO_MINIMO = 0.01;
    private static final double MONTO_MAXIMO = 1000000.0;
    
    /**
     * Valida si un usuario existe y es válido.
     * 
     * @param usuario Usuario a validar
     * @return true si el usuario es válido
     */
    public static boolean validarUsuario(Usuario usuario) {
        if (usuario == null) {
            System.out.println("[ERROR] El usuario es nulo.");
            return false;
        }
        
        if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
            System.out.println("[ERROR] El usuario no tiene ID válido.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida si una billetera existe y es válida.
     * 
     * @param billetera Billetera a validar
     * @return true si la billetera es válida
     */
    public static boolean validarBilletera(Billetera billetera) {
        if (billetera == null) {
            System.out.println("[ERROR] La billetera es nula.");
            return false;
        }
        
        if (!billetera.isActiva()) {
            System.out.println("[ERROR] La billetera " + billetera.getCodigo() + " no está activa.");
            return false;
        }
        
        if (billetera.getSaldo() < 0) {
            System.out.println("[ERROR] Saldo negativo en billetera: " + billetera.getCodigo());
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida un monto de dinero.
     * 
     * @param monto Monto a validar
     * @return true si el monto es válido
     */
    public static boolean validarMonto(double monto) {
        if (monto < MONTO_MINIMO) {
            System.out.println("[ERROR] El monto debe ser mayor a " + MONTO_MINIMO);
            return false;
        }
        
        if (monto > MONTO_MAXIMO) {
            System.out.println("[ERROR] El monto excede el máximo permitido: " + MONTO_MAXIMO);
            return false;
        }
        
        if (Double.isNaN(monto) || Double.isInfinite(monto)) {
            System.out.println("[ERROR] El monto contiene un valor inválido.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida si hay saldo suficiente en una billetera.
     * 
     * @param billetera Billetera a verificar
     * @param monto Monto a retirar
     * @return true si hay saldo suficiente
     */
    public static boolean validarSaldoSuficiente(Billetera billetera, double monto) {
        if (billetera == null) {
            System.out.println("[ERROR] Billetera nula para validación de saldo.");
            return false;
        }
        
        if (!validarMonto(monto)) {
            return false;
        }
        
        if (billetera.getSaldo() < monto) {
            System.out.println("[ERROR] Saldo insuficiente. Disponible: " + billetera.getSaldo() + 
                             ", Solicitado: " + monto);
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida si una transacción es reversible.
     * 
     * @param transaccion Transacción a validar
     * @return true si puede revertirse
     */
    public static boolean validarReversibilidad(Transaccion transaccion) {
        if (transaccion == null) {
            System.out.println("[ERROR] Transacción nula.");
            return false;
        }
        
        if (!transaccion.isReversible()) {
            System.out.println("[ERROR] La transacción no es reversible: " + transaccion.getId());
            return false;
        }
        
        if ("REVERTIDA".equals(transaccion.getEstado())) {
            System.out.println("[ERROR] La transacción ya fue revertida: " + transaccion.getId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida una operación de recarga.
     * 
     * @param usuario Usuario que recarga
     * @param codigoBilletera Código de la billetera
     * @param monto Monto a recargar
     * @return true si la recarga es válida
     */
    public static boolean validarRecarga(Usuario usuario, String codigoBilletera, double monto) {
        if (!validarUsuario(usuario)) {
            return false;
        }
        
        if (!validarMonto(monto)) {
            return false;
        }
        
        if (!usuario.tieneBilletera(codigoBilletera)) {
            System.out.println("[ERROR] El usuario no tiene la billetera: " + codigoBilletera);
            return false;
        }
        
        Billetera billetera = usuario.obtenerBilletera(codigoBilletera);
        if (!validarBilletera(billetera)) {
            return false;
        }
        
        // Validar que no se exceda el saldo máximo
        if (billetera.getSaldo() + monto > 1000000.0) {
            System.out.println("[ERROR] La recarga excedería el saldo máximo permitido.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida una operación de retiro.
     * 
     * @param usuario Usuario que retira
     * @param codigoBilletera Código de la billetera
     * @param monto Monto a retirar
     * @return true si el retiro es válido
     */
    public static boolean validarRetiro(Usuario usuario, String codigoBilletera, double monto) {
        if (!validarUsuario(usuario)) {
            return false;
        }
        
        if (!validarMonto(monto)) {
            return false;
        }
        
        if (!usuario.tieneBilletera(codigoBilletera)) {
            System.out.println("[ERROR] El usuario no tiene la billetera: " + codigoBilletera);
            return false;
        }
        
        Billetera billetera = usuario.obtenerBilletera(codigoBilletera);
        if (!validarBilletera(billetera)) {
            return false;
        }
        
        return validarSaldoSuficiente(billetera, monto);
    }
    
    /**
     * Valida una operación de transferencia entre billeteras del mismo usuario.
     * 
     * @param usuario Usuario
     * @param codigoOrigen Billetera origen
     * @param codigoDestino Billetera destino
     * @param monto Monto a transferir
     * @return true si la transferencia es válida
     */
    public static boolean validarTransferenciaInterna(Usuario usuario, String codigoOrigen, 
                                                      String codigoDestino, double monto) {
        if (!validarUsuario(usuario)) {
            return false;
        }
        
        if (!validarMonto(monto)) {
            return false;
        }
        
        if (!usuario.tieneBilletera(codigoOrigen)) {
            System.out.println("[ERROR] El usuario no tiene la billetera origen: " + codigoOrigen);
            return false;
        }
        
        if (!usuario.tieneBilletera(codigoDestino)) {
            System.out.println("[ERROR] El usuario no tiene la billetera destino: " + codigoDestino);
            return false;
        }
        
        if (codigoOrigen.equals(codigoDestino)) {
            System.out.println("[ERROR] No se puede transferir a la misma billetera.");
            return false;
        }
        
        Billetera billeteraOrigen = usuario.obtenerBilletera(codigoOrigen);
        Billetera billeteraDestino = usuario.obtenerBilletera(codigoDestino);
        
        if (!validarBilletera(billeteraOrigen) || !validarBilletera(billeteraDestino)) {
            return false;
        }
        
        return validarSaldoSuficiente(billeteraOrigen, monto);
    }
    
    /**
     * Valida una operación de transferencia entre usuarios.
     * 
     * @param usuarioOrigen Usuario que envía dinero
     * @param codigoOrigen Billetera origen
     * @param usuarioDestino Usuario que recibe dinero
     * @param codigoDestino Billetera destino
     * @param monto Monto a transferir
     * @return true si la transferencia es válida
     */
    public static boolean validarTransferenciaExterna(Usuario usuarioOrigen, String codigoOrigen,
                                                    Usuario usuarioDestino, String codigoDestino,
                                                    double monto) {
        if (!validarUsuario(usuarioOrigen) || !validarUsuario(usuarioDestino)) {
            return false;
        }

        if (usuarioOrigen.getId().equals(usuarioDestino.getId())) {
            System.out.println("[ERROR] No se puede transferir a la misma cuenta.");
            return false;
        }

        if (!validarMonto(monto)) {
            return false;
        }

        if (!usuarioOrigen.tieneBilletera(codigoOrigen)) {
            System.out.println("[ERROR] El usuario origen no tiene la billetera: " + codigoOrigen);
            return false;
        }

        Billetera billeteraOrigen = usuarioOrigen.obtenerBilletera(codigoOrigen);
        if (!validarBilletera(billeteraOrigen)) {
            return false;
        }

        if (!validarSaldoSuficiente(billeteraOrigen, monto)) {
            return false;
        }

        if (!usuarioDestino.tieneBilletera(codigoDestino)) {
            System.out.println("[ERROR] El usuario destino no tiene la billetera: " + codigoDestino);
            return false;
        }

        Billetera billeteraDestino = usuarioDestino.obtenerBilletera(codigoDestino);
        if (!validarBilletera(billeteraDestino)) {
            return false;
        }

        if (billeteraDestino.getSaldo() + monto > 1000000.0) {
            System.out.println("[ERROR] La transferencia haria exceder el saldo maximo en destino.");
            return false;
        }

        return true;
    }
    
    /**
     * Valida los detalles de una transacción.
     * 
     * @param transaccion Transacción a validar
     * @return true si la transacción es válida
     */
    public static boolean validarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            System.out.println("[ERROR] Transacción nula.");
            return false;
        }
        
        if (transaccion.getId() == null || transaccion.getId().trim().isEmpty()) {
            System.out.println("[ERROR] ID de transacción inválido.");
            return false;
        }
        
        if (!validarMonto(transaccion.getMonto())) {
            return false;
        }
        
        if (transaccion.getTipo() == null || transaccion.getTipo().trim().isEmpty()) {
            System.out.println("[ERROR] Tipo de transacción no especificado.");
            return false;
        }
        
        return true;
    }
}
