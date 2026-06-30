package org.ups.citasalud.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.interfaces.rest.dto.CitaResponseDto;
import org.ups.citasalud.interfaces.rest.dto.FranjaInfoDto;
import org.ups.citasalud.interfaces.rest.dto.MedicoInfoDto;

@Component
public class CitaMapper {

    public CitaResponseDto toDto(Cita cita, Medico medico, FranjaHoraria franja) {
        return toDto(cita, medico, franja, null);
    }

    public CitaResponseDto toDto(Cita cita, Medico medico, FranjaHoraria franja, String advertencia) {
        MedicoInfoDto medicoDto = new MedicoInfoDto(
            medico.id(), medico.nombre(), medico.especialidad(), medico.consultorio()
        );
        FranjaInfoDto franjaDto = new FranjaInfoDto(
            franja.getFecha(), franja.getHoraInicio(), franja.getHoraFin()
        );
        return new CitaResponseDto(
            cita.getId(),
            cita.getPacienteId(),
            medicoDto,
            franjaDto,
            cita.getEstado().name(),
            cita.getCreadoEn(),
            advertencia
        );
    }
}
