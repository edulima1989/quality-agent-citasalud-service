package org.ups.citasalud.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Notificacion;
import org.ups.citasalud.domain.port.out.NotificacionPort;
import org.ups.citasalud.domain.port.out.NotificacionRepository;
import org.ups.citasalud.infrastructure.persistence.jpa.MedicoJpaRepository;

@Component
public class WhatsAppNotificacionAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificacionAdapter.class);

    private final NotificacionRepository notificacionRepository;
    private final MedicoJpaRepository medicoJpaRepository;
    private final boolean stubMode;

    public WhatsAppNotificacionAdapter(NotificacionRepository notificacionRepository,
                                       MedicoJpaRepository medicoJpaRepository,
                                       @Value("${app.whatsapp.stub:true}") boolean stubMode) {
        this.notificacionRepository = notificacionRepository;
        this.medicoJpaRepository = medicoJpaRepository;
        this.stubMode = stubMode;
    }

    @Override
    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delayExpression = "${app.whatsapp.retry.delay:2000}"))
    public void enviar(Cita cita, Notificacion notificacion, FranjaHoraria franja) {
        try {
            if (stubMode) {
                String mensajeResumen = construirMensaje(cita, franja);
                log.info("WhatsApp stub: {}", mensajeResumen);
            } else {
                enviarMensajeReal(cita, franja);
            }
            notificacion.marcarEnviada();
        } catch (Exception ex) {
            log.error("Error enviando WhatsApp para cita={}: {}", cita.getId(), ex.getMessage());
            notificacion.marcarFallida(ex.getMessage());
            throw ex;
        } finally {
            notificacionRepository.save(notificacion);
        }
    }

    private String construirMensaje(Cita cita, FranjaHoraria franja) {
        String medicoInfo = medicoJpaRepository.findById(franja.getMedicoId())
            .map(m -> m.getNombre() + " (" + m.getEspecialidad() + ") - " + m.getConsultorio())
            .orElse("médico id=" + franja.getMedicoId());

        return "Confirmación de cita para paciente=%s | Médico: %s | Fecha: %s %s-%s"
            .formatted(cita.getPacienteId(), medicoInfo, franja.getFecha(),
                franja.getHoraInicio(), franja.getHoraFin());
    }

    private void enviarMensajeReal(Cita cita, FranjaHoraria franja) {
        // Integración real con WhatsApp Business API (fuera del alcance de esta historia)
        throw new UnsupportedOperationException("WhatsApp real integration not configured");
    }
}
