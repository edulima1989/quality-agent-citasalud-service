package org.ups.citasalud.infrastructure.notification;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.ups.citasalud.domain.model.Cita;
import org.ups.citasalud.domain.model.EstadoFranja;
import org.ups.citasalud.domain.model.EstadoNotificacion;
import org.ups.citasalud.domain.model.FranjaHoraria;
import org.ups.citasalud.domain.model.Notificacion;
import org.ups.citasalud.domain.port.out.NotificacionRepository;
import org.ups.citasalud.infrastructure.persistence.entity.MedicoEntity;
import org.ups.citasalud.infrastructure.persistence.jpa.MedicoJpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppNotificacionAdapterTest {

    @Mock NotificacionRepository notificacionRepository;
    @Mock MedicoJpaRepository medicoJpaRepository;

    WhatsAppNotificacionAdapter adapter;

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        // stub mode = true para testear construirMensaje sin llamada real
        adapter = new WhatsAppNotificacionAdapter(notificacionRepository, medicoJpaRepository, true);

        Logger logger = (Logger) LoggerFactory.getLogger(WhatsAppNotificacionAdapter.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(WhatsAppNotificacionAdapter.class);
        logger.detachAppender(logAppender);
    }

    private MedicoEntity medicoEntity(UUID medicoId) {
        MedicoEntity m = new MedicoEntity();
        m.setId(medicoId);
        m.setNombre("Dr. García López");
        m.setEspecialidad("Cardiología");
        m.setConsultorio("C-204");
        return m;
    }

    @Test
    void enviar_stubMode_mensajeContieneNombreMedicoEspecialidadFechaHoraConsultorio() {
        // FR-005: el mensaje debe incluir médico, especialidad, fecha, hora, consultorio
        UUID medicoId = UUID.randomUUID();
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), medicoId,
            LocalDate.of(2026, 7, 15), LocalTime.of(10, 30), LocalTime.of(11, 0),
            EstadoFranja.DISPONIBLE, 0L);
        Cita cita = Cita.crear(UUID.randomUUID(), medicoId, franja.getId());
        Notificacion notificacion = Notificacion.pendiente(cita.getId());

        when(medicoJpaRepository.findById(medicoId)).thenReturn(Optional.of(medicoEntity(medicoId)));
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Llamada directa (sin proxy @Async ni @Retryable) para testear lógica del mensaje
        adapter.enviar(cita, notificacion, franja);

        // Verificar que el log del stub contiene todos los campos requeridos por spec FR-005
        String mensajeLog = logAppender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .filter(m -> m.startsWith("WhatsApp stub:"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No se encontró el log 'WhatsApp stub:'"));

        assertAll("FR-005: el mensaje WhatsApp debe incluir todos los campos requeridos",
            () -> assertTrue(mensajeLog.contains("Dr. García López"),
                "Debe incluir el nombre del médico"),
            () -> assertTrue(mensajeLog.contains("Cardiología"),
                "Debe incluir la especialidad del médico"),
            () -> assertTrue(mensajeLog.contains("C-204"),
                "Debe incluir el consultorio/ubicación"),
            () -> assertTrue(mensajeLog.contains("2026-07-15"),
                "Debe incluir la fecha de la cita"),
            () -> assertTrue(mensajeLog.contains("10:30"),
                "Debe incluir la hora de inicio")
        );
    }

    @Test
    void enviar_exitoso_guardaNotificacionEnviada() {
        UUID medicoId = UUID.randomUUID();
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), medicoId,
            LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
            EstadoFranja.DISPONIBLE, 0L);
        Cita cita = Cita.crear(UUID.randomUUID(), medicoId, franja.getId());
        Notificacion notificacion = Notificacion.pendiente(cita.getId());

        when(medicoJpaRepository.findById(medicoId)).thenReturn(Optional.of(medicoEntity(medicoId)));
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adapter.enviar(cita, notificacion, franja);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());
        assertEquals(EstadoNotificacion.ENVIADA, captor.getValue().getEstado());
    }

    @Test
    void enviar_fallaEnvio_guardaNotificacionFallidaYPropagaExcepcion() {
        // FR-006 / EC-001: cuando el envío falla, la notificación queda FALLIDA y se persiste
        UUID medicoId = UUID.randomUUID();
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), medicoId,
            LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
            EstadoFranja.DISPONIBLE, 0L);
        Cita cita = Cita.crear(UUID.randomUUID(), medicoId, franja.getId());
        Notificacion notificacion = Notificacion.pendiente(cita.getId());

        // Simula fallo: medicoJpaRepository lanza excepción → construirMensaje falla → catch → marcarFallida
        when(medicoJpaRepository.findById(any()))
            .thenThrow(new RuntimeException("DB timeout"));
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // La excepción se propaga (para que @Retryable pueda reintentarla)
        assertThrows(RuntimeException.class, () -> adapter.enviar(cita, notificacion, franja));

        // Verificar que la notificación fue guardada con estado FALLIDA
        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());
        assertAll("FR-006: notificación debe quedar registrada como FALLIDA",
            () -> assertEquals(EstadoNotificacion.FALLIDA, captor.getValue().getEstado()),
            () -> assertNotNull(captor.getValue().getError(), "El error debe estar registrado"),
            () -> assertTrue(captor.getValue().getError().contains("DB timeout"))
        );
    }
}
