package org.ups.citasalud.interfaces.rest.dto;

import java.time.Instant;

public record ErrorResponseDto(String codigo, String mensaje, Instant timestamp) {}
