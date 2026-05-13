# GUÍA DE COMPILACIÓN Y EJECUCIÓN

## Plataforma Fintech de Billeteras Digitales
## Compañero 1: Base del Sistema

---

## 📦 Estructura de Carpetas

```
Proyecto/
├── src/
│   ├── modelos/
│   │   ├── Usuario.java
│   │   ├── Billetera.java
│   │   └── Transaccion.java
│   ├── estructuras/
│   │   ├── TablaHash.java
│   │   ├── Pila.java
│   │   └── MiLista.java
│   ├── operaciones/
│   │   └── OperacionesFinancieras.java
│   ├── validaciones/
│   │   └── ValidadorFinanciero.java
│   └── main/
│       └── Main.java
├── README.md
├── DISEÑO.md
├── ESTRUCTURA_CLASES.md
└── COMPILACIÓN_EJECUCIÓN.md
```

---

## 🔧 REQUISITOS

- **Java**: JDK 8 o superior
- **Sistema Operativo**: Windows / Mac / Linux
- **Terminal**: CMD / Bash / PowerShell

### Verificar Java

```bash
javac -version
java -version
```

Ambos deberían mostrar versión 8 o superior.

---

## 📝 COMPILACIÓN

### Opción 1: Compilar Todo de Una Vez (RECOMENDADO)

```bash
cd src
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
```

**Explicación**:
- `cd src`: Entra a la carpeta fuente
- `javac`: Compilador de Java
- `-d ..`: Coloca los .class en la carpeta padre
- `modelos/*.java ...`: Compila todos los archivos .java

**Resultado**: Se crean archivos `.class` en la carpeta raíz del proyecto

### Opción 2: Compilar Paquete por Paquete

```bash
cd src
javac -d .. modelos/*.java
javac -d .. estructuras/*.java
javac -d .. validaciones/*.java
javac -d .. operaciones/*.java
javac -d .. main/*.java
```

**Ventaja**: Puedes ver errores por etapas

### Opción 3: Compilar en Carpeta de Trabajo

Si prefieres compilar en la carpeta raíz:

```bash
javac -d . -sourcepath src src/modelos/*.java src/estructuras/*.java src/validaciones/*.java src/operaciones/*.java src/main/*.java
```

---

## ▶️ EJECUCIÓN

### Opción 1: Desde Carpeta Raíz del Proyecto

```bash
java main.Main
```

### Opción 2: Especificando Classpath

```bash
java -cp . main.Main
```

**Nota**: Asegúrate de que los archivos `.class` están en la carpeta raíz o especifica el path correctamente.

---

## 📊 Salida Esperada

Cuando ejecutes el programa, verás algo como:

```
╔════════════════════════════════════════════════════════════════╗
║   PLATAFORMA FINTECH DE BILLETERAS DIGITALES - VERSIÓN 1.0   ║
╚════════════════════════════════════════════════════════════════╝

┌─ PRUEBA 1: Registro de Usuarios ─────────────────────────────────┐
[ÉXITO] Usuario Juan Pérez registrado exitosamente.
[ÉXITO] Usuario María García registrada exitosamente.
[ÉXITO] Usuario Carlos López registrado exitosamente.
Total de usuarios registrados: 3

┌─ PRUEBA 2: Creación de Billeteras ──────────────────────────────┐
[ÉXITO] Billetera BIL001 creada para Gastos Diarios
[ÉXITO] Billetera BIL002 creada para Ahorro Mensual
...

[ÉXITO] Recarga de $5000.00 realizada en billetera BIL001
...
```

---

## 🛠️ SOLUCIÓN DE PROBLEMAS

### Error: "javac: command not found"

**Solución**: Instalar JDK o agregar a PATH

```bash
# Windows
set PATH=%PATH%;C:\Program Files\Java\jdk1.8.0_xxx\bin

# Linux/Mac
export PATH=$PATH:/usr/local/bin/java
```

### Error: "cannot find symbol"

**Solución**: Verificar que todos los archivos `.java` están compilados

```bash
# Borrar archivos compilados e intentar de nuevo
rm -rf *.class  # Linux/Mac
del *.class     # Windows

# Compilar nuevamente
javac -d . src/**/*.java
```

