# Informe Técnico — Plataforma Fintech de Billeteras Digitales

## 1. Resumen del Problema

El proyecto consiste en una plataforma de billeteras digitales con usuarios, transferencias internas/externas, puntos de fidelidad, operaciones programadas, notificaciones automáticas y detección de fraude. El backend implementa **Arquitectura Hexagonal / Clean Architecture** en Java 21 + Spring Boot 3.3. El frontend usa React 19 + TypeScript + TanStack Query.

La restricción académica central es que **ningún componente de dominio o aplicación puede usar estructuras de datos de la JDK** (HashMap, ArrayList, LinkedList, etc.) — todo debe implementarse con estructuras propias definidas en `domain/structures/`.

---

## 2. Módulos del Sistema

| Módulo | Descripción |
|--------|-------------|
| `domain/model` | Entidades puras: Usuario, Billetera, Transaccion, OperacionProgramada, Notificacion, FraudEvent |
| `domain/service` | PuntosCalculator, LoyaltyLevelCalculator, FraudDetector |
| `domain/structures` | TablaHash, MiLista, Pila, ColaSimple, ColaPrioridad, ArbolBST, GrafoTransferencias |
| `domain/port` | Interfaces de repositorio y generadores de ID (output ports) |
| `application/usecase` | 25+ casos de uso: operaciones de billetera, analytics, CRUD usuario, programadas |
| `application/service` | NotificationEmitter |
| `infrastructure/output/memory` | Adaptadores in-memory para todos los repositorios |
| `infrastructure/input/rest` | Controladores REST, DTOs, mappers |
| `infrastructure/config` | Configuración de beans Spring |

---

## 3. Estructuras de Datos Propias

### 3.1 TablaHash\<K, V\>

**Definición**: Tabla hash con encadenamiento separado. Array de `Node<K,V>[]` con `capacity` inicial = 16, load factor 0.75; redimensiona duplicando capacidad.

**Operaciones y Big-O**:

| Operación | Caso promedio | Caso peor |
|-----------|--------------|-----------|
| `put(k, v)` | O(1) | O(n) |
| `get(k)` | O(1) | O(n) |
| `remove(k)` | O(1) | O(n) |
| `containsKey(k)` | O(1) | O(n) |
| `size()` | O(1) | O(1) |
| `keys()` | O(n) | O(n) |

**Justificación**: Usada como índice principal en todos los repositorios in-memory (UserRepository, WalletRepository, etc.), para contar por tipo en analytics (GetMovementByTypeUseCase, GetTopWalletCategoriesUseCase) y para acumular puntos en GetPointsRankingUseCase.

**Rutas de código reales**: `InMemoryUserRepository.store`, `GetTopUsersUseCase.counts`, `GetMovementByTypeUseCase.counts`.

**Alternativas consideradas**: `java.util.HashMap` (prohibida por restricción académica). `TreeMap` ofrecería O(log n) en peor caso pero sin ventaja en caso promedio.

**Trade-off**: Colisiones en tablas muy cargadas degradan a O(n). Aceptable porque los datos en memoria son pequeños en el contexto académico.

---

### 3.2 MiLista\<T\>

**Definición**: Lista enlazada simple con nodo head y size.

**Operaciones y Big-O**:

| Operación | Complejidad |
|-----------|-------------|
| `add(v)` al final | O(n) |
| `addFirst(v)` | O(1) |
| `remove(v)` | O(n) |
| `size()` | O(1) |
| `iterator()` | O(1) inicio, O(n) recorrido |

**Justificación**: Usada en GrafoTransferencias para las listas de adyacencia de cada nodo, en InMemoryTransactionRepository para la lista `all` y `byUser`, y en InMemoryFraudEventRepository.

**Rutas de código reales**: `GrafoTransferencias.AdjacencyEntry.out`, `InMemoryTransactionRepository.all`.

**Alternativas**: `java.util.LinkedList` (prohibida). `java.util.ArrayList` tiene mejor localidad de caché pero prohibida también.

---

### 3.3 Pila\<T\> (Stack)

**Definición**: Pila LIFO sobre array redimensionable.

**Operaciones y Big-O**:

| Operación | Complejidad |
|-----------|-------------|
| `push(v)` | O(1) amortizado |
| `pop()` | O(1) |
| `peek()` | O(1) |
| `isEmpty()` | O(1) |

