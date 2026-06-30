package org.ups.citasalud.domain.model;

import org.ups.citasalud.domain.exception.FranjaNoDisponibleException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class FranjaHoraria {

    private final UUID id;
    private final UUID medicoId;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private EstadoFranja estado;
    private final Long version;

    public FranjaHoraria(UUID id, UUID medicoId, LocalDate fecha, LocalTime horaInicio,
                         LocalTime horaFin, EstadoFranja estado, Long version) {
        this.id = id;
        this.medicoId = medicoId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.version = version;
    }

    public void confirmar() {
        if (this.estado != EstadoFranja.DISPONIBLE) {
            throw new FranjaNoDisponibleException(
                "La franja " + this.id + " no está disponible (estado: " + this.estado + ")",
                List.of()
            );
        }
        this.estado = EstadoFranja.OCUPADA;
    }

    public UUID getId() { return id; }
    public UUID getMedicoId() { return medicoId; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public EstadoFranja getEstado() { return estado; }
    public Long getVersion() { return version; }
}
