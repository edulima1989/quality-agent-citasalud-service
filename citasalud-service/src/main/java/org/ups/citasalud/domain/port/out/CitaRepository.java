package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;

import java.util.List;
import java.util.UUID;

public interface CitaRepository {

    Cita save(Cita cita);

    List<Cita> findByPacienteId(UUID pacienteId, EstadoCita estadoFiltro);

    List<Cita> findByPacienteIdAndMedicoId(UUID pacienteId, UUID medicoId, EstadoCita estado);
}
