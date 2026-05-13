# NOTAS DE DISEÑO Y DECISIONES ARQUITECTÓNICAS

## Proyecto: Plataforma Fintech de Billeteras Digitales
## Compañero 1: Base del Sistema + Estructuras Principales

---

## 1. DECISIONES ARQUITECTÓNICAS

### 1.1 Separación por Paquetes

```
modelos/        → Clases de dominio (Usuario, Billetera, Transaccion)
estructuras/    → Estructuras de datos personalizadas
operaciones/    → Lógica de negocio y operaciones
validaciones/   → Validadores centralizados
main/           → Punto de entrada y pruebas
```

**Justificación**: Mantiene código limpio, escalable y fácil de mantener. Cada paquete tiene responsabilidad única.

---

## 2. ESTRUCTURAS DE DATOS IMPLEMENTADAS

### 2.1 TablaHash<K, V> - Personalizada

#### ¿Por qué una Tabla Hash personalizada?

**Para demostrar académicamente**:
- Cómo funcionan internamente los HashMap
- Resolución de colisiones por encadenamiento
- Factor de carga y redimensionamiento
- Complejidad O(1) promedio

#### Características:

```java
// Resolución de colisiones
private static class Entrada<K, V> {
    K clave;
    V valor;
    Entrada<K, V> siguiente;  // Encadenamiento
}

// Redimensionamiento automático
private void redimensionar() {
    // Duplica capacidad cuando tamaño >= FACTOR_CARGA * capacidad
}
```

**Uso en el sistema**:
```java
usuarios.insertar("USR001", usuario);      // O(1)
Usuario u = usuarios.obtener("USR001");    // O(1)
usuarios.eliminar("USR001");                 // O(1)
```

---

### 2.2 MiLista<T> - Lista Doblemente Enlazada

#### ¿Por qué doblemente enlazada?

**Ventajas**:
- Búsqueda optimizada desde ambos extremos
- Reversión eficiente
- Acceso O(n/2) en promedio en lugar de O(n)

#### Características especiales:

```java
private Nodo<T> cabeza;  // Primer elemento
private Nodo<T> cola;    // Último elemento

// Nodo doblemente enlazado
private static class Nodo<T> {
    T dato;
    Nodo<T> siguiente;
    Nodo<T> anterior;  // Permite ir hacia atrás
}

// Búsqueda inteligente
private Nodo<T> obtenerNodo(int indice) {
    if (indice < tamaño / 2) {
        // Buscar desde inicio
    } else {
        // Buscar desde final (más eficiente)
    }
}
```

**Método Recursivo para Reversión**:
```java
public void invertir() {
    invertirRecursivo(cabeza);  // O(n) con recursión
    // Intercambiar cabeza y cola
}

private void invertirRecursivo(Nodo<T> nodo) {
    if (nodo == null) return;
    invertirRecursivo(nodo.siguiente);
    // Invertir punteros
    Nodo<T> temp = nodo.siguiente;
    nodo.siguiente = nodo.anterior;
    nodo.anterior = temp;
}
```

---

### 2.3 Pila<T> - Stack Enlazada

#### Diseño:

```java
private Nodo<T> cima;  // Punto de inserción/extracción

public void apilar(T dato) {
    Nodo<T> nuevoNodo = new Nodo<>(dato);
    nuevoNodo.siguiente = cima;  // Nuevo punto de cima
    cima = nuevoNodo;
}

public T desapilar() {
    if (estaVacia()) return null;
    T dato = cima.dato;
    cima = cima.siguiente;  // Mover cima al siguiente
    return dato;
}
```

#### Uso para Reversión:

```java
// En OperacionesFinancieras
private Pila<Transaccion> pilaDeshecho;

public boolean recargar(...) {
    // ... ejecutar recarga ...
    pilaDeshecho.apilar(transaccion);  // Guardar para deshacer
}

public boolean deshacerOperacion() {
    Transaccion t = pilaDeshecho.desapilar();  // LIFO
    // Revertir transacción
}
```

**Ventaja LIFO**: La última operación es la primera en revertirse (natural e intuitivo).

---

### 2.4 Recursión en el Proyecto

#### Ubicaciones donde se utiliza:

**1. MiLista.invertir()** - Inversión recursiva
```java
private void invertirRecursivo(Nodo<T> nodo) {
    if (nodo == null) return;                    // Base
    invertirRecursivo(nodo.siguiente);           // Recursión
    // Procesar en retorno
}
```

**2. MiLista.obtenerUltimas()** - Copia recursiva
```java
private void copiarDesde(LinkedList<T> resultado, int indice) {
    if (indice >= historialLocal.size()) return; // Base
    resultado.add(historialLocal.get(indice));
    copiarDesde(resultado, indice + 1);          // Recursión
}
```

