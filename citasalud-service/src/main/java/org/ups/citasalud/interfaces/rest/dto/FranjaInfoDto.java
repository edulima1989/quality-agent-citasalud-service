package org.ups.citasalud.interfaces.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record FranjaInfoDto(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {}
