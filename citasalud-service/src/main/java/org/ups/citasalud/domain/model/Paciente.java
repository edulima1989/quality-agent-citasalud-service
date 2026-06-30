package org.ups.citasalud.domain.model;

import java.util.UUID;

public record Paciente(
    UUID id,
    String nombreCompleto,
    String telefonoWhatsApp
) {}