**3. Pila.contarConFiltro()** - Conteo recursivo
```java
private int contarRecursivo(Nodo<T> nodo, Filtro<T> filtro) {
    if (nodo == null) return 0;                  // Base
    int cuenta = filtro.cumple(nodo.dato) ? 1 : 0;
    return cuenta + contarRecursivo(...);        // Recursión
}
```

---

## 3. CLASES MODELO

### 3.1 Usuario.java

#### Responsabilidades:

- Almacenar información personal
- Gestionar billeteras (HashMap)
- Mantener historial de transacciones (LinkedList)
- Gestionar puntos y niveles

#### Puntos de Diseño:

```java
// Tabla Hash para billeteras (acceso O(1))
private HashMap<String, Billetera> billeteras;

// LinkedList para historial (flexible)
private LinkedList<Transaccion> historialTransacciones;

// Niveles automáticos
private void actualizarNivelFidelizacion() {
    if (puntosAcumulados <= 500) nivel = "Bronce";
    else if (puntosAcumulados <= 1000) nivel = "Plata";
    else if (puntosAcumulados <= 5000) nivel = "Oro";
    else nivel = "Platino";
}
```

---

### 3.2 Billetera.java

#### Responsabilidades:

- Mantener saldo
- Registrar transacciones locales
- Validar operaciones simples

#### Puntos de Diseño:

```java
// LinkedList para historial local
private LinkedList<Transaccion> historialLocal;

// Método con recursión
public LinkedList<Transaccion> obtenerUltimas(int n) {
    LinkedList<Transaccion> resultado = new LinkedList<>();
    int inicio = Math.max(0, historialLocal.size() - n);
    copiarDesde(resultado, inicio);  // Recursivo
    return resultado;
}

// Validaciones simples
private static final double SALDO_MAXIMO = 1000000.0;
private static final double SALDO_MINIMO = 0.0;
```

---

### 3.3 Transaccion.java

#### Responsabilidades:

- Registrar movimiento de dinero
- Generar ID único
- Gestionar reversibilidad
- Almacenar puntos generados

#### Puntos de Diseño:

```java
// ID único usando nanotime
private String generarId() {
    return "TXN" + System.nanoTime();
}

// Control de reversibilidad
private boolean reversible = true;

public boolean puedeRevertirse() {
    return reversible && !estado.equals("REVERTIDA");
}

public boolean marcarComoRevertida() {
    if (puedeRevertirse()) {
        this.estado = "REVERTIDA";
        this.reversible = false;  // No se puede revertir dos veces
        return true;
    }
    return false;
}
```

---

## 4. LÓGICA DE OPERACIONES

### 4.1 OperacionesFinancieras.java

#### Estructura Central:

```java
public class OperacionesFinancieras {
    // Tabla Hash: O(1) búsqueda de usuarios
    private TablaHash<String, Usuario> usuarios;
    
    // Pila: Historial de deshacer (LIFO)
    private Pila<Transaccion> pilaDeshecho;
    
    // LinkedList: Historial global
    private LinkedList<Transaccion> historialGlobal;
}
```

#### Patrón de Operación:

```java
public boolean recargar(String idUsuario, String codigoBilletera, double monto) {
    // 1. Obtener usuario (O(1))
    Usuario usuario = obtenerUsuario(idUsuario);
    
    // 2. Validar (múltiples checks)
    if (!ValidadorFinanciero.validarRecarga(...)) return false;
    
    // 3. Modificar estado
    billetera.aumentarSaldo(monto);
    
    // 4. Registrar transacción
    Transaccion transaccion = new Transaccion(...);
    usuario.registrarTransaccion(transaccion);
    billetera.registrarTransaccion(transaccion);
    historialGlobal.add(transaccion);
    
    // 5. Actualizar puntos
    usuario.agregarPuntos(puntos);
    
    // 6. Guardar para deshacer
    pilaDeshecho.apilar(transaccion);
    
    return true;
}
```

---

## 5. POLÍTICA DE PUNTOS

### Fórmula de Cálculo:

| Operación | Fórmula | Justificación |
|-----------|---------|---------------|
| Recarga | monto ÷ 100 | Baja recompensa (menos riesgo) |
| Retiro | (monto ÷ 100) × 2 | Media recompensa |
| Transferencia | (monto ÷ 100) × 3 | Alta recompensa (activi compleja) |
| Recibida | 0 | Sin puntos (no es actividad propia) |

### Niveles Automáticos:

```java
private void actualizarNivelFidelizacion() {
    String anterior = nivelFidelizacion;
    
    // Cambiar nivel según puntos
    if (puntosAcumulados <= 500) {
        nivelFidelizacion = "Bronce";
    } else if (...) {
        // Otros niveles
    }
    
    // Notificar ascenso
    if (!anterior.equals(nivelFidelizacion)) {
        System.out.println("[ALERTA] Ascendió a: " + nivelFidelizacion);
    }
}
```

---

## 6. VALIDACIONES

### Centralización en ValidadorFinanciero

#### Ventajas:

- Lógica de validación en un solo lugar
- Reutilizable desde cualquier módulo
- Fácil de actualizar reglas
- Mensajes de error consistentes

