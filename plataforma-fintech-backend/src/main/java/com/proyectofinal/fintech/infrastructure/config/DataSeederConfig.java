package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.usecase.CreateScheduledOperationUseCase;
import com.proyectofinal.fintech.application.usecase.CreateUserUseCase;
import com.proyectofinal.fintech.application.usecase.CreateWalletUseCase;
import com.proyectofinal.fintech.application.usecase.ExternalTransferUseCase;
import com.proyectofinal.fintech.application.usecase.InternalTransferUseCase;
import com.proyectofinal.fintech.application.usecase.RechargeWalletUseCase;
import com.proyectofinal.fintech.application.usecase.WithdrawWalletUseCase;
import com.proyectofinal.fintech.domain.exception.DomainException;
import com.proyectofinal.fintech.domain.model.ScheduledOperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Seed realista al startup — usuarios, billeteras, transacciones, operaciones programadas.
 * Excluido del perfil "test" para no interferir con @SpringBootTest.
 */
@Configuration
@Profile("!test")
public class DataSeederConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSeederConfig.class);

    @Bean
    public CommandLineRunner seedDatabase(
            CreateUserUseCase createUser,
            CreateWalletUseCase createWallet,
            RechargeWalletUseCase recharge,
            WithdrawWalletUseCase withdraw,
            InternalTransferUseCase internalTransfer,
            ExternalTransferUseCase externalTransfer,
            CreateScheduledOperationUseCase createScheduled,
            Clock clock) {

        return args -> {
            log.info("[seed] ============================================================");
            log.info("[seed] Iniciando seed de datos realistas...");
            log.info("[seed] ============================================================");

            // ------------------------------------------------------------------
            // 1. Usuarios (Argentina / LATAM)
            // ------------------------------------------------------------------
            log.info("[seed] Creando 8 usuarios reales...");
            safe(() -> createUser.execute("USR001", "Mariana López Pérez",   "mariana.lopez@gmail.com"));
            safe(() -> createUser.execute("USR002", "Federico Romero",        "fede.romero@hotmail.com"));
            safe(() -> createUser.execute("USR003", "Lucía Fernández",        "l.fernandez@outlook.com"));
            safe(() -> createUser.execute("USR004", "Mateo Álvarez",          "mateo.alvarez@gmail.com"));
            safe(() -> createUser.execute("USR005", "Camila Castro",          "cami.castro@yahoo.com.ar"));
            safe(() -> createUser.execute("USR006", "Joaquín Méndez",         "j.mendez@empresa.com.ar"));
            safe(() -> createUser.execute("USR007", "Valentina Ríos",         "vale.rios@gmail.com"));
            safe(() -> createUser.execute("USR008", "Tomás Sosa",             "tomas.sosa@hotmail.com"));

            // ------------------------------------------------------------------
            // 2. Billeteras (tipos del PDF §4.1)
            // ------------------------------------------------------------------
            log.info("[seed] Creando 16 billeteras de distintos tipos...");
            // USR001 — Mariana
            safe(() -> createWallet.execute("USR001", "WAL-MAR-01", "Gastos diarios",  "DAILY_EXPENSES"));
            safe(() -> createWallet.execute("USR001", "WAL-MAR-02", "Ahorro",           "SAVINGS"));
            // USR002 — Federico
            safe(() -> createWallet.execute("USR002", "WAL-FED-01", "Gastos diarios",  "DAILY_EXPENSES"));
            safe(() -> createWallet.execute("USR002", "WAL-FED-02", "Compras",          "SHOPPING"));
            safe(() -> createWallet.execute("USR002", "WAL-FED-03", "Inversión",        "INVESTMENT"));
            // USR003 — Lucía
            safe(() -> createWallet.execute("USR003", "WAL-LUC-01", "Ahorro",           "SAVINGS"));
            safe(() -> createWallet.execute("USR003", "WAL-LUC-02", "Transporte",       "TRANSPORT"));
            // USR004 — Mateo
            safe(() -> createWallet.execute("USR004", "WAL-MAT-01", "Gastos diarios",  "DAILY_EXPENSES"));
            // USR005 — Camila
            safe(() -> createWallet.execute("USR005", "WAL-CAM-01", "Gastos diarios",  "DAILY_EXPENSES"));
            safe(() -> createWallet.execute("USR005", "WAL-CAM-02", "Compras",          "SHOPPING"));
            // USR006 — Joaquín
            safe(() -> createWallet.execute("USR006", "WAL-JOA-01", "Gastos diarios",  "DAILY_EXPENSES"));
            safe(() -> createWallet.execute("USR006", "WAL-JOA-02", "Inversión",        "INVESTMENT"));
            // USR007 — Valentina
            safe(() -> createWallet.execute("USR007", "WAL-VAL-01", "Suscripciones",   "SUBSCRIPTIONS"));
            safe(() -> createWallet.execute("USR007", "WAL-VAL-02", "Gastos diarios",  "DAILY_EXPENSES"));
            // USR008 — Tomás
            safe(() -> createWallet.execute("USR008", "WAL-TOM-01", "Ahorro",           "SAVINGS"));

            // ------------------------------------------------------------------
            // 3. Recargas iniciales (fondos de arranque)
            // ------------------------------------------------------------------
            log.info("[seed] Insertando recargas iniciales...");
            safe(() -> recharge.execute("USR001", "WAL-MAR-01", 85000.0,  "Sueldo quincenal noviembre"));
            safe(() -> recharge.execute("USR001", "WAL-MAR-02", 12500.0,  "Transferencia ahorro mensual"));
            safe(() -> recharge.execute("USR002", "WAL-FED-01", 93500.0,  "Sueldo noviembre"));
            safe(() -> recharge.execute("USR002", "WAL-FED-02", 27800.0,  "Cobro trabajo freelance"));
            safe(() -> recharge.execute("USR002", "WAL-FED-03", 50000.0,  "Depósito inversión inicial"));
            safe(() -> recharge.execute("USR003", "WAL-LUC-01", 68000.0,  "Sueldo quincenal"));
            safe(() -> recharge.execute("USR003", "WAL-LUC-02", 8500.0,   "Recarga tarjeta SUBE"));
            safe(() -> recharge.execute("USR004", "WAL-MAT-01", 47200.0,  "Sueldo mensual neto"));
            safe(() -> recharge.execute("USR005", "WAL-CAM-01", 62000.0,  "Sueldo noviembre"));
            safe(() -> recharge.execute("USR005", "WAL-CAM-02", 15300.0,  "Cobro consultoría"));
            safe(() -> recharge.execute("USR006", "WAL-JOA-01", 115000.0, "Sueldo directivo"));
            safe(() -> recharge.execute("USR006", "WAL-JOA-02", 75000.0,  "Depósito cartera de inversiones"));
            safe(() -> recharge.execute("USR007", "WAL-VAL-01", 18000.0,  "Fondo suscripciones anuales"));
            safe(() -> recharge.execute("USR007", "WAL-VAL-02", 54000.0,  "Sueldo quincenal"));
            safe(() -> recharge.execute("USR008", "WAL-TOM-01", 38500.0,  "Depósito ahorro"));

            // ------------------------------------------------------------------
            // 4. Retiros (gastos reales)
            // ------------------------------------------------------------------
            log.info("[seed] Insertando retiros con descripciones realistas...");
            safe(() -> withdraw.execute("USR001", "WAL-MAR-01", 4250.0,  "Compra supermercado Coto"));
            safe(() -> withdraw.execute("USR001", "WAL-MAR-01", 1800.0,  "Pago peluquería"));
            safe(() -> withdraw.execute("USR002", "WAL-FED-01", 3450.0,  "Retiro cajero Banco Nación"));
            safe(() -> withdraw.execute("USR002", "WAL-FED-02", 12800.0, "Compra electrodoméstico Frávega"));
            safe(() -> withdraw.execute("USR003", "WAL-LUC-01", 28000.0, "Pago alquiler departamento"));
            safe(() -> withdraw.execute("USR004", "WAL-MAT-01", 2350.0,  "Servicio luz EDESUR"));
            safe(() -> withdraw.execute("USR005", "WAL-CAM-01", 5700.0,  "Compra ropa Zara"));
            safe(() -> withdraw.execute("USR006", "WAL-JOA-01", 9800.0,  "Pago expensas edificio"));

            // ------------------------------------------------------------------
            // 5. Transferencias internas
            // ------------------------------------------------------------------
            log.info("[seed] Insertando transferencias internas...");
            safe(() -> internalTransfer.execute("USR001", "WAL-MAR-01", "WAL-MAR-02", 10000.0, "Paso a ahorro mensual"));
            safe(() -> internalTransfer.execute("USR002", "WAL-FED-01", "WAL-FED-02", 8500.0,  "Fondeo cuenta compras"));
            safe(() -> internalTransfer.execute("USR002", "WAL-FED-02", "WAL-FED-03", 5000.0,  "Transferencia compras → inversión"));
            safe(() -> internalTransfer.execute("USR006", "WAL-JOA-01", "WAL-JOA-02", 20000.0, "Aporte mensual a inversiones"));
            safe(() -> internalTransfer.execute("USR007", "WAL-VAL-02", "WAL-VAL-01", 3500.0,  "Carga fondo suscripciones"));

            // ------------------------------------------------------------------
            // 6. Transferencias externas — grafo rico para cycles + frequent routes
            //    Ciclo: USR001 → USR002 → USR003 → USR001
            //    Rutas frecuentes: USR001→USR002 x3, USR002→USR004 x2
            // ------------------------------------------------------------------
            log.info("[seed] Insertando transferencias externas (grafo de rutas)...");

            // Ciclo A→B
            safe(() -> externalTransfer.execute("USR001", "WAL-MAR-01", "USR002", "WAL-FED-01",
                    7500.0,  "Transferencia familiar — Mariana a Fede"));
            safe(() -> externalTransfer.execute("USR001", "WAL-MAR-01", "USR002", "WAL-FED-01",
                    5000.0,  "Cuota deuda pendiente"));
            safe(() -> externalTransfer.execute("USR001", "WAL-MAR-01", "USR002", "WAL-FED-01",
                    3200.0,  "Reintegro gastos compartidos"));

            // Ciclo B→C
            safe(() -> externalTransfer.execute("USR002", "WAL-FED-01", "USR003", "WAL-LUC-01",
                    6800.0,  "Transferencia a Lucía — parte del alquiler"));

            // Ciclo C→A (cierra el ciclo USR001→USR002→USR003→USR001)
            safe(() -> externalTransfer.execute("USR003", "WAL-LUC-01", "USR001", "WAL-MAR-01",
                    4500.0,  "Devolución de préstamo"));

            // Ruta frecuente: USR002 → USR004 x2
            safe(() -> externalTransfer.execute("USR002", "WAL-FED-01", "USR004", "WAL-MAT-01",
                    9000.0,  "Pago servicio profesional"));
            safe(() -> externalTransfer.execute("USR002", "WAL-FED-01", "USR004", "WAL-MAT-01",
                    6200.0,  "Segunda cuota honorarios"));

            // Transferencia grande → gatilla LARGE_TRANSACTION HIGH (>10000)
            log.info("[seed] Insertando transferencia grande para gatillar LARGE_TRANSACTION...");
            safe(() -> externalTransfer.execute("USR002", "WAL-FED-03", "USR006", "WAL-JOA-02",
                    18500.0, "Inversión conjunta proyecto inmobiliario"));

            // Rutas adicionales para enriquecer el grafo
            safe(() -> externalTransfer.execute("USR005", "WAL-CAM-01", "USR007", "WAL-VAL-02",
                    4100.0,  "Pago clases particulares"));
            safe(() -> externalTransfer.execute("USR006", "WAL-JOA-01", "USR008", "WAL-TOM-01",
                    11000.0, "Transferencia a Tomás — anticipo"));
            safe(() -> externalTransfer.execute("USR007", "WAL-VAL-02", "USR001", "WAL-MAR-01",
                    2800.0,  "Reintegro cumpleaños"));

            // ------------------------------------------------------------------
            // 7. Recargas rápidas USR005 — gatilla HIGH_VELOCITY HIGH (3+ rápidas)
            // ------------------------------------------------------------------
            log.info("[seed] Insertando recargas rápidas de USR005 para gatillar HIGH_VELOCITY...");
            safe(() -> recharge.execute("USR005", "WAL-CAM-01", 1500.0, "Recarga rápida 1 — cobro puntual"));
            safe(() -> recharge.execute("USR005", "WAL-CAM-01", 2200.0, "Recarga rápida 2 — anticipo cliente"));
            safe(() -> recharge.execute("USR005", "WAL-CAM-01", 1800.0, "Recarga rápida 3 — pago parcial factura"));
            safe(() -> recharge.execute("USR005", "WAL-CAM-01", 950.0,  "Recarga rápida 4 — reintegro viáticos"));

            // ------------------------------------------------------------------
            // 8. Operaciones programadas (2 vencidas + 3 futuras)
            // ------------------------------------------------------------------
            log.info("[seed] Creando 5 operaciones programadas (2 vencidas, 3 futuras)...");

            // Vencida 1 — recharge para USR001 (pasado: ~4 meses atrás)
            Instant past1 = clock.instant().minus(Duration.ofDays(118));
            safe(() -> createScheduled.execute(
                    ScheduledOperationType.RECHARGE,
                    "USR001", "WAL-MAR-02",
                    null, null,
                    5000.0, past1,
                    "Ahorro automático enero — vencida"));

            // Vencida 2 — transferencia interna para USR002 (pasado: ~63 días)
            Instant past2 = clock.instant().minus(Duration.ofDays(63));
            safe(() -> createScheduled.execute(
                    ScheduledOperationType.INTERNAL_TRANSFER,
                    "USR002", "WAL-FED-01",
                    "USR002", "WAL-FED-03",
                    8000.0, past2,
                    "Aporte quincenal a inversión — vencida"));

            // Futura 1 — recharge mensual (hoy + 7 días)
            Instant future1 = clock.instant().plus(Duration.ofDays(7));
            safe(() -> createScheduled.execute(
                    ScheduledOperationType.RECHARGE,
                    "USR001", "WAL-MAR-02",
                    null, null,
                    5000.0, future1,
                    "Ahorro automático semanal"));

            // Futura 2 — interna para USR003 (hoy + 14 días)
            Instant future2 = clock.instant().plus(Duration.ofDays(14));
            safe(() -> createScheduled.execute(
                    ScheduledOperationType.INTERNAL_TRANSFER,
                    "USR003", "WAL-LUC-01",
                    "USR003", "WAL-LUC-02",
                    3000.0, future2,
                    "Recarga SUBE mensual programada"));

            // Futura 3 — externa bimensual USR005 → USR007 (hoy + 30 días)
            Instant future3 = clock.instant().plus(Duration.ofDays(30));
            safe(() -> createScheduled.execute(
                    ScheduledOperationType.EXTERNAL_TRANSFER,
                    "USR005", "WAL-CAM-01",
                    "USR007", "WAL-VAL-01",
                    799.0, future3,
                    "Suscripción Spotify Premium — bimensual"));

            log.info("[seed] ============================================================");
            log.info("[seed] Seed completado: 8 usuarios, 16 billeteras, ~30 transacciones, 5 operaciones programadas.");
            log.info("[seed] Triggers activos: LARGE_TRANSACTION (18500) + HIGH_VELOCITY (4 recargas rápidas USR005).");
            log.info("[seed] Ciclo en grafo: USR001 → USR002 → USR003 → USR001.");
            log.info("[seed] ============================================================");
        };
    }

    /**
     * Ejecuta la acción ignorando DomainException (idempotencia al reiniciar).
     * Cualquier otra excepción se deja propagar para no ocultar errores inesperados.
     */
    private void safe(Runnable action) {
        try {
            action.run();
        } catch (DomainException e) {
            log.warn("[seed] Ignorado (posible duplicado): {}", e.getMessage());
        }
    }
}
