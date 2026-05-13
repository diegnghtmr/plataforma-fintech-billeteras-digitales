# ESTRUCTURA DE CLASES Y MÉTODOS

## Compañero 1: Base del Sistema + Estructuras Principales

---

## 🏗️ PAQUETE: modelos

### 📄 Clase: Usuario

```
ATTRIBUTES:
  ├─ id: String                              (Identificador único)
  ├─ nombre: String                          (Nombre completo)
  ├─ email: String                           (Email de contacto)
  ├─ fechaRegistro: long                     (Timestamp de registro)
  ├─ puntosAcumulados: double               (Puntos totales)
  ├─ nivelFidelizacion: String              (Bronce/Plata/Oro/Platino)
  ├─ billeteras: HashMap<String, Billetera> (Billeteras del usuario)
  └─ historialTransacciones: LinkedList     (Todas sus transacciones)

CONSTRUCTOR:
  └─ Usuario(id: String, nombre: String, email: String)

GETTERS:
  ├─ getId(): String
  ├─ getNombre(): String
  ├─ getEmail(): String
  ├─ getPuntosAcumulados(): double
  ├─ getNivelFidelizacion(): String
  ├─ getBilleteras(): HashMap<String, Billetera>
  ├─ getHistorialTransacciones(): LinkedList<Transaccion>
  └─ getFechaRegistro(): long

SETTERS:
  ├─ setNombre(nombre: String): void
  ├─ setEmail(email: String): void
  ├─ setPuntosAcumulados(puntos: double): void
  ├─ agregarPuntos(puntos: double): void
  └─ descontarPuntos(puntos: double): void

MÉTODOS PRINCIPALES:
  ├─ crearBilletera(código: String, nombre: String, tipo: String): boolean
  │  └─ Tiempo: O(1) - Usa HashMap
  │
  ├─ obtenerBilletera(código: String): Billetera
  │  └─ Tiempo: O(1) - Búsqueda en HashMap
  │
  ├─ tieneBilletera(código: String): boolean
  │  └─ Tiempo: O(1) - Búsqueda en HashMap
  │
  ├─ cantidadBilleteras(): int
  │  └─ Tiempo: O(1)
  │
  ├─ calcularSaldoTotal(): double
  │  └─ Tiempo: O(n) - Suma todos los saldos
  │
  ├─ registrarTransaccion(transaccion: Transaccion): void
  │  └─ Tiempo: O(1) - Agregar a LinkedList
  │
  ├─ obtenerHistorial(): LinkedList<Transaccion>
  │  └─ Tiempo: O(n) - Copia la lista
  │
  └─ cantidadTransacciones(): int
     └─ Tiempo: O(1)

MÉTODOS PRIVADOS:
  └─ actualizarNivelFidelizacion(): void
     └─ Verifica puntos y asigna nivel (Bronce/Plata/Oro/Platino)
```

---

### 📄 Clase: Billetera

```
ATTRIBUTES:
  ├─ codigo: String                (Identificador único)
  ├─ nombre: String                (Nombre descriptivo)
  ├─ tipo: String                  (Tipo de billetera)
  ├─ idPropietario: String         (ID del usuario propietario)
  ├─ saldo: double                 (Dinero disponible)
  ├─ activa: boolean               (Estado de la billetera)
  ├─ fechaCreacion: long           (Timestamp de creación)
  └─ historialLocal: LinkedList    (Transacciones de esta billetera)

CONSTANTES:
  ├─ SALDO_MINIMO: 0.0
  └─ SALDO_MAXIMO: 1000000.0

CONSTRUCTOR:
  └─ Billetera(código: String, nombre: String, tipo: String, propietario: String)

GETTERS:
  ├─ getCodigo(): String
  ├─ getNombre(): String
  ├─ getTipo(): String
  ├─ getIdPropietario(): String
  ├─ getSaldo(): double
  ├─ isActiva(): boolean
  ├─ getFechaCreacion(): long
  └─ getHistorialLocal(): LinkedList<Transaccion>

SETTERS:
  ├─ setNombre(nombre: String): void
  └─ setActiva(activa: boolean): void

MÉTODOS DE SALDO:
  ├─ aumentarSaldo(monto: double): boolean
  │  └─ Valida monto, suma al saldo, retorna éxito
  │
  ├─ disminuirSaldo(monto: double): boolean
  │  └─ Valida saldo suficiente, resta, retorna éxito
  │
  └─ tieneSaldoSuficiente(monto: double): boolean
     └─ Verifica si hay fondos suficientes

MÉTODOS DE HISTORIAL:
  ├─ registrarTransaccion(transaccion: Transaccion): void
  │  └─ Tiempo: O(1)
  │
  ├─ cantidadTransacciones(): int
  │  └─ Tiempo: O(1)
  │
  ├─ obtenerUltimas(n: int): LinkedList<Transaccion>
  │  └─ Tiempo: O(n) - IMPLEMENTADO CON RECURSIÓN
  │
  └─ calcularMontoTotalMovido(): double
     └─ Tiempo: O(n) - Suma todos los montos

MÉTODOS PRIVADOS:
  └─ copiarDesde(resultado: LinkedList, índice: int): void
     └─ Método recursivo para copiar transacciones
```

