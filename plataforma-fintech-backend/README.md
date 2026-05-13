# PLATAFORMA FINTECH DE BILLETERAS DIGITALES

## Proyecto Final - Estructura de Datos

### Compañero 1: Base del Sistema + Estructuras Principales

---

## 📋 Descripción

Este módulo implementa el núcleo del sistema de billeteras digitales, incluyendo las estructuras de datos fundamentales y todas las operaciones financieras básicas necesarias para que otros módulos construyan funcionalidades adicionales.

---

## 🏗️ Arquitectura del Proyecto

```
src/
├── modelos/
│   ├── Usuario.java          # Clase que representa un usuario
│   ├── Billetera.java        # Clase que representa una billetera digital
│   └── Transaccion.java      # Clase que representa una transacción
├── estructuras/
│   ├── TablaHash.java        # Tabla Hash personalizada para búsqueda O(1)
│   ├── Pila.java             # Pila (Stack) para reversión de operaciones
│   └── MiLista.java          # Lista enlazada doblemente enlazada
├── operaciones/
│   └── OperacionesFinancieras.java  # Lógica de todas las operaciones
├── validaciones/
│   └── ValidadorFinanciero.java     # Validaciones de operaciones
└── main/
    └── Main.java             # Clase principal con pruebas
```

---

## 📊 Estructuras de Datos Implementadas

### 1. **TablaHash<K, V>** - Acceso Rápido
- **Propósito**: Búsqueda eficiente de usuarios y billeteras
- **Implementación**: Tabla hash con encadenamiento para resolver colisiones
- **Complejidad**:
  - Inserción: O(1) promedio
  - Búsqueda: O(1) promedio
  - Eliminación: O(1) promedio
  - Redimensionamiento: O(n)
- **Factor de carga**: 0.75
- **Uso**: 
  - Almacenar usuarios por ID
  - Almacenar billeteras por usuario

### 2. **MiLista<T>** - Historial Flexible
- **Propósito**: Almacenar histórico de transacciones
- **Implementación**: Lista doblemente enlazada
- **Complejidad**:
  - Inserción al final: O(1)
  - Búsqueda: O(n)
  - Acceso: O(n) - optimizado para extremos
  - Inversión: O(n) con recursión
- **Características especiales**:
  - Búsqueda desde el extremo más cercano
  - Iterador implementado
  - Método de inversión recursiva
- **Uso**: Historial de transacciones por usuario y billetera

### 3. **Pila<T>** - Reversión de Operaciones
- **Propósito**: Permitir "deshacer" transacciones (LIFO)
- **Implementación**: Pila enlazada
- **Complejidad**:
  - Apilar: O(1)
  - Desapilar: O(1)
  - Cima: O(1)
  - Contar con filtro: O(n) con recursión
- **Características especiales**:
  - Conteo recursivo con filtro
  - Método de consulta sin extracción
- **Uso**: Historial de deshacer para revertir operaciones

### 4. **LinkedList** (Java Collections)
- **Propósito**: Historial global de transacciones
- **Uso**: Almacenar todas las transacciones del sistema

---

## 🎯 Clases Modelo

### Usuario.java
```java
- id: String                              // ID único
- nombre: String                          // Nombre completo
- email: String                           // Email de contacto
- fechaRegistro: long                     // Timestamp de registro
- puntosAcumulados: double               // Puntos de fidelización
- nivelFidelizacion: String              // Nivel (Bronce/Plata/Oro/Platino)
- billeteras: HashMap<String, Billetera> // Billeteras del usuario
- historialTransacciones: LinkedList      // Transacciones del usuario
```

**Métodos principales**:
- `crearBilletera()` - Crea nueva billetera (O(1))
- `obtenerBilletera()` - Obtiene billetera (O(1))
- `agregarPuntos()` / `descontarPuntos()` - Gestiona puntos
- `calcularSaldoTotal()` - Suma todos los saldos (O(n))

### Billetera.java
```java
- codigo: String              // ID único
- nombre: String              // Nombre descriptivo
- tipo: String                // Tipo (Ahorro, Gastos diarios, etc.)
- saldo: double              // Saldo disponible
- activa: boolean            // Estado de la billetera
- historialLocal: LinkedList  // Transacciones de esta billetera
```

**Métodos principales**:
- `aumentarSaldo()` - Incrementa saldo (O(1))
- `disminuirSaldo()` - Decrementa saldo (O(1))
- `registrarTransaccion()` - Añade a historial (O(1))
- `obtenerUltimas()` - Últimas N transacciones con recursión (O(n))

### Transaccion.java
```java
- id: String                  // ID único (nanotiempo)
- timestamp: long            // Momento de transacción
- tipo: String               // RECARGA, RETIRO, TRANSFERENCIA, etc.
- monto: double              // Cantidad de dinero
- billeteraOrigen: String    // Código origen
- billeteraDestino: String   // Código destino
- estado: String             // EXITOSA, REVERTIDA, PENDIENTE, RECHAZADA
- puntosGenerados: double    // Puntos ganados
- reversible: boolean        // Puede revertirse
```

**Métodos principales**:
- `puedeRevertirse()` - Verifica reversibilidad
- `marcarComoRevertida()` - Marca como revertida
- `crearCopia()` - Copia profunda para auditoría

---

## 💰 Operaciones Financieras

### Implementadas en OperacionesFinancieras.java

#### 1. **Recargar** (O(1) promedio)
```java
public boolean recargar(String idUsuario, String codigoBilletera, double monto)
```
- Incrementa saldo
- Genera puntos (1 por cada 100 unidades)
- Registra transacción
- Guarda en Pila para reversión

#### 2. **Retirar** (O(1) promedio)
```java
public boolean retirar(String idUsuario, String codigoBilletera, double monto)
```
- Decrementa saldo (con validación)
- Genera puntos (2 por cada 100 unidades)
- Registra transacción
- Guarda en Pila para reversión

#### 3. **Transferencia Interna** (O(1) promedio)
```java
public boolean transferirInterno(String idUsuario, String codigoOrigen, 
                                 String codigoDestino, double monto)
```
- Mueve dinero entre billeteras del mismo usuario
- Genera puntos (3 por cada 100 unidades)
- Valida origen ≠ destino
- Se puede revertir

#### 4. **Transferencia Externa** (O(1) promedio)
```java
public boolean transferirExterno(String idUsuarioOrigen, String codigoOrigen,
                                String idUsuarioDestino, String codigoDestino,
                                double monto)
```
- Mueve dinero entre años diferentes
- Crea dos transacciones (envío y recepción)
- Genera puntos solo para quien envía
- Se registra en ambas cuentas

#### 5. **Deshacer Operación** - Pila (O(1))
```java
public boolean deshacerOperacion()
```
- Desapila última transacción (LIFO)
- Revierte saldos según tipo
- Descuenta puntos
- Marca como REVERTIDA

---

## ✅ Sistema de Validaciones

### ValidadorFinanciero.java

Proporciona métodos estáticos para validar:
- ✓ Usuarios válidos e existentes
- ✓ Billeteras activas y válidas
- ✓ Montos en rango permitido (0.01 - 1,000,000)
- ✓ Saldo suficiente en origen
- ✓ Operaciones no duplicadas
- ✓ Transacciones reversibles
- ✓ Consistencia de datos

**Todas las operaciones son validadas antes de ejecutarse.**

---

## 🎓 Justificación de Estructuras

| Estructura | Uso | Justificación |
|-----------|-----|---------------|
| **TablaHash** | Usuarios, Billeteras | O(1) en búsqueda es crítico para acceso instantáneo |
| **LinkedList** | Historial transacciones | Flexibilidad para inserción/eliminación en cualquier punto |
| **Pila** | Reversión operaciones | LIFO es natural para deshacer acciones |
| **Recursión** | Inversión listas, búsqueda | Elegancia y simplicidad en algoritmos |
| **LinkedList Java** | Historial global | Estructura estándar, confiable, O(1) insert/delete |

---

## 📊 Política de Puntos

| Operación | Puntos | Fórmula |
|-----------|--------|---------|
| Recarga | 1 x 100 | monto ÷ 100 |
| Retiro | 2 x 100 | (monto ÷ 100) × 2 |
| Transferencia | 3 x 100 | (monto ÷ 100) × 3 |
| Recibida | 0 | Sin puntos |

### Niveles de Fidelización

