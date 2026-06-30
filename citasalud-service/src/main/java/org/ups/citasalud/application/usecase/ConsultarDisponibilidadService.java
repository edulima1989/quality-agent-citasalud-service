package org.ups.citasalud.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ups.citasalud.application.dto.DisponibilidadQuery;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.port.in.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.domain.port.out.FranjaHorariaRepository;
import org.ups.citasalud.infrastructure.persistence.jpa.MedicoJpaRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarDisponibilidadService.class);

    private final FranjaHorariaRepository franjaRepository;
    private final MedicoJpaRepository medicoJpaRepository;

    public ConsultarDisponibilidadService(FranjaHorariaRepository franjaRepository,
                                          MedicoJpaRepository medicoJpaRepository) {
        this.franjaRepository = franjaRepository;
        this.medicoJpaRepository = medicoJpaRepository;
    }

    @Override
    public List<ResultadoDisponibilidad> consultar(DisponibilidadQuery query) {
        List<FranjaHoraria> franjas = franjaRepository.findDisponibles(
            query.medicoId(), query.especialidadId(), query.fechaDesde(), query.fechaHasta()
        );

        List<ResultadoDisponibilidad> result = agruparPorMedico(franjas);

        // EC-003: si no hay franjas para el médico solicitado, sugerir otros de la misma especialidad
        if (result.isEmpty() && query.medicoId() != null) {
            result = sugerirAlternativosDeEspecialidad(query);
        }

        return result;
    }

    private List<ResultadoDisponibilidad> sugerirAlternativosDeEspecialidad(DisponibilidadQuery query) {
        return medicoJpaRepository.findById(query.medicoId())
            .map(m -> {
                log.info("EC-003: medico={} sin franjas, buscando alternativos de especialidad={}",
                    query.medicoId(), m.getEspecialidad());
                List<FranjaHoraria> alternativas = franjaRepository.findDisponibles(
                    null, m.getEspecialidad(), query.fechaDesde(), query.fechaHasta()
                );
                // Excluir el médico original (ya verificamos que no tiene slots)
                List<FranjaHoraria> soloAlternativas = alternativas.stream()
                    .filter(f -> !f.getMedicoId().equals(query.medicoId()))
                    .toList();
                return agruparPorMedico(soloAlternativas);
            })
            .orElse(List.of());
    }

    private List<ResultadoDisponibilidad> agruparPorMedico(List<FranjaHoraria> franjas) {
        Map<UUID, List<FranjaHoraria>> byMedico = new LinkedHashMap<>();
        for (FranjaHoraria f : franjas) {
            byMedico.computeIfAbsent(f.getMedicoId(), k -> new ArrayList<>()).add(f);
        }

        List<ResultadoDisponibilidad> result = new ArrayList<>();
        for (Map.Entry<UUID, List<FranjaHoraria>> entry : byMedico.entrySet()) {
            medicoJpaRepository.findById(entry.getKey()).ifPresent(m -> {
                Medico medico = new Medico(m.getId(), m.getNombre(), m.getEspecialidad(), m.getConsultorio());
                result.add(new ResultadoDisponibilidad(medico, entry.getValue()));
            });
        }
        return result;
    }
}
