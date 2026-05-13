package main;

import operaciones.OperacionesFinancieras;
import operaciones.GestorOperaciones;
import operaciones.GestorOperaciones.IntegracionComp1;
import operaciones.AnaliticaFinanciera;
import operaciones.AnaliticaFinanciera.FuenteDatos;
import operaciones.DetectorFraude;
import modelos.*;
import estructuras.GrafoTransferencias;
import java.util.HashMap;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   PLATAFORMA FINTECH DE BILLETERAS DIGITALES - VERSION 3.0   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        OperacionesFinancieras operaciones = new OperacionesFinancieras();

        // =====================================================================
        // PRUEBA 1: Registro de Usuarios
        // =====================================================================
        System.out.println("┌─ PRUEBA 1: Registro de Usuarios ──────────────────────────────┐");
        operaciones.registrarUsuario("USR001", "Juan Perez", "juan@example.com");
        operaciones.registrarUsuario("USR002", "Maria Garcia", "maria@example.com");
        operaciones.registrarUsuario("USR003", "Carlos Lopez", "carlos@example.com");
        System.out.println("Total de usuarios registrados: " + operaciones.cantidadUsuarios());
        System.out.println();

        // =====================================================================
        // PRUEBA 2: Creacion de Billeteras
        // =====================================================================
        System.out.println("┌─ PRUEBA 2: Creacion de Billeteras ────────────────────────────┐");
        operaciones.crearBilletera("USR001", "BIL001", "Gastos Diarios", "Gastos diarios");
        operaciones.crearBilletera("USR001", "BIL002", "Ahorro Mensual", "Ahorro");
        operaciones.crearBilletera("USR001", "BIL003", "Transporte", "Transporte");
        operaciones.crearBilletera("USR002", "BIL004", "Gastos Principales", "Gastos diarios");
        operaciones.crearBilletera("USR002", "BIL005", "Inversion", "Inversion");
        operaciones.crearBilletera("USR003", "BIL006", "Mi Billetera", "Gastos diarios");
        System.out.println();

        // =====================================================================
        // PRUEBA 3: Recargas
        // =====================================================================
        System.out.println("┌─ PRUEBA 3: Operacion de Recarga ──────────────────────────────┐");
        operaciones.recargar("USR001", "BIL001", 5000.00);
        operaciones.recargar("USR001", "BIL002", 10000.00);
        operaciones.recargar("USR001", "BIL003", 2000.00);
        operaciones.recargar("USR002", "BIL004", 8000.00);
        operaciones.recargar("USR002", "BIL005", 15000.00);
        operaciones.recargar("USR003", "BIL006", 3000.00);
        System.out.println("\nSaldo total Juan:   $" + String.format("%.2f", operaciones.obtenerSaldoTotal("USR001")));
        System.out.println("Saldo total Maria:  $" + String.format("%.2f", operaciones.obtenerSaldoTotal("USR002")));
        System.out.println("Saldo total Carlos: $" + String.format("%.2f", operaciones.obtenerSaldoTotal("USR003")));
        System.out.println();

        // =====================================================================
        // PRUEBA 4: Retiros
        // =====================================================================
        System.out.println("┌─ PRUEBA 4: Operacion de Retiro ───────────────────────────────┐");
        operaciones.retirar("USR001", "BIL001", 500.00);
        operaciones.retirar("USR002", "BIL005", 1000.00);
        System.out.println();

        // =====================================================================
        // PRUEBA 5: Transferencias Internas
        // =====================================================================
        System.out.println("┌─ PRUEBA 5: Transferencias Internas (mismo usuario) ───────────┐");
        operaciones.transferirInterno("USR001", "BIL001", "BIL002", 1000.00);
        operaciones.transferirInterno("USR002", "BIL005", "BIL004", 2000.00);
        System.out.println();

        // =====================================================================
        // PRUEBA 6: Transferencias Externas
        // =====================================================================
        System.out.println("┌─ PRUEBA 6: Transferencias Externas (entre usuarios) ──────────┐");
        operaciones.transferirExterno("USR001", "BIL001", "USR002", "BIL004", 500.00);
        operaciones.transferirExterno("USR002", "BIL004", "USR003", "BIL006", 300.00);
        System.out.println();

        // =====================================================================
        // PRUEBA 7: Consulta de Saldos
        // =====================================================================
        System.out.println("┌─ PRUEBA 7: Consulta de Saldos ────────────────────────────────┐");
        System.out.println("Saldo BIL001 (Juan):   $" + String.format("%.2f", operaciones.obtenerSaldo("USR001", "BIL001")));
        System.out.println("Saldo BIL002 (Juan):   $" + String.format("%.2f", operaciones.obtenerSaldo("USR001", "BIL002")));
        System.out.println("Saldo BIL004 (Maria):  $" + String.format("%.2f", operaciones.obtenerSaldo("USR002", "BIL004")));
        System.out.println("Saldo BIL006 (Carlos): $" + String.format("%.2f", operaciones.obtenerSaldo("USR003", "BIL006")));
        System.out.println();

        // =====================================================================
        // PRUEBA 8: Historial de Transacciones
        // =====================================================================
        System.out.println("┌─ PRUEBA 8: Historial de Transacciones ────────────────────────┐");
        LinkedList<Transaccion> historialJuan = operaciones.obtenerHistorialUsuario("USR001");
        System.out.println("Transacciones de Juan (Total: " + historialJuan.size() + "):");
        int contador = 1;
        for (Transaccion t : historialJuan) {
            System.out.println("  " + contador + ". " + t.getTipo() + " - $" +
                    String.format("%.2f", t.getMonto()) + " - Puntos: " + t.getPuntosGenerados());
            contador++;
        }
        System.out.println();

        // =====================================================================
        // PRUEBA 9: Puntos y Niveles
        // =====================================================================
        System.out.println("┌─ PRUEBA 9: Sistema de Puntos y Niveles ───────────────────────┐");
        Usuario juan   = operaciones.obtenerUsuario("USR001");
        Usuario maria  = operaciones.obtenerUsuario("USR002");
        Usuario carlos = operaciones.obtenerUsuario("USR003");
        System.out.println("Juan   - Puntos: " + juan.getPuntosAcumulados()   + " - Nivel: " + juan.getNivelFidelizacion());
        System.out.println("Maria  - Puntos: " + maria.getPuntosAcumulados()  + " - Nivel: " + maria.getNivelFidelizacion());
        System.out.println("Carlos - Puntos: " + carlos.getPuntosAcumulados() + " - Nivel: " + carlos.getNivelFidelizacion());
        System.out.println();

        // =====================================================================
        // PRUEBA 10: Reversion de Operaciones
        // =====================================================================
        System.out.println("┌─ PRUEBA 10: Reversion de Operaciones (Pila) ──────────────────┐");
        System.out.println("Operaciones reversibles: " + operaciones.cantidadOperacionesReversibles());
        double saldoAntes = operaciones.obtenerSaldo("USR003", "BIL006");
        System.out.println("Saldo Carlos antes de revertir: $" + String.format("%.2f", saldoAntes));
        operaciones.deshacerOperacion();
        double saldoDespues = operaciones.obtenerSaldo("USR003", "BIL006");
        System.out.println("Saldo Carlos despues de revertir: $" + String.format("%.2f", saldoDespues));
        System.out.println("Operaciones reversibles restantes: " + operaciones.cantidadOperacionesReversibles());
        System.out.println();

        // =====================================================================
        // PRUEBA 11: Validaciones
        // =====================================================================
        System.out.println("┌─ PRUEBA 11: Validaciones de Saldo ────────────────────────────┐");
        System.out.println("Intentando retirar mas de lo disponible:");
        operaciones.retirar("USR001", "BIL001", 100000.00);
        System.out.println("\nIntentando transferir desde billetera inexistente:");
        operaciones.transferirInterno("USR001", "BIL999", "BIL002", 100.00);
        System.out.println();

        // =====================================================================
        // PRUEBA 12: Informacion de Usuarios
        // =====================================================================
        System.out.println("┌─ PRUEBA 12: Informacion Detallada de Usuarios ────────────────┐");
        System.out.println(operaciones.obtenerDetallesUsuario("USR001"));
        System.out.println(operaciones.obtenerDetallesUsuario("USR002"));
        System.out.println();

        // =====================================================================
        // PRUEBA 13: Historial Global
        // =====================================================================
        System.out.println("┌─ PRUEBA 13: Historial Global de Transacciones ────────────────┐");
        LinkedList<Transaccion> historialGlobal = operaciones.obtenerHistorialGlobal();
        System.out.println("Total de transacciones en el sistema: " + historialGlobal.size());
        System.out.println("Primeras 5 transacciones:");
        int count = 0;
        for (Transaccion t : historialGlobal) {
            if (count < 5) {
                System.out.println("  - " + t.getTipo() + " | $" + String.format("%.2f", t.getMonto())
                        + " | " + t.getEstado());
                count++;
            }
        }
        System.out.println();

        // =====================================================================
        // MODULO 2 - LOGICA DE NEGOCIO
        // =====================================================================
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              MODULO 2 - LOGICA DE NEGOCIO                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        IntegracionComp1 adaptador = new IntegracionComp1() {
            @Override public boolean recargar(String id, String bil, double m) { return operaciones.recargar(id, bil, m); }
            @Override public boolean retirar(String id, String bil, double m)  { return operaciones.retirar(id, bil, m); }
            @Override public boolean transferirInterno(String id, String o, String d, double m) { return operaciones.transferirInterno(id, o, d, m); }
            @Override public boolean transferirExterno(String io, String bo, String id, String bd, double m) { return operaciones.transferirExterno(io, bo, id, bd, m); }
            @Override public double getSaldo(String id, String bil) { return operaciones.obtenerSaldo(id, bil); }
            @Override public String getNombreBilletera(String id, String bil) {
                Billetera b = operaciones.obtenerBilletera(id, bil);
                return b != null ? b.getNombre() : "?";
            }
        };

        GestorOperaciones motor = new GestorOperaciones(adaptador);
        motor.setUmbralSaldoBajo(200.0);

        // =====================================================================
        // PRUEBA 14: Registro en Sistema de Puntos
        // =====================================================================
        System.out.println("┌─ PRUEBA 14: Registro en Sistema de Puntos (Modulo 2) ─────────┐");
        motor.registrarUsuario("USR001", "Juan Perez");
        motor.registrarUsuario("USR002", "Maria Garcia");
        motor.registrarUsuario("USR003", "Carlos Lopez");
        System.out.println("USR001: " + motor.getPuntos("USR001") + " pts | Nivel: " + motor.getNivel("USR001"));
        System.out.println("USR002: " + motor.getPuntos("USR002") + " pts | Nivel: " + motor.getNivel("USR002"));
        System.out.println("USR003: " + motor.getPuntos("USR003") + " pts | Nivel: " + motor.getNivel("USR003"));
        System.out.println();

        // =====================================================================
        // PRUEBA 15: Cola de Prioridad - Operaciones Programadas
        // =====================================================================
        System.out.println("┌─ PRUEBA 15: Cola de Prioridad - Operaciones Programadas ──────┐");
        long ahora = System.currentTimeMillis();

        OperacionProgramada op3 = OperacionProgramada.recarga("USR001", "BIL001", 2000.0, ahora + 3000, "Recarga mensual");
        OperacionProgramada op1 = OperacionProgramada.retiro("USR002", "BIL004", 50.0, ahora + 1000, "Pago streaming");
        OperacionProgramada op2 = OperacionProgramada.transferenciaInterna("USR001", "BIL001", "BIL002", 500.0, ahora + 2000, "Mover a ahorro");

        motor.programarOperacion(op3);
        motor.programarOperacion(op1);
        motor.programarOperacion(op2);

        System.out.println("Pendientes en cola (min-heap): " + motor.operacionesPendientes());
        System.out.println("(La cola los ordenara por fecha al ejecutar)");
        System.out.println();

        // =====================================================================
        // PRUEBA 16: Simulacion de Ejecucion Automatica
        // =====================================================================
        System.out.println("┌─ PRUEBA 16: Simulacion de Ejecucion Automatica ───────────────┐");
        OperacionProgramada opA = OperacionProgramada.recarga("USR002", "BIL004", 1000.0, ahora - 200, "Ingreso nomina");
        OperacionProgramada opB = OperacionProgramada.retiro("USR002", "BIL004", 30.0, ahora - 100, "Pago suscripcion");
        motor.programarOperacion(opA);
        motor.programarOperacion(opB);

        double saldoAntesOp = operaciones.obtenerSaldo("USR002", "BIL004");
        System.out.println("Saldo USR002/BIL004 antes: $" + String.format("%.2f", saldoAntesOp));
        int ejecutadas = motor.ejecutarOperacionesVencidas();
        System.out.println("Operaciones ejecutadas: " + ejecutadas);
        System.out.println("Saldo USR002/BIL004 despues: $" + String.format("%.2f", operaciones.obtenerSaldo("USR002", "BIL004")));
        System.out.println();

        // =====================================================================
        // PRUEBA 17: Sistema de Puntos - Acumular
        // =====================================================================
        System.out.println("┌─ PRUEBA 17: Sistema de Puntos - Acumular ─────────────────────┐");
        motor.registrarTransaccionManual("USR001", "TRANSFERENCIA", 15000.0, "Gastos Diarios",
                operaciones.obtenerSaldo("USR001", "BIL001"));
        System.out.println("USR001 puntos: " + motor.getPuntos("USR001") + " pts");
        System.out.println("USR001 nivel:  " + motor.getNivel("USR001"));
        System.out.println("Puntos para siguiente nivel: " + motor.puntosParaSiguienteNivel("USR001"));
        System.out.println();

        // =====================================================================
        // PRUEBA 18: ArbolBST - Ranking por Puntos
        // =====================================================================
        System.out.println("┌─ PRUEBA 18: ArbolBST - Ranking por Puntos ────────────────────┐");
        motor.registrarTransaccionManual("USR002", "TRANSFERENCIA", 20000.0, "Gastos Principales",
                operaciones.obtenerSaldo("USR002", "BIL004"));
        motor.registrarTransaccionManual("USR003", "RECARGA", 3000.0, "Mi Billetera",
                operaciones.obtenerSaldo("USR003", "BIL006"));

        System.out.println("Ranking descendente (mayor a menor puntaje):");
        LinkedList<String> ranking = motor.rankingDescendente();
        int pos = 1;
        for (String id : ranking) {
            System.out.println("  #" + pos + " " + id + " | " + motor.getPuntos(id)
                    + " pts | Nivel: " + motor.getNivel(id));
            pos++;
        }
        System.out.println("\nUsuarios con 0-500 pts (Bronce):");
        System.out.println("  " + motor.buscarUsuariosPorRangoPuntos(0, 500));
        System.out.println("Altura del ArbolBST: " + motor.getSistemaPuntos().getArbol().altura());
        System.out.println();

        // =====================================================================
        // PRUEBA 19: Cola de Notificaciones FIFO
        // =====================================================================
        System.out.println("┌─ PRUEBA 19: Cola de Notificaciones FIFO ──────────────────────┐");
        System.out.println("Notificaciones pendientes de USR001:");
        motor.mostrarNotificaciones("USR001");
        System.out.println("Total pendientes USR001: " + motor.contarNotificacionesPendientes("USR001"));
        System.out.println("\nConsumir primera notificacion:");
        Notificacion n = motor.obtenerNotificacion("USR001");
        if (n != null) System.out.println("  Recibida: [" + n.getTipo() + "] " + n.getTitulo());
        System.out.println("Pendientes restantes: " + motor.contarNotificacionesPendientes("USR001"));
        System.out.println();

        // =====================================================================
        // PRUEBA 20: Alerta Saldo Bajo + Cancelacion de Operacion
        // =====================================================================
        System.out.println("┌─ PRUEBA 20: Alerta Saldo Bajo + Cancelacion de Operacion ─────┐");
        motor.registrarTransaccionManual("USR003", "RETIRO", 2900.0, "Mi Billetera",
                operaciones.obtenerSaldo("USR003", "BIL006"));
        System.out.println("Notificaciones de USR003:");
        motor.mostrarNotificaciones("USR003");

        OperacionProgramada opFutura = OperacionProgramada.retiro("USR001", "BIL001",
                9999.0, ahora + 60000, "Retiro a cancelar");
        motor.programarOperacion(opFutura);
        System.out.println("\nPendientes antes de cancelar: " + motor.operacionesPendientes());
        boolean cancelada = motor.cancelarOperacion(opFutura.getId());
        System.out.println("Cancelacion exitosa: " + cancelada);
        System.out.println("Pendientes despues:  " + motor.operacionesPendientes());
        System.out.println();

        // =====================================================================
        // MODULO 3 - ANALITICA + GRAFOS + DETECCION DE FRAUDE
        // =====================================================================
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        MODULO 3 - ANALITICA + GRAFOS + FRAUDE                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Adaptador FuenteDatos → conecta AnaliticaFinanciera con Comp.1
        FuenteDatos fuente = new FuenteDatos() {
            @Override
            public LinkedList<Transaccion> obtenerHistorialGlobal() {
                return operaciones.obtenerHistorialGlobal();
            }
            @Override
            public Usuario obtenerUsuario(String id) {
                return operaciones.obtenerUsuario(id);
            }
            @Override
            public Billetera obtenerBilletera(String idUsuario, String codigo) {
                return operaciones.obtenerBilletera(idUsuario, codigo);
            }
        };

        AnaliticaFinanciera analitica = new AnaliticaFinanciera(fuente);
        DetectorFraude detector = new DetectorFraude();

        // Transferencias adicionales para poblar el grafo con ciclos
        operaciones.recargar("USR001", "BIL001", 5000.00);
        operaciones.transferirExterno("USR001", "BIL001", "USR002", "BIL004", 400.00);
        operaciones.transferirExterno("USR001", "BIL001", "USR002", "BIL004", 400.00);
        operaciones.transferirExterno("USR002", "BIL004", "USR003", "BIL006", 200.00);
        operaciones.transferirExterno("USR003", "BIL006", "USR001", "BIL001", 100.00);

        // =====================================================================
        // PRUEBA 21: Construccion del Grafo de Transferencias
        // =====================================================================
        System.out.println("┌─ PRUEBA 21: Construccion del Grafo de Transferencias ─────────┐");
        analitica.reconstruirGrafo();
        GrafoTransferencias grafo = analitica.getGrafo();
        System.out.println("Nodos (usuarios): " + grafo.cantidadNodos());
        System.out.println("Aristas (rutas):  " + grafo.cantidadAristas());
        System.out.println(grafo);
        System.out.println();

        // =====================================================================
        // PRUEBA 22: BFS desde un nodo
        // =====================================================================
        System.out.println("┌─ PRUEBA 22: BFS - Recorrido por Niveles desde USR001 ─────────┐");
        System.out.println("Alcanzables desde USR001 (BFS): " + grafo.bfs("USR001"));
        System.out.println("Vecinos salientes de USR001:    " + grafo.vecinosSalientes("USR001"));
        System.out.println("Quien envia a USR001:           " + grafo.vecinosEntrantes("USR001"));
        System.out.println("Grado salida: " + grafo.gradoSalida("USR001") +
                           " | Grado entrada: " + grafo.gradoEntrada("USR001"));
        System.out.println();

        // =====================================================================
        // PRUEBA 23: DFS desde un nodo
        // =====================================================================
        System.out.println("┌─ PRUEBA 23: DFS - Recorrido en Profundidad desde USR001 ──────┐");
        System.out.println("Alcanzables desde USR001 (DFS): " + grafo.dfs("USR001"));
        System.out.println("\nRutas entre USR001 y USR003:");
        LinkedList<LinkedList<String>> rutas = grafo.encontrarRutas("USR001", "USR003");
        int r = 1;
        for (LinkedList<String> ruta : rutas) {
            System.out.println("  Ruta " + r++ + ": " + ruta);
        }
        System.out.println();

        // =====================================================================
        // PRUEBA 24: Deteccion de Ciclos
        // =====================================================================
        System.out.println("┌─ PRUEBA 24: Deteccion de Ciclos en el Grafo ──────────────────┐");
        System.out.println("Hay ciclos en el grafo: " + grafo.tieneCiclos());
        LinkedList<LinkedList<String>> ciclos = grafo.encontrarCiclos();
        System.out.println("Ciclos detectados: " + ciclos.size());
        int c = 1;
        for (LinkedList<String> ciclo : ciclos) {
            System.out.println("  Ciclo " + c++ + ": " + ciclo);
        }
        System.out.println("\nRutas frecuentes (>= 2 transferencias):");
        for (String ruta : grafo.rutasFrecuentes(2)) {
            System.out.println("  " + ruta);
        }
        System.out.println();

        // =====================================================================
        // PRUEBA 25: Consulta 1 - Billeteras con Mayor Uso
        // =====================================================================
        System.out.println("┌─ PRUEBA 25: Billeteras con Mayor Uso ─────────────────────────┐");
        System.out.println("Top 3 billeteras mas usadas:");
        for (String b : analitica.billeterasConMayorUso(3)) {
            System.out.println("  " + b);
        }
        System.out.println();

        // =====================================================================
        // PRUEBA 26: Consulta 2 - Usuarios Mas Activos
        // =====================================================================
        System.out.println("┌─ PRUEBA 26: Usuarios Mas Activos ─────────────────────────────┐");
        System.out.println("Top 3 usuarios mas activos:");
        for (String u : analitica.usuariosMasActivos(3)) {
            System.out.println("  " + u);
        }
        System.out.println();

        // =====================================================================
        // PRUEBA 27: Consulta 3 - Monto Movilizado por Rango de Tiempo
        // =====================================================================
        System.out.println("┌─ PRUEBA 27: Monto Movilizado por Rango de Tiempo ─────────────┐");
        long hace1hora = ahora - (60 * 60 * 1000);
        double montoTotal = analitica.montoMovilizadoPorRango(hace1hora, ahora);
        System.out.println("Monto movilizado en la ultima hora: $" + String.format("%.2f", montoTotal));
        System.out.println("Transacciones en ese rango: " + analitica.transaccionesEnRango(hace1hora, ahora).size());
        System.out.println("Usuario mas activo en el rango: " + analitica.usuarioConMayorActividad(hace1hora, ahora));
        System.out.println();

        // =====================================================================
        // PRUEBA 28: Consultas 4+5 - Frecuencia por Tipo y Top Transacciones
        // =====================================================================
        System.out.println("┌─ PRUEBA 28: Frecuencia por Tipo y Top Transacciones ──────────┐");
        System.out.println("Frecuencia por tipo:");
        for (java.util.Map.Entry<String, Integer> e : analitica.frecuenciaTransaccionesPorTipo().entrySet()) {
            System.out.println("  " + e.getKey() + ": " + e.getValue());
        }
        System.out.println("\nTop 3 transacciones por monto:");
        int p = 1;
        for (Transaccion t : analitica.topTransaccionesPorMonto(3)) {
            System.out.println("  #" + p++ + " " + t.getTipo() +
                    " $" + String.format("%.2f", t.getMonto()) + " | " + t.getUsuarioOrigen());
        }
        System.out.println();

        // =====================================================================
        // PRUEBA 29: Reporte General del Sistema
        // =====================================================================
        System.out.println("┌─ PRUEBA 29: Reporte General del Sistema ──────────────────────┐");
        System.out.println(analitica.generarReporte());

        // =====================================================================
        // PRUEBA 30: Deteccion de Fraude y Auditoria
        // =====================================================================
        System.out.println("┌─ PRUEBA 30: Deteccion de Fraude y Auditoria ──────────────────┐");
        // Simular comportamiento sospechoso
        operaciones.recargar("USR001", "BIL001", 3000.00);
        operaciones.transferirExterno("USR001", "BIL001", "USR002", "BIL004", 100.00);
        operaciones.transferirExterno("USR001", "BIL001", "USR002", "BIL004", 100.00);
        operaciones.transferirExterno("USR001", "BIL001", "USR002", "BIL004", 100.00);

        LinkedList<EventoSospechoso> eventos =
                detector.analizarSistema(operaciones.obtenerHistorialGlobal());

        System.out.println("Eventos sospechosos detectados: " + eventos.size());
        System.out.println("\nEventos de USR001:");
        for (EventoSospechoso e : detector.getEventosPorUsuario("USR001")) {
            System.out.println("  [" + e.getNivelRiesgo() + "] " + e.getTipo() +
                    " - " + e.getDescripcion() + ": " + e.getDetalles());
        }
        System.out.println("\nEventos de riesgo ALTO:");
        for (EventoSospechoso e : detector.getEventosRiesgoAlto()) {
            System.out.println("  " + e.getIdUsuario() + " | " + e.getDescripcion());
        }
        detector.mostrarResumenAuditoria();
        System.out.println();

        // =====================================================================
        // RESUMEN FINAL
        // =====================================================================
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              30/30 PRUEBAS COMPLETADAS                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Estructuras de Datos utilizadas:");
        System.out.println("  [Comp.1] TablaHash           -> Busqueda O(1) de usuarios y billeteras");
        System.out.println("  [Comp.1] Lista doble         -> Historial de transacciones");
        System.out.println("  [Comp.1] Pila (LIFO)         -> Reversion de operaciones");
        System.out.println("  [Comp.2] ColaPrioridad       -> Operaciones programadas por fecha");
        System.out.println("  [Comp.2] ColaSimple          -> Notificaciones FIFO por usuario");
        System.out.println("  [Comp.2] ArbolBST            -> Ranking y rango de puntos");
        System.out.println("  [Comp.3] GrafoTransferencias -> BFS, DFS, ciclos entre usuarios");
        System.out.println("  [Comp.1+2+3] Recursion       -> Listas, pilas, arboles, heapify, DFS");
    }
}