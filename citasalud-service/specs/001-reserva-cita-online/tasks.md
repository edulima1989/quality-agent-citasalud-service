---
description: "Lista de tareas para US-01 · Reserva de Cita en Línea 24/7"
---

# Tasks: US-01 · Reserva de Cita en Línea 24/7

**Input**: Documentos de diseño en `specs/001-reserva-cita-online/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅ | quickstart.md ✅

**Tests**: Per constitution Principio II (BDD Testing Strategy), los tests unitarios, de integración y funcionales son OBLIGATORIOS para cada user story. Los tests se escriben ANTES de la implementación (ciclo rojo-verde BDD). Las métricas JaCoCo (por clase > 80%, global ≥ 80%) deben pasar antes del merge.

**Organization**: Las tareas están agrupadas por user story para habilitar implementación y prueba independiente de cada historia.

## Format: `[ID] [P?] [Story?] Descripción con ruta de archivo`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias)
- **[Story]**: A qué user story pertenece la tarea (US1, US2)
- Cada tarea incluye rutas de archivo exactas

## Path Conventions

- Código fuente: `src/main/java/org/ups/citasalud/`
- Tests: `src/test/java/org/ups/citasalud/`
- Recursos principales: `src/main/resources/`
- Esquema y datos DB: `src/main/resources/db/`
- Código generado (no commitear): `build/generated/`

---

## Phase 1: Setup (Infraestructura Compartida)

**Purpose**: Inicialización del proyecto, configuración de herramientas y preparación de la base de datos embebida

- [X] T001 Agregar dependencias en `build.gradle`: openapi-generator-gradle-plugin 7.x, Cucumber 7.x (`cucumber-java`, `cucumber-spring`, `cucumber-junit-platform-engine`), Spring Retry (`spring-retry`), JaCoCo plugin; confirmar que Lombok ya está presente
- [X] T002 Configurar tarea openapi-generator en `build.gradle`: modo `spring`, `delegatePattern=true`, outputDir `build/generated/`, inputSpec `src/main/resources/openapi/citasalud-api.yaml`, packageName `org.ups.citasalud.interfaces.rest.generated`
- [X] T003 Copiar `specs/001-reserva-cita-online/contracts/openapi.yaml` a `src/main/resources/openapi/citasalud-api.yaml`
- [X] T004 Configurar JaCoCo en `build.gradle`: tasks `jacocoTestReport` + `jacocoTestCoverageVerification`; límites `classLine > 0.80` y `bundleLine >= 0.80`; excluir patrones `**/generated/**`, `**/*Application*`, `**/*Config*`, `**/*Entity*`
- [X] T005 [P] Habilitar `@EnableRetry` y `@EnableScheduling` en `src/main/java/org/ups/citasalud/CitasaludServiceApplication.java`
- [X] T006 [P] Configurar Cucumber: crear `src/test/resources/cucumber.properties` (`cucumber.plugin=pretty,html:build/reports/cucumber`) y clase runner `src/test/java/org/ups/citasalud/interfaces/rest/CucumberRunnerIT.java` con `@Suite`, `@IncludeEngines("cucumber")`, `@SelectClasspathResource("features")`
- [X] T007 [P] Crear `src/main/resources/db/schema.sql` con DDL compatible H2 y PostgreSQL: tablas `medico`, `franja_horaria` (columnas: `id UUID`, `medico_id UUID`, `fecha DATE`, `hora_inicio TIME`, `hora_fin TIME`, `estado VARCHAR(30)`, `expirado_en TIMESTAMP`, `version BIGINT`; constraint `UNIQUE (medico_id, fecha, hora_inicio)`), `cita` (columnas: `id UUID`, `paciente_id UUID`, `medico_id UUID`, `franja_horaria_id UUID`; constraint `UNIQUE (franja_horaria_id)`), `notificacion` (columnas: `id UUID`, `cita_id UUID`, `canal VARCHAR(20)`, `estado VARCHAR(20)`, `intentos INT`, `ultimo_intento_en TIMESTAMP`, `enviado_en TIMESTAMP`, `error TEXT`)
- [X] T008 [P] Crear `src/main/resources/db/data.sql` con DML precargado: 2 registros en `medico` (IDs fijos UUID, especialidades distintas), 10 registros en `franja_horaria` con estado `DISPONIBLE` distribuidos en los próximos 7 días (fechas relativas con `DATEADD(DAY, N, CURRENT_DATE)`), usando `MERGE INTO` para idempotencia en H2
- [X] T009 Crear `src/main/resources/application.yaml` con: H2 datasource en modo `mem`, `spring.sql.init.mode=always`, `spring.sql.init.schema-locations=classpath:db/schema.sql`, `spring.sql.init.data-locations=classpath:db/data.sql`, `spring.jpa.hibernate.ddl-auto=none` (el schema lo maneja el SQL, no Hibernate), zona horaria UTC-5, `app.whatsapp.stub=true`
- [X] T010 [P] Agregar entrada en `.gitignore` para `build/generated/`
- [X] T011 [P] Ejecutar `./gradlew build -x test` para verificar que openapi-generator produce interfaces en `build/generated/` y el proyecto compila limpio

---

## Phase 2: Foundational (Prerequisitos Bloqueantes)

**Purpose**: Modelos de dominio, ports, entidades JPA y repositorios Spring Data que TODAS las user stories necesitan

**⚠️ CRÍTICO**: Ninguna user story puede comenzar hasta completar esta fase

- [X] T012 Crear enumeraciones del dominio en `src/main/java/org/ups/citasalud/domain/model/`: `EstadoCita.java` (CONFIRMADA, CANCELADA, COMPLETADA), `EstadoFranja.java` (DISPONIBLE, OCUPADA), `EstadoNotificacion.java` (PENDIENTE, ENVIADA, FALLIDA), `CanalNotificacion.java` (WHATSAPP)
- [X] T013 [P] Crear `src/main/java/org/ups/citasalud/domain/model/Medico.java`: record o clase inmutable con `id UUID`, `nombre`, `especialidad`, `consultorio`; sin dependencias de frameworks
- [X] T014 [P] Crear `src/main/java/org/ups/citasalud/domain/model/Paciente.java`: record o clase inmutable con `id UUID`, `nombreCompleto`, `telefonoWhatsApp`; sin dependencias de frameworks
- [X] T015 Crear `src/main/java/org/ups/citasalud/domain/model/FranjaHoraria.java`: campos `id UUID`, `medicoId`, `fecha LocalDate`, `horaInicio LocalTime`, `horaFin LocalTime`, `estado EstadoFranja`, `version Long`; método `confirmar()` que valida que `estado == DISPONIBLE` y lo cambia a `OCUPADA`; lanza `FranjaNoDisponibleException` (sin alternativas — el use case las poblará) si el estado es distinto de `DISPONIBLE`
- [X] T016 Crear `src/main/java/org/ups/citasalud/domain/model/Cita.java`: campos `id UUID`, `pacienteId UUID`, `medicoId UUID`, `franjaHorariaId UUID`, `estado EstadoCita`, `creadoEn Instant`; método de fábrica `crear(pacienteId, medicoId, franjaHorariaId)`
- [X] T017 [P] Crear `src/main/java/org/ups/citasalud/domain/model/Notificacion.java`: campos `id UUID`, `citaId UUID`, `canal CanalNotificacion`, `estado EstadoNotificacion`, `intentos int`, `ultimoIntentoEn Instant`, `enviadoEn Instant`, `error String`; método de fábrica `pendiente(citaId)`
- [X] T018 [P] Crear excepciones de dominio: `src/main/java/org/ups/citasalud/domain/exception/CitaSaludDomainException.java` (base, RuntimeException) y `FranjaNoDisponibleException.java` (extiende base, campo `alternativas List<FranjaHoraria>`)
- [X] T019 [P] Crear output ports: `src/main/java/org/ups/citasalud/domain/port/out/FranjaHorariaRepository.java` (métodos: `findById`, `findDisponibles`, `findAlternativasDisponibles`, `save`), `CitaRepository.java` (métodos: `save`, `findByPacienteId`), `NotificacionRepository.java` (métodos: `save`), `NotificacionPort.java` (método: `enviar(Notificacion)`)
- [X] T020 [P] Crear entidad JPA `src/main/java/org/ups/citasalud/infrastructure/persistence/entity/FranjaHorariaEntity.java`: `@Entity`, `@Table(name="franja_horaria", uniqueConstraints=@UniqueConstraint(columnNames={"medico_id","fecha","hora_inicio"}))`, campo `@Version Long version`
- [X] T021 [P] Crear entidad JPA `src/main/java/org/ups/citasalud/infrastructure/persistence/entity/CitaEntity.java`: `@Entity`, `@Table(name="cita", uniqueConstraints=@UniqueConstraint(columnNames={"franja_horaria_id"}))`, relación a `FranjaHorariaEntity`
- [X] T022 [P] Crear entidad JPA `src/main/java/org/ups/citasalud/infrastructure/persistence/entity/NotificacionEntity.java`: `@Entity`, `@Table(name="notificacion")`, todos los campos de `Notificacion`
- [X] T023 [P] Crear repositorios Spring Data JPA: `src/main/java/org/ups/citasalud/infrastructure/persistence/jpa/FranjaHorariaJpaRepository.java` (query `findByMedicoIdAndFechaAndEstado`, `findByEstadoAndExpiradoEnBefore`), `CitaJpaRepository.java` (query `findByPacienteIdAndEstadoIn`), `NotificacionJpaRepository.java`
- [X] T024 Verificar que el schema arranca correctamente: `./gradlew bootRun` debe inicializar H2 con las tablas de `db/schema.sql` y los datos de `db/data.sql` sin errores; confirmar en la consola H2 (`/h2-console`) que existen filas en `medico` y `franja_horaria`

**Checkpoint**: Infraestructura lista — las user stories pueden comenzar en paralelo

---

## Phase 3: User Story 1 — Reservar cita en horario no laborable (Priority: P1) 🎯 MVP

**Goal**: El paciente autenticado puede consultar disponibilidad, reservar una franja y recibir confirmación por WhatsApp (stub en tests).

**Independent Test**: `./gradlew test --tests "org.ups.citasalud.interfaces.rest.CucumberRunnerIT" --tests "org.ups.citasalud.domain.*"` — si pasa, US1 es funcional de forma independiente.

### Tests para User Story 1 (OBLIGATORIOS — escribir ANTES de implementar)

> **NOTA: Escribir estos tests PRIMERO, verificar que FALLAN antes de implementar (ciclo BDD rojo-verde)**

- [X] T025 [P] [US1] Crear `src/test/java/org/ups/citasalud/domain/model/FranjaHorariaTest.java`: Given franja en estado DISPONIBLE, When se llama a `confirmar()`, Then estado cambia a OCUPADA; Given franja en estado OCUPADA, When se llama a `confirmar()`, Then lanza `FranjaNoDisponibleException` (JUnit 5, sin mocks)
- [X] T026 [P] [US1] Crear `src/test/java/org/ups/citasalud/domain/usecase/ConsultarDisponibilidadServiceTest.java`: Given médico con franjas DISPONIBLE en rango de fechas, When se ejecuta el use case, Then retorna solo las DISPONIBLE; Given sin franjas, When se ejecuta, Then retorna lista vacía (JUnit 5 + Mockito)
- [X] T027 [P] [US1] Crear `src/test/java/org/ups/citasalud/domain/usecase/ReservarCitaServiceTest.java`: Given franja DISPONIBLE, When se reserva, Then crea Cita con estado CONFIRMADA, persiste Notificacion PENDIENTE y llama a NotificacionPort.enviar; Given NotificacionPort lanza excepción, When se reserva, Then la Cita se crea igualmente (JUnit 5 + Mockito)
- [X] T028 [P] [US1] Crear `src/test/java/org/ups/citasalud/infrastructure/persistence/CitaRepositoryAdapterIT.java`: Given datos de `db/data.sql` en H2, When se reserva una franja DISPONIBLE, Then la franja queda OCUPADA y existe una Cita con estado CONFIRMADA en BD; verificar que el constraint UNIQUE de `franja_horaria_id` existe (`@DataJpaTest`)
- [X] T029 [P] [US1] Crear `src/test/resources/features/reservar_cita.feature`: primera línea `@us1` a nivel de Feature (`@us1\nFeature: Reservar cita en línea`); dos escenarios con Given/When/Then: "Reservar cita disponible exitosamente" (criterio US1 escenario 1 de spec.md) y "Cita reservada aparece en listado del paciente" (criterio US1 escenario 2); los IDs UUID de médico y franja en los steps DEBEN coincidir con los insertados en `db/data.sql`
- [X] T030 [US1] Crear `src/test/java/org/ups/citasalud/interfaces/rest/ReservarCitaFT.java`: step definitions para `reservar_cita.feature` usando MockMvc; usar `@WithMockUser(username = "<uuid-paciente-fijo-de-data.sql>")` para simular paciente autenticado; steps: When POST `/v1/citas` con `franjaHorariaId` de los datos precargados en `db/data.sql` → Then HTTP 201 con `estado=CONFIRMADA`; When GET `/v1/citas` → Then la cita aparece con `estado=CONFIRMADA`; agregar `spring-security-test` en `testImplementation` de `build.gradle` si no está presente

### Implementación para User Story 1

- [X] T031 [P] [US1] Crear input ports en `src/main/java/org/ups/citasalud/domain/port/in/`: `ConsultarDisponibilidadUseCase.java` (método `consultar(DisponibilidadQuery): List<FranjaHoraria>`), `ReservarCitaUseCase.java` (método `reservar(ReservaCitaCommand): Cita`), `ConsultarCitasPacienteUseCase.java` (método `consultar(UUID pacienteId, EstadoCita estado): List<Cita>`)
- [X] T032 [P] [US1] Crear DTOs de aplicación: `src/main/java/org/ups/citasalud/application/dto/DisponibilidadQuery.java` (medicoId UUID nullable, especialidad String nullable, fechaDesde LocalDate, fechaHasta LocalDate) y `ReservaCitaCommand.java` (franjaHorariaId UUID, pacienteId UUID)
- [X] T033 [US1] Implementar `src/main/java/org/ups/citasalud/application/usecase/ConsultarDisponibilidadService.java`: delega a `FranjaHorariaRepository.findDisponibles(query)`; valida que `fechaHasta - fechaDesde <= 30 días`; anotado con `@Service`
- [X] T034 [US1] Implementar `src/main/java/org/ups/citasalud/application/usecase/ReservarCitaService.java`: anotado con `@Service` y `@Transactional`; pasos: (1) `franjaHorariaRepository.findById(command.franjaHorariaId())`, lanzar si no existe; (2) `franja.confirmar()` — propaga `FranjaNoDisponibleException` si el estado no es DISPONIBLE; (3) `franjaHorariaRepository.save(franja)` (estado OCUPADA); (4) `citaRepository.save(Cita.crear(...))` ; (5) `notificacionRepository.save(Notificacion.pendiente(cita.id()))`; (6) llamar `notificacionPort.enviar(notificacion)` como llamada síncrona ordinaria — el service NO declara `@Async`; el adaptador `WhatsAppNotificacionAdapter` (T039) lleva `@Async` en su implementación y Spring lo despacha asíncronamente de forma transparente (Clean Architecture Principio I: la capa de aplicación no conoce la mecánica de dispatch)
- [X] T035 [US1] Implementar `src/main/java/org/ups/citasalud/application/usecase/ConsultarCitasPacienteService.java`: `findByPacienteId(pacienteId, estado)` filtrando opcionalmente por estado
- [X] T036 [US1] Implementar `src/main/java/org/ups/citasalud/infrastructure/persistence/adapter/FranjaHorariaRepositoryAdapter.java`: implementa `FranjaHorariaRepository`; usa `FranjaHorariaJpaRepository`; incluye método `findAlternativasDisponibles(medicoId, fecha)` que busca franjas DISPONIBLE del mismo médico ±7 días
- [X] T037 [US1] Implementar `src/main/java/org/ups/citasalud/infrastructure/persistence/adapter/CitaRepositoryAdapter.java`: implementa `CitaRepository`; usa `CitaJpaRepository`; mapeo bidireccional `CitaEntity ↔ Cita`
- [X] T038 [US1] Implementar `src/main/java/org/ups/citasalud/infrastructure/persistence/adapter/NotificacionRepositoryAdapter.java`: implementa `NotificacionRepository`; usa `NotificacionJpaRepository`; mapeo bidireccional
- [X] T039 [US1] Implementar `src/main/java/org/ups/citasalud/infrastructure/notification/WhatsAppNotificacionAdapter.java`: implementa `NotificacionPort`; si `app.whatsapp.stub=true` solo loguea INFO; en modo real hace HTTP al proveedor; anotado con `@Async` y `@Retryable(maxAttempts=3, backoff=@Backoff(delay=2000))`; actualiza `estado` y `enviado_en` de la `Notificacion` en BD al finalizar
- [X] T040 [US1] Implementar `src/main/java/org/ups/citasalud/interfaces/rest/DisponibilidadController.java`: implementa la interfaz `DisponibilidadApi` generada por openapi-generator; delega a `ConsultarDisponibilidadUseCase`; mapea `List<FranjaHoraria>` → `DisponibilidadResponse` usando `FranjaHorariaMapper`
- [X] T041 [US1] Implementar `src/main/java/org/ups/citasalud/interfaces/rest/CitasController.java`: implementa la interfaz `CitasApi` generada; POST `/v1/citas` construye `ReservaCitaCommand` con `franjaHorariaId` del request y `pacienteId` extraído del principal autenticado (ver T041b), delega a `ReservarCitaUseCase` → 201; GET `/v1/citas` delega a `ConsultarCitasPacienteUseCase` → 200; captura `FranjaNoDisponibleException` y delega al GlobalExceptionHandler → 409
- [X] T041b [US1] Extraer `pacienteId` del contexto de autenticación en `CitasController.java`: inyectar `@AuthenticationPrincipal` o leer `SecurityContextHolder.getContext().getAuthentication().getName()` y convertir a UUID; este valor se pasa como `pacienteId` en `ReservaCitaCommand`; agregar `spring-security-test` en `testImplementation` de `build.gradle`; en `ReservarCitaFT.java` (T030) usar `@WithMockUser(username = "<uuid-paciente-fijo>")` donde el UUID coincide con un `paciente_id` referenciado en `db/data.sql`
- [X] T042 [P] [US1] Implementar mappers `src/main/java/org/ups/citasalud/interfaces/rest/mapper/CitaMapper.java` y `FranjaHorariaMapper.java`: conversión entre modelos de dominio y DTOs generados por openapi-generator (sin frameworks de mapeo — YAGNI)
- [X] T043 [US1] Implementar `src/main/java/org/ups/citasalud/interfaces/rest/exception/GlobalExceptionHandler.java`: `@RestControllerAdvice`; captura `FranjaNoDisponibleException` → 409 con `FranjaNoDisponibleResponse`; captura `CitaSaludDomainException` → 400; captura `Exception` → 500; nunca expone stack traces en la respuesta
- [X] T044 [US1] Ejecutar `./gradlew test --tests "org.ups.citasalud.domain.*"` — verificar que todos los unit tests US1 están en verde
- [X] T045 [US1] Ejecutar `./gradlew test --tests "org.ups.citasalud.infrastructure.*IT"` — verificar que los integration tests US1 pasan con datos precargados de `db/data.sql`
- [X] T046 [US1] Ejecutar `./gradlew test --tests "org.ups.citasalud.interfaces.rest.CucumberRunnerIT"` filtrando tag `@us1` — verificar que los functional tests de `reservar_cita.feature` pasan

**Checkpoint**: User Story 1 completamente funcional. Validar con `quickstart.md` escenarios US1-1 y US1-2.

---

## Phase 4: User Story 2 — Intento de reserva en franja ocupada (Priority: P2)

**Goal**: El sistema rechaza con 409 el intento de reservar una franja ocupada, devuelve alternativas disponibles y garantiza cero duplicados bajo concurrencia (SC-002).

**Independent Test**: `./gradlew test --tests "org.ups.citasalud.interfaces.rest.CucumberRunnerIT"` filtrando tag `@us2` — si pasa, US2 es funcional de forma independiente.

### Tests para User Story 2 (OBLIGATORIOS — escribir ANTES de implementar)

> **NOTA: Escribir estos tests PRIMERO, verificar que FALLAN antes de implementar (ciclo BDD rojo-verde)**

- [X] T047 [P] [US2] Ampliar `src/test/java/org/ups/citasalud/domain/usecase/ReservarCitaServiceTest.java`: Given franja OCUPADA, When se reserva, Then lanza `FranjaNoDisponibleException` con campo `alternativas` no vacío y NO se llama a `CitaRepository.save` (JUnit 5 + Mockito)
- [X] T048 [P] [US2] Crear `src/test/java/org/ups/citasalud/infrastructure/persistence/ConcurrenciaReservaIT.java`: Given franja DISPONIBLE en H2 (de `db/data.sql`), When dos threads ejecutan `ReservarCitaService.reservar` simultáneamente con la misma franja, Then exactamente una Cita queda con estado CONFIRMADA; la segunda lanza `FranjaNoDisponibleException`; verificar conteo en BD = 1 (`@SpringBootTest`)
- [X] T049 [P] [US2] Crear `src/test/resources/features/franja_ocupada.feature`: primera línea `@us2` a nivel de Feature (`@us2\nFeature: Manejo de franja ocupada`); dos escenarios con Given/When/Then: "Intentar reservar franja ya ocupada" y "Reserva concurrente de la misma franja" (criterios US2 de spec.md); el step "Given una franja ya ocupada" ejecuta una reserva previa en el setup del step definition usando datos de `db/data.sql`
- [X] T050 [US2] Crear `src/test/java/org/ups/citasalud/interfaces/rest/FranjaOcupadaFT.java`: step definitions para `franja_ocupada.feature` usando MockMvc; verifica que POST `/v1/citas` con franja ocupada retorna HTTP 409, `codigo=FRANJA_NO_DISPONIBLE` y `alternativas` con al menos un elemento

### Implementación para User Story 2

- [X] T051 [US2] Actualizar `src/main/java/org/ups/citasalud/application/usecase/ReservarCitaService.java`: capturar `OptimisticLockingFailureException` y `DataIntegrityViolationException` de la capa JPA; relanzar como `FranjaNoDisponibleException` poblando el campo `alternativas` con el resultado de `franjaHorariaRepository.findAlternativasDisponibles(medicoId, fecha)`
- [X] T052 [US2] Actualizar `src/main/java/org/ups/citasalud/interfaces/rest/exception/GlobalExceptionHandler.java`: al capturar `FranjaNoDisponibleException`, construir `FranjaNoDisponibleResponse` con el campo `alternativas` mapeado desde `exception.getAlternativas()` usando `FranjaHorariaMapper`
- [X] T053 [US2] Ejecutar `./gradlew test --tests "org.ups.citasalud.domain.usecase.ReservarCitaServiceTest"` — verificar que todos los casos US2 pasan
- [X] T054 [US2] Ejecutar `./gradlew test --tests "org.ups.citasalud.infrastructure.persistence.ConcurrenciaReservaIT"` — verificar cero duplicados
- [X] T055 [US2] Ejecutar functional tests US2: `./gradlew test --tests "org.ups.citasalud.interfaces.rest.CucumberRunnerIT"` filtrando tag `@us2`

**Checkpoint**: User Stories 1 Y 2 son independientemente funcionales. Validar con `quickstart.md` escenarios US2-1 y US2-2.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Logging, validación de cobertura y hardening final

- [X] T056 [P] Agregar logging SLF4J en `ReservarCitaService` (INFO al confirmar cita con citaId, WARN al capturar concurrencia) y en `WhatsAppNotificacionAdapter` (INFO al enviar exitosamente, ERROR al agotar reintentos con causa)
- [X] T057 [P] Validar cumplimiento Clean Architecture: ejecutar `grep -r "import org.ups.citasalud.infrastructure" src/main/java/org/ups/citasalud/domain/` y `grep -r "import org.ups.citasalud.interfaces" src/main/java/org/ups/citasalud/application/` — ambos deben retornar vacío
- [X] T058 [P] Ejecutar `./gradlew test jacocoTestReport jacocoTestCoverageVerification` — debe pasar; revisar reporte en `build/reports/jacoco/test/html/index.html` y corregir clases por debajo del 80%
- [X] T059 Validar flujo completo con `quickstart.md`: escenarios US1-1, US1-2, US2-1, US2-2 y escenario de fallo de notificación WhatsApp
- [X] T060 Ejecutar `./gradlew clean build` — BUILD SUCCESSFUL con todos los tests en verde y cobertura ≥ 80%

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — comienza de inmediato
- **Foundational (Phase 2)**: Depende de completar Phase 1 — BLOQUEA todas las user stories
- **User Story 1 (Phase 3)**: Depende de Phase 2 — MVP independiente
- **User Story 2 (Phase 4)**: Depende de Phase 3 — extiende `ReservarCitaService` y `FranjaNoDisponibleException`
- **Polish (Phase 5)**: Depende de Phase 3 + Phase 4 completadas

### User Story Dependencies

- **US1 (P1)**: Puede comenzar tras Phase 2 — sin dependencias en otras historias
- **US2 (P2)**: Depende de US1 — amplía el service y la excepción existentes; no crea nuevas clases de infraestructura

### Dentro de Cada User Story

- Tests DEBEN escribirse y FALLAR antes de la implementación
- Enumeraciones antes que entidades de dominio
- Puertos (out) antes que use cases
- Use cases antes que adaptadores de infraestructura
- Adaptadores antes que controllers
- Controllers antes que functional tests finales

### Oportunidades de Paralelismo

- **Phase 1**: T005, T006, T007, T008, T010 pueden ejecutarse en paralelo tras T001–T004
- **Phase 2**: T013–T014, T018–T023 (hasta 8 tareas en paralelo)
- **Phase 3 (tests)**: T025, T026, T027, T028, T029 en paralelo
- **Phase 3 (ports + DTOs)**: T031, T032 en paralelo
- **Phase 3 (mappers)**: T042 en paralelo con T040–T041
- **Phase 4 (tests)**: T047, T048, T049 en paralelo
- **Phase 5**: T056, T057, T058 en paralelo

---

## Parallel Execution Example: User Story 1

```bash
# Escribir todos los tests de US1 simultáneamente:
Task: "T025 Unit test FranjaHorariaTest.java"
Task: "T026 Unit test ConsultarDisponibilidadServiceTest.java"
Task: "T027 Unit test ReservarCitaServiceTest.java"
Task: "T028 Integration test CitaRepositoryAdapterIT.java"
Task: "T029 Feature file reservar_cita.feature"

