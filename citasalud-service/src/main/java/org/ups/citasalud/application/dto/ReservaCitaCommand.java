package org.ups.citasalud.application.dto;

import java.util.UUID;

public record ReservaCitaCommand(
    UUID franjaHorariaId,
    UUID pacienteId
) {
    public ReservaCitaCommand {
        if (franjaHorariaId == null) throw new IllegalArgumentException("franjaHorariaId es requerido");
        if (pacienteId == null) throw new IllegalArgumentException("pacienteId es requerido");
    }
}
