package org.ups.citasalud.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ups.citasalud.domain.model.EstadoCita;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "cita",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_cita_franja",
        columnNames = {"franja_horaria_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class CitaEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "paciente_id", nullable = false, columnDefinition = "UUID")
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false, columnDefinition = "UUID")
    private UUID medicoId;

    @Column(name = "franja_horaria_id", nullable = false, columnDefinition = "UUID")
    private UUID franjaHorariaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCita estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;
}
