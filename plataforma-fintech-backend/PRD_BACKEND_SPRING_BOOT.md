# PRD Backend — Plataforma Fintech de Billeteras Digitales

Este PRD define la implementación del backend Spring Boot que reemplazará la ejecución por consola actual por una API REST consumida por el frontend React. La prioridad técnica es mantener el valor académico del proyecto: **las estructuras de datos propias deben ser parte real del dominio, no decoración**.

## Quick path

1. Migrar el Java actual a un backend Spring Boot 3.x con Java 21.
2. Separar dominio, casos de uso e infraestructura HTTP.
3. Exponer la API definida en `../docs/openapi.yaml`.
4. Reemplazar colecciones estándar del core por estructuras propias donde la consigna lo exige.
5. Agregar tests automáticos antes de conectar el frontend.

## Objetivo

Construir un backend REST para gestionar usuarios, billeteras, transacciones, reversión de operaciones, puntos, alertas, analítica y fraude sobre el dominio fintech existente.

El backend debe ser compatible con el frontend React definido en `../plataforma-fintech-frontend/PRD_FRONTEND_REACT.md` y usar `../docs/openapi.yaml` como contrato obligatorio.

## Alcance

### Incluido

- API REST JSON.
- Migración del dominio Java actual.
- Casos de uso para operaciones financieras.
- Validaciones centralizadas.
- Historial de transacciones.
- Reversión de operaciones.
- Sistema de puntos y niveles.
- Alertas/notificaciones.
- Analítica básica.
- Eventos de fraude.
- Tests unitarios y de controller.
- Documentación OpenAPI sincronizada.

### Fuera de alcance inicial

- Autenticación real con JWT.
- Pasarela de pagos real.
- Persistencia distribuida.
- Microservicios.
- Deploy productivo.

## Stack definido

| Área | Tecnología | Decisión |
|------|------------|----------|
| Runtime | Java 21 | Versión moderna, compatible con Spring Boot 3.x. |
| Framework | Spring Boot 3.3+ | Backend REST estable y estándar. |
| Build | Maven | Simple para proyecto académico y fácil de evaluar. |
| API | Spring Web + Bean Validation | Controllers REST y validación declarativa. |
| Docs API | springdoc-openapi | Publicar `/v3/api-docs` y Swagger UI. |
| Tests | JUnit 5 + Mockito + MockMvc | Unitarios + slices web. |
| Persistencia inicial | In-memory repositories | Mantiene foco en estructuras propias. |
| Persistencia futura | PostgreSQL + Spring Data JPA | Opcional para evolución después del MVP. |

## Principios de arquitectura

### 1. Dominio sin Spring

Las clases de dominio no deben importar Spring.

Permitido en dominio:

- entidades
- value objects
- enums
- estructuras propias
- reglas de negocio puras

Prohibido en dominio:

- `@Entity`
- `@Service`
- `@Component`
- `@Repository`
- `@Autowired`
- `ResponseEntity`
- `HttpStatus`

### 2. Inyección por constructor

Toda dependencia Spring debe inyectarse por constructor. Nada de field injection.

### 3. Contrato primero

El backend implementa `../docs/openapi.yaml`. Si el contrato cambia, se actualiza primero el OpenAPI y después el código.

### 4. Estructuras propias como requisito real

Las estructuras propias deben usarse en los contenedores principales del dominio.

| Caso | Estructura requerida |
|------|----------------------|
| Usuarios por ID | `TablaHash<String, Usuario>` |
| Billeteras por usuario | `TablaHash<String, Billetera>` |
| Historial de usuario | `MiLista<Transaccion>` |
| Historial de billetera | `MiLista<Transaccion>` |
| Historial global | `MiLista<Transaccion>` |
| Reversión de operaciones | `Pila<Transaccion>` |
| Operaciones programadas | `ColaPrioridad<OperacionProgramada>` |
| Alertas pendientes | `ColaSimple<Notificacion>` |
| Ranking/puntos | `ArbolBST` o estructura propia equivalente |
| Análisis de transferencias | `GrafoTransferencias` |