### Error: "package modelos does not exist"

**Solución**: Asegúrate de compilar desde el directorio correcto

```bash
# Debe haber estructura de carpetas modelos/ bajo el classpath
ls modelos/Usuario.class
```

### Error: "NoClassDefFoundError"

**Solución**: Especificar classpath correctamente

```bash
# Si los .class están en carpeta actual
java -cp . main.Main

# Si están en subcarpeta
java -cp ./bin main.Main
```

---

## 🎯 GUÍA PASO A PASO (WINDOWS)

1. **Abre Terminal/CMD**:
   ```
   Win + R → "cmd" → Enter
   ```

2. **Navega a la carpeta del proyecto**:
   ```bash
   cd C:\Users\USER\Downloads\Universidad\Estructura de datos\Proyecto
   ```

3. **Entra a la carpeta src**:
   ```bash
   cd src
   ```

4. **Compila todos los archivos**:
   ```bash
   javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
   ```

5. **Vuelve a la carpeta raíz**:
   ```bash
   cd ..
   ```

6. **Ejecuta el programa**:
   ```bash
   java main.Main
   ```

---

## 🎯 GUÍA PASO A PASO (LINUX/MAC)

```bash
# 1. Navega a la carpeta
cd ~/Descargas/Universidad/Estructura\ de\ datos/Proyecto

# 2. Entra a src
cd src

# 3. Compila
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java

# 4. Vuelve atrás
cd ..

# 5. Ejecuta
java main.Main
```

---

## 💡 ALTERNATIVAS DE COMPILACIÓN

### Usando Script (Linux/Mac)

Crea `compile.sh`:

```bash
#!/bin/bash
cd src
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
echo "Compilación completada"
```

Luego ejecuta:

```bash
chmod +x compile.sh
./compile.sh
java main.Main
```

### Usando Script (Windows)

Crea `compile.bat`:

```batch
@echo off
cd src
javac -d .. modelos\*.java estructuras\*.java validaciones\*.java operaciones\*.java main\*.java
cd ..
echo Compilacion completada
pause
```

Luego ejecuta:

```batch
compile.bat
java main.Main
```

---

## 📦 DISTRIBUIR EL PROYECTO

### Opción 1: Con archivos .class

```
Proyecto.zip/
├── modelos/
│   ├── Usuario.class
│   ├── Billetera.class
│   └── Transaccion.class
├── estructuras/
│   ├── TablaHash.class
│   ├── Pila.class
│   └── MiLista.class
├── operaciones/
│   └── OperacionesFinancieras.class
├── validaciones/
│   └── ValidadorFinanciero.class
└── main/
    └── Main.class
```

Ejecutar:
```bash
java main.Main
```

### Opción 2: Solo archivos fuente (sin .class)

```
Proyecto.zip/
└── src/
    ├── modelos/
    ├── estructuras/
    ├── operaciones/
    ├── validaciones/
    └── main/
```

El receptor debe compilar:
```bash
cd src
javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java
cd ..
java main.Main
```

---

## ✅ VERIFICACIÓN

Para verificar que todo está compilado correctamente:

```bash
# Listar archivos compilados
ls -la modelos/*.class        # Linux/Mac
dir modelos\*.class           # Windows

# Debe mostrar:
# - Usuario.class
# - Billetera.class
# - Transaccion.class
# - TablaHash.class
# - Pila.class
# - MiLista.class
# - OperacionesFinancieras.class
# - ValidadorFinanciero.class
# - Main.class
```

---

## 🚀 EJECUCIÓN CON ARGUMENTOS (Extensión)

Aunque Main no acepta argumentos actualmente, puedes modificarlo:

```bash
java main.Main arg1 arg2 arg3
```

En `Main.java`:

```java
public static void main(String[] args) {
    if (args.length > 0) {
        System.out.println("Argumentos recibidos: " + Arrays.toString(args));
    }
    // ... resto del código
}
```

---

## 🔍 DEBUG

Si algo no funciona, prueba:

1. **Verificar compilación**:
   ```bash
   javac -verbose src/main/Main.java
   ```

2. **Ejecutar con más información**:
   ```bash
   java -verbose main.Main
   ```

