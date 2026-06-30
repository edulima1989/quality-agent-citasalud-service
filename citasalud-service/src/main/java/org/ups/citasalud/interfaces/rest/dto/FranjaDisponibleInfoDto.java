package org.ups.citasalud.interfaces.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record FranjaDisponibleInfoDto(UUID id, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {}
