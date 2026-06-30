package org.ups.citasalud.domain.port.in;

import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;

import java.util.List;
import java.util.UUID;

public interface ConsultarCitasPacienteUseCase {

    List<Cita> consultar(UUID pacienteId, EstadoCita estadoFiltro);
}