---

### 📄 Clase: Transaccion

```
ATTRIBUTES:
  ├─ id: String                 (ID único con nanotime)
  ├─ timestamp: long            (Momento de la operación)
  ├─ tipo: String               (RECARGA, RETIRO, TRANSFERENCIA, TRANSFERENCIA_RECIBIDA)
  ├─ monto: double              (Cantidad de dinero)
  ├─ billeteraOrigen: String    (Código de origen)
  ├─ billeteraDestino: String   (Código de destino)
  ├─ usuarioOrigen: String      (ID usuario origen)
  ├─ usuarioDestino: String     (ID usuario destino)
  ├─ estado: String             (EXITOSA, REVERTIDA, PENDIENTE, RECHAZADA)
  ├─ puntosGenerados: double   (Puntos ganados)
  ├─ descripcion: String        (Detalles adicionales)
  └─ reversible: boolean        (Puede revertirse)

CONSTRUCTOR:
  └─ Transaccion(tipo: String, monto: double, billeteraOrigen: String,
                 billeteraDestino: String, usuarioOrigen: String,
                 usuarioDestino: String, descripcion: String)

GETTERS:
  ├─ getId(): String
  ├─ getTimestamp(): long
  ├─ getTipo(): String
  ├─ getMonto(): double
  ├─ getBilleteraOrigen(): String
  ├─ getBilleteraDestino(): String
  ├─ getUsuarioOrigen(): String
  ├─ getUsuarioDestino(): String
  ├─ getEstado(): String
  ├─ getPuntosGenerados(): double
  ├─ getDescripcion(): String
  └─ isReversible(): boolean

SETTERS:
  ├─ setEstado(estado: String): void
  ├─ setPuntosGenerados(puntos: double): void
  └─ setReversible(reversible: boolean): void

MÉTODOS DE VALIDACIÓN:
  ├─ puedeRevertirse(): boolean
  │  └─ Retorna true si es reversible y no ha sido revertida
  │
  └─ marcarComoRevertida(): boolean
     └─ Marca como REVERTIDA y no reversible (una sola vez)

MÉTODOS DE INFORMACIÓN:
  ├─ obtenerDetalles(): String
  │  └─ Retorna descripción completa de la transacción
  │
  └─ crearCopia(): Transaccion
     └─ Copia profunda para auditoría

MÉTODOS ESPECIALES:
  ├─ equals(obj: Object): boolean
  ├─ hashCode(): int
  └─ toString(): String
```

---

## 🏗️ PAQUETE: estructuras

### 📄 Clase: TablaHash<K, V>

```
ATRIBUTOS ESTÁTICOS:
  ├─ CAPACIDAD_INICIAL: 16
  └─ FACTOR_CARGA: 0.75

ATRIBUTOS:
  ├─ tabla: Entrada<K, V>[]    (Array de cadenas)
  └─ tamaño: int               (Número de pares clave-valor)

CLASE INTERNA: Entrada<K, V>
  ├─ clave: K
  ├─ valor: V
  └─ siguiente: Entrada<K, V>  (Siguiente en la cadena - resolución de colisiones)

CONSTRUCTOR:
  └─ TablaHash()

MÉTODOS PRINCIPALES:
  ├─ insertar(clave: K, valor: V): void
  │  └─ Tiempo: O(1) promedio, O(n) peor caso
  │  └─ Redimensiona si es necesario
  │
  ├─ obtener(clave: K): V
  │  └─ Tiempo: O(1) promedio, O(n) peor caso
  │  └─ Retorna null si no existe
  │
  ├─ contiene(clave: K): boolean
  │  └─ Tiempo: O(1) promedio
  │
  ├─ eliminar(clave: K): V
  │  └─ Tiempo: O(1) promedio, O(n) peor caso
  │  └─ Retorna valor eliminado o null
  │
  ├─ tamaño(): int
  │  └─ Tiempo: O(1)
  │
  ├─ estaVacia(): boolean
  │  └─ Tiempo: O(1)
  │
  ├─ limpiar(): void
  │  └─ Tiempo: O(1) - Reset
  │
  └─ obtenerEstadisticas(): String
     └─ Retorna información sobre ocupación

MÉTODOS PRIVADOS:
  ├─ hash(clave: K): int
  │  └─ Calcula índice usando hashCode
  │
  └─ redimensionar(): void
     └─ Tiempo: O(n)
     └─ Duplica capacidad cuando tamaño >= FACTOR_CARGA * capacidad
```

