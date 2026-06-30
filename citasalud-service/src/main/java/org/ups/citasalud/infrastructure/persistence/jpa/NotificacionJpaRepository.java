package org.ups.citasalud.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.citasalud.infrastructure.persistence.entity.NotificacionEntity;

import java.util.UUID;

public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, UUID> {
}
