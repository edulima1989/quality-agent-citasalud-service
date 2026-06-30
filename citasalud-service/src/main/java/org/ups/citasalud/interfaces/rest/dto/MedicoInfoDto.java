package org.ups.citasalud.interfaces.rest.dto;

import java.util.UUID;

public record MedicoInfoDto(UUID id, String nombre, String especialidad, String consultorio) {}