> Las colecciones estándar de Java solo deberían usarse en bordes técnicos: DTOs, serialización JSON, tests o integración con frameworks. Si se usan en dominio, debe justificarse.

## Estructura propuesta

```text
plataforma-fintech-backend/
├── pom.xml
├── PRD_BACKEND_SPRING_BOOT.md
└── src/
    ├── main/java/com/proyectofinal/fintech/
    │   ├── FintechWalletApplication.java
    │   ├── domain/
    │   │   ├── model/
    │   │   ├── structures/
    │   │   ├── service/
    │   │   └── port/
    │   ├── application/
    │   │   ├── usecase/
    │   │   └── result/
    │   └── infrastructure/
    │       ├── input/rest/
    │       ├── input/rest/dto/
    │       ├── output/memory/
    │       ├── mapper/
    │       └── config/
    └── test/java/com/proyectofinal/fintech/
```

## Módulos funcionales

### Usuarios

Permite crear y consultar usuarios.

Requisitos:

- Crear usuario con nombre y email.
- Consultar usuario por ID.
- Consultar saldo total, puntos y nivel.
- Evitar IDs duplicados.

Endpoints:

- `POST /api/v1/users`
- `GET /api/v1/users/{userId}`

### Billeteras

Permite crear y consultar billeteras por usuario.

Requisitos:

- Crear billetera para usuario existente.
- Consultar billeteras del usuario.
- Consultar saldo y estado.
- No permitir dos billeteras con el mismo código para el mismo usuario.

Endpoints:

- `GET /api/v1/users/{userId}/wallets`
- `POST /api/v1/users/{userId}/wallets`

### Operaciones financieras

Permite recargar, retirar y transferir dinero.

Requisitos:

- Validar montos positivos.
- Validar saldo suficiente.
- Registrar cada transacción en historial.
- Generar puntos según la operación.
- Registrar operaciones reversibles en `Pila`.

Endpoints:

- `POST /api/v1/users/{userId}/wallets/{walletId}/recharge`
- `POST /api/v1/users/{userId}/wallets/{walletId}/withdraw`
- `POST /api/v1/users/{userId}/transfers/internal`
- `POST /api/v1/transfers/external`

### Historial y reversión

Permite consultar movimientos y deshacer operaciones reversibles.

Requisitos:

- Historial por usuario.
- Historial por billetera.
- Historial global interno.
- Reversión de última operación o de transacción específica si es reversible.

Endpoints:

- `GET /api/v1/users/{userId}/transactions`
- `GET /api/v1/users/{userId}/wallets/{walletId}/transactions`
- `POST /api/v1/transactions/{transactionId}/reverse`

### Puntos y fidelización

Permite consultar puntos, nivel y ranking.

Requisitos:

- Calcular puntos por operación.
- Actualizar nivel automáticamente.
- Consultar ranking descendente.

Endpoints:

- `GET /api/v1/users/{userId}/points`
- `GET /api/v1/points/ranking`

### Operaciones programadas

Permite programar y cancelar operaciones.

Requisitos:

- Usar `ColaPrioridad` para ordenar ejecución.
- Consultar pendientes.
- Cancelar operación.

Endpoints:

- `POST /api/v1/scheduled-operations`
- `GET /api/v1/scheduled-operations`
- `POST /api/v1/scheduled-operations/{operationId}/cancel`

### Alertas

Permite consultar notificaciones del usuario.

Requisitos:

- Generar alerta por saldo bajo.
- Generar alerta por operación sospechosa.
- Gestionar pendientes con `ColaSimple`.

Endpoints:

- `GET /api/v1/notifications/users/{userId}`
- `POST /api/v1/notifications/{notificationId}/read`

### Analítica y fraude

Permite consultar métricas y eventos sospechosos.

Requisitos:

- Resumen global.
- Usuarios más activos.
- Billeteras con mayor uso.
- Rutas frecuentes en grafo.
- Eventos sospechosos por usuario y severidad.