3. **Verificar classpath**:
   ```bash
   echo $CLASSPATH  # Linux/Mac
   echo %CLASSPATH% # Windows
   ```

---

## 📚 RECURSOS ADICIONALES

### Documentación Oficial
- [Java Compiler (javac)](https://docs.oracle.com/javase/tutorial/getStarted/cupojava/)
- [Java Virtual Machine](https://docs.oracle.com/javase/specs/jvms/se15/html/)

### Herramientas IDE Alternativas

Si prefieres no compilar desde línea de comandos:

**IntelliJ IDEA**:
1. Abre el proyecto
2. Build → Build Project
3. Run → Run 'Main'

**Eclipse**:
1. Abre el proyecto
2. Project → Build All
3. Run → Run As → Java Application

**VS Code**:
1. Instala "Extension Pack for Java"
2. Presiona Run (▶️)

---

## 🎓 NOTAS DE APRENDIZAJE

### Concepto: Compilación

```
[Código Fuente .java] --[javac]--> [Bytecode .class] --[java]--> [Ejecución JVM]
```

### Concepto: Classpath

Es la ruta donde Java busca las clases compiladas.

```bash
java -cp /ruta/a/clases mipackage.MiClase
```

### Concepto: Paquetes

Los paquetes organizan las clases (carpetas en el filesystem).

```java
package modelos;  // Archivo en carpeta modelos/
public class Usuario { }

// Para usar:
import modelos.Usuario;
// o
modelos.Usuario usuario = new modelos.Usuario(...);
```

---

## ✨ COMANDOS RÁPIDOS

```bash
# Compilar y ejecutar (todo de una vez)
cd src && javac -d .. modelos/*.java estructuras/*.java validaciones/*.java operaciones/*.java main/*.java && cd .. && java main.Main

# Limpiar archivos compilados
find . -name "*.class" -delete  # Linux/Mac
for /r %f in (*.class) do del %f  # Windows

# Compilar solo un archivo
javac -d . src/main/Main.java

# Ver versión de Java
java -version
```

---

## 📱 PORTABILIDAD

El proyecto es completamente **portable** porque:

✅ No usa librerias externas  
✅ Solo usa Java Collections built-in  
✅ Funciona en Windows, Mac y Linux  
✅ Compatible con Java 8+  

Puedes mover la carpeta `src/` a cualquier lugar y compilar/ejecutar sin problemas.

---

**¡Listo para compilar y ejecutar!** 🚀

---

## Spring Boot API (SDD 02+)

Con la aplicación Spring Boot corriendo (`./mvnw spring-boot:run`), los siguientes endpoints están disponibles:

| URL | Descripción |
|-----|-------------|
| `http://localhost:8080/api/v1/swagger-ui/index.html` | Swagger UI interactivo |
| `http://localhost:8080/api/v1/v3/api-docs` | OpenAPI JSON (fuente de verdad del contrato) |
| `http://localhost:8080/api/v1/health` | Health check (SDD 01) |

---

## Flujo end-to-end

Los siguientes 10 pasos reproducen el flujo completo del PRD usando `curl`. Ejecutar la aplicación primero (`./mvnw spring-boot:run`) y correr los comandos en orden.

### S1 — Crear usuario USR-E2E

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"id":"USR-E2E","name":"E2E User","email":"e2e@example.com"}' | jq .
```

Respuesta esperada: HTTP 201, body con `"id": "USR-E2E"`.

### S2 — Crear billetera W-E2E-01 para USR-E2E

```bash
curl -s -X POST http://localhost:8080/api/v1/users/USR-E2E/wallets \
  -H "Content-Type: application/json" \
  -d '{"code":"W-E2E-01","name":"Main Wallet","type":"SAVINGS"}' | jq .
```

Respuesta esperada: HTTP 201, `"balance": 0`.

### S3 — Recargar W-E2E-01 con 20000

```bash
curl -s -X POST http://localhost:8080/api/v1/users/USR-E2E/wallets/W-E2E-01/recharge \
  -H "Content-Type: application/json" \
  -d '{"amount":20000.0,"description":"Initial recharge"}' | jq .
```

Respuesta esperada: HTTP 200, transaction con `"type":"RECHARGE"`. Anotar el campo `"id"` como `TX_RECHARGE_ID`.

### S4 — Retirar 1000 de W-E2E-01

```bash
curl -s -X POST http://localhost:8080/api/v1/users/USR-E2E/wallets/W-E2E-01/withdraw \
  -H "Content-Type: application/json" \
  -d '{"amount":1000.0,"description":"E2E withdraw"}' | jq .
```

Respuesta esperada: HTTP 200, transaction con `"type":"WITHDRAWAL"`. Anotar el campo `"id"` como `TX_WITHDRAW_ID`.

### S5 — Crear W-E2E-02 y transferencia interna de 500

```bash
# Crear segunda billetera
curl -s -X POST http://localhost:8080/api/v1/users/USR-E2E/wallets \
  -H "Content-Type: application/json" \
  -d '{"code":"W-E2E-02","name":"Backup Wallet","type":"SAVINGS"}' | jq .

# Transferencia interna
curl -s -X POST http://localhost:8080/api/v1/users/USR-E2E/transfers/internal \
  -H "Content-Type: application/json" \
  -d '{"sourceWalletId":"W-E2E-01","targetWalletId":"W-E2E-02","amount":500.0,"description":"Internal"}' | jq .
```

Respuesta esperada: W-E2E-01 balance = 18500, W-E2E-02 balance = 500.

### S6 — Historial de transacciones de USR-E2E

```bash
curl -s http://localhost:8080/api/v1/users/USR-E2E/transactions | jq 'length'
```

Respuesta esperada: HTTP 200, lista con al menos 3 transacciones.

### S7 — Revertir el retiro ($TX_WITHDRAW_ID)

```bash
TX_WITHDRAW_ID="<id-del-retiro-obtenido-en-S4>"
curl -s -X POST http://localhost:8080/api/v1/transactions/${TX_WITHDRAW_ID}/reverse | jq .
```

Respuesta esperada: HTTP 200, `"status":"REVERSED"`. Balance W-E2E-01 = 19500.

### S8 — Puntos y nivel de lealtad de USR-E2E

```bash
curl -s http://localhost:8080/api/v1/users/USR-E2E/points | jq .
```

Respuesta esperada: HTTP 200, `"points" >= 0`, `"loyaltyLevel"` no nulo.

### S9 — Crear USR-E2E-B y transferencia externa de 15000

```bash
# Crear usuario B
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"id":"USR-E2E-B","name":"User B","email":"b@example.com"}' | jq .

