package org.ups.citasalud.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ups.citasalud.application.dto.ReservaCitaCommand;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Medico;
import org.ups.citasalud.domain.port.in.ConsultarCitasPacienteUseCase;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase.ReservaResult;
import org.ups.citasalud.domain.port.out.FranjaHorariaRepository;
import org.ups.citasalud.infrastructure.persistence.jpa.MedicoJpaRepository;
import org.ups.citasalud.interfaces.rest.dto.CitaResponseDto;
import org.ups.citasalud.interfaces.rest.dto.CitasListResponseDto;
import org.ups.citasalud.interfaces.rest.dto.FranjaInfoDto;
import org.ups.citasalud.interfaces.rest.dto.MedicoInfoDto;
import org.ups.citasalud.interfaces.rest.dto.ReservaCitaRequestDto;
import org.ups.citasalud.interfaces.rest.mapper.CitaMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/citas")
public class CitasController {

    private final ReservarCitaUseCase reservarUseCase;
    private final ConsultarCitasPacienteUseCase consultarUseCase;
    private final FranjaHorariaRepository franjaRepository;
    private final MedicoJpaRepository medicoJpaRepository;
    private final CitaMapper citaMapper;

    public CitasController(ReservarCitaUseCase reservarUseCase,
                           ConsultarCitasPacienteUseCase consultarUseCase,
                           FranjaHorariaRepository franjaRepository,
                           MedicoJpaRepository medicoJpaRepository,
                           CitaMapper citaMapper) {
        this.reservarUseCase = reservarUseCase;
        this.consultarUseCase = consultarUseCase;
        this.franjaRepository = franjaRepository;
        this.medicoJpaRepository = medicoJpaRepository;
        this.citaMapper = citaMapper;
    }

    @PostMapping
    public ResponseEntity<CitaResponseDto> reservarCita(
            @RequestBody ReservaCitaRequestDto request,
            Authentication authentication) {

        UUID pacienteId = UUID.fromString(authentication.getName());
        ReservaCitaCommand command = new ReservaCitaCommand(request.franjaHorariaId(), pacienteId);
        ReservaResult resultado = reservarUseCase.reservar(command);
        Cita cita = resultado.cita();

        FranjaHoraria franja = franjaRepository.findById(cita.getFranjaHorariaId())
            .orElseThrow();
        Medico medico = medicoJpaRepository.findById(cita.getMedicoId())
            .map(m -> new Medico(m.getId(), m.getNombre(), m.getEspecialidad(), m.getConsultorio()))
            .orElseThrow();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(citaMapper.toDto(cita, medico, franja, resultado.advertencia()));
    }

    @GetMapping
    public ResponseEntity<CitasListResponseDto> consultarCitas(
            @RequestParam(required = false) EstadoCita estado,
            Authentication authentication) {

        UUID pacienteId = UUID.fromString(authentication.getName());
        List<Cita> citas = consultarUseCase.consultar(pacienteId, estado);

        List<CitaResponseDto> dtos = citas.stream()
            .map(cita -> {
                FranjaHoraria franja = franjaRepository.findById(cita.getFranjaHorariaId()).orElseThrow();
                Medico medico = medicoJpaRepository.findById(cita.getMedicoId())
                    .map(m -> new Medico(m.getId(), m.getNombre(), m.getEspecialidad(), m.getConsultorio()))
                    .orElseThrow();
                return citaMapper.toDto(cita, medico, franja);
            })
            .toList();

        return ResponseEntity.ok(new CitasListResponseDto(dtos, dtos.size()));
    }
}
