@us2
Feature: Intento de reserva en franja ocupada

  Background:
    Given el sistema está operativo con franjas disponibles

  @us2-1
  Scenario: Paciente intenta reservar franja ya ocupada
    Given el paciente autenticado con id "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
    And la franja "aa000003-0000-0000-0000-000000000003" ya fue reservada por otro paciente
    When el paciente intenta reservar la franja "aa000003-0000-0000-0000-000000000003"
    Then la respuesta es 409
    And el cuerpo contiene el código "FRANJA_NO_DISPONIBLE"
    And el cuerpo incluye franjas alternativas