| Nivel | Rango de Puntos | Beneficios |
|-------|-----------------|-----------|
| 🥉 Bronce | 0 - 500 | Base |
| 🥈 Plata | 501 - 1000 | Reducción 1% comisiones |
| 🥇 Oro | 1001 - 5000 | Reducción 2%, Prioridad |
| 💎 Platino | 5001+ | Reducción 5%, VIP |

---

## 🧪 Pruebas Incluidas

El archivo `Main.java` incluye 13 pruebas exhaustivas:

1. ✓ Registro de usuarios
2. ✓ Creación de billeteras
3. ✓ Operaciones de recarga
4. ✓ Operaciones de retiro
5. ✓ Transferencias internas
6. ✓ Transferencias externas
7. ✓ Consulta de saldos
8. ✓ Historial de transacciones
9. ✓ Sistema de puntos y niveles
10. ✓ Reversión de operaciones
11. ✓ Validaciones de saldo
12. ✓ Información detallada de usuarios
13. ✓ Historial global del sistema

---

## 🚀 Cómo Ejecutar

### Compilar
```bash
javac -d . src/modelos/*.java src/estructuras/*.java src/validaciones/*.java src/operaciones/*.java src/main/*.java
```

### Ejecutar
```bash
java main.Main
```

---

## 📈 Complejidades Temporales

| Operación | Complejidad | Observaciones |
|-----------|-------------|--------------|
| Registrar usuario | O(1) | Tabla Hash |
| Crear billetera | O(1) | HashMap interno |
| Recargar | O(1) | Múltiples O(1) |
| Retirar | O(1) | Múltiples O(1) |
| Transferencia | O(1) | Múltiples O(1) |
| Deshacer | O(1) | Desapilar Pila |
| Buscar usuario | O(1) | Tabla Hash |
| Historial usuario | O(n) | n = # transacciones |
| Calcular saldo total | O(m) | m = # billeteras |

---

## 🔒 Seguridad y Validación

- Todas las operaciones son validadas
- No se permite saldo negativo
- Máximo permitido: $1,000,000 por billetera
- Mínimo permitido: $0.01 por transacción
- Todas las transacciones son registradas
- Puntos no pueden ser negativos
- Billeteras inactivas no aceptan operaciones

---

## 🎯 Extensiones Posibles (Para otros compañeros)

### Compañero 2: Operaciones Programadas
- Cola de Prioridad para operaciones futuras
- Procesamiento automático por fecha/hora
- Recordatorios y notificaciones

### Compañero 3: Análisis Avanzado
- Árboles para clasificar usuarios por puntos
- Grafos para analizar transferencias
- Reportes estadísticos

### Compañero 4: Detección de Fraude
- Algoritmos para patrones sospechosos
- Alertas de comportamiento inusual
- Auditoría de transacciones

---

## 📺 Salida de Ejemplo

```
╔════════════════════════════════════════════════════════════════╗
║   PLATAFORMA FINTECH DE BILLETERAS DIGITALES - VERSIÓN 1.0   ║
╚════════════════════════════════════════════════════════════════╝

┌─ PRUEBA 1: Registro de Usuarios ─────────────────────────────────┐
[ÉXITO] Usuario Juan Pérez registrado exitosamente.
[ÉXITO] Usuario María García registrada exitosamente.
Total de usuarios registrados: 3

...más pruebas...

║                     PRUEBAS COMPLETADAS                      ║
```

---

## 👨‍💻 Autor

**Compañero 1 - Base del Sistema**

---

## 📝 Notas de Implementación

1. **Recursión**: Se utiliza en:
   - `MiLista.invertir()` - Inversión recursiva de listas
   - `MiLista.obtenerUltimas()` - Copia recursiva
   - `Pila.contarConFiltro()` - Conteo recursivo
   - `Billetera.copiarDesde()` - Copia recursiva

2. **Java Collections**: Se utiliza:
   - `HashMap` internamente en Usuario
   - `LinkedList` para históriales
   - `Stack` podría reemplazar Pila personalizada

3. **Patrones de Diseño**:
   - Validator: `ValidadorFinanciero`
   - Observer: Notificaciones de nivel (extensible)

---

## 📅 Versión

**v1.0** - Implementación inicial con todas las operaciones básicas
