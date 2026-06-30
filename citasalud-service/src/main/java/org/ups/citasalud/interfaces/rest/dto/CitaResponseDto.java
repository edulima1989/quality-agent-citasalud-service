package org.ups.citasalud.interfaces.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record CitaResponseDto(
    UUID id,
    UUID pacienteId,
    MedicoInfoDto medico,
    FranjaInfoDto franja,
    String estado,
    Instant creadoEn,
    String advertencia
) {}
