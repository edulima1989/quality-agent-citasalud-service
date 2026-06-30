package org.ups.citasalud.domain.port.in;

import org.ups.citasalud.application.dto.DisponibilidadQuery;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;

import java.util.List;

public interface ConsultarDisponibilidadUseCase {

    record ResultadoDisponibilidad(Medico medico, List<FranjaHoraria> franjas) {}

    List<ResultadoDisponibilidad> consultar(DisponibilidadQuery query);
}
