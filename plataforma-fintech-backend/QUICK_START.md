# 🚀 QUICK START - Guía Rápida

## Plataforma Fintech de Billeteras Digitales
### Compañero 1: Base del Sistema

---

## ⚡ 30 SEGUNDOS PARA EJECUTAR

### Windows (CMD)

```batch
cd src
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
java main.Main
```

### Mac / Linux (Terminal)

```bash
cd src
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
java main.Main
```

---

## ✅ UNA SOLA LÍNEA (copy-paste)

### Windows
```batch
cd src & javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java & cd .. & java main.Main
```

### Mac / Linux
```bash
cd src && javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java && cd .. && java main.Main
```

---

## 📁 ESTRUCTURA DEL PROYECTO

```
Proyecto/
├── src/
│   ├── modelos/              (Usuario, Billetera, Transaccion)
│   ├── estructuras/          (TablaHash, Pila, MiLista)
│   ├── operaciones/          (OperacionesFinancieras)
│   ├── validaciones/         (ValidadorFinanciero)
│   └── main/                 (Main - pruebas)
│
└── Documentación/
    ├── README.md             ← Lee primero
    ├── RESUMEN.md            ← Visión general
    ├── DISEÑO.md             ← Arquitectura
    ├── ESTRUCTURA_CLASES.md  ← Referencia detallada
    ├── COMPILACIÓN_EJECUCIÓN.md  ← Cómo compilar
    ├── ÍNDICE.md             ← Índice completo
    └── QUICK_START.md        ← Este archivo
```

---

## 🎯 PRINCIPALES CARACTERÍSTICAS

✅ **Operaciones Financieras**
- Recargar dinero
- Retirar dinero
- Transferencias (internas y externas)
- Deshacer operaciones

✅ **Estructuras de Datos**
- TablaHash (O(1) búsqueda)
- Pila (LIFO para deshacer)
- Lista Enlazada (historial flexible)
- Recursión (múltiples métodos)

✅ **Sistema Automático**
- Puntos de fidelización
- Niveles (Bronce → Oro → Platino)
- Validaciones completas
- Historial de todas las transacciones

---

## 📊 SALIDA ESPERADA

```
╔════════════════════════════════════════════════════════════════╗
║   PLATAFORMA FINTECH DE BILLETERAS DIGITALES - VERSIÓN 1.0   ║
╚════════════════════════════════════════════════════════════════╝

┌─ PRUEBA 1: Registro de Usuarios ─────────────────────────────────┐
[ÉXITO] Usuario Juan Pérez registrado exitosamente.
[ÉXITO] Usuario María García registrada exitosamente.
Total de usuarios registrados: 3

[ÉXITO] Recarga de $5000.00 realizada en billetera BIL001
...

╔════════════════════════════════════════════════════════════════╗
║                     PRUEBAS COMPLETADAS                      ║
╚════════════════════════════════════════════════════════════════╝
```

---

## 📚 DOCUMENTACIÓN RÁPIDA

| Archivo | Contenido | Leer si... |
|---------|-----------|-----------|
| README.md | Descripción general y arquitectura | Quieres entender la visión |
| RESUMEN.md | Alto nivel, características, stats | Necesitas un resumen ejecutivo |
| DISEÑO.md | Decisiones arquitectónicas detalladas | Quieres saber por qué se hizo así |
| ESTRUCTURA_CLASES.md | Referencia completa de clases | Necesitas detalles de implementación |
| COMPILACIÓN_EJECUCIÓN.md | Guía de compilación paso a paso | Tienes problemas compilando |

---

## 🔧 OPCIONES DE COMPILACIÓN

### Opción 1: Compilar Todo (RECOMENDADO)
```bash
cd src
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
```

### Opción 2: Compilar con Verbose (Ver más detalles)
```bash
cd src
javac -verbose -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
```

### Opción 3: Compilar Específico (para debug)
```bash
cd src
javac -d .. modelos/Usuario.java
javac -d .. estructuras/TablaHash.java
# ... etc
cd ..
```

---

## ❌ PROBLEMAS COMUNES

### Error: "javac: command not found"
→ Java no está instalado o no está en PATH

**Solución**: 
```bash
java -version  # Verifica que esté instalado
# Si no aparece, instala JDK desde java.oracle.com
```

### Error: "cannot find symbol"
→ No todos los archivos se compilaron

**Solución**:
```bash
# Borrar compilados previos y recompilar todo
rm *.class 2>/dev/null  # Linux/Mac
del *.class             # Windows
# Luego compilar nuevamente
```

