package org.ups.citasalud.domain.port.out;

import org.ups.citasalud.domain.model.Notificacion;

public interface NotificacionRepository {

    Notificacion save(Notificacion notificacion);
}