---

### 📄 Clase: Pila<T>

```
ATRIBUTOS:
  ├─ cima: Nodo<T>  (Punto de inserción/extracción)
  └─ tamaño: int    (Número de elementos)

CLASE INTERNA: Nodo<T>
  ├─ dato: T
  └─ siguiente: Nodo<T>

CONSTRUCTOR:
  └─ Pila()

MÉTODOS PRINCIPALES:
  ├─ apilar(dato: T): void
  │  └─ Tiempo: O(1)
  │  └─ Inserta en la cima
  │
  ├─ desapilar(): T
  │  └─ Tiempo: O(1)
  │  └─ Extrae y retorna de la cima, null si vacía
  │
  ├─ cima(): T
  │  └─ Tiempo: O(1)
  │  └─ Consulta sin extraer, null si vacía
  │
  ├─ estaVacia(): boolean
  │  └─ Tiempo: O(1)
  │
  ├─ tamaño(): int
  │  └─ Tiempo: O(1)
  │
  ├─ limpiar(): void
  │  └─ Tiempo: O(1)
  │  └─ Vacía la pila
  │
  ├─ obtenerElementos(): LinkedList<T>
  │  └─ Tiempo: O(n)
  │  └─ Copia todos los elementos en orden (cima -> base)
  │
  ├─ contarConFiltro(filtro: Filtro<T>): int
  │  └─ Tiempo: O(n)
  │  └─ IMPLEMENTADO CON RECURSIÓN
  │
  └─ toString(): String
     └─ Retorna representación visual

MÉTODOS PRIVADOS:
  └─ contarRecursivo(nodo: Nodo<T>, filtro: Filtro<T>): int
     └─ Método recursivo para contar elementos que cumplen condición

INTERFAZ ANIDADA: Filtro<T>
  └─ cumple(elemento: T): boolean
     └─ Método para filtrar elementos
```

---

### 📄 Clase: MiLista<T> implements Iterable<T>

```
ATRIBUTOS:
  ├─ cabeza: Nodo<T>  (Primer elemento)
  ├─ cola: Nodo<T>    (Último elemento)
  └─ tamaño: int      (Número de elementos)

CLASE INTERNA: Nodo<T>
  ├─ dato: T
  ├─ siguiente: Nodo<T>   (Puntero adelante)
  └─ anterior: Nodo<T>    (Puntero atrás - lista doblemente enlazada)

CONSTRUCTOR:
  └─ MiLista()

MÉTODOS PRINCIPALES:
  ├─ agregar(dato: T): void
  │  └─ Tiempo: O(1)
  │  └─ Añade al final
  │
  ├─ insertar(índice: int, dato: T): boolean
  │  └─ Tiempo: O(n)
  │  └─ Inserta en posición específica
  │
  ├─ obtener(índice: int): T
  │  └─ Tiempo: O(n) - Búsqueda desde extremo más cercano
  │
  ├─ eliminar(índice: int): T
  │  └─ Tiempo: O(n)
  │  └─ Elimina y retorna elemento
  │
  ├─ buscar(dato: T): int
  │  └─ Tiempo: O(n)
  │  └─ Retorna índice o -1
  │
  ├─ contiene(dato: T): boolean
  │  └─ Tiempo: O(n)
  │
  ├─ tamaño(): int
  │  └─ Tiempo: O(1)
  │
  ├─ estaVacia(): boolean
  │  └─ Tiempo: O(1)
  │
  ├─ limpiar(): void
  │  └─ Tiempo: O(1)
  │
  ├─ invertir(): void
  │  └─ Tiempo: O(n)
  │  └─ IMPLEMENTADO CON RECURSIÓN
  │
  ├─ obtenerPrimero(): T
  │  └─ Tiempo: O(1)
  │
  ├─ obtenerUltimo(): T
  │  └─ Tiempo: O(1)
  │
  ├─ iterator(): Iterator<T>
  │  └─ Retorna iterador
  │
  └─ toString(): String

MÉTODOS PRIVADOS:
  ├─ obtenerNodo(índice: int): Nodo<T>
  │  └─ Tiempo: O(n) pero optimizado (busca desde extremo más cercano)
  │
  ├─ invertirRecursivo(nodo: Nodo<T>): void
  │  └─ Método recursivo para invertir
  │
  └─ IteradorLista (clase privada)
     ├─ hasNext(): boolean
     └─ next(): T
```

