package org.ups.citasalud.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionTest {

    @Test
    void pendiente_creaNotificacionConEstadoPendiente() {
        UUID citaId = UUID.randomUUID();
        Notificacion n = Notificacion.pendiente(citaId);

        assertEquals(EstadoNotificacion.PENDIENTE, n.getEstado());
        assertEquals(citaId, n.getCitaId());
        assertEquals(0, n.getIntentos());
        assertNotNull(n.getId());
    }

    @Test
    void marcarEnviada_actualizaEstadoYTimestamp() {
        Notificacion n = Notificacion.pendiente(UUID.randomUUID());
        n.marcarEnviada();

        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
        assertNotNull(n.getEnviadoEn());
    }

    @Test
    void marcarFallida_actualizaEstadoYError() {
        Notificacion n = Notificacion.pendiente(UUID.randomUUID());
        n.marcarFallida("timeout");

        assertEquals(EstadoNotificacion.FALLIDA, n.getEstado());
        assertEquals("timeout", n.getError());
        assertEquals(1, n.getIntentos());
    }
}
