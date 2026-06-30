package org.ups.citasalud.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Cita {

    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final UUID franjaHorariaId;
    private EstadoCita estado;
    private final Instant creadoEn;

    private Cita(UUID id, UUID pacienteId, UUID medicoId, UUID franjaHorariaId,
                 EstadoCita estado, Instant creadoEn) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.franjaHorariaId = franjaHorariaId;
        this.estado = estado;
        this.creadoEn = creadoEn;
    }

    public static Cita crear(UUID pacienteId, UUID medicoId, UUID franjaHorariaId) {
        return new Cita(
            UUID.randomUUID(),
            pacienteId,
            medicoId,
            franjaHorariaId,
            EstadoCita.CONFIRMADA,
            Instant.now()
        );
    }

    public static Cita reconstituir(UUID id, UUID pacienteId, UUID medicoId,
                                    UUID franjaHorariaId, EstadoCita estado, Instant creadoEn) {
        return new Cita(id, pacienteId, medicoId, franjaHorariaId, estado, creadoEn);
    }

    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getMedicoId() { return medicoId; }
    public UUID getFranjaHorariaId() { return franjaHorariaId; }
    public EstadoCita getEstado() { return estado; }
    public Instant getCreadoEn() { return creadoEn; }
}
