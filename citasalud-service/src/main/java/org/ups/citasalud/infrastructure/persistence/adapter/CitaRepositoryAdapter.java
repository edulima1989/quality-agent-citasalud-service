package org.ups.citasalud.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.port.out.CitaRepository;
import org.ups.citasalud.infrastructure.persistence.entity.CitaEntity;
import org.ups.citasalud.infrastructure.persistence.jpa.CitaJpaRepository;

import java.util.List;
import java.util.UUID;

@Component
public class CitaRepositoryAdapter implements CitaRepository {

    private final CitaJpaRepository jpaRepository;

    public CitaRepositoryAdapter(CitaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cita save(Cita cita) {
        CitaEntity entity = toEntity(cita);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Cita> findByPacienteId(UUID pacienteId, EstadoCita estadoFiltro) {
        if (estadoFiltro != null) {
            return jpaRepository.findByPacienteIdAndEstado(pacienteId, estadoFiltro)
                .stream().map(this::toDomain).toList();
        }
        return jpaRepository.findByPacienteId(pacienteId)
            .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Cita> findByPacienteIdAndMedicoId(UUID pacienteId, UUID medicoId, EstadoCita estado) {
        return jpaRepository.findByPacienteIdAndMedicoIdAndEstado(pacienteId, medicoId, estado)
            .stream().map(this::toDomain).toList();
    }

    private Cita toDomain(CitaEntity e) {
        return Cita.reconstituir(e.getId(), e.getPacienteId(), e.getMedicoId(),
            e.getFranjaHorariaId(), e.getEstado(), e.getCreadoEn());
    }

    private CitaEntity toEntity(Cita c) {
        CitaEntity e = new CitaEntity();
        e.setId(c.getId());
        e.setPacienteId(c.getPacienteId());
        e.setMedicoId(c.getMedicoId());
        e.setFranjaHorariaId(c.getFranjaHorariaId());
        e.setEstado(c.getEstado());
        e.setCreadoEn(c.getCreadoEn());
        return e;
    }
}
