package org.ups.citasalud.interfaces.rest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ups.citasalud.application.dto.DisponibilidadQuery;
import org.ups.citasalud.domain.port.in.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.domain.port.in.ConsultarDisponibilidadUseCase.ResultadoDisponibilidad;
import org.ups.citasalud.interfaces.rest.dto.DisponibilidadResponseDto;
import org.ups.citasalud.interfaces.rest.dto.MedicoInfoDto;
import org.ups.citasalud.interfaces.rest.mapper.FranjaHorariaMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class DisponibilidadController {

    private final ConsultarDisponibilidadUseCase useCase;
    private final FranjaHorariaMapper franjaMapper;

    public DisponibilidadController(ConsultarDisponibilidadUseCase useCase,
                                    FranjaHorariaMapper franjaMapper) {
        this.useCase = useCase;
        this.franjaMapper = franjaMapper;
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadResponseDto>> consultarDisponibilidad(
            @RequestParam(required = false) UUID medicoId,
            @RequestParam(required = false) String especialidadId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

        LocalDate desde = fechaDesde != null ? fechaDesde : LocalDate.now();
        LocalDate hasta = fechaHasta != null ? fechaHasta : LocalDate.now().plusDays(7);
        DisponibilidadQuery query = new DisponibilidadQuery(medicoId, especialidadId, desde, hasta);
        List<ResultadoDisponibilidad> resultados = useCase.consultar(query);

        List<DisponibilidadResponseDto> response = resultados.stream()
            .map(r -> {
                MedicoInfoDto medicoDto = new MedicoInfoDto(
                    r.medico().id(), r.medico().nombre(),
                    r.medico().especialidad(), r.medico().consultorio()
                );
                return new DisponibilidadResponseDto(medicoDto,
                    r.franjas().stream().map(franjaMapper::toDisponibleDto).toList());
            })
            .toList();

        return ResponseEntity.ok(response);
    }
}