---

## 🏗️ PAQUETE: validaciones

### 📄 Clase: ValidadorFinanciero

```
CONSTANTES ESTÁTICAS:
  ├─ MONTO_MINIMO: 0.01
  └─ MONTO_MAXIMO: 1000000.0

MÉTODOS ESTÁTICOS DE VALIDACIÓN:

1. USUARIOS:
  └─ validarUsuario(usuario: Usuario): boolean
     └─ Verifica usuario no nulo, ID válido

2. BILLETERAS:
  ├─ validarBilletera(billetera: Billetera): boolean
  │  └─ Verifica no nula, activa, saldo válido
  │
  └─ validarSaldoSuficiente(billetera: Billetera, monto: double): boolean
     └─ Verifica hay suficiente dinero

3. MONTOS:
  └─ validarMonto(monto: double): boolean
     └─ Verifica es positivo, en rango, no NaN/Infinito

4. TRANSACCIONES:
  ├─ validarRecarga(usuario: Usuario, código: String, monto: double): boolean
  │  └─ Todas las validaciones para recargar
  │
  ├─ validarRetiro(usuario: Usuario, código: String, monto: double): boolean
  │  └─ Todas las validaciones para retirar
  │
  ├─ validarTransferenciaInterna(usuario: Usuario, origen: String,
  │                               destino: String, monto: double): boolean
  │  └─ Validar transferencia dentro de mismo usuario
  │
  ├─ validarTransferenciaExterna(usuarioOrigen: Usuario, origenCódigo: String,
  │                               usuarioDestino: Usuario, destinoCódigo: String,
  │                               monto: double): boolean
  │  └─ Validar transferencia entre usuarios
  │
  ├─ validarTransaccion(transaccion: Transaccion): boolean
  │  └─ Validar detalles de transacción
  │
  └─ validarReversibilidad(transaccion: Transaccion): boolean
     └─ Verificar puede revertirse
```

---

## 🏗️ PAQUETE: operaciones

### 📄 Clase: OperacionesFinancieras