Endpoints:

- `GET /api/v1/analytics/summary`
- `GET /api/v1/analytics/top-users`
- `GET /api/v1/analytics/top-wallets`
- `GET /api/v1/analytics/frequent-routes`
- `GET /api/v1/fraud/events`

## Modelo de errores

Todos los errores deben responder el mismo formato:

```json
{
  "code": "INSUFFICIENT_FUNDS",
  "message": "Saldo insuficiente para realizar la operación.",
  "details": ["walletId=WALLET-001"]
}
```

Códigos mínimos:

| Código | Uso |
|--------|-----|
| `VALIDATION_ERROR` | Request inválido. |
| `USER_NOT_FOUND` | Usuario inexistente. |
| `WALLET_NOT_FOUND` | Billetera inexistente. |
| `DUPLICATED_RESOURCE` | Usuario/billetera duplicado. |
| `INSUFFICIENT_FUNDS` | Saldo insuficiente. |
| `TRANSACTION_NOT_REVERSIBLE` | No se puede revertir. |
| `OPERATION_NOT_FOUND` | Operación programada inexistente. |

## Testing requerido

| Nivel | Herramienta | Qué validar |
|------|-------------|-------------|
| Estructuras | JUnit 5 | `TablaHash`, `MiLista`, `Pila`, colas, árbol, grafo. |
| Dominio | JUnit 5 | reglas de saldo, puntos, reversión, estados. |
| Casos de uso | JUnit 5 + Mockito | flujos felices y errores. |
| REST | MockMvc | contrato HTTP y validaciones. |
| Integración | SpringBootTest mínimo | arranque del contexto. |

## Criterios de aceptación

- [ ] El backend compila con Java 21 y Spring Boot 3.x.
- [ ] No hay `System.out.println` en dominio o aplicación.
- [ ] El dominio usa estructuras propias para almacenamiento principal.
- [ ] La API implementa `../docs/openapi.yaml`.
- [ ] Los errores siguen el schema `ApiError`.
- [ ] Hay tests unitarios para cada estructura propia.
- [ ] Hay tests de operaciones financieras principales.
- [ ] El frontend puede consumir usuarios, billeteras, operaciones e historial sin mocks.

## Plan de implementación

### Fase 1 — Base Spring Boot

- Crear `pom.xml`.
- Crear aplicación Spring Boot.
- Configurar paquetes.
- Agregar health endpoint.
- Agregar springdoc-openapi.

### Fase 2 — Dominio limpio

- Migrar modelos actuales.
- Convertir strings de estado/tipo a enums.
- Eliminar prints.
- Integrar estructuras propias en entidades y servicios de dominio.

### Fase 3 — Casos de uso

- Crear casos de uso para usuarios, billeteras y operaciones.
- Crear resultados explícitos en vez de booleanos sin contexto.
- Centralizar validaciones.

### Fase 4 — REST

- Crear DTOs.
- Crear controllers.
- Crear mappers.
- Implementar manejo global de errores.

### Fase 5 — Analítica, fraude y programadas

- Adaptar `AnaliticaFinanciera`.
- Adaptar `DetectorFraude`.
- Adaptar `GestorOperaciones`.
- Exponer endpoints restantes.

### Fase 6 — Pruebas e integración frontend

- Tests unitarios.
- Tests REST.
- Validar contrato contra frontend.
- Documentar requests de ejemplo.

## Riesgos

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Migrar todo de golpe | Alto | Hacer fases por módulo. |
| Estructuras propias mal integradas | Alto | Tests específicos y revisión de uso real. |
| API cambia sin avisar al frontend | Alto | OpenAPI como fuente de verdad. |
| Dominio contaminado con Spring | Medio | Revisar imports por paquete. |
| Estado en memoria se pierde | Medio | Aceptarlo en MVP; documentar persistencia futura. |

## Próximo paso

Crear el esqueleto Spring Boot y migrar primero `Usuario`, `Billetera`, `Transaccion`, `TablaHash`, `MiLista` y `Pila` con tests.