**Justificación**: Usada en el algoritmo de DFS para detección de ciclos en GrafoTransferencias (coloreado WHITE/GRAY/BLACK). La pila guarda el camino actual del DFS.

**Rutas de código reales**: `GrafoTransferencias.findCycles()` → `Pila<String>` propia para el back-stack del DFS (efecto LIFO).

---

### 3.4 ColaSimple\<T\> (Queue)

**Definición**: Cola FIFO con nodos encadenados. head y tail apuntan al frente y final.

**Operaciones y Big-O**:

| Operación | Complejidad |
|-----------|-------------|
| `enqueue(v)` | O(1) |
| `dequeue()` | O(1) |
| `peek()` | O(1) |
| `isEmpty()` | O(1) |

**Justificación**: Estructura base para procesamiento en orden de llegada. Usada internamente por ColaPrioridad.

---

### 3.5 ColaPrioridad\<T extends Comparable\<T\>\>

**Definición**: Min-heap binario sobre array. El elemento con menor valor según `compareTo` se extrae primero.

**Operaciones y Big-O**:

| Operación | Complejidad |
|-----------|-------------|
| `insert(v)` | O(log n) |
| `extractMin()` | O(log n) |
| `peek()` | O(1) |
| `size()` | O(1) |

**Justificación**: Usada en `ScheduledOperationRepository.findPendingInPriorityOrder()` — las operaciones programadas se ordenan por `scheduledAt` ascendente (la que vence antes se procesa primero). `ExecuteDueScheduledOperationsUseCase` itera este orden.

**Rutas de código reales**: `InMemoryScheduledOperationRepository.findPendingInPriorityOrder()`, `ExecuteDueScheduledOperationsUseCase.execute()`.

**Alternativas**: `java.util.PriorityQueue` (prohibida). Un sort O(n log n) por request sería equivalente pero ineficiente si la cola es grande.

---

### 3.6 ArbolBST\<T extends Comparable\<T\>\>

**Definición**: Árbol binario de búsqueda sin balanceo. Los nodos se insertan según `compareTo`. `inOrder()` retorna elementos en orden ascendente.

**Operaciones y Big-O**:

| Operación | Caso promedio | Caso peor (árbol degenerado) |
|-----------|--------------|------------------------------|
| `insert(v)` | O(log n) | O(n) |
| `inOrder()` | O(n) | O(n) |

**Justificación**: Usado en analytics para ordenar resultados descendentes. El truco es definir `compareTo` como negativo del valor real (o inversión) para que el inorder natural produzca orden descendente:
- `GetTopUsersUseCase.MetricNode`: `Double.compare(-this.value, -other.value)`
- `GetTopWalletsUseCase.MetricNode`: ídem
- `GetTopTransactionsUseCase.TxValueNode`: `Double.compare(other.amount, this.amount)`
- `GetTopWalletCategoriesUseCase.MetricNode`: ídem

**Rutas de código reales**: `GetTopTransactionsUseCase.execute()`, `GetTopUsersUseCase.execute()`.

**Alternativas**: Sort O(n log n) con `Collections.sort` (prohibido). `TreeSet` (prohibido). El BST propio permite integración con las restricciones académicas.

**Trade-off**: Sin balanceo (AVL/Red-Black), el árbol degenera a lista enlazada con datos ya ordenados. Aceptable en contexto académico con pocos registros.

---

### 3.7 GrafoTransferencias

**Definición**: Grafo dirigido ponderado. Almacenamiento interno: `TablaHash<String, AdjacencyEntry>` donde cada `AdjacencyEntry` contiene `MiLista<EdgeNode>` de aristas salientes.

**Operaciones y Big-O**:

| Operación | Complejidad |
|-----------|-------------|
| `addNode(id)` | O(1) promedio |
| `addEdge(src, tgt, amt)` | O(k) donde k = out-degree de src |
| `frequentRoutes(min)` | O(V + E) |
| `findCycles()` | O(V + E) |
| `nodes()` | O(V) |

**Justificación**: Modela las transferencias entre usuarios para detectar rutas frecuentes y ciclos (posibles esquemas de lavado de dinero). `findCycles()` usa DFS con coloreo WHITE/GRAY/BLACK — back-edge detecta ciclo, se reconstruye desde el stack, se normaliza rotando al id más pequeño.

**Rutas de código reales**: `ExternalTransferUseCase` llama `transferGraphRepository.addEdge()`. `GetFrequentRoutesUseCase` llama `frequentRoutes()`. `GetCyclesUseCase` llama `findCycles()`.

