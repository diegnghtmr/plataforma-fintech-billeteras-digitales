# Entregables — Proyecto Final Estructuras de Datos 2026-1 Noche

Inventario de los 7 entregables requeridos por el PDF §9.

| # | Entregable PDF | Archivo / Carpeta | Estado |
|---|----------------|-------------------|--------|
| 1 | Código fuente completo | `plataforma-fintech-backend/src/`, `plataforma-fintech-frontend/src/` | ✅ |
| 2 | Diagrama de clases | `docs/DIAGRAMA_CLASES.md` (4 diagramas Mermaid) | ✅ |
| 3 | Descripción del problema | `docs/INFORME_TECNICO.md` §1, `plataforma-fintech-backend/PRD_BACKEND_SPRING_BOOT.md`, `plataforma-fintech-frontend/PRD_FRONTEND_REACT.md` | ✅ |
| 4 | Explicación de estructuras de datos | `docs/INFORME_TECNICO.md` §3 (7 estructuras con Big-O) | ✅ |
| 5 | Justificación por estructura | `docs/INFORME_TECNICO.md` §3 (subsección "Justificación" por cada estructura) | ✅ |
| 6 | Casos de prueba o ejemplos de ejecución | Tests automatizados (447 BE + 206 FE), `docs/INFORME_TECNICO.md` §5, `plataforma-fintech-backend/COMPILACIÓN_EJECUCIÓN.md` (curls 10 pasos) | ✅ |
| 7 | Informe final técnico | `docs/INFORME_TECNICO.md` | ✅ |

---

## Verificación independiente

```bash
cd plataforma-fintech-backend && ./mvnw -q test    # → 447/447 GREEN
cd plataforma-fintech-frontend && npm test -- --run  # → 206/206 GREEN
npx tsc --noEmit -p tsconfig.app.json                # → exit 0
npm run build                                         # → exit 0
```

**Fecha de cierre:** 2026-05-13
