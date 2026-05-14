# Entregables — Proyecto Final Estructuras de Datos 2026-1 Noche

Inventario de los 7 entregables requeridos por el PDF §9.

| # | Entregable PDF | Archivo / Carpeta | Estado |
|---|----------------|-------------------|--------|
| 1 | Código fuente completo | `plataforma-fintech-backend/src/`, `plataforma-fintech-frontend/src/` | ✅ |
| 2 | Diagrama de clases | `docs/DIAGRAMA_CLASES.md` (4 diagramas Mermaid) | ✅ |
| 3 | Descripción del problema | `docs/INFORME_TECNICO.md` §1, `plataforma-fintech-backend/PRD_BACKEND_SPRING_BOOT.md`, `plataforma-fintech-frontend/PRD_FRONTEND_REACT.md` | ✅ |
| 4 | Explicación de estructuras de datos | `docs/INFORME_TECNICO.md` §3 (7 estructuras con Big-O) + **`docs/BENCHMARK_REPORT.md`** (comparación empírica TablaHash vs HashMap, MiLista vs ArrayList, ArbolBST vs Collections.sort, ColaPrioridad vs PriorityQueue — 24 mediciones con metodología warmup+mediana) | ✅ |
| 5 | Justificación por estructura | `docs/INFORME_TECNICO.md` §3 (subsección "Justificación" por cada estructura) + `BENCHMARK_REPORT.md` §interpretación por par | ✅ |
| 6 | Casos de prueba o ejemplos de ejecución | Tests automatizados (**479 BE + 309 FE**), `docs/INFORME_TECNICO.md` §5, `plataforma-fintech-backend/COMPILACIÓN_EJECUCIÓN.md` (curls 10 pasos) | ✅ |
| 7 | Informe final técnico | `docs/INFORME_TECNICO.md` | ✅ |

---

## Verificación independiente

```bash
cd plataforma-fintech-backend && ./mvnw -q test    # → 479/479 GREEN
cd plataforma-fintech-frontend && ./node_modules/.bin/vitest run  # → 309/309 GREEN
cd plataforma-fintech-frontend && ./node_modules/.bin/tsc --noEmit  # → exit 0
# Run benchmarks on-demand:
cd plataforma-fintech-backend && ./mvnw test -Dsurefire.excludedGroups= -Dgroups=benchmark
```

**Fecha de cierre:** 2026-05-14
