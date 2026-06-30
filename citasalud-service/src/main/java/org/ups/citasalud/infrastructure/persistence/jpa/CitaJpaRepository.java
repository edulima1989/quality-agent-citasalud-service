package org.ups.citasalud.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.infrastructure.persistence.entity.CitaEntity;

import java.util.List;
import java.util.UUID;

public interface CitaJpaRepository extends JpaRepository<CitaEntity, UUID> {

    List<CitaEntity> findByPacienteId(UUID pacienteId);

    List<CitaEntity> findByPacienteIdAndEstado(UUID pacienteId, EstadoCita estado);

    List<CitaEntity> findByPacienteIdAndMedicoIdAndEstado(UUID pacienteId, UUID medicoId, EstadoCita estado);
}
