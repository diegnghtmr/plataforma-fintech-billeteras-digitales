# PRD Frontend — Plataforma Fintech de Billeteras Digitales

Este PRD define la implementación del frontend React que consumirá el backend Spring Boot mediante el contrato `../docs/openapi.yaml`. La app debe permitir operar el sistema fintech sin depender de `Main.java` ni de pruebas por consola. El diseño visual clave del producto está definido en [`design.md`](./design.md) y debe guiar la implementación de UI.

## Quick path

1. Crear frontend con React 19, TypeScript y Vite.
2. Usar TanStack Query para server state.
3. Usar Zustand para estado local/UI.
4. Generar o mantener tipos desde `../docs/openapi.yaml`.
5. Implementar la UI siguiendo [`design.md`](./design.md) como referencia visual obligatoria.
6. Conectar pantallas principales contra el backend Spring Boot.

## Objetivo

Construir una interfaz web para gestionar usuarios, billeteras, transacciones, puntos, alertas, analítica y fraude.

El frontend debe ser compatible con el backend definido en `../plataforma-fintech-backend/PRD_BACKEND_SPRING_BOOT.md` y usar el OpenAPI compartido como contrato.

## Diseño clave

[`design.md`](./design.md) es la fuente de verdad visual del frontend. Define el lenguaje de interfaz que debe respetarse durante la implementación: sistema de canvas oscuro/claro, tipografía, colores, botones tipo pill, cards, espaciado, jerarquía visual y comportamiento de secciones.

La implementación visual se hará con **Tailwind CSS**. El `design.md` define el criterio de diseño; Tailwind lo materializa en clases, tokens y componentes reutilizables.

Toda pantalla nueva debe validar dos cosas antes de considerarse terminada:

- Respeta el contrato funcional de este PRD.
- Respeta el criterio visual definido en `design.md`.
- Usa Tailwind CSS como sistema de estilos principal.

## Stack definido

| Área | Tecnología | Decisión |
|------|------------|----------|
| UI | React 19 | Componentes modernos y simples. |
| Lenguaje | TypeScript strict | Tipado fuerte del contrato API. |
| Build | Vite | Rápido y simple para SPA. |
| Server state | TanStack Query v5 | Cache, loading, error y refetch de API. |
| Estado local | Zustand 5 | UI state, filtros, sesión seleccionada. |
| Routing | React Router | Navegación SPA. |
| Forms | React Hook Form | Formularios controlados y validables. |
| Validación | Zod | Validar forms y responses críticas. |
| HTTP client | Fetch wrapper tipado u Orval/OpenAPI generator | Cliente alineado al contrato. |
| Estilos | Tailwind CSS + `design.md` | Tailwind implementa los tokens y patrones visuales definidos en el diseño clave. |
| Tests | Vitest + React Testing Library | Componentes, stores y hooks. |

## Sistema visual con Tailwind CSS

Tailwind CSS será la única estrategia de estilos para la UI principal.

Reglas:

- Los colores, radios, espaciados y tipografías deben derivarse de `design.md`.
- No usar CSS Modules como estrategia principal.
- No hardcodear colores hex en `className`; crear tokens/clases semánticas.
- No usar `var()` directamente dentro de `className`; mapear tokens a utilidades Tailwind.
- Usar `cn()` solo para clases condicionales o merge de clases externas.
- Los componentes base — botones, cards, inputs, chips, tablas y layout — deben encapsular las decisiones visuales de `design.md`.

## Principios de arquitectura frontend

### 1. React Query para datos del servidor

Todo dato que venga del backend debe vivir en TanStack Query:

- usuarios
- billeteras
- transacciones
- notificaciones
- analítica
- fraude
- operaciones programadas

No duplicar server state en Zustand.

### 2. Zustand solo para estado local

Zustand se usa para:

- usuario seleccionado
- billetera seleccionada
- filtros de tablas
- estado de layout/sidebar
- preferencias visuales
- modo demo si hiciera falta

### 3. OpenAPI como fuente de verdad

Los tipos y requests deben salir de `../docs/openapi.yaml` o mantenerse sincronizados con ese archivo.

### 4. Componentes chicos

Cada pantalla orquesta. Los componentes renderizan. Los hooks conectan con API.

## Estructura propuesta

