# Plataforma Fintech Billeteras Digitales

Monorepo con backend Spring Boot 3 + Java 21 (hexagonal, in-memory) y frontend React 19 + Vite + Tailwind 4.

```
plataforma-fintech-backend/   Spring Boot, hexagonal, ArchUnit, in-memory repos
plataforma-fintech-frontend/  React 19, Vite, TanStack Query, Zustand, Vitest
docs/openapi.yaml             Contrato OpenAPI (fuente de tipos del frontend)
```

## Requisitos

- Java 21
- Node 20.19+ o 22.12+
- pnpm 11

## Arranque

### Backend

```bash
cd plataforma-fintech-backend
./mvnw spring-boot:run
```

Servidor en `http://localhost:8080/api/v1`. Swagger UI en `/swagger-ui.html`.

### Frontend

```bash
cd plataforma-fintech-frontend
pnpm install
pnpm dev
```

App en `http://localhost:5173`. Regenerar tipos del OpenAPI:

```bash
pnpm openapi:types
```

## Tests

```bash
# Backend
cd plataforma-fintech-backend && ./mvnw test

# Frontend
cd plataforma-fintech-frontend && pnpm test
```

## Asistente IA (OpenRouter)

El backend incluye un asistente conversacional opcional sobre la analítica del sistema. Por defecto está desactivado (adapter NoOp); activando OpenRouter como provider se conecta a un modelo gratuito.

### Variables de entorno

Plantilla committeable: `plataforma-fintech-backend/env.example`. Para habilitarlo localmente, crea `plataforma-fintech-backend/.env` (gitignored) con tu key real:

| Variable | Default | Notas |
| --- | --- | --- |
| `APP_AI_PROVIDER` | `none` | `openrouter` activa el adapter real. |
| `OPENROUTER_API_KEY` | — | Key de https://openrouter.ai/keys |
| `OPENROUTER_BASE_URL` | `https://openrouter.ai/api/v1` | |
| `OPENROUTER_MODEL` | `openrouter/free` | Otros free: `openrouter/auto`, `openai/gpt-oss-120b:free`, `z-ai/glm-4.5-air:free`. |
| `OPENROUTER_HTTP_REFERER` | `http://localhost:5173` | Header recomendado por OpenRouter. |
| `OPENROUTER_APP_TITLE` | `Plataforma Fintech Billeteras Digitales` | |
| `APP_AI_MAX_USER_MESSAGE_LENGTH` | `1000` | Tope de caracteres del mensaje del usuario. |
| `APP_AI_REQUEST_TIMEOUT_SECONDS` | `30` | |
| `APP_AI_CACHE_TTL_MINUTES` | `10` | TTL del caché en memoria. |

### Cargar el `.env` al arrancar

Spring Boot no lee `.env` automáticamente.

```bash
# Bash / Zsh
cd plataforma-fintech-backend
set -a; source .env; set +a
./mvnw spring-boot:run
```

```fish
# Fish
cd plataforma-fintech-backend
for line in (grep -v '^\s*#' .env | grep -v '^\s*$')
    set -x (string split -m1 = $line)
end
./mvnw spring-boot:run
```

### Endpoints

| Método | Path | Descripción |
| --- | --- | --- |
| POST | `/api/v1/ai/chat` | Chat con contexto analítico (USER o ADMIN). |
| GET | `/api/v1/ai/fraud-events/{fraudEventId}/explain` | Explicación natural de un evento de fraude. |
| POST | `/api/v1/ai/action-draft` | Borrador de acción operativa (siempre `requiresConfirmation=true`). |

Si `APP_AI_PROVIDER=none` o el provider upstream falla, los tres endpoints responden `503` con `code=AI_UNAVAILABLE`. El frontend (`/ai`) muestra el fallback correspondiente.

### Reglas que cumple

- La IA nunca opera sobre transacciones crudas; consume snapshots agregados producidos por los casos de uso de analítica.
- La IA nunca ejecuta operaciones financieras: el endpoint `action-draft` devuelve borradores y la confirmación queda del lado del usuario.
- Validación de intent contra allowlist; intents desconocidos se rechazan con `502 AI_INVALID_INTENT`.
- Mensaje del usuario tope `1000` caracteres por default.
- El prompt body nunca se loguea; el audit log estructurado registra `conversationId`, `role`, `intent`, `model`, `latencyMs` y `success`.
- Dominio y aplicación libres de imports de Spring; estructuras de datos propias en lugar de `HashMap`/`LinkedList`.

### Seguridad

- `.env` está en `.gitignore`. Nunca commitear claves reales.
- Rotar la `OPENROUTER_API_KEY` si quedó expuesta en logs, chats o histórico.
- El `actorRole` viaja en el body de la request (limitación conocida: no hay Spring Security todavía).
