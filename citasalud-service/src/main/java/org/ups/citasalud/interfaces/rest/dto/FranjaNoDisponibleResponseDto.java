package org.ups.citasalud.interfaces.rest.dto;

import java.util.List;

public record FranjaNoDisponibleResponseDto(
    String codigo,
    String mensaje,
    List<FranjaDisponibleInfoDto> alternativas
) {}
