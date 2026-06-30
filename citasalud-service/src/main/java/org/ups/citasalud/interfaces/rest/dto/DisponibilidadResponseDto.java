package org.ups.citasalud.interfaces.rest.dto;

import java.util.List;

public record DisponibilidadResponseDto(MedicoInfoDto medico, List<FranjaDisponibleInfoDto> franjas) {}
