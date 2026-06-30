package org.ups.citasalud.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.interfaces.rest.dto.FranjaDisponibleInfoDto;
import org.ups.citasalud.interfaces.rest.dto.FranjaInfoDto;

@Component
public class FranjaHorariaMapper {

    public FranjaDisponibleInfoDto toDisponibleDto(FranjaHoraria f) {
        return new FranjaDisponibleInfoDto(f.getId(), f.getFecha(), f.getHoraInicio(), f.getHoraFin());
    }

    public FranjaInfoDto toInfoDto(FranjaHoraria f) {
        return new FranjaInfoDto(f.getFecha(), f.getHoraInicio(), f.getHoraFin());
    }
}
