# Feature Specification: US-01 · Reserva de Cita en Línea 24/7

**Feature Branch**: `001-reserva-cita-online`

**Created**: 2026-06-27

**Status**: Draft

**Épica**: E-01 · Reserva de Citas Online | **Story Points**: 8

**Input**: Como paciente, quiero reservar una cita en línea en cualquier momento del día, para no tener que llamar durante mi horario de almuerzo ni acumular intentos fallidos.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reservar cita en horario no laborable (Priority: P1)

El paciente accede al sistema de citas fuera del horario de atención telefónica (por ejemplo, a las 22:00), consulta la disponibilidad de un médico específico, selecciona una fecha y franja horaria disponible, confirma la reserva y recibe una notificación de confirmación por WhatsApp.

**Why this priority**: Es el flujo de valor principal de la historia: garantizar que el paciente pueda reservar sin restricción horaria. Sin este flujo no existe el producto mínimo viable de la épica.

**Independent Test**: Puede probarse completamente ejecutando el flujo de reserva contra el sistema con un usuario paciente autenticado, verificando que la cita persiste en base de datos y que se dispara la notificación WhatsApp (stub/mock del proveedor en tests automatizados).

**Acceptance Scenarios**:

1. **Given** que el paciente está autenticado y accede al módulo de reservas fuera del horario de atención telefónica (cualquier hora),
   **When** selecciona un médico, una fecha y una franja horaria disponible y confirma la reserva,
   **Then** la cita queda registrada en el sistema con estado `CONFIRMADA` y el paciente recibe un mensaje de confirmación por WhatsApp con los datos de la cita (médico, fecha, hora, consultorio).

2. **Given** que el paciente ha completado una reserva exitosamente,
   **When** consulta su listado de citas,
   **Then** la nueva cita aparece en el listado con estado `CONFIRMADA` y los datos correctos.

---

### User Story 2 - Intento de reserva en franja ocupada (Priority: P2)

El paciente intenta confirmar una franja horaria que ya fue tomada por otro paciente (condición de carrera o simplemente una franja no disponible en el catálogo de disponibilidad).

**Why this priority**: Protege la integridad de los datos y la experiencia del usuario: sin este control, dos pacientes podrían reservar la misma franja con el mismo médico.

**Independent Test**: Puede probarse cargando una franja como ocupada en el sistema y luego intentando reservarla; el sistema debe rechazarla y mantener el flujo activo para elegir otra opción.

**Acceptance Scenarios**:

1. **Given** que existe una franja horaria ya ocupada para un médico en una fecha dada,
   **When** el paciente intenta seleccionar y confirmar esa franja,
   **Then** el sistema responde indicando que la franja no está disponible y presenta al paciente las franjas alternativas disponibles para ese médico y fecha (o días próximos si no hay más franjas ese día).

2. **Given** que dos pacientes intentan reservar simultáneamente la misma franja (condición de carrera),
   **When** ambas solicitudes llegan al sistema al mismo tiempo,
   **Then** solo una de las reservas es aceptada y confirmada; la otra recibe respuesta de franja no disponible sin que se genere duplicidad en la base de datos.

---

### Edge Cases

