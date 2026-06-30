# Implementation Plan: US-01 · Reserva de Cita en Línea 24/7

**Branch**: `001-reserva-cita-online` | **Date**: 2026-06-27 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-reserva-cita-online/spec.md`

---

## Summary

Implementar el flujo completo de reserva de cita médica en línea 24/7 siguiendo Clean Architecture. El paciente autenticado consulta disponibilidad, selecciona una franja, confirma la reserva (operación atómica anti-concurrencia) y recibe confirmación por WhatsApp. El dominio expone tres use cases: `ConsultarDisponibilidad`, `ReservarCita` y `ConsultarCitasPaciente`. La capa de interfaces REST se genera desde el contrato OpenAPI 3.0.3 ubicado en `contracts/openapi.yaml`.

---

## Technical Context

**Language/Version**: Java 26

**Primary Dependencies**:
- Spring Boot 4.1.0 (web MVC, data JPA, scheduling, async, retry)
- Lombok (reducción de boilerplate)
- openapi-generator-gradle-plugin 7.x (generación de interfaces/DTOs desde contrato)
- Cucumber 7.x + JUnit 5 (BDD functional tests)
- Mockito (unit tests)
- JaCoCo (coverage gate ≥ 80%)
- Spring Retry (reintentos asíncronos de notificación WhatsApp)

**Storage**: H2 (dev/test) — PostgreSQL 16 (prod, vía variable de entorno `SPRING_DATASOURCE_URL`)

**Testing**:
- Unit: JUnit 5 + Mockito (capas domain + application)
- Integration: Spring Boot Test + H2 en memoria
- Functional/Acceptance: Cucumber 7 + MockMvc

**Target Platform**: Linux server / JVM 26 (API REST JSON)

**Project Type**: Web service (REST API backend)

**Performance Goals**:
- Flujo de reserva completo < 3 min (SC-001)
- Tasa de duplicados = 0% bajo concurrencia (SC-002)
- Reintento WhatsApp exitoso < 10 min (SC-003)

**Constraints**:
- Franja sin confirmar expira en 5 min (FR-008, configurable)
- Rango de búsqueda de disponibilidad máximo 30 días
- Zona horaria UTC-5 (Colombia)
- Código generado por openapi-generator en `build/generated/` (excluido de VCS)

**Scale/Scope**: Servicio de citas médicas para institución de salud — historia de 8 pts, épica E-01

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Principle | Gate Question | Status |
|---|-----------|---------------|--------|
| I | Clean Architecture | ¿Están definidas las capas domain/application/infrastructure/interfaces con dependencias apuntando hacia adentro? | ✅ |
| II | BDD Testing | ¿Se planean unit + integration + functional tests en Given/When/Then para cada user story? | ✅ |
| III | SOLID / YAGNI / DRY | ¿El diseño evita abstracciones especulativas, duplicación y violaciones SRP/OCP? | ✅ |
| IV | API First | ¿El contrato OpenAPI 3.0.3 existe antes de la implementación del controller? | ✅ |
| V | Quality Metrics | ¿JaCoCo está configurado con per-class > 80% y global ≥ 80% en build.gradle? | ✅ |

Todos los gates pasan. No se requiere Complexity Tracking.

---

## Project Structure

### Documentation (this feature)

```text
specs/001-reserva-cita-online/
├── plan.md              ← este archivo
├── spec.md              ← especificación funcional
├── research.md          ← decisiones técnicas Phase 0
├── data-model.md        ← modelo de dominio y paquetes
├── quickstart.md        ← guía de validación end-to-end
├── contracts/
│   └── openapi.yaml     ← contrato OpenAPI 3.0.3 (fuente de verdad)
└── tasks.md             ← generado por /speckit-tasks (pendiente)
```

### Source Code (repository root)

```text
src/main/java/org/ups/citasalud/
├── domain/
│   ├── model/
│   │   ├── Cita.java
│   │   ├── FranjaHoraria.java
│   │   ├── Medico.java
│   │   ├── Paciente.java
│   │   ├── Notificacion.java
│   │   ├── EstadoCita.java
│   │   ├── EstadoFranja.java
│   │   ├── EstadoNotificacion.java
│   │   └── CanalNotificacion.java
│   ├── port/
│   │   ├── in/
│   │   │   ├── ConsultarDisponibilidadUseCase.java
│   │   │   ├── ReservarCitaUseCase.java
│   │   │   └── ConsultarCitasPacienteUseCase.java
│   │   └── out/
│   │       ├── CitaRepository.java
│   │       ├── FranjaHorariaRepository.java
│   │       ├── NotificacionRepository.java
│   │       └── NotificacionPort.java
│   └── exception/
│       ├── FranjaNoDisponibleException.java
│       └── CitaSaludDomainException.java
│
├── application/
│   ├── usecase/
│   │   ├── ConsultarDisponibilidadService.java
│   │   ├── ReservarCitaService.java
│   │   └── ConsultarCitasPacienteService.java
│   └── dto/
│       ├── ReservaCitaCommand.java
│       └── DisponibilidadQuery.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── CitaEntity.java
│   │   │   ├── FranjaHorariaEntity.java
│   │   │   └── NotificacionEntity.java
│   │   ├── jpa/
│   │   │   ├── CitaJpaRepository.java
│   │   │   ├── FranjaHorariaJpaRepository.java
│   │   │   └── NotificacionJpaRepository.java
│   │   └── adapter/
│   │       ├── CitaRepositoryAdapter.java
│   │       ├── FranjaHorariaRepositoryAdapter.java
│   │       └── NotificacionRepositoryAdapter.java
│   ├── notification/
│   │   ├── WhatsAppNotificacionAdapter.java
│   │   └── WhatsAppClientConfig.java
│   └── scheduler/
│       └── FranjaExpiracionScheduler.java
│
└── interfaces/
    └── rest/
        ├── CitasController.java
        ├── DisponibilidadController.java
        ├── mapper/
        │   ├── CitaMapper.java
        │   └── FranjaHorariaMapper.java
        └── exception/
            └── GlobalExceptionHandler.java