#### Ejemplo:

```java
public static boolean validarRecarga(Usuario usuario, String codigoBilletera, double monto) {
    if (!validarUsuario(usuario)) return false;
    if (!validarMonto(monto)) return false;
    if (!usuario.tieneBilletera(codigoBilletera)) {
        println("[ERROR] Billetera no existe");
        return false;
    }
    if (billetera.getSaldo() + monto > 1000000.0) {
        println("[ERROR] Excedería límite máximo");
        return false;
    }
    return true;
}
```

---

## 7. COMPLEJIDADES TEMPORALES

### Análisis:

| Operación | Complejidad | Nodos Visitados |
|-----------|-------------|-----------------|
| registrarUsuario() | O(1) | TablaHash.insertar |
| crearBilletera() | O(1) | HashMap.put |
| recargar() | O(1) | Múltiples O(1) |
| retirar() | O(1) | Múltiples O(1) |
| transferirInterno() | O(1) | Múltiples O(1) |
| transferirExterno() | O(1) | Múltiples O(1) |
| deshacerOperacion() | O(1) | Pila.desapilar |
| obtenerHistorial() | O(n) | LinkedList.copy |
| calcularSaldo() | O(m) | m = billeteras |

**Conclusión**: Todas las operaciones son O(1) o muy eficientes.

---

## 8. SEGURIDAD Y ROBUSTEZ

### Principios Implementados:

1. **Validación exhaustiva** - Todo es validado antes de ejecutar
2. **Atomicidad** - Las operaciones son "todo o nada"
3. **Reversibilidad** - Se puede deshacer cualquier transacción
4. **Inmutabilidad parcial** - IDs no cambian, estados sí
5. **Auditoria** - Todo se registra

### Ejemplo de Atomicidad:

```java
// Si falla el aumentarSaldo, volvemos atrás
if (!billeteraDestino.aumentarSaldo(monto)) {
    billeteraOrigen.aumentarSaldo(monto);  // Devolver dinero
    return false;
}
```

---

## 9. EXTENSIBILIDAD

### Para Próximos Módulos:

#### Compañero 2 (Operaciones Programadas):
```java
Queue<Operacion> colaOperaciones;      // Cola normal
PriorityQueue<Operacion> colaPrioritaria;  // Cola con prioridad
```

#### Compañero 3 (Análisis):
```java
ArbolAVL<Integer, Usuario> usuariosPorPuntos;  // Árbol para búsqueda rango
Grafo<Usuario> transferencias;      // Grafo de relaciones
```

#### Compañero 4 (Fraude):
```java
Set<Transaccion> transaccionesSospechosas;
Map<String, PatronFraude> patrones;
```

---

## 10. PATRONES DE DISEÑO UTILIZADOS

### 1. **Validator Pattern**
- `ValidadorFinanciero` centraliza validaciones

### 2. **Factory Pattern** (Potencial)
- Crear transacciones específicas
- Crear billeteras por tipo

### 3. **Strategy Pattern** (Potencial)
- Diferentes algoritmos de cálculo de puntos
- Diferentes políticas de comisiones por nivel

### 4. **Observer Pattern** (Extensible)
- Notificaciones de cambio de nivel
- Alertas de operaciones

---

## 11. NOTAS IMPORTANTES

### ✅ Qué Funciona Bien:

- O(1) búsqueda de usuarios y billeteras
- O(1) operaciones financieras básicas
- Reversión con Pila es intuitiva
- Validaciones completas
- Sistema de puntos automático

### ⚠️ Limitaciones Actuales:

- No hay persistencia (BD)
- No hay colas de prioridad para operaciones programadas
- No hay análisis de grafos
- No hay detección de fraude

### 🚀 Mejoras Futuras:

- Añadir persistencia en base de datos
- Ordenamiento de transacciones por valor
- Análisis de patrones
- Concurrencia/Threading

---

## 12. GUÍA DE PRUEBAS

### Ejecutar Pruebas:

```bash
# 1. Compilar
javac -d . src/**/*.java

# 2. Ejecutar
java main.Main

# 3. Ver salida
```

### Estructura de Prueba en Main.java:

```
1. Registro de usuarios (3)
2. Creación de billeteras (6)
3. Recargas iniciales (6)
4. Retiros
5. Transferencias internas
6. Transferencias externas
7. Consultas de saldo
8. Historial
9. Puntos y niveles
10. Reversión
11. Validaciones (fallos esperados)
12. Información detallada
13. Historial global
```

---

## Conclusión

Este módulo proporciona una **base sólida**, **escalable** y **académicamente fundamentada** para construir el sistema completo de billeteras digitales. 

Utiliza correctamente las estructuras de datos para cada caso de uso, demostrando comprensión profunda de sus ventajas y limitaciones.

**Versión**: 1.0  
**Estado**: ✅ Completo y Funcional  
**Listo para**: Módulos de extensión
