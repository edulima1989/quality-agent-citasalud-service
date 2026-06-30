package org.ups.citasalud.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ups.citasalud.domain.model.EstadoFranja;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
    name = "franja_horaria",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_franja_medico_fecha_hora",
        columnNames = {"medico_id", "fecha", "hora_inicio"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class FranjaHorariaEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "medico_id", nullable = false, columnDefinition = "UUID")
    private UUID medicoId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoFranja estado;

    @Version
    @Column(nullable = false)
    private Long version;
}
