package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Notificacion;

public interface NotificacionPort {

    void enviar(Cita cita, Notificacion notificacion, FranjaHoraria franja);
}
