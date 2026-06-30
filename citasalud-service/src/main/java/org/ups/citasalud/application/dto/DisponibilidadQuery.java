package org.ups.citasalud.application.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DisponibilidadQuery(
    UUID medicoId,
    String especialidadId,
    LocalDate fechaDesde,
    LocalDate fechaHasta
) {
    public DisponibilidadQuery {
        if (fechaDesde == null || fechaHasta == null) {
            throw new IllegalArgumentException("fechaDesde y fechaHasta son requeridas");
        }
        if (fechaHasta.isBefore(fechaDesde)) {
            throw new IllegalArgumentException("fechaHasta debe ser >= fechaDesde");
        }
        if (fechaDesde.plusDays(30).isBefore(fechaHasta)) {
            throw new IllegalArgumentException("El rango de fechas no puede superar 30 días");
        }
        if (medicoId == null && (especialidadId == null || especialidadId.isBlank())) {
            throw new IllegalArgumentException("Debe especificar medicoId o especialidadId");
        }
    }
}
