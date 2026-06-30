# Data Model: US-01 · Reserva de Cita en Línea 24/7

**Feature**: `specs/001-reserva-cita-online`
**Date**: 2026-06-27

---

## Entidades del Dominio

### Paciente *(referencia — fuera del alcance de esta historia)*

Gestionado por el módulo de autenticación/registro. Esta historia lo consume como referencia por `pacienteId`.

| Campo | Tipo | Restricciones |
|-------|------|---------------|
| id | UUID | PK, NOT NULL |
| nombreCompleto | String | NOT NULL, max 200 |
| telefonoWhatsApp | String | NOT NULL, E.164 format |

---

### Medico *(referencia — preexistente en el sistema)*

| Campo | Tipo | Restricciones |
|-------|------|---------------|
| id | UUID | PK, NOT NULL |
| nombre | String | NOT NULL, max 200 |
| especialidad | String | NOT NULL, max 100 |
| consultorio | String | NOT NULL, max 100 |

---

### FranjaHoraria

Unidad atómica de disponibilidad de un médico. Su estado debe cambiar de forma concurrente-segura.

| Campo | Tipo | Restricciones |
|-------|------|---------------|
| id | UUID | PK, NOT NULL |
| medicoId | UUID | FK → Medico, NOT NULL |
| fecha | LocalDate | NOT NULL |
| horaInicio | LocalTime | NOT NULL |
| horaFin | LocalTime | NOT NULL |
| estado | EstadoFranja | NOT NULL, default `DISPONIBLE` |
| version | Long | @Version para control optimista, NOT NULL |

**Unique constraint**: `(medico_id, fecha, hora_inicio)` — garantiza unicidad de franja por médico.

**Transiciones de estado**:

```
DISPONIBLE ──► OCUPADA   (confirmación atómica vía POST /v1/citas)
OCUPADA    ──► CANCELADA (fuera del alcance de esta historia)
```

**Reglas de negocio**:
- Solo se puede confirmar una franja en estado `DISPONIBLE`. Si el estado es distinto, se lanza `FranjaNoDisponibleException`.
- Una franja `OCUPADA` no puede volver a `DISPONIBLE` (a menos que la cita asociada sea cancelada — fuera del alcance de esta historia).
- El sistema nunca retiene una franja entre la consulta de disponibilidad y la confirmación; la franja permanece `DISPONIBLE` hasta que `POST /v1/citas` la confirme atómicamente (FR-008).

---

### Cita

Aggregate root del dominio. Representa una reserva confirmada.

| Campo | Tipo | Restricciones |
|-------|------|---------------|
| id | UUID | PK, NOT NULL, generado |
| pacienteId | UUID | NOT NULL |
| medicoId | UUID | FK → Medico, NOT NULL |
| franjaHorariaId | UUID | FK → FranjaHoraria, NOT NULL, UNIQUE |
| estado | EstadoCita | NOT NULL, default `CONFIRMADA` |
| creadoEn | Instant | NOT NULL, generado |

**Unique constraint**: `(franja_horaria_id)` — cada franja solo puede tener una cita activa.

**Transiciones de estado**:

```
CONFIRMADA ──► CANCELADA
CONFIRMADA ──► COMPLETADA
```

---

### Notificacion

Registro de auditoría de cada intento de envío de WhatsApp.

| Campo | Tipo | Restricciones |
|-------|------|---------------|
| id | UUID | PK, NOT NULL, generado |
| citaId | UUID | FK → Cita, NOT NULL |
| canal | CanalNotificacion | NOT NULL, valor `WHATSAPP` |
| estado | EstadoNotificacion | NOT NULL, default `PENDIENTE` |
| intentos | Integer | NOT NULL, default 0 |
| ultimoIntentoEn | Instant | NULL |
| enviadoEn | Instant | NULL — relleno al estado `ENVIADA` |
| error | String | NULL — mensaje del último fallo |

---

## Enumeraciones

### EstadoFranja
```
DISPONIBLE   // Puede ser reservada por cualquier paciente
OCUPADA      // Confirmada atómicamente por un paciente vía POST /v1/citas
```

### EstadoCita
```
CONFIRMADA    // Reserva completada
CANCELADA     // Cancelada por el paciente o el sistema
COMPLETADA    // Cita atendida
```

### EstadoNotificacion
```
PENDIENTE   // En cola de envío
ENVIADA     // Enviada exitosamente al proveedor
FALLIDA     // Falló después de todos los reintentos
```

### CanalNotificacion
```
WHATSAPP
```

---

## Diagrama de relaciones

```
Medico ──────────────────────────────────────────┐
  │                                              │
  │ (1)                                          │ (1)
  ▼                                              ▼
FranjaHoraria (1) ◄──── (1) Cita (1) ────────► Paciente
                               │
                               │ (1)
                               ▼
                         Notificacion (0..*)
```

---

## Invariantes de dominio

1. Una `FranjaHoraria` en estado `DISPONIBLE` pasa atómicamente a `OCUPADA` al confirmar la reserva. Solo puede existir una `Cita` con estado `CONFIRMADA` por franja.
2. La transición `DISPONIBLE → OCUPADA` y la creación de la `Cita` ocurren en la misma transacción de base de datos. Si la franja ya está `OCUPADA`, la operación lanza `FranjaNoDisponibleException` antes de persistir nada.
3. Una `Notificacion` DEBE existir para cada `Cita` con estado `CONFIRMADA` (con estado inicial `PENDIENTE`).
4. `FranjaHoraria.horaFin` DEBE ser mayor que `FranjaHoraria.horaInicio`.
5. `FranjaHoraria.fecha` DEBE ser ≥ `LocalDate.now()` al momento de la reserva.

---

## Estructura de paquetes (Clean Architecture)

```
src/main/java/org/ups/citasalud/
│
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
│   │   ├── out/
│   │   │   ├── CitaRepository.java
│   │   │   ├── FranjaHorariaRepository.java
│   │   │   ├── NotificacionRepository.java
│   │   │   └── NotificacionPort.java
│   │   └── in/
│   │       ├── ConsultarDisponibilidadUseCase.java
│   │       ├── ReservarCitaUseCase.java
│   │       └── ConsultarCitasPacienteUseCase.java
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
        ├── CitasController.java          ← implements generated ApiDelegate
        ├── DisponibilidadController.java ← implements generated ApiDelegate
        ├── mapper/
        │   ├── CitaMapper.java
        │   └── FranjaHorariaMapper.java
        └── exception/
            └── GlobalExceptionHandler.java

src/main/resources/
├── openapi/
│   └── citasalud-api.yaml
├── db/
│   ├── schema.sql             ← DDL de todas las entidades (H2 + PostgreSQL compatible)
│   └── data.sql               ← DML con datos precargados para dev/test
└── application.yaml

src/test/java/org/ups/citasalud/
├── domain/
│   ├── usecase/
│   │   ├── ReservarCitaServiceTest.java       ← unit (JUnit5 + Mockito)
│   │   └── ConsultarDisponibilidadServiceTest.java
│   └── model/
│       └── FranjaHorariaTest.java
├── infrastructure/
│   ├── persistence/
│   │   └── CitaRepositoryAdapterIT.java       ← integration (H2)
│   └── notification/
│       └── WhatsAppNotificacionAdapterTest.java
└── interfaces/
    └── rest/
        ├── ReservarCitaFT.java                 ← functional (Cucumber + MockMvc)
        └── features/
            ├── reservar_cita.feature
            └── franja_ocupada.feature
```
