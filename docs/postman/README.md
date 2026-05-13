# Colección Postman — Plataforma Fintech

Archivo: `Plataforma-Fintech.postman_collection.json` (Postman Collection v2.1).

## Importar

1. Abrí Postman → **Import** → arrastrá el JSON o pegá su contenido.
2. La colección aparece como **"Plataforma Fintech — Billeteras Digitales"** con 10 carpetas.

## Variables (definidas en la colección)

| Variable | Valor inicial | Para qué |
|---|---|---|
| `baseUrl` | `http://localhost:8080/api/v1` | Cambialo si el backend corre en otro host/puerto |
| `userIdA` | `USR001` | Usuario principal de la demo |
| `userIdB` | `USR002` | Segundo usuario (para transferencias externas) |
| `walletA1` | `WALLET-001` | Primera billetera de A |
| `walletA2` | `WALLET-002` | Segunda billetera de A |
| `walletB1` | `WALLET-B-01` | Billetera de B |
| `rechargeTxId` | *(vacío)* | Se autocompleta tras "POST recharge" |
| `withdrawTxId` | *(vacío)* | Se autocompleta tras "POST withdraw" |
| `internalTxId` | *(vacío)* | Se autocompleta tras "POST internal transfer" |
| `externalSentTxId` | *(vacío)* | Se autocompleta tras "POST external transfer" |
| `scheduledOpId` | *(vacío)* | Se autocompleta tras "POST scheduled-operations" |
| `notificationId` | *(vacío)* | Se autocompleta tras "GET notifications" |

Los IDs se capturan automáticamente vía test scripts en cada request relevante, así podés correr la cadena entera sin pegar valores a mano.

## Orden recomendado para la demo end-to-end

1. **00 — Health** → `GET /health` (sanity check).
2. **01 — Usuarios** → POST USR001, POST USR002, GET, PUT.
3. **02 — Billeteras** → POST WALLET-001, POST WALLET-002, POST WALLET-B-01, GET.
4. **03 — Operaciones** → recharge, withdraw, internal transfer, external transfer (15000 dispara FraudEvent LARGE_TRANSACTION).
5. **04 — Historial** → GET (con y sin filtros), POST reverse del withdraw.
6. **05 — Puntos** → GET puntos del usuario, GET ranking.
7. **06 — Programadas** → POST (con `scheduledAt` en el pasado), POST `/run` (ejecuta + bono +5 pts), GET, cancel.
8. **07 — Notificaciones** → GET (capta `notificationId`), POST mark read.
9. **08 — Analytics** → 9 endpoints (summary, top-users, top-wallets, top-transactions, top-wallet-categories, movement-by-type, frequent-routes, cycles, total-moved).
10. **09 — Fraude** → GET events (con/sin filtros).

## Gatillar la regla HIGH_VELOCITY

En **03 — Operaciones**, ejecutá la request **"POST recharge x4 rápido"** cuatro veces seguidas en menos de un minuto. A la cuarta, `GET /fraud/events` devuelve un evento `HIGH_VELOCITY` severity HIGH.

## Tip: Collection Runner

Postman → tres puntos junto a la colección → **Run collection** → seleccioná las carpetas en orden y dale "Run". Corre todo el flujo encadenado en ~5 segundos, con asserts de status y captura de IDs incluidos.

## Si cambia el base URL

Override `baseUrl` desde un Environment (ej. para staging/cloud). Postman prioriza Environment sobre Collection variables.
