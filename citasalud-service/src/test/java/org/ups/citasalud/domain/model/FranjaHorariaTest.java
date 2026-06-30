package org.ups.citasalud.domain.model;

import org.junit.jupiter.api.Test;
import org.ups.citasalud.domain.exception.FranjaNoDisponibleException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FranjaHorariaTest {

    private FranjaHoraria buildFranja(EstadoFranja estado) {
        return new FranjaHoraria(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now().plusDays(1),
            LocalTime.of(9, 0),
            LocalTime.of(9, 30),
            estado,
            0L
        );
    }

    @Test
    void confirmar_cuandoDisponible_cambiaEstadoAOcupada() {
        FranjaHoraria franja = buildFranja(EstadoFranja.DISPONIBLE);

        franja.confirmar();

        assertEquals(EstadoFranja.OCUPADA, franja.getEstado());
    }

    @Test
    void confirmar_cuandoOcupada_lanzaFranjaNoDisponibleException() {
        FranjaHoraria franja = buildFranja(EstadoFranja.OCUPADA);

        assertThrows(FranjaNoDisponibleException.class, franja::confirmar);
    }

    @Test
    void confirmar_cuandoOcupada_mensajeContieneId() {
        FranjaHoraria franja = buildFranja(EstadoFranja.OCUPADA);

        FranjaNoDisponibleException ex = assertThrows(
            FranjaNoDisponibleException.class, franja::confirmar
        );

        assertEquals(0, ex.getAlternativas().size());
    }
}
