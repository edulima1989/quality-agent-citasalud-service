package org.ups.citasalud.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.ups.citasalud.application.dto.ReservaCitaCommand;
import org.ups.citasalud.domain.exception.FranjaNoDisponibleException;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase;
import org.ups.citasalud.domain.port.out.CitaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ConcurrenciaReservaIT {

    @Autowired
    ReservarCitaUseCase reservarUseCase;

    @Autowired
    CitaRepository citaRepository;

    // Esta franja existe en data.sql como DISPONIBLE (bb000005 no usada por otros tests)
    private static final UUID FRANJA_ID = UUID.fromString("bb000005-0000-0000-0000-000000000005");
    // Pacientes únicos para este test (no usados en Cucumber ni CitaRepositoryAdapterIT)
    private static final UUID PACIENTE_1 = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
    private static final UUID PACIENTE_2 = UUID.fromString("b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2");

    @Test
    void dosReservasConcurrentes_soloUnaEsExitosa() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Boolean> tarea1 = () -> {
            try {
                reservarUseCase.reservar(new ReservaCitaCommand(FRANJA_ID, PACIENTE_1));
                return true;
            } catch (FranjaNoDisponibleException e) {
                return false;
            }
        };

        Callable<Boolean> tarea2 = () -> {
            try {
                reservarUseCase.reservar(new ReservaCitaCommand(FRANJA_ID, PACIENTE_2));
                return true;
            } catch (FranjaNoDisponibleException e) {
                return false;
            }
        };

        List<Future<Boolean>> futures = executor.invokeAll(List.of(tarea1, tarea2));
        executor.shutdown();

        long exitosas = futures.stream().filter(f -> {
            try { return f.get(); }
            catch (InterruptedException | ExecutionException e) { return false; }
        }).count();

        assertEquals(1, exitosas, "Solo una reserva debe ser exitosa");

        // Verificar que entre ambos pacientes solo hay 1 cita (la franja no fue duplicada)
        List<Cita> citasPaciente1 = citaRepository.findByPacienteId(PACIENTE_1, null);
        List<Cita> citasPaciente2 = citaRepository.findByPacienteId(PACIENTE_2, null);
        int total = citasPaciente1.size() + citasPaciente2.size();
        assertEquals(1, total, "Solo debe existir una cita para esta franja (total ambos pacientes)");
    }
}