### Error: "package modelos does not exist"
→ No estás en el directorio correcto

**Solución**:
```bash
cd src
# Luego compilar
javac -d .. modelos/*.java ...
```

### Error: "NoClassDefFoundError"
→ Los .class no se encuentran

**Solución**:
```bash
# Asegúrate de estar en la carpeta raíz donde están modelos/, estructuras/, etc.
java -cp . main.Main
```

---

## 🎮 EJEMPLOS DE USO

### Crear usuario y billetera
```java
operaciones.registrarUsuario("USR001", "Juan", "juan@email.com");
operaciones.crearBilletera("USR001", "BIL001", "Mi Billetera", "Gastos diarios");
```

### Recargar dinero
```java
operaciones.recargar("USR001", "BIL001", 5000.00);
// Genera 50 puntos (5000 / 100 = 50)
```

### Transferir dinero
```java
operaciones.transferirExterno("USR001", "BIL001", "USR002", "BIL002", 500.00);
// Genera 15 puntos (500 / 100 * 3 = 15)
```

### Deshacer operación
```java
operaciones.deshacerOperacion();
// Revierte la última transacción (LIFO)
```

### Consultar
```java
double saldo = operaciones.obtenerSaldo("USR001", "BIL001");
Usuario usuario = operaciones.obtenerUsuario("USR001");
```

---

## 💡 TIPS & TRUCOS

### Limpiar pantalla después de compilar
```bash
cd src
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
clear  # Linux/Mac
cls    # Windows
java main.Main
```

### Guardar salida en archivo
```bash
java main.Main > salida.txt
cat salida.txt  # Linux/Mac
type salida.txt # Windows
```

### Ver solo errores de compilación
```bash
javac -d .. modelos/*.java 2>&1 | grep error
```

### Compilar con optimizaciones
```bash
cd src
javac -O -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
```

---

## 🎓 PRÓXIMOS PASOS

1. **Ejecutar el programa**
   ```bash
   java main.Main
   ```

2. **Leer la documentación**
   - Empezar con README.md
   - Luego RESUMEN.md
   - Luego DISEÑO.md

3. **Explorar el código**
   - Abrir Main.java para ver las pruebas
   - Ver OperacionesFinancieras.java para la lógica
   - Ver TablaHash.java, Pila.java, MiLista.java para estructuras

4. **Modificar y experimentar**
   - Cambiar valores en Main.java
   - Agregar nuevas operaciones
   - Crear nuevas pruebas

5. **Extender el proyecto**
   - Agregar Colas de Prioridad (Compañero 2)
   - Agregar Árboles y Grafos (Compañero 3)
   - Agregar Detección de Fraude (Compañero 4)

---

## 📋 REQUISITOS MÍNIMOS

- **Java**: JDK 8 o superior
- **Espacio**: ~5 MB
- **RAM**: 256 MB
- **SO**: Windows, Mac, o Linux

---

## ✨ CARACTERÍSTICAS ACADÉMICAS

✅ Estructuras de datos personalizadas desde cero  
✅ Complejidades O(1) en operaciones críticas  
✅ Recursión en múltiples métodos  
✅ Validaciones exhaustivas  
✅ Codigo limpio y bien documentado  
✅ Extensibilidad para otros módulos  
✅ 13 pruebas completas  
✅ 6 archivos de documentación  

---

## 🆘 AYUDA RÁPIDA

| Necesito... | Archivo |
|------------|---------|
| Entender qué hace el proyecto | README.md |
| Ver características principales | RESUMEN.md |
| Saber cómo está diseñado | DISEÑO.md |
| Detalle de cada clase | ESTRUCTURA_CLASES.md |
| Cómo compilar/ejecutar | COMPILACIÓN_EJECUCIÓN.md |
| Índice del proyecto | ÍNDICE.md |
| Inicio rápido | QUICK_START.md (este) |

---

## 🚀 COMANDO FINAL (COPY-PASTE READY)

### Windows
```batch
cd src & javac -d .. modelos\*.java estructuras\*.java validaciones\*.java operaciones\*.java main\*.java & cd .. & java main.Main
```

### Mac/Linux
```bash
cd src && javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java && cd .. && java main.Main
```

---

**¡LISTO! Ahora solo necesitas compilar y ejecutar** 🎉

Para más información, consulta la documentación incluida.

---

**Versión**: 1.0  
**Última actualización**: 2026-04-28  
**Estado**: ✅ Listo para usar
