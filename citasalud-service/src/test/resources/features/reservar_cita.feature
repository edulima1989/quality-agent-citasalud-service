@us1
Feature: Reservar cita en línea 24/7

  Background:
    Given el sistema está operativo con franjas disponibles

  @us1-1
  Scenario: Paciente reserva cita exitosamente
    Given el paciente autenticado con id "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
    And existe la franja horaria disponible "aa000001-0000-0000-0000-000000000001"
    When el paciente confirma la reserva de la franja "aa000001-0000-0000-0000-000000000001"
    Then la respuesta es 201
    And la cita queda registrada con estado "CONFIRMADA"
    And la notificación WhatsApp fue disparada

  @us1-2
  Scenario: Paciente consulta su listado de citas
    Given el paciente autenticado con id "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
    And el paciente tiene una cita reservada
    When el paciente consulta su listado de citas
    Then la respuesta es 200
    And el listado contiene al menos 1 cita
