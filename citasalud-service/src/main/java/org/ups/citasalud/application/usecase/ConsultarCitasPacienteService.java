package org.ups.citasalud.application.usecase;

import org.springframework.stereotype.Service;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.port.in.ConsultarCitasPacienteUseCase;
import org.ups.citasalud.domain.port.out.CitaRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultarCitasPacienteService implements ConsultarCitasPacienteUseCase {

    private final CitaRepository citaRepository;

    public ConsultarCitasPacienteService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public List<Cita> consultar(UUID pacienteId, EstadoCita estadoFiltro) {
        return citaRepository.findByPacienteId(pacienteId, estadoFiltro);
    }
}