# Crear billetera de B
curl -s -X POST http://localhost:8080/api/v1/users/USR-E2E-B/wallets \
  -H "Content-Type: application/json" \
  -d '{"code":"W-E2E-B-01","name":"User B Wallet","type":"SAVINGS"}' | jq .

# Transferencia externa (dispara FraudDetector: 15000 > 10000)
curl -s -X POST http://localhost:8080/api/v1/transfers/external \
  -H "Content-Type: application/json" \
  -d '{"sourceUserId":"USR-E2E","sourceWalletId":"W-E2E-01","targetUserId":"USR-E2E-B","targetWalletId":"W-E2E-B-01","amount":15000.0,"description":"External transfer"}' | jq .
```

Respuesta esperada: HTTP 200, body con `outgoingTransaction` e `incomingTransaction`.

### S10 — Analytics, fraude y notificaciones

```bash
# Resumen analítico
curl -s http://localhost:8080/api/v1/analytics/summary | jq .

# Eventos de fraude (debe incluir LARGE_TRANSACTION con severity HIGH)
curl -s http://localhost:8080/api/v1/fraud/events | jq .

# Notificaciones de USR-E2E
curl -s http://localhost:8080/api/v1/notifications/users/USR-E2E | jq .
```

Respuestas esperadas:
- `/analytics/summary`: `totalUsers >= 2`, `totalTransactions >= 3`, `fraudEventCount >= 1`.
- `/fraud/events`: lista no vacía, al menos un evento con `"severity":"HIGH"` y `"type":"LARGE_TRANSACTION"`.
- `/notifications/users/USR-E2E`: HTTP 200, array válido (puede estar vacío).