- ¿Qué sucede si el proveedor de WhatsApp no está disponible al momento de enviar la confirmación? El sistema DEBE registrar la cita igualmente y reintentar el envío de notificación de forma asíncrona.
- ¿Qué sucede si el médico no tiene franjas disponibles para los próximos N días? El sistema DEBE informarlo claramente y sugerir otros médicos de la misma especialidad.
- ¿Qué sucede si el paciente cierra el navegador sin confirmar? La franja permanece `DISPONIBLE` para cualquier otro paciente porque el sistema no la retiene hasta recibir `POST /v1/citas`. No se requiere liberación: nunca se bloqueó.
- ¿Qué sucede si el paciente ya tiene una cita activa con el mismo médico en la misma semana? El sistema DEBE advertir al paciente, pero permitirle continuar (no bloquear).

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir a un paciente autenticado consultar la disponibilidad de franjas horarias por médico, especialidad y rango de fechas, en cualquier momento del día (24/7).
- **FR-002**: El sistema DEBE mostrar únicamente las franjas horarias cuyo estado sea `DISPONIBLE` en la selección de cita.
- **FR-003**: El paciente DEBE poder reservar una cita seleccionando médico, fecha y franja horaria, y confirmar la operación en un único flujo.
- **FR-004**: El sistema DEBE registrar la cita con estado `CONFIRMADA` de forma atómica, garantizando que no se generen duplicados ante concurrencia.
- **FR-005**: El sistema DEBE enviar una notificación de confirmación por WhatsApp al paciente al completarse la reserva, incluyendo: nombre del médico, especialidad, fecha, hora y ubicación/consultorio.
- **FR-006**: Si el envío de WhatsApp falla, el sistema DEBE registrar el error y reintentar el envío de forma asíncrona sin afectar el registro de la cita.
- **FR-007**: El sistema DEBE rechazar la confirmación de una franja que haya pasado a estado `OCUPADA` entre el momento de selección y el de confirmación, e informar al paciente con las alternativas disponibles.
- **FR-008**: El sistema NO retiene (bloquea) una franja horaria entre el momento en que el paciente la visualiza y el momento en que confirma la reserva. La franja permanece en estado `DISPONIBLE` hasta que `POST /v1/citas` sea procesado exitosamente de forma atómica. Si dos solicitudes llegan simultáneamente, el mecanismo de bloqueo optimista garantiza que solo una tenga éxito (cubierto por FR-004).
- **FR-009**: El paciente DEBE poder consultar el listado de sus citas con estado `CONFIRMADA`, `CANCELADA` o `COMPLETADA`.

### Key Entities

- **Paciente**: Persona que solicita la cita. Atributos clave: identificador único, nombre completo, número de WhatsApp registrado.
- **Médico**: Profesional de salud. Atributos clave: identificador único, nombre, especialidad, consultorio.
- **Franja horaria**: Unidad de disponibilidad de un médico. Atributos: fecha, hora inicio, hora fin, estado (`DISPONIBLE`, `OCUPADA`), médico asociado.
- **Cita**: Reserva confirmada. Atributos: identificador único, paciente, médico, franja horaria, estado (`CONFIRMADA`, `CANCELADA`, `COMPLETADA`), timestamp de creación.
- **Notificación**: Registro del envío WhatsApp. Atributos: cita asociada, canal (`WHATSAPP`), estado (`PENDIENTE`, `ENVIADA`, `FALLIDA`), timestamp.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El paciente puede completar el flujo de reserva (buscar médico → seleccionar franja → confirmar) en menos de 3 minutos desde el inicio hasta recibir el WhatsApp de confirmación.
- **SC-002**: El sistema garantiza cero duplicidades de reserva en la misma franja horaria bajo condiciones de concurrencia (tasa de duplicados = 0%).
- **SC-003**: El 100% de las reservas exitosas disparan una notificación WhatsApp; en caso de fallo del proveedor, el reintento exitoso se completa en menos de 10 minutos.
- **SC-004**: Las franjas mostradas como disponibles reflejan el estado real del sistema con un desfase máximo de 10 segundos.
- **SC-005**: Al menos el 90% de los pacientes que inician el flujo de reserva lo completan sin necesidad de soporte externo (tasa de abandono < 10%).

---

## Assumptions

- El paciente ya está registrado y autenticado en el sistema antes de iniciar el flujo de reserva (la autenticación es un servicio previo fuera del alcance de esta historia).
- El proveedor de mensajería WhatsApp Business API ya está contratado y configurado; esta historia consume dicho servicio como dependencia externa.
- Las franjas horarias de cada médico son precargadas/configuradas por el área administrativa; esta historia no gestiona la creación de la agenda médica.
- El sistema opera sobre zona horaria UTC-5 (Colombia) como zona por defecto; no se requiere soporte multi-zona en esta historia.
- La infraestructura de base de datos soporta transacciones con aislamiento `SERIALIZABLE` o equivalente para garantizar la unicidad de la reserva de franja.
- El número de WhatsApp del paciente se obtiene de su perfil ya registrado; no se solicita en el flujo de reserva.
