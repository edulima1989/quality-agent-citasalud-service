package org.ups.citasalud.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ups.citasalud.infrastructure.persistence.entity.FranjaHorariaEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FranjaHorariaJpaRepository extends JpaRepository<FranjaHorariaEntity, UUID> {

    @Query("""
        SELECT f FROM FranjaHorariaEntity f
        WHERE (:medicoId IS NULL OR f.medicoId = :medicoId)
          AND f.fecha BETWEEN :fechaDesde AND :fechaHasta
          AND f.estado = 'DISPONIBLE'
        ORDER BY f.fecha, f.horaInicio
    """)
    List<FranjaHorariaEntity> findDisponiblesByMedicoIdAndFechaRange(
        @Param("medicoId") UUID medicoId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("""
        SELECT f FROM FranjaHorariaEntity f
        WHERE f.medicoId = :medicoId
          AND f.fecha BETWEEN :fechaDesde AND :fechaHasta
          AND f.estado = 'DISPONIBLE'
        ORDER BY f.fecha, f.horaInicio
    """)
    List<FranjaHorariaEntity> findAlternativasDisponibles(
        @Param("medicoId") UUID medicoId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    @Query("""
        SELECT f FROM FranjaHorariaEntity f
        JOIN MedicoEntity m ON m.id = f.medicoId
        WHERE m.especialidad = :especialidad
          AND f.fecha BETWEEN :fechaDesde AND :fechaHasta
          AND f.estado = 'DISPONIBLE'
        ORDER BY f.fecha, f.horaInicio
    """)
    List<FranjaHorariaEntity> findDisponiblesByEspecialidadAndFechaRange(
        @Param("especialidad") String especialidad,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );
}
