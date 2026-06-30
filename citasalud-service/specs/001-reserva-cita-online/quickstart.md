# Quickstart & Validation Guide: US-01 · Reserva de Cita en Línea 24/7

**Feature**: `specs/001-reserva-cita-online`
**Date**: 2026-06-27

---

## Prerequisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java | 26 |
| Gradle (wrapper) | incluido (`./gradlew`) |
| curl / httpie | cualquiera |

No se requiere base de datos externa: H2 se levanta embebido.

---

## 1. Construir el proyecto

```bash
# Desde la raíz del proyecto
./gradlew clean build
```

Esto ejecuta:
1. `openapi-generator` → genera interfaces y DTOs en `build/generated/`
2. Compilación de todas las capas (domain, application, infrastructure, interfaces)
3. Suite de tests completa (unit + integration + functional)
4. JaCoCo coverage report

Verificar que el build termina en `BUILD SUCCESSFUL`.

---

## 2. Ejecutar solo los tests

```bash
# Todos los tests
./gradlew test

# Solo unit tests (dominio + aplicación)
./gradlew test --tests "org.ups.citasalud.domain.*"

# Solo integration tests
./gradlew test --tests "org.ups.citasalud.infrastructure.*IT"

# Solo functional/acceptance tests (Cucumber)
./gradlew test --tests "org.ups.citasalud.interfaces.rest.*FT"
```

---

## 3. Verificar cobertura JaCoCo

```bash
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

Reporte HTML: `build/reports/jacoco/test/html/index.html`

**Gate de cobertura** (el build falla si no se cumple):
- Por clase (domain + application): > 80% líneas
- Global: ≥ 80% líneas

---

## 4. Levantar el servidor localmente

```bash
./gradlew bootRun
```

El servidor arranca en `http://localhost:8080`.

---

## 5. Validar escenarios de aceptación

### Escenario US1-1: Reservar cita exitosamente

**Prerequisito**: La base de datos H2 se inicializa con datos de prueba (médicos y franjas) vía `src/main/resources/data.sql`.

```bash
# Paso 1 — Consultar disponibilidad
curl -s "http://localhost:8080/v1/disponibilidad?medicoId=3fa85f64-5717-4562-b3fc-2c963f66afa6&fechaDesde=2026-07-01&fechaHasta=2026-07-07" \
  | jq '.franjas[0]'

# Resultado esperado: objeto con id, fecha, horaInicio, horaFin

# Paso 2 — Reservar la primera franja disponible (reemplazar <franjaId> con el id obtenido)
curl -s -X POST "http://localhost:8080/v1/citas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token-paciente>" \
  -d '{"franjaHorariaId": "<franjaId>"}' \
  | jq '.'

# Resultado esperado: HTTP 201 con cita.estado = "CONFIRMADA"
```

**Verificaciones**:
- `estado` en la respuesta es `"CONFIRMADA"` ✓
- La franja ya no aparece en `GET /v1/disponibilidad` para el mismo rango ✓
- El log de la aplicación muestra intento de envío WhatsApp ✓

---

### Escenario US1-2: Cita aparece en listado del paciente

```bash
curl -s "http://localhost:8080/v1/citas" \
  -H "Authorization: Bearer <token-paciente>" \
  | jq '.citas[] | select(.estado == "CONFIRMADA")'

# Resultado esperado: la cita reservada en el paso anterior aparece en el listado
```

---

### Escenario US2-1: Intentar reservar franja ocupada

```bash
# Usar el mismo <franjaId> ya reservado en el escenario anterior
curl -s -X POST "http://localhost:8080/v1/citas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token-otro-paciente>" \
  -d '{"franjaHorariaId": "<franjaId>"}' \
  -w "\n\nHTTP Status: %{http_code}"

# Resultado esperado:
# HTTP 409
# Body: { "codigo": "FRANJA_NO_DISPONIBLE", "mensaje": "...", "alternativas": [...] }
```

**Verificaciones**:
- HTTP status es `409` ✓
- `alternativas` contiene al menos una franja disponible ✓
- La franja original sigue con estado `OCUPADA` (no se creó duplicado) ✓

---

### Escenario US2-2: Reserva concurrente (condición de carrera)

```bash
# Ejecutar dos reservas simultáneas sobre la misma franja disponible
curl -s -X POST "http://localhost:8080/v1/citas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token-paciente-A>" \
  -d '{"franjaHorariaId": "<franjaId-libre>"}' &

curl -s -X POST "http://localhost:8080/v1/citas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token-paciente-B>" \
  -d '{"franjaHorariaId": "<franjaId-libre>"}' &

wait

# Resultado esperado:
# - Una respuesta HTTP 201 (reserva exitosa)
# - Una respuesta HTTP 409 (franja no disponible)
# - En base de datos: exactamente una cita con estado CONFIRMADA para esa franja
```

---

## 6. Verificar escenario de fallo de notificación WhatsApp

En entorno de desarrollo, el adaptador WhatsApp es un stub configurado para fallar en el primer intento.

1. Revisar el log: buscar `[WHATSAPP] Fallo en intento 1 — reintentando en 2s`
2. Verificar que la cita se creó con estado `CONFIRMADA` a pesar del fallo
3. Tras el reintento: buscar `[WHATSAPP] Notificación enviada exitosamente` en el log
4. En base de datos: `notificacion.estado = 'ENVIADA'`

---

## 7. Verificar expiración de franja temporal (FR-008)

```bash
# 1. Seleccionar una franja (PUT /v1/disponibilidad/{franjaId}/reservar-temporal — interno)
# 2. Esperar 5+ minutos (o reducir la expiración a 30s en application.yaml para tests)
# 3. Verificar que la franja volvió a estado DISPONIBLE

curl -s "http://localhost:8080/v1/disponibilidad?medicoId=...&fechaDesde=...&fechaHasta=..." \
  | jq '.franjas[] | select(.id == "<franjaId-expirado>")'

# Resultado esperado: la franja aparece de nuevo en el listado de disponibles
```

---

## Contratos y referencias

- OpenAPI contract: [`contracts/openapi.yaml`](contracts/openapi.yaml)
- Data model: [`data-model.md`](data-model.md)
- Research decisions: [`research.md`](research.md)
- Swagger UI (local): `http://localhost:8080/swagger-ui/index.html`