src/main/resources/
├── openapi/
│   └── citasalud-api.yaml     ← copia del contrato para generación (referencia a contracts/)
├── db/
│   ├── schema.sql             ← DDL: CREATE TABLE para todas las entidades (H2 + PostgreSQL compatible)
│   └── data.sql               ← DML: datos precargados para dev/test (médicos, franjas horarias)
└── application.yaml           ← spring.sql.init apunta a db/schema.sql y db/data.sql

build/generated/               ← salida de openapi-generator (excluido de VCS)
└── src/main/java/org/ups/citasalud/interfaces/rest/generated/
    ├── CitasApi.java           ← interfaz generada
    ├── DisponibilidadApi.java  ← interfaz generada
    └── model/                 ← DTOs generados

src/test/java/org/ups/citasalud/
├── domain/
│   ├── usecase/
│   │   ├── ReservarCitaServiceTest.java
│   │   └── ConsultarDisponibilidadServiceTest.java
│   └── model/
│       └── FranjaHorariaTest.java
├── infrastructure/
│   ├── persistence/
│   │   └── CitaRepositoryAdapterIT.java
│   └── notification/
│       └── WhatsAppNotificacionAdapterTest.java
└── interfaces/
    └── rest/
        ├── ReservarCitaFT.java
        ├── FranjaOcupadaFT.java
        └── features/
            ├── reservar_cita.feature
            └── franja_ocupada.feature
```

**Structure Decision**: Single-project Clean Architecture. El código de producción sigue las cuatro capas de la constitución (domain → application → infrastructure → interfaces). Los controladores implementan interfaces generadas por openapi-generator. Los tests están organizados por capa para reflejar la misma separación.

---

## Complexity Tracking

> No hay violaciones de la constitución que justificar.
