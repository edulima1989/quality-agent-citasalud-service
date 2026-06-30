package org.ups.citasalud.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.port.out.CitaRepository;
import org.ups.citasalud.domain.port.out.FranjaHorariaRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class CitaRepositoryAdapterIT {

    @Autowired
    CitaRepository citaRepository;

    @Autowired
    FranjaHorariaRepository franjaRepository;

    // franja aa000004 is not used by Cucumber tests (which use aa000001-aa000003) or ConcurrenciaIT (bb000005)
    private static final UUID MEDICO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FRANJA_ID = UUID.fromString("aa000004-0000-0000-0000-000000000004");
    // Paciente único para este test
    private static final UUID PACIENTE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Test
    void guardarCita_persiste_y_recupera() {
        Cita cita = Cita.crear(PACIENTE_ID, MEDICO_ID, FRANJA_ID);
        Cita guardada = citaRepository.save(cita);

        assertNotNull(guardada.getId());
        assertEquals(EstadoCita.CONFIRMADA, guardada.getEstado());
        assertEquals(PACIENTE_ID, guardada.getPacienteId());
    }

    @Test
    void findByPacienteId_retornaCitasDelPaciente() {
        Cita cita = Cita.crear(PACIENTE_ID, MEDICO_ID, FRANJA_ID);
        citaRepository.save(cita);

        List<Cita> citas = citaRepository.findByPacienteId(PACIENTE_ID, null);

        assertEquals(1, citas.size());
        assertEquals(PACIENTE_ID, citas.get(0).getPacienteId());
    }

    @Test
    void findByPacienteId_conFiltroEstado_retorlasSolasConfirmadas() {
        Cita cita = Cita.crear(PACIENTE_ID, MEDICO_ID, FRANJA_ID);
        citaRepository.save(cita);

        List<Cita> confirmadas = citaRepository.findByPacienteId(PACIENTE_ID, EstadoCita.CONFIRMADA);
        List<Cita> canceladas = citaRepository.findByPacienteId(PACIENTE_ID, EstadoCita.CANCELADA);

        assertEquals(1, confirmadas.size());
        assertEquals(0, canceladas.size());
    }
}
