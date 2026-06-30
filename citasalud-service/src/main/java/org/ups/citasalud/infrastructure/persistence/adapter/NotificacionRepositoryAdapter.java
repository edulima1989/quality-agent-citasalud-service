package org.ups.citasalud.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.ups.citasalud.domain.model.Notificacion;
import org.ups.citasalud.domain.port.out.NotificacionRepository;
import org.ups.citasalud.infrastructure.persistence.entity.NotificacionEntity;
import org.ups.citasalud.infrastructure.persistence.jpa.NotificacionJpaRepository;

@Component
public class NotificacionRepositoryAdapter implements NotificacionRepository {

    private final NotificacionJpaRepository jpaRepository;

    public NotificacionRepositoryAdapter(NotificacionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notificacion save(Notificacion notificacion) {
        NotificacionEntity entity = toEntity(notificacion);
        return toDomain(jpaRepository.save(entity));
    }

    private Notificacion toDomain(NotificacionEntity e) {
        return Notificacion.reconstituir(e.getId(), e.getCitaId(), e.getCanal(),
            e.getEstado(), e.getIntentos(), e.getUltimoIntentoEn(), e.getEnviadoEn(), e.getError());
    }

    private NotificacionEntity toEntity(Notificacion n) {
        NotificacionEntity e = new NotificacionEntity();
        e.setId(n.getId());
        e.setCitaId(n.getCitaId());
        e.setCanal(n.getCanal());
        e.setEstado(n.getEstado());
        e.setIntentos(n.getIntentos());
        e.setUltimoIntentoEn(n.getUltimoIntentoEn());
        e.setEnviadoEn(n.getEnviadoEn());
        e.setError(n.getError());
        return e;
    }
}
