package org.ups.citasalud.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Notificacion {

    private final UUID id;
    private final UUID citaId;
    private final CanalNotificacion canal;
    private EstadoNotificacion estado;
    private int intentos;
    private Instant ultimoIntentoEn;
    private Instant enviadoEn;
    private String error;

    private Notificacion(UUID id, UUID citaId, CanalNotificacion canal,
                         EstadoNotificacion estado, int intentos,
                         Instant ultimoIntentoEn, Instant enviadoEn, String error) {
        this.id = id;
        this.citaId = citaId;
        this.canal = canal;
        this.estado = estado;
        this.intentos = intentos;
        this.ultimoIntentoEn = ultimoIntentoEn;
        this.enviadoEn = enviadoEn;
        this.error = error;
    }

    public static Notificacion pendiente(UUID citaId) {
        return new Notificacion(
            UUID.randomUUID(),
            citaId,
            CanalNotificacion.WHATSAPP,
            EstadoNotificacion.PENDIENTE,
            0,
            null,
            null,
            null
        );
    }

    public static Notificacion reconstituir(UUID id, UUID citaId, CanalNotificacion canal,
                                            EstadoNotificacion estado, int intentos,
                                            Instant ultimoIntentoEn, Instant enviadoEn, String error) {
        return new Notificacion(id, citaId, canal, estado, intentos, ultimoIntentoEn, enviadoEn, error);
    }

    public void marcarEnviada() {
        this.estado = EstadoNotificacion.ENVIADA;
        this.enviadoEn = Instant.now();
        this.ultimoIntentoEn = this.enviadoEn;
        this.intentos++;
    }

    public void marcarFallida(String mensajeError) {
        this.estado = EstadoNotificacion.FALLIDA;
        this.error = mensajeError;
        this.ultimoIntentoEn = Instant.now();
        this.intentos++;
    }

    public void incrementarIntento() {
        this.intentos++;
        this.ultimoIntentoEn = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCitaId() { return citaId; }
    public CanalNotificacion getCanal() { return canal; }
    public EstadoNotificacion getEstado() { return estado; }
    public int getIntentos() { return intentos; }
    public Instant getUltimoIntentoEn() { return ultimoIntentoEn; }
    public Instant getEnviadoEn() { return enviadoEn; }
    public String getError() { return error; }
}