```
ATRIBUTOS:
  ├─ usuarios: TablaHash<String, Usuario>          (Tabla Hash para O(1))
  ├─ pilaDeshecho: Pila<Transaccion>               (Para deshacer - LIFO)
  └─ historialGlobal: LinkedList<Transaccion>      (Histórico global)

CONSTRUCTOR:
  └─ OperacionesFinancieras()

MÉTODOS DE GESTIÓN DE USUARIOS:
  ├─ registrarUsuario(id: String, nombre: String, email: String): boolean
  │  └─ Tiempo: O(1)
  │
  ├─ obtenerUsuario(id: String): Usuario
  │  └─ Tiempo: O(1)
  │  └─ Retorna null si no existe
  │
  ├─ usuarioExiste(id: String): boolean
  │  └─ Tiempo: O(1)
  │
  └─ cantidadUsuarios(): int
     └─ Tiempo: O(1)

MÉTODOS DE GESTIÓN DE BILLETERAS:
  ├─ crearBilletera(idUsuario: String, código: String,
  │                 nombre: String, tipo: String): boolean
  │  └─ Tiempo: O(1)
  │
  └─ obtenerBilletera(idUsuario: String, código: String): Billetera
     └─ Tiempo: O(1)

MÉTODOS DE OPERACIONES FINANCIERAS:
  ├─ recargar(idUsuario: String, código: String, monto: double): boolean
  │  └─ Tiempo: O(1)
  │  └─ Valida, aumenta saldo, genera puntos, registra
  │
  ├─ retirar(idUsuario: String, código: String, monto: double): boolean
  │  └─ Tiempo: O(1)
  │  └─ Valida, disminuye saldo, genera puntos, registra
  │
  ├─ transferirInterno(idUsuario: String, codigoOrigen: String,
  │                    codigoDestino: String, monto: double): boolean
  │  └─ Tiempo: O(1)
  │  └─ Mueve entre billeteras del mismo usuario
  │
  └─ transferirExterno(idUsuarioOrigen: String, codigoOrigen: String,
                       idUsuarioDestino: String, codigoDestino: String,
                       monto: double): boolean
     └─ Tiempo: O(1)
     └─ Mueve entre usuarios diferentes

MÉTODOS DE REVERSIÓN:
  ├─ deshacerOperacion(): boolean
  │  └─ Tiempo: O(1)
  │  └─ Desapila, valida, revierte dinero y puntos
  │
  └─ cantidadOperacionesReversibles(): int
     └─ Tiempo: O(1)
     └─ Retorna tamaño de pila

MÉTODOS DE CONSULTA:
  ├─ obtenerSaldoTotal(idUsuario: String): double
  │  └─ Suma todos los saldos del usuario
  │
  ├─ obtenerSaldo(idUsuario: String, código: String): double
  │  └─ Saldo de billetera específica
  │
  ├─ obtenerHistorialUsuario(idUsuario: String): LinkedList<Transaccion>
  │  └─ Retorna copia del histórico
  │
  ├─ obtenerHistorialGlobal(): LinkedList<Transaccion>
  │  └─ Retorna todas las transacciones del sistema
  │
  └─ obtenerDetallesUsuario(idUsuario: String): String
     └─ Retorna información formateada del usuario
```

---

## 🏗️ PAQUETE: main

### 📄 Clase: Main

```
MÉTODO PRINCIPAL:
  └─ main(args: String[]): void
     └─ Ejecuta 13 pruebas exhaustivas:

1. Registro de Usuarios (3 usuarios)
2. Creación de Billeteras (6 billeteras)
3. Operaciones de Recarga (6 recargas)
4. Operaciones de Retiro (2 retiros)
5. Transferencias Internas (2 transferencias)
6. Transferencias Externas (2 transferencias)
7. Consulta de Saldos (4 consultas)
8. Historial de Transacciones (análisis)
9. Sistema de Puntos y Niveles (análisis)
10. Reversión de Operaciones (1 deshacer)
11. Validaciones (pruebas de error)
12. Información Detallada de Usuarios
13. Historial Global del Sistema

SALIDA:
  └─ Interfaz visual con tablas y texto formateado
     └─ Muestra éxitos y errores esperados
```

---

## 📊 RESUMEN DE COMPLEJIDADES

| Operación | Complejidad | Estructura |
|-----------|-------------|-----------|
| registrar usuario | O(1) | TablaHash |
| crear billetera | O(1) | HashMap |
| buscar usuario | O(1) | TablaHash |
| recargar | O(1) | Múltiples O(1) |
| retirar | O(1) | Múltiples O(1) |
| transferir | O(1) | Múltiples O(1) |
| deshacer | O(1) | Pila.desapilar |
| historial | O(n) | LinkedList.copy |
| saldo total | O(m) | m = # billeteras |
| invertir lista | O(n) | Recursión |
| obtener últimas | O(n) | Recursión |
| contar con filtro | O(n) | Recursión |

---

## 🎯 CARACTERÍSTICAS ACADÉMICAS

✅ **Estructuras de Datos Personalizadas**:
- TablaHash con encadenamiento
- Pila enlazada
- Lista doblemente enlazada

✅ **Recursión Implementada En**:
- MiLista.invertir()
- MiLista.obtenerUltimas()
- Pila.contarConFiltro()
- Billetera.copiarDesde()

✅ **Collections de Java**:
- HashMap internamente
- LinkedList internamente
- Iterable implementado

✅ **Patrones de Diseño**:
- Validator Pattern
- Factory Pattern (extensible)
- Strategy Pattern (extensible)

✅ **Validaciones Exhaustivas**:
- Cada operación es validada
- Integridad de datos
- Inversión de errores

**TODAS LAS CLASES ESTÁN DOCUMENTADAS CON JAVADOC**
