package org.ups.citasalud.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.ups.citasalud.application.dto.ReservaCitaCommand;
import org.ups.citasalud.domain.exception.FranjaNoDisponibleException;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoCita;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Notificacion;
import org.ups.citasalud.domain.port.in.ReservarCitaUseCase;
import org.ups.citasalud.domain.port.out.CitaRepository;
import org.ups.citasalud.domain.port.out.FranjaHorariaRepository;
import org.ups.citasalud.domain.port.out.NotificacionPort;
import org.ups.citasalud.domain.port.out.NotificacionRepository;

import java.util.List;

@Service
public class ReservarCitaService implements ReservarCitaUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReservarCitaService.class);
    private static final int ALTERNATIVAS_DIAS_RANGO = 7;

    private final FranjaHorariaRepository franjaRepository;
    private final CitaRepository citaRepository;
    private final NotificacionRepository notificacionRepository;
    private final NotificacionPort notificacionPort;

    public ReservarCitaService(FranjaHorariaRepository franjaRepository,
                               CitaRepository citaRepository,
                               NotificacionRepository notificacionRepository,
                               NotificacionPort notificacionPort) {
        this.franjaRepository = franjaRepository;
        this.citaRepository = citaRepository;
        this.notificacionRepository = notificacionRepository;
        this.notificacionPort = notificacionPort;
    }

    @Override
    @Transactional
    public ReservaResult reservar(ReservaCitaCommand command) {
        // 1. Cargar la franja
        FranjaHoraria franja = franjaRepository.findById(command.franjaHorariaId())
            .orElseThrow(() -> new FranjaNoDisponibleException(
                "Franja no encontrada: " + command.franjaHorariaId(), List.of()
            ));

        try {
            // 2. Validar y confirmar (lanza si estado != DISPONIBLE)
            franja.confirmar();

            // 3. Persistir la franja actualizada (activa @Version)
            franjaRepository.save(franja);

            // 4. Crear la cita
            Cita cita = Cita.crear(command.pacienteId(), franja.getMedicoId(), franja.getId());
            Cita citaGuardada = citaRepository.save(cita);
            log.info("Cita confirmada: id={} paciente={} franja={}",
                citaGuardada.getId(), citaGuardada.getPacienteId(), franja.getId());

            // EC-004: Advertir si el paciente ya tiene otra cita activa con el mismo médico
            String advertencia = detectarCitaActivaMismoMedico(command.pacienteId(),
                franja.getMedicoId(), citaGuardada.getId());

            // 5. Registrar notificación en estado PENDIENTE
            Notificacion notificacion = Notificacion.pendiente(citaGuardada.getId());
            notificacionRepository.save(notificacion);

            // 6. Disparar envío WhatsApp después del commit (evita race condition con FK)
            final Cita citaFinal = citaGuardada;
            final Notificacion notificacionFinal = notificacion;
            final FranjaHoraria franjaFinal = franja;
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        dispararNotificacion(citaFinal, notificacionFinal, franjaFinal);
                    }
                });
            } else {
                dispararNotificacion(citaFinal, notificacionFinal, franjaFinal);
            }

            return new ReservaResult(citaGuardada, advertencia);

        } catch (FranjaNoDisponibleException ex) {
            throw ex;
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException ex) {
            log.warn("Conflicto de concurrencia reservando franja={}: {}", command.franjaHorariaId(), ex.getMessage());
            List<FranjaHoraria> alternativas = franjaRepository.findAlternativasDisponibles(
                franja.getMedicoId(), franja.getFecha(), ALTERNATIVAS_DIAS_RANGO
            );
            throw new FranjaNoDisponibleException(
                "La franja ya fue reservada por otro paciente", alternativas
            );
        }
    }

    private String detectarCitaActivaMismoMedico(java.util.UUID pacienteId, java.util.UUID medicoId,
                                                   java.util.UUID idNuevaCita) {
        boolean tieneOtra = citaRepository
            .findByPacienteIdAndMedicoId(pacienteId, medicoId, EstadoCita.CONFIRMADA)
            .stream()
            .anyMatch(c -> !c.getId().equals(idNuevaCita));

        if (tieneOtra) {
            log.warn("EC-004: paciente={} ya tiene cita activa con medico={}", pacienteId, medicoId);
            return "Ya tiene una cita activa con este médico. Verifique su agenda antes de confirmar.";
        }
        return null;
    }

    private void dispararNotificacion(Cita cita, Notificacion notificacion, FranjaHoraria franja) {
        try {
            // FR-006: excepción aquí no debe afectar la cita ya registrada
            notificacionPort.enviar(cita, notificacion, franja);
        } catch (Exception e) {
            log.warn("Error al iniciar notificación WhatsApp para cita={}: {}", cita.getId(), e.getMessage());
        }
    }
}
