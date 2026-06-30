package org.ups.citasalud.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ups.citasalud.domain.model.CanalNotificacion;
import org.ups.citasalud.domain.model.EstadoNotificacion;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
public class NotificacionEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "cita_id", nullable = false, columnDefinition = "UUID")
    private UUID citaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalNotificacion canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNotificacion estado;

    @Column(nullable = false)
    private int intentos;

    @Column(name = "ultimo_intento_en")
    private Instant ultimoIntentoEn;

    @Column(name = "enviado_en")
    private Instant enviadoEn;

    @Column(length = 500)
    private String error;
}
