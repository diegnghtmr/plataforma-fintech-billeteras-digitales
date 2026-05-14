

## Proyecto Final
Plataforma Fintech de Gestión de Billeteras Digitales y Analítica de Transacciones
- Descripción general
Se desea desarrollar una aplicación que simule una plataforma fintech de billeteras
digitales, en la cual los usuarios puedan administrar su dinero, mover fondos entre
billeteras, programar operaciones, recibir alertas, consultar su historial y obtener beneficios
según su nivel de uso dentro de la plataforma.
El sistema debe representar un entorno moderno de pagos digitales, donde cada usuario
puede contar con varias billeteras con distintos propósitos, por ejemplo: gastos diarios,
ahorro, transporte, compras o suscripciones. Además, la plataforma debe incluir
mecanismos de análisis de transacciones, sistema de recompensas, procesamiento eficiente
de operaciones programadas y uso explícito de diversas estructuras de datos.
El propósito del proyecto no es únicamente construir una aplicación funcional, sino
demostrar cómo diferentes estructuras de datos permiten resolver de forma eficiente
problemas reales de organización, búsqueda, priorización, reversión, clasificación y análisis
de información.
- Objetivo del proyecto
Desarrollar un sistema de billeteras digitales que permita gestionar usuarios, billeteras,
movimientos financieros y beneficios, aplicando correctamente estructuras de datos como
listas, pilas, colas, árboles, tablas hash y grafos para resolver necesidades reales del dominio.
- Contexto del problema
En la actualidad, las aplicaciones financieras digitales permiten a los usuarios realizar pagos,
transferencias, separar dinero en bolsillos, programar movimientos y visualizar sus hábitos
de consumo. Estas plataformas requieren organizar grandes volúmenes de información,
responder rápidamente a consultas, manejar operaciones pendientes, revertir movimientos
y detectar patrones de uso.
A partir de este contexto, se propone construir una plataforma académica que simule ese
comportamiento, permitiendo que el estudiante modele un sistema realista y, al mismo
tiempo, justifique técnicamente el uso de cada estructura de datos implementada.
- Funcionalidades del sistema

4.1 Gestión de usuarios y billeteras
El sistema debe permitir registrar usuarios con su información básica.
Cada usuario podrá tener una o varias billeteras digitales, cada una con un nombre, tipo,
saldo disponible y estado.
Ejemplos de tipos de billetera:
## ● Ahorro
● Gastos diarios
## ● Compras
## ● Transporte
## ● Inversión
4.2 Operaciones financieras básicas
Cada usuario podrá realizar operaciones como:
● Recargar saldo en una billetera.
● Retirar saldo de una billetera.
● Transferir dinero entre billeteras del mismo usuario.
● Transferir dinero a billeteras de otros usuarios.
● Consultar saldo actual.
● Consultar historial de movimientos.
Cada operación debe quedar registrada con datos como:
● identificador
● fecha
● tipo de transacción
● valor
● billetera origen
● billetera destino
● estado
● puntos generados
4.3 Operaciones programadas
El sistema permitirá programar movimientos automáticos para ejecutarse en fechas futuras,
por ejemplo:
● transferencias periódicas
● recargas automáticas
● pagos recurrentes

● ahorro automático semanal
Estas operaciones deben procesarse respetando la prioridad según la fecha y hora de
ejecución.
4.4 Sistema de recompensas
Cada transacción realizada por un usuario generará puntos de fidelización.
La cantidad de puntos dependerá del tipo de movimiento y del valor de la operación.
Ejemplo de política:
● Recarga: 1 punto por cada 100 unidades
● Retiro: 2 puntos por cada 100 unidades
● Transferencia: 3 puntos por cada 100 unidades
● Pago programado ejecutado exitosamente: bono adicional
Los puntos podrán ser usados para obtener beneficios dentro de la plataforma.
4.5 Niveles de usuario
Según la cantidad de puntos acumulados, cada usuario se clasificará en un nivel:
● Bronce: 0 a 500 puntos
● Plata: 501 a 1000 puntos
● Oro: 1001 a 5000 puntos
● Platino: más de 5000 puntos
Cada nivel puede otorgar ventajas, por ejemplo:
● reducción en comisiones
● prioridad en ciertas operaciones
● bonos de puntos
● mayor límite de transacciones
4.6 Reversión de operaciones
El sistema debe permitir revertir ciertas operaciones válidas, por ejemplo:
● una transferencia realizada por error
● una recarga mal aplicada
● una operación duplicada
Toda reversión debe actualizar el saldo, el historial y los puntos acumulados.


