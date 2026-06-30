# Research: US-01 · Reserva de Cita en Línea 24/7

**Feature**: `specs/001-reserva-cita-online`
**Date**: 2026-06-27

---

## Decision 1: Lenguaje y Framework

**Decision**: Java 26 + Spring Boot 4.1.0 (ya establecido en el proyecto).
**Rationale**: El proyecto ya tiene `build.gradle` con Spring Boot 4.1.0 y Java 26. Cambiar el stack en esta historia generaría deuda de migración sin valor incremental.
**Alternatives considered**: Quarkus (descartado: mayor fricción de adopción en equipo existente), Micronaut (descartado: misma razón).

---

## Decision 2: Base de datos

**Decision**: H2 para desarrollo/pruebas locales e integración; PostgreSQL 16 para producción (referenciado vía variable de entorno).
**Rationale**: H2 ya está configurado en el proyecto. Para producción se requiere un motor con soporte de aislamiento `SERIALIZABLE` real (requerido por FR-004 para evitar reservas duplicadas). H2 soporta `SERIALIZABLE` para tests de integración.
**Alternatives considered**: MySQL (descartado: menor soporte de `SERIALIZABLE` en modo row-lock), H2 en producción (descartado: no apto para datos persistentes en ambiente productivo).

---

## Decision 3: Framework BDD / Testing

**Decision**: JUnit 5 + Mockito para unit tests; Spring Boot Test + H2 + Testcontainers (opcional producción) para integration tests; MockMvc + Cucumber 7.x para functional/acceptance tests.
**Rationale**: JUnit 5 y Mockito están disponibles vía `spring-boot-starter-*-test`. Cucumber permite escribir escenarios Gherkin directamente de los criterios de aceptación de la spec sin duplicación (DRY). MockMvc integra sin servidor HTTP real.
**Alternatives considered**: REST-assured (descartado: requiere servidor real arriba, más lento para CI); solo JUnit con Given/When/Then en nombres de métodos (descartado: pierde trazabilidad directa con los criterios Gherkin de la spec).

---

## Decision 4: API First — OpenAPI Generator

**Decision**: `org.openapi.generator` Gradle plugin (versión 7.x) con generación de interfaces de servidor (modo `spring`, `delegate` pattern).
**Rationale**: El `delegate` pattern de openapi-generator genera una interfaz que el controller implementa, separando el contrato (generado) de la lógica (manual). Esto cumple con la Constitución IV (API First) sin acoplar el controlador a detalles del generador.
**Alternatives considered**: Springdoc (genera desde código → rompe API First); mapstruct-only (no genera contrato ni interfaces → descartado).

---

## Decision 5: Notificaciones WhatsApp

**Decision**: Adaptador de infraestructura que implementa `NotificacionPort` (interfaz de dominio). En desarrollo usa un stub en memoria; en producción llama a **Twilio WhatsApp API** (o Meta Cloud API) via HTTP.
**Rationale**: La spec asume que el proveedor WhatsApp ya está contratado. Modelar la notificación como un puerto de salida (output port) cumple Dependency Inversion (SOLID III) y permite sustituir el proveedor sin tocar el dominio.
**Alternatives considered**: Llamada directa al proveedor desde el use case (descartado: viola Clean Architecture — capa de aplicación no DEBE depender de infraestructura externa).

---

## Decision 6: Concurrencia / Unicidad de reserva

**Decision**: Bloqueo optimista con `@Version` en la entidad `FranjaHoraria` + restricción `UNIQUE` en base de datos sobre `(medico_id, fecha, hora_inicio)` + transacción `SERIALIZABLE` en el use case de reserva.
**Rationale**: El doble mecanismo (optimistic lock + unique constraint) garantiza que dos solicitudes concurrentes sobre la misma franja resulten en exactamente una confirmación y una respuesta `409 Conflict`, cubriendo SC-002 (0% duplicados).
**Alternatives considered**: Bloqueo pesimista `SELECT FOR UPDATE` (descartado: mayor contención en consultas de disponibilidad); cola de reservas (descartado: YAGNI — sobre-ingeniería para el volumen esperado de esta historia).

---

## Decision 7: Reintento de notificación WhatsApp

**Decision**: Spring `@Async` + `@Retryable` (Spring Retry) para reintentos de notificación fuera del hilo principal de la reserva.
**Rationale**: FR-006 exige que el fallo de WhatsApp no afecte el registro de la cita. Separar el envío asíncrono con reintentos automáticos cubre SC-003 (reintento exitoso < 10 min) con mínima complejidad (YAGNI).
**Alternatives considered**: Kafka / mensaje broker (descartado: YAGNI — agrega operaciones de infraestructura complejas sin necesidad justificada a 8 pts); Quartz scheduler (descartado: misma razón).

---

## Decision 8: Expiración de reserva temporal (FR-008)

**Decision**: Campo `expiradoEn` en `FranjaHoraria` (timestamp) + job programado (`@Scheduled`) que libera franjas en estado `RESERVADA_TEMPORAL` cuyo `expiradoEn < now()`.
**Rationale**: Solución simple y auto-contenida. El job puede ejecutarse cada minuto con impacto mínimo dado el volumen esperado (YAGNI).
**Alternatives considered**: TTL en Redis (descartado: agrega dependencia de infraestructura no justificada por esta historia).

---

## Resumen de stack tecnológico

| Categoría | Decisión |
|-----------|----------|
| Lenguaje | Java 26 |
| Framework | Spring Boot 4.1.0 |
| Persistencia | Spring Data JPA + H2 (dev/test) + PostgreSQL (prod) |
| API contract | OpenAPI 3.0.3 YAML + openapi-generator-gradle-plugin 7.x |
| Unit tests | JUnit 5 + Mockito |
| Integration tests | Spring Boot Test + H2 |
| Functional tests | Cucumber 7.x + MockMvc |
| Coverage | JaCoCo (per-class > 80%, global ≥ 80%) |
| Notificaciones | Puerto de dominio + adaptador Twilio WhatsApp |
| Async / Retry | Spring Async + Spring Retry |
| Expiración slots | Spring `@Scheduled` |
| Código generado | `build/generated/` (excluido de VCS) |