# Crear todos los ports e input ports de US1 simultáneamente:
Task: "T031 Input ports (ConsultarDisponibilidad, ReservarCita, ConsultarCitasPaciente)"
Task: "T032 DTOs DisponibilidadQuery + ReservaCitaCommand"
```

---

## Implementation Strategy

### MVP First (Solo User Story 1)

1. Completar Phase 1: Setup (incluye `db/schema.sql` y `db/data.sql`)
2. Completar Phase 2: Foundational — CRÍTICO
3. Completar Phase 3: User Story 1
4. **PARAR Y VALIDAR**: `./gradlew test` — todos los tests US1 en verde
5. Verificar con `quickstart.md` que los datos precargados permiten reservar sin configuración adicional
6. Demo / despliegue si está listo

### Entrega Incremental

1. Setup + Foundational → Infraestructura lista con datos precargados
2. User Story 1 → Testar independientemente → Demo (MVP: reserva funciona)
3. User Story 2 → Testar independientemente → Demo (MVP+: manejo de conflictos)
4. Polish → Cobertura ≥ 80% confirmada → PR listo para merge

---

## Notes

- `[P]` = archivos distintos, sin dependencias entre sí — pueden ejecutarse en paralelo
- `[US1]` / `[US2]` = traza cada tarea a su user story para trazabilidad
- Los tests deben FALLAR antes de implementar (verificar con `./gradlew test` antes de escribir producción)
- Los datos de `db/data.sql` se usan tanto en tests de integración (`@DataJpaTest`) como en los functional tests (`@SpringBootTest`); las UUIDs de los médicos y franjas son fijas para permitir referencias directas en los tests
- Commit tras cada tarea o grupo lógico usando `./gradlew test` como gate local
- Detenerse en cada **Checkpoint** para validar la historia de forma independiente
- Código generado por openapi-generator en `build/generated/` — NUNCA commitear
