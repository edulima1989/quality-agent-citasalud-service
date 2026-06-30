package org.ups.citasalud.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.port.out.FranjaHorariaRepository;
import org.ups.citasalud.infrastructure.persistence.entity.FranjaHorariaEntity;
import org.ups.citasalud.infrastructure.persistence.jpa.FranjaHorariaJpaRepository;
import org.ups.citasalud.infrastructure.persistence.jpa.MedicoJpaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FranjaHorariaRepositoryAdapter implements FranjaHorariaRepository {

    private final FranjaHorariaJpaRepository jpaRepository;
    private final MedicoJpaRepository medicoJpaRepository;

    public FranjaHorariaRepositoryAdapter(FranjaHorariaJpaRepository jpaRepository,
                                          MedicoJpaRepository medicoJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.medicoJpaRepository = medicoJpaRepository;
    }

    @Override
    public Optional<FranjaHoraria> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<FranjaHoraria> findDisponibles(UUID medicoId, String especialidad,
                                               LocalDate fechaDesde, LocalDate fechaHasta) {
        if (medicoId != null) {
            return jpaRepository.findDisponiblesByMedicoIdAndFechaRange(medicoId, fechaDesde, fechaHasta)
                .stream().map(this::toDomain).toList();
        }
        return jpaRepository.findDisponiblesByEspecialidadAndFechaRange(especialidad, fechaDesde, fechaHasta)
            .stream().map(this::toDomain).toList();
    }

    @Override
    public List<FranjaHoraria> findAlternativasDisponibles(UUID medicoId, LocalDate fecha, int diasRango) {
        LocalDate desde = fecha.minusDays(diasRango);
        LocalDate hasta = fecha.plusDays(diasRango);
        return jpaRepository.findAlternativasDisponibles(medicoId, desde, hasta)
            .stream().map(this::toDomain).toList();
    }

    @Override
    public FranjaHoraria save(FranjaHoraria franja) {
        FranjaHorariaEntity entity = toEntity(franja);
        return toDomain(jpaRepository.save(entity));
    }

    private FranjaHoraria toDomain(FranjaHorariaEntity e) {
        return new FranjaHoraria(e.getId(), e.getMedicoId(), e.getFecha(),
            e.getHoraInicio(), e.getHoraFin(), e.getEstado(), e.getVersion());
    }

    private FranjaHorariaEntity toEntity(FranjaHoraria f) {
        FranjaHorariaEntity e = new FranjaHorariaEntity();
        e.setId(f.getId());
        e.setMedicoId(f.getMedicoId());
        e.setFecha(f.getFecha());
        e.setHoraInicio(f.getHoraInicio());
        e.setHoraFin(f.getHoraFin());
        e.setEstado(f.getEstado());
        e.setVersion(f.getVersion());
        return e;
    }
}
