package org.ups.citasalud.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.citasalud.infrastructure.persistence.entity.MedicoEntity;

import java.util.List;
import java.util.UUID;

public interface MedicoJpaRepository extends JpaRepository<MedicoEntity, UUID> {

    List<MedicoEntity> findByEspecialidad(String especialidad);
}
