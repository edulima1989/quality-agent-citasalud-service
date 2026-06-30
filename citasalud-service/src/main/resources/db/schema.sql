CREATE TABLE IF NOT EXISTS medico (
    id          UUID         NOT NULL,
    nombre      VARCHAR(200) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    consultorio VARCHAR(100) NOT NULL,
    CONSTRAINT pk_medico PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS franja_horaria (
    id          UUID         NOT NULL,
    medico_id   UUID         NOT NULL,
    fecha       DATE         NOT NULL,
    hora_inicio TIME         NOT NULL,
    hora_fin    TIME         NOT NULL,
    estado      VARCHAR(30)  NOT NULL DEFAULT 'DISPONIBLE',
    version     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_franja_horaria PRIMARY KEY (id),
    CONSTRAINT fk_franja_medico FOREIGN KEY (medico_id) REFERENCES medico(id),
    CONSTRAINT uq_franja_medico_fecha_hora UNIQUE (medico_id, fecha, hora_inicio)
);

CREATE TABLE IF NOT EXISTS cita (
    id               UUID        NOT NULL,
    paciente_id      UUID        NOT NULL,
    medico_id        UUID        NOT NULL,
    franja_horaria_id UUID       NOT NULL,
    estado           VARCHAR(30) NOT NULL DEFAULT 'CONFIRMADA',
    creado_en        TIMESTAMP   NOT NULL,
    CONSTRAINT pk_cita PRIMARY KEY (id),
    CONSTRAINT fk_cita_medico   FOREIGN KEY (medico_id)        REFERENCES medico(id),
    CONSTRAINT fk_cita_franja   FOREIGN KEY (franja_horaria_id) REFERENCES franja_horaria(id),
    CONSTRAINT uq_cita_franja   UNIQUE (franja_horaria_id)
);

CREATE TABLE IF NOT EXISTS notificacion (
    id               UUID        NOT NULL,
    cita_id          UUID        NOT NULL,
    canal            VARCHAR(20) NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    intentos         INT         NOT NULL DEFAULT 0,
    ultimo_intento_en TIMESTAMP  NULL,
    enviado_en       TIMESTAMP   NULL,
    error            VARCHAR(500) NULL,
    CONSTRAINT pk_notificacion PRIMARY KEY (id),
    CONSTRAINT fk_notificacion_cita FOREIGN KEY (cita_id) REFERENCES cita(id)
);
