package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.FranjaHoraria;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FranjaHorariaRepository {

    Optional<FranjaHoraria> findById(UUID id);

    List<FranjaHoraria> findDisponibles(UUID medicoId, String especialidad,
                                        LocalDate fechaDesde, LocalDate fechaHasta);

    List<FranjaHoraria> findAlternativasDisponibles(UUID medicoId, LocalDate fecha, int diasRango);

    FranjaHoraria save(FranjaHoraria franja);
}
