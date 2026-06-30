package org.ups.citasalud.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.citasalud.application.dto.ReservaCitaCommand;
import org.ups.citasalud.application.usecase.ReservarCitaService;
import org.ups.citasalud.domain.exception.FranjaNoDisponibleException;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Notificacion;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase.ReservaResult;
import org.ups.citasalud.domain.port.out.CitaRepository;
import org.ups.citasalud.domain.port.out.FranjaHorariaRepository;
import org.ups.citasalud.domain.port.out.NotificacionPort;
import org.ups.citasalud.domain.port.out.NotificacionRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservarCitaServiceTest {

    @Mock FranjaHorariaRepository franjaRepository;
    @Mock CitaRepository citaRepository;
    @Mock NotificacionRepository notificacionRepository;
    @Mock NotificacionPort notificacionPort;

    @InjectMocks ReservarCitaService service;

    private FranjaHoraria buildFranja(EstadoFranja estado) {
        return new FranjaHoraria(
            UUID.randomUUID(), UUID.randomUUID(),
            LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30), estado, 0L
        );
    }

    @Test
    void reservar_franjaDisponible_creaYConfirmaLaCita() {
        FranjaHoraria franja = buildFranja(EstadoFranja.DISPONIBLE);
        UUID pacienteId = UUID.randomUUID();
        ReservaCitaCommand command = new ReservaCitaCommand(franja.getId(), pacienteId);

        when(franjaRepository.findById(franja.getId())).thenReturn(Optional.of(franja));
        when(franjaRepository.save(any())).thenReturn(franja);
        when(citaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(citaRepository.findByPacienteIdAndMedicoId(any(), any(), any())).thenReturn(List.of());
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaResult resultado = service.reservar(command);

        assertEquals(EstadoCita.CONFIRMADA, resultado.cita().getEstado());
        assertEquals(pacienteId, resultado.cita().getPacienteId());
        assertNull(resultado.advertencia());
        verify(notificacionPort).enviar(any(Cita.class), any(Notificacion.class), any(FranjaHoraria.class));
    }

    @Test
    void reservar_franjaOcupada_lanzaFranjaNoDisponibleException() {
        FranjaHoraria franja = buildFranja(EstadoFranja.OCUPADA);
        ReservaCitaCommand command = new ReservaCitaCommand(franja.getId(), UUID.randomUUID());

        when(franjaRepository.findById(franja.getId())).thenReturn(Optional.of(franja));

        assertThrows(FranjaNoDisponibleException.class, () -> service.reservar(command));
        verify(citaRepository, never()).save(any());
    }

    @Test
    void reservar_fallaWhatsApp_citaQuedaConfirmadaSinLanzarExcepcion() {
        // FR-006: el fallo del envío WhatsApp NO debe afectar el registro de la cita
        FranjaHoraria franja = buildFranja(EstadoFranja.DISPONIBLE);
        UUID pacienteId = UUID.randomUUID();
        ReservaCitaCommand command = new ReservaCitaCommand(franja.getId(), pacienteId);

        when(franjaRepository.findById(franja.getId())).thenReturn(Optional.of(franja));
        when(franjaRepository.save(any())).thenReturn(franja);
        when(citaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(citaRepository.findByPacienteIdAndMedicoId(any(), any(), any())).thenReturn(List.of());
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // Simula fallo real del adaptador WhatsApp
        doThrow(new RuntimeException("WhatsApp timeout"))
            .when(notificacionPort).enviar(any(), any(), any());

        // No debe lanzar excepción — la cita se registra igual
        ReservaResult resultado = service.reservar(command);

        assertEquals(EstadoCita.CONFIRMADA, resultado.cita().getEstado());
        verify(notificacionPort).enviar(any(Cita.class), any(Notificacion.class), any(FranjaHoraria.class));
    }

    @Test
    void reservar_franjaNoEncontrada_lanzaFranjaNoDisponibleException() {
        UUID franjaId = UUID.randomUUID();
        ReservaCitaCommand command = new ReservaCitaCommand(franjaId, UUID.randomUUID());

        when(franjaRepository.findById(franjaId)).thenReturn(Optional.empty());

        assertThrows(FranjaNoDisponibleException.class, () -> service.reservar(command));
    }

    @Test
    void reservar_pacienteConCitaActivaMismoMedico_retornaAdvertencia() {
        // EC-004: paciente ya tiene otra cita activa con el mismo médico
        FranjaHoraria franja = buildFranja(EstadoFranja.DISPONIBLE);
        UUID pacienteId = UUID.randomUUID();
        ReservaCitaCommand command = new ReservaCitaCommand(franja.getId(), pacienteId);

        Cita citaExistente = Cita.crear(pacienteId, franja.getMedicoId(), UUID.randomUUID());

        when(franjaRepository.findById(franja.getId())).thenReturn(Optional.of(franja));
        when(franjaRepository.save(any())).thenReturn(franja);
        when(citaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(citaRepository.findByPacienteIdAndMedicoId(eq(pacienteId), eq(franja.getMedicoId()), eq(EstadoCita.CONFIRMADA)))
            .thenReturn(List.of(citaExistente));
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaResult resultado = service.reservar(command);

        assertEquals(EstadoCita.CONFIRMADA, resultado.cita().getEstado());
        assertNotNull(resultado.advertencia(), "Debe incluir advertencia EC-004");
        assertTrue(resultado.advertencia().contains("cita activa"));
    }
}