```text
plataforma-fintech-frontend/
├── package.json
├── PRD_FRONTEND_REACT.md
├── design.md
├── vite.config.ts
└── src/
    ├── app/
    │   ├── router.tsx
    │   └── query-client.ts
    ├── api/
    │   ├── client.ts
    │   ├── generated/
    │   └── query-keys.ts
    ├── features/
    │   ├── users/
    │   ├── wallets/
    │   ├── operations/
    │   ├── transactions/
    │   ├── points/
    │   ├── notifications/
    │   ├── analytics/
    │   └── fraud/
    ├── shared/
    │   ├── components/
    │   ├── layout/
    │   ├── feedback/
    │   └── utils/
    ├── stores/
    │   ├── use-app-store.ts
    │   └── use-selection-store.ts
    └── main.tsx
```

## Pantallas requeridas

### Dashboard

Resumen general del sistema.

Debe mostrar:

- total de usuarios
- total de transacciones
- volumen total movido
- cantidad de alertas/fraude
- accesos rápidos a operaciones

Consume:

- `GET /api/v1/analytics/summary`

### Usuarios

Alta y consulta de usuarios.

Debe permitir:

- crear usuario
- buscar usuario por ID
- ver nombre, email, puntos, nivel y saldo total
- seleccionar usuario activo para operar

Consume:

- `POST /api/v1/users`
- `GET /api/v1/users/{userId}`

Estado local:

- `selectedUserId` en Zustand.

### Billeteras

Gestión de billeteras de un usuario.

Debe permitir:

- listar billeteras del usuario seleccionado
- crear nueva billetera
- seleccionar billetera activa
- ver saldo y estado

Consume:

- `GET /api/v1/users/{userId}/wallets`
- `POST /api/v1/users/{userId}/wallets`

Estado local:

- `selectedWalletId` en Zustand.

### Operaciones

Formularios para movimientos financieros.

Debe permitir:

- recargar
- retirar
- transferencia interna
- transferencia externa
- revertir transacción

Consume:

- `POST /api/v1/users/{userId}/wallets/{walletId}/recharge`
- `POST /api/v1/users/{userId}/wallets/{walletId}/withdraw`
- `POST /api/v1/users/{userId}/transfers/internal`
- `POST /api/v1/transfers/external`
- `POST /api/v1/transactions/{transactionId}/reverse`

React Query:

- invalidar queries de usuario, billeteras, transacciones, puntos y analytics después de cada mutación exitosa.

### Historial

Tabla de transacciones.

Debe permitir:

- filtrar por tipo
- filtrar por estado
- filtrar por billetera
- ver detalle de transacción
- iniciar reversión si es reversible

Consume:

- `GET /api/v1/users/{userId}/transactions`
- `GET /api/v1/users/{userId}/wallets/{walletId}/transactions`

Estado local:

- filtros de tabla en Zustand.

### Puntos

Vista de fidelización.

Debe mostrar:

- puntos acumulados
- nivel actual
- ranking descendente

Consume:

- `GET /api/v1/users/{userId}/points`
- `GET /api/v1/points/ranking`

### Operaciones programadas

Gestión de operaciones futuras.

Debe permitir:

- crear operación programada
- listar pendientes
- cancelar operación

Consume:

- `POST /api/v1/scheduled-operations`
- `GET /api/v1/scheduled-operations`
- `POST /api/v1/scheduled-operations/{operationId}/cancel`

### Alertas

Centro de notificaciones.

Debe permitir:

- listar alertas del usuario
- marcar como leída
- destacar severidad alta

Consume:

- `GET /api/v1/notifications/users/{userId}`
- `POST /api/v1/notifications/{notificationId}/read`

### Analítica y fraude

Visualización de métricas y eventos sospechosos.

Debe mostrar:

- usuarios más activos
- billeteras con mayor uso
- rutas frecuentes
- eventos sospechosos

Consume:

- `GET /api/v1/analytics/top-users`
- `GET /api/v1/analytics/top-wallets`
- `GET /api/v1/analytics/frequent-routes`
- `GET /api/v1/fraud/events`

## Estado con Zustand

Stores mínimos:

```text
useSelectionStore
├── selectedUserId
├── selectedWalletId
├── setSelectedUserId
├── setSelectedWalletId
└── clearSelection

useAppStore
├── sidebarOpen
├── theme
├── transactionFilters
├── setTransactionFilters
└── resetFilters
```