**Alternativas**: Lista de adyacencia con `java.util.Map<String, List<String>>` (prohibida). La implementación propia garantiza cumplimiento de restricción académica.

---

## 4. Tabla de Cumplimiento PDF (§4 Requisitos)

| Requisito PDF | Estado | Referencia de implementación |
|--------------|--------|------------------------------|
| TablaHash propia | ✅ CUMPLE | `domain/structures/TablaHash.java` |
| MiLista propia | ✅ CUMPLE | `domain/structures/MiLista.java` |
| Pila propia | ✅ CUMPLE | `domain/structures/Pila.java` |
| ColaSimple propia | ✅ CUMPLE | `domain/structures/ColaSimple.java` |
| ColaPrioridad propia | ✅ CUMPLE | `domain/structures/ColaPrioridad.java` |
| ArbolBST propio | ✅ CUMPLE | `domain/structures/ArbolBST.java` |
| GrafoTransferencias | ✅ CUMPLE | `domain/structures/GrafoTransferencias.java` |
| Detección de ciclos en grafo | ✅ CUMPLE | `GrafoTransferencias.findCycles()` |
| Puntos: floor(amount/100)*rate | ✅ CUMPLE | `PuntosCalculator.compute()` |
| Niveles: ≤500 BRONZE, 501-1000 SILVER, 1001-5000 GOLD, >5000 PLATINUM | ✅ CUMPLE | `LoyaltyLevelCalculator.from()` |
| FraudDetector velocidad ≥3 tx en 60s | ✅ CUMPLE | `FraudDetector.detect()` Rule B |
| FraudDetector monto >10000 | ✅ CUMPLE | `FraudDetector.detect()` Rule A |
| **Regla C — REPEATED_DESTINATION (destino repetido)** | ✅ CUMPLE | `FraudDetector.checkRepeatedDestination()` — severity HIGH, ventana 5 min, umbral ≥3 |
| **Regla D — WALLET_FRAGMENTATION (fragmentación de monto)** | ✅ CUMPLE | `FraudDetector.checkAmountFragmentation()` — severity HIGH, ventana 2 min, ≥3 billeteras y >5000 |
| **Regla E — FREQUENCY_BURST (pico de frecuencia)** | ✅ CUMPLE | `FraudDetector.checkFrequencySpike()` — severity MEDIUM, ≥10 historial, last1h ≥ 5× promedio |
| **Regla F — OFF_HOURS (horario inusual)** | ✅ CUMPLE | `FraudDetector.checkUnusualHours()` — severity LOW, ≥20 historial, hora no vista antes |
| **Benchmarks comparativos estructuras propias vs JDK** | ✅ CUMPLE | `StructureBenchmarkTest.java` + `docs/BENCHMARK_REPORT.md` — 4 pares × 3 tamaños = 24 mediciones (ver §8) |
| Notificaciones LOW_BALANCE, POINTS_LEVEL, TRANSACTION | ✅ CUMPLE | `NotificationEmitter` |
| **Notificación SCHEDULED_REMINDER** | ✅ CUMPLE | `NotificationEmitter.emitScheduledNear(userId, opId, scheduledAt)` — emitida por `ExecuteDueScheduledOperationsUseCase` (idempotente via `remindersSent`; incluye fecha programada en el cuerpo del mensaje) |
| **Notificación OPERATION_REJECTED** | ✅ CUMPLE | `NotificationEmitter.emitOperationRejected()` + `emitScheduledRejected()` — emitida en rutas de fallo de los 4 use cases de transferencia |
| **Notificación BENEFIT_REDEEMED** | ✅ CUMPLE (enum-only) | `NotificationType.BENEFIT_REDEEMED` — slot de compatibilidad futura; no tiene emitter ni use case (ADR-7.2, REQ-3.6) |
| ColaPrioridad para operaciones programadas | ✅ CUMPLE | `InMemoryScheduledOperationRepository.findPendingInPriorityOrder()` |
| Arquitectura Hexagonal | ✅ CUMPLE | Capas domain / application / infrastructure separadas |
| ZERO Spring/Jakarta en domain y application | ✅ CUMPLE | Verificado con `rg` en CI |
| CRUD usuario completo (create, get, update, delete) | ✅ CUMPLE | `UserController`, `UpdateUserUseCase`, `DeleteUserUseCase` |
| Cascade delete al eliminar usuario | ✅ CUMPLE | `DeleteUserUseCase.execute()` — orden: tx → wallet → sched → notif → fraud → user |
| Top transacciones por valor (ArbolBST) | ✅ CUMPLE | `GetTopTransactionsUseCase` |
| Rutas frecuentes (GrafoTransferencias) | ✅ CUMPLE | `GetFrequentRoutesUseCase` |
| Analytics summary | ✅ CUMPLE | `GetAnalyticsSummaryUseCase` |
| Top usuarios, billeteras | ✅ CUMPLE | `GetTopUsersUseCase`, `GetTopWalletsUseCase` |
| Movimientos por tipo | ✅ CUMPLE | `GetMovementByTypeUseCase` |
| Total movido en rango | ✅ CUMPLE | `GetTotalMovedInRangeUseCase` |
| Frontend React con TanStack Query | ✅ CUMPLE | `plataforma-fintech-frontend/src/` |
| Tests ≥420 backend | ✅ CUMPLE | **479 tests** (./mvnw test — 2026-05-14) |
| Tests ≥200 frontend | ✅ CUMPLE | **309 tests** (vitest run — 2026-05-14) |

