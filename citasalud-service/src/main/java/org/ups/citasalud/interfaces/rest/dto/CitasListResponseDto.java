package org.ups.citasalud.interfaces.rest.dto;

import java.util.List;

public record CitasListResponseDto(List<CitaResponseDto> citas, int total) {}
