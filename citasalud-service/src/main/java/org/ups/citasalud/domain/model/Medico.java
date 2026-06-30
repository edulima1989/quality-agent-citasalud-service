package org.ups.citasalud.domain.model;

import java.util.UUID;

public record Medico(
    UUID id,
    String nombre,
    String especialidad,
    String consultorio
) {}
