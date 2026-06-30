package ec.citasalud.agenda;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona las reservas de citas.
 *
 * FR-003/FR-006: no se permite doble reserva en la misma franja para el mismo profesional.
 *
 * OJO (hueco deliberado para la clase): la verificación de "franja ocupada" y la
 * inserción NO son atómicas y la lista no está sincronizada. Entre el chequeo y el
 * add(), dos hebras concurrentes pueden colar dos reservas para la misma franja.
 * El requisito FR-006 (concurrencia) NO está cubierto por pruebas.
 */
@Service
public class AgendaService {

    private final List<Reserva> reservas = new ArrayList<>();

    public Reserva reservar(String profesionalId, LocalDateTime franja, String pacienteId) {
        boolean ocupada = reservas.stream()
                .anyMatch(r -> r.profesionalId().equals(profesionalId)
                        && r.franja().equals(franja));
        if (ocupada) {
            throw new FranjaOcupadaException(profesionalId, franja);
        }
        // Carrera: entre el check de arriba y este add() cabe otra hebra.
        Reserva nueva = new Reserva(profesionalId, franja, pacienteId);
        reservas.add(nueva);
        return nueva;
    }

    public List<Reserva> listar() {
        return List.copyOf(reservas);
    }
}