### ADRs referenciados

- **ADR-7.1** — First-match-wins preservado en `FraudDetector.detect()` (`Optional<FraudEvent>` — cadena A→B→C→D→E→F).
- **ADR-7.2** — `FraudType` como clase de constantes String (no enum) para no romper el wire format de `FraudEvent.type`.

### §8 — Nota sobre Benchmarks

Los benchmarks de estructuras propias se ejecutan on-demand con `./mvnw test -Dsurefire.excludedGroups= -Dgroups=benchmark`. Están excluidos del run normal para no penalizar el tiempo de CI. Los resultados detallados (4 pares, 3 tamaños, mediana de 5 iteraciones con warmup de 3) se encuentran en `docs/BENCHMARK_REPORT.md`.

---

## 5. Casos de Prueba Destacados

| Test | Archivo | Qué valida |
|------|---------|-----------|
| `execute_fiveTransactions_returnsTop3Descending` | `GetTopTransactionsUseCaseTest` | BST ordena desc por amount correctamente |
| `execute_tieBreakByIdAsc` | `GetTopTransactionsUseCaseTest` | Tie-break léxico asc por id |
| `findCycles_singleCycle_returnsNormalizedCycle` | `GrafoTransferenciasTest` | Ciclo normalizado al id más pequeño |
| `findCycles_triangleCycle_returnsNormalized` | `GrafoTransferenciasTest` | Ciclo triangular A→B→C→A detectado y rotado |
| `detect_highVelocity_returnsHighFraudEvent` | `FraudDetectorTest` | ≥3 tx en 60s para mismo user = HIGH |
| `compute_externalTransferSent_floor_correct` | `PuntosCalculatorTest` | floor(amount/100)*3 para EXTERNAL_TRANSFER_SENT |
| `from_boundary501_returnsSilver` | `LoyaltyLevelCalculatorTest` | Borde 501 → SILVER (no BRONZE) |
| `execute_recharge_bonusPlusPointsGranted` | `ExecuteDueScheduledOperationsUseCaseTest` | +5 bonus al ejecutar operación programada |
| `execute_cascadeDeletesAllRepos` | `DeleteUserUseCaseTest` | Orden correcto de cascade delete |

---

## 6. Trade-offs Aceptados

| Decisión | Razón | Consecuencia |
|----------|-------|--------------|
| ArbolBST sin balanceo | Restricción académica; datos pequeños | Degrada a O(n) con datos ordenados |
| Estado in-memory (no persistencia) | Restricción académica | Datos se pierden al reiniciar |
| `ArrayList` de JDK en boundary de infraestructura | Necesario para interop con Spring y serialización JSON (responses REST devuelven `List<T>`) | Aceptado por ADR-9.1: boundary translation. Las estructuras internas siguen siendo propias. |
| DFS iterativo en `GrafoTransferencias.findCycles()`: usa `TablaHash<String,Integer>` para coloreo, `Pila<String>` para back-stack y `MiLista<String>` para reconstrucción de ciclos. Boundary helper retorna `ArrayList` para serialización REST. | Integración total de estructuras propias en el algoritmo DFS | `ArrayList` solo en el boundary de infraestructura para serialización JSON |
| FE: 206 tests cubriendo hooks, páginas y componentes compartidos | Cobertura completa de hooks, páginas y componentes | — |
