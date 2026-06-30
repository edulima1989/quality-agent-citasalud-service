package ec.citasalud.agenda;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de AgendaService.
 *
 * Cubre FR-001 (reserva libre), FR-002 (otra franja) y FR-003 (rechazo secuencial).
 *
 * FALTA, a propósito, la prueba de FR-006 (concurrencia): no existe
 * 'reservaFranjaOcupada_concurrente_rechaza'. Por eso ese criterio queda 'incumple'
 * y el gate bloquea — es justo lo que el auditor debe detectar.
 */
class AgendaServiceTest {

    private final AgendaService agenda = new AgendaService();
    private static final LocalDateTime FRANJA = LocalDateTime.of(2026, 7, 1, 9, 0);

    @Test
    void reservaFranjaLibre_ok() {
        Reserva r = agenda.reservar("prof-1", FRANJA, "pac-1");
        assertEquals("prof-1", r.profesionalId());
        assertEquals(FRANJA, r.franja());
        assertEquals(1, agenda.listar().size());
    }

    @Test
    void reservaFranjaOcupada_secuencial_rechaza() {
        agenda.reservar("prof-1", FRANJA, "pac-1");
        assertThrows(FranjaOcupadaException.class,
                () -> agenda.reservar("prof-1", FRANJA, "pac-2"));
        assertEquals(1, agenda.listar().size());
    }

    @Test
    void reservaOtraFranja_ok() {
        agenda.reservar("prof-1", FRANJA, "pac-1");
        Reserva otra = agenda.reservar("prof-1", FRANJA.plusHours(1), "pac-2");
        assertNotNull(otra);
        assertEquals(2, agenda.listar().size());
    }

    // NOTA: no hay prueba para el caso concurrente (dos hebras a la vez).
    // El requisito FR-006 NO está cubierto -> el gate debe bloquear.
}