Reglas:

- Usar selectors para evitar renders innecesarios.
- Usar `useShallow` cuando se seleccionen varios campos.
- No guardar responses completas del backend si ya están en React Query.

## React Query

Query keys sugeridas:

```text
['users', userId]
['wallets', userId]
['transactions', userId, filters]
['wallet-transactions', userId, walletId]
['points', userId]
['points-ranking']
['notifications', userId]
['scheduled-operations']
['analytics', 'summary']
['analytics', 'top-users']
['analytics', 'top-wallets']
['fraud-events', filters]
```

Reglas de invalidación:

| Mutación | Invalidar |
|----------|-----------|
| Crear usuario | `users` |
| Crear billetera | `wallets`, `users` |
| Recarga/retiro/transferencia | `wallets`, `transactions`, `points`, `analytics` |
| Reversión | `wallets`, `transactions`, `points`, `analytics` |
| Marcar notificación leída | `notifications` |
| Crear/cancelar programada | `scheduled-operations` |

## Tipado TypeScript

Reglas:

- `strict: true`.
- No usar `any`.
- Preferir tipos generados desde OpenAPI.
- Para constantes locales, usar objeto `as const` y derivar tipo.
- Interfaces planas; objetos anidados deben tener interfaz propia.

Ejemplo:

```ts
export const TransactionStatus = {
  SUCCESSFUL: 'SUCCESSFUL',
  REVERSED: 'REVERSED',
  PENDING: 'PENDING',
  REJECTED: 'REJECTED',
} as const;

export type TransactionStatus =
  (typeof TransactionStatus)[keyof typeof TransactionStatus];
```

## Manejo de errores

El frontend debe leer el schema `ApiError` del backend.

Comportamiento esperado:

- `400`: mostrar errores de validación cerca del formulario.
- `404`: mostrar mensaje contextual.
- `409`: mostrar conflicto de recurso duplicado.
- `422`: mostrar regla de negocio incumplida.
- `500`: mostrar error genérico y opción de reintentar.

## Criterios de aceptación

- [ ] La app corre como SPA con Vite + React + TypeScript.
- [ ] Usa React Query para todo dato remoto.
- [ ] Usa Zustand solo para estado local/UI.
- [ ] Consume la API definida en `../docs/openapi.yaml`.
- [ ] Tiene pantallas de usuarios, billeteras, operaciones e historial.
- [ ] Invalida queries después de mutaciones financieras.
- [ ] No usa `any`.
- [ ] Los formularios validan monto, usuario y billetera antes de enviar.
- [ ] Los errores del backend se muestran con mensajes claros.

## Plan de implementación

### Fase 1 — Base del frontend

- Crear proyecto Vite React TypeScript.
- Configurar router.
- Configurar QueryClient.
- Crear layout base.
- Crear cliente HTTP.

### Fase 2 — Contrato API

- Conectar `../docs/openapi.yaml`.
- Generar tipos o crear types sincronizados.
- Crear query keys.
- Crear hooks base por feature.

### Fase 3 — Usuarios y billeteras

- Pantalla de usuarios.
- Pantalla de billeteras.
- Store de selección con Zustand.

### Fase 4 — Operaciones e historial

- Formularios de recarga, retiro y transferencias.
- Tabla de transacciones.
- Reversión.

### Fase 5 — Puntos, alertas, analítica y fraude

- Pantalla de puntos.
- Centro de notificaciones.
- Dashboard de analítica.
- Vista de eventos sospechosos.

### Fase 6 — Calidad

- Tests de stores.
- Tests de formularios.
- Tests de hooks con mocks.
- Revisión de accesibilidad básica.

## Riesgos

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Duplicar estado entre Zustand y React Query | Alto | React Query para server state, Zustand para UI. |
| Contrato backend cambia | Alto | OpenAPI como fuente de verdad. |
| Formularios sin validación | Medio | React Hook Form + Zod. |
| Pantallas gigantes | Medio | Separar por feature y componentes. |
| Errores técnicos expuestos al usuario | Medio | Adaptador de errores por `ApiError`. |

## Próximo paso

Crear la base Vite + React + TypeScript y generar el cliente desde `../docs/openapi.yaml` antes de implementar pantallas.