4.7 Alertas y notificaciones
La plataforma debe generar alertas automáticas, tales como:
● saldo bajo
● transacción programada próxima a ejecutarse
● operación rechazada
● ascenso de nivel
● canje de beneficio realizado
El sistema debe conservar un historial de notificaciones recientes por usuario.
4.8 Analítica de movimientos
El sistema debe generar consultas e informes que permitan identificar:
● billeteras con mayor uso
● usuarios con más transferencias realizadas
● categorías de billetera más activas
● patrones de movimiento entre usuarios
● frecuencia de transacciones por tipo
● monto total movilizado en un rango de tiempo
4.9 Detección de comportamiento financiero inusual
La plataforma debe incorporar un módulo capaz de identificar patrones sospechosos o
atípicos en las transacciones realizadas por los usuarios. El objetivo es detectar situaciones
que se desvíen del comportamiento normal de uso de una billetera digital y generar alertas
preventivas.
El sistema debe analizar aspectos como:
● múltiples transferencias consecutivas en un tiempo muy corto
● retiros o envíos por valores inusualmente altos respecto al promedio del usuario
● movimientos repetitivos hacia un mismo destino en intervalos reducidos
● uso simultáneo de varias billeteras del mismo usuario para fragmentar montos
● cambios bruscos en la frecuencia de transacciones
● actividad en horarios no habituales para el comportamiento histórico del usuario
Cuando el sistema detecte un patrón inusual, deberá:
● generar una alerta de seguridad
● marcar la transacción con un nivel de riesgo
● registrar el evento en un historial especial de auditoría

● permitir consulta posterior de los eventos detectados

- Estructuras de datos que deben aplicarse
El proyecto debe justificar el uso de las siguientes estructuras:
## 5.1 Listas
Se utilizarán para almacenar:
● historial de transacciones por usuario
● historial de transacciones por billetera
● beneficios canjeados
● operaciones realizadas en un período
## 5.2 Pilas
Se utilizarán para:
● manejar operaciones reversibles
● conservar el historial de deshacer
● restaurar estados anteriores de ciertas transacciones
5.3 Colas y colas de prioridad
Se utilizarán para:
● administrar transacciones programadas
● procesar pagos automáticos según la fecha
● atender notificaciones pendientes de despacho
## 5.4 Árboles
Se utilizarán para:
● organizar usuarios según puntos acumulados
● clasificar niveles de fidelización
● realizar búsquedas eficientes por rango de puntos
● generar reportes ordenados
5.5 Tablas hash
Se utilizarán para:

● acceder rápidamente a usuarios por identificación
● localizar billeteras por código
● consultar saldos, puntos o configuraciones de forma rápida
## 5.6 Grafos
Se utilizarán para:
● representar transferencias entre usuarios
● analizar relaciones entre billeteras
● detectar rutas frecuentes de movimiento de dinero
● estudiar patrones de interacción financiera

- Requisitos funcionales
El sistema debe permitir:
- Registrar, modificar, buscar y eliminar usuarios.
- Crear y administrar múltiples billeteras por usuario.
- Realizar recargas, retiros y transferencias.
- Consultar historial de movimientos.
- Programar operaciones futuras.
- Procesar automáticamente operaciones programadas.
- Acumular y descontar puntos.
- Asignar nivel al usuario según sus puntos.
- Revertir operaciones permitidas.
- Generar alertas automáticas.
- Emitir reportes e indicadores de uso.
- Analizar relaciones de transacción entre usuarios.

- Requisitos no funcionales
El proyecto debe cumplir con lo siguiente:
● El código debe estar organizado en clases y módulos con responsabilidades claras.
● Cada estructura de datos implementada debe tener una justificación técnica.
● Las operaciones principales deben procurar eficiencia en tiempo de ejecución.
● El sistema debe manejar correctamente casos inválidos.
● Debe incluir validaciones de saldo, existencia de usuarios y consistencia de datos.
● La interfaz puede ser de consola o gráfica, según el alcance definido por el docente.

● El proyecto debe estar documentado.
- Requisitos adicionales
Incluir las siguientes funcionalidades:
● recalcular puntos si una transacción es revertida
● detectar el usuario con mayor actividad en un período
● encontrar ciclos o rutas frecuentes en el grafo de transferencias
● obtener las transacciones de mayor valor usando estructuras ordenadas
● simular ejecución automática de transacciones programadas
● comparar el rendimiento de distintas estructuras en operaciones similares
- Entregables esperados
El proyecto debe incluir:
● código fuente completo
● diagrama de clases
● descripción del problema
● explicación de las estructuras de datos utilizadas
● justificación de por qué se eligió cada estructura
● casos de prueba o ejemplos de ejecución
● informe final técnico
