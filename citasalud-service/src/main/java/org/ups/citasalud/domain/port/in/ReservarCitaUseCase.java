package org.ups.citasalud.domain.port.in;

import org.ups.citasalud.application.dto.ReservaCitaCommand;
import org.ups.citasalud.domain.model.Cita;

public interface ReservarCitaUseCase {

    record ReservaResult(Cita cita, String advertencia) {}

    ReservaResult reservar(ReservaCitaCommand command);
}
