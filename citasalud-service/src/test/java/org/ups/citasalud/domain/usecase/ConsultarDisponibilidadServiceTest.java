package org.ups.citasalud.domain.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.citasalud.application.dto.DisponibilidadQuery;
import org.ups.citasalud.application.usecase.ConsultarDisponibilidadService;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.port.in.ConsultarDisponibilidadUseCase.ResultadoDisponibilidad;
import org.ups.citasalud.domain.port.out.FranjaHorariaRepository;
import org.ups.citasalud.infrastructure.persistence.entity.MedicoEntity;
import org.ups.citasalud.infrastructure.persistence.jpa.MedicoJpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadServiceTest {

    @Mock FranjaHorariaRepository franjaRepository;
    @Mock MedicoJpaRepository medicoJpaRepository;

    @InjectMocks ConsultarDisponibilidadService service;

    private MedicoEntity medicoEntity(UUID id, String especialidad) {
        MedicoEntity m = new MedicoEntity();
        m.setId(id);
        m.setNombre("Dr. Test");
        m.setEspecialidad(especialidad);
        m.setConsultorio("C1");
        return m;
    }

    @Test
    void consultar_retornaFranjasDisponibles() {
        UUID medicoId = UUID.randomUUID();
        LocalDate desde = LocalDate.now().plusDays(1);
        LocalDate hasta = LocalDate.now().plusDays(7);

        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), medicoId,
            desde, LocalTime.of(9, 0), LocalTime.of(9, 30), EstadoFranja.DISPONIBLE, 0L);

        when(franjaRepository.findDisponibles(medicoId, null, desde, hasta)).thenReturn(List.of(franja));
        when(medicoJpaRepository.findById(medicoId)).thenReturn(Optional.of(medicoEntity(medicoId, "General")));

        List<ResultadoDisponibilidad> resultado = service.consultar(
            new DisponibilidadQuery(medicoId, null, desde, hasta));

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).franjas().size());
    }

    @Test
    void consultar_medicoSinFranjas_retornaAlternativasMismaEspecialidad() {
        // EC-003: cuando el médico solicitado no tiene franjas, debe sugerir otros de la misma especialidad
        UUID medicoSolicitado = UUID.randomUUID();
        UUID medicoAlternativo = UUID.randomUUID();
        String especialidad = "Cardiología";
        LocalDate desde = LocalDate.now().plusDays(1);
        LocalDate hasta = LocalDate.now().plusDays(7);

        FranjaHoraria franjaAlternativa = new FranjaHoraria(UUID.randomUUID(), medicoAlternativo,
            desde, LocalTime.of(10, 0), LocalTime.of(10, 30), EstadoFranja.DISPONIBLE, 0L);

        // Médico solicitado: sin franjas
        when(franjaRepository.findDisponibles(medicoSolicitado, null, desde, hasta))
            .thenReturn(List.of());
        // Lookup de la especialidad del médico solicitado
        when(medicoJpaRepository.findById(medicoSolicitado))
            .thenReturn(Optional.of(medicoEntity(medicoSolicitado, especialidad)));
        // Búsqueda de alternativas por especialidad
        when(franjaRepository.findDisponibles(isNull(), eq(especialidad), eq(desde), eq(hasta)))
            .thenReturn(List.of(franjaAlternativa));
        when(medicoJpaRepository.findById(medicoAlternativo))
            .thenReturn(Optional.of(medicoEntity(medicoAlternativo, especialidad)));

        List<ResultadoDisponibilidad> resultado = service.consultar(
            new DisponibilidadQuery(medicoSolicitado, null, desde, hasta));

        assertFalse(resultado.isEmpty(), "Debe retornar médicos alternativos de la misma especialidad");
        assertEquals(medicoAlternativo, resultado.get(0).medico().id());
    }

    @Test
    void consultar_sinFranjasSinAlternativas_retornaListaVacia() {
        UUID medicoId = UUID.randomUUID();
        LocalDate desde = LocalDate.now().plusDays(1);
        LocalDate hasta = LocalDate.now().plusDays(7);

        when(franjaRepository.findDisponibles(medicoId, null, desde, hasta)).thenReturn(List.of());
        when(medicoJpaRepository.findById(medicoId))
            .thenReturn(Optional.of(medicoEntity(medicoId, "Neurología")));
        when(franjaRepository.findDisponibles(isNull(), eq("Neurología"), eq(desde), eq(hasta)))
            .thenReturn(List.of());

        List<ResultadoDisponibilidad> resultado = service.consultar(
            new DisponibilidadQuery(medicoId, null, desde, hasta));

        assertEquals(0, resultado.size());
    }
}
