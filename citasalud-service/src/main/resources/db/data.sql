-- Idempotent seed data for dev/test (H2 MERGE INTO syntax)
-- Médico 1
MERGE INTO medico (id, nombre, especialidad, consultorio)
    KEY(id) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Dr. Carlos Pérez',
    'Medicina General',
    'Consultorio 3 - Piso 2'
);

-- Médico 2
MERGE INTO medico (id, nombre, especialidad, consultorio)
    KEY(id) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'Dra. Ana González',
    'Cardiología',
    'Consultorio 7 - Piso 3'
);

-- Franjas horarias Médico 1 (próximos días)
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('aa000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', DATEADD('DAY', 1, CURRENT_DATE), '09:00', '09:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('aa000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', DATEADD('DAY', 1, CURRENT_DATE), '10:00', '10:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('aa000003-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', DATEADD('DAY', 2, CURRENT_DATE), '09:00', '09:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('aa000004-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', DATEADD('DAY', 2, CURRENT_DATE), '11:00', '11:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('aa000005-0000-0000-0000-000000000005', '11111111-1111-1111-1111-111111111111', DATEADD('DAY', 3, CURRENT_DATE), '14:00', '14:30', 'DISPONIBLE', 0);

-- Franjas horarias Médico 2
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('bb000001-0000-0000-0000-000000000001', '22222222-2222-2222-2222-222222222222', DATEADD('DAY', 1, CURRENT_DATE), '08:00', '08:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('bb000002-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', DATEADD('DAY', 1, CURRENT_DATE), '09:00', '09:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('bb000003-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222', DATEADD('DAY', 2, CURRENT_DATE), '10:00', '10:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('bb000004-0000-0000-0000-000000000004', '22222222-2222-2222-2222-222222222222', DATEADD('DAY', 3, CURRENT_DATE), '08:00', '08:30', 'DISPONIBLE', 0);
MERGE INTO franja_horaria (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
    KEY(id) VALUES ('bb000005-0000-0000-0000-000000000005', '22222222-2222-2222-2222-222222222222', DATEADD('DAY', 4, CURRENT_DATE), '15:00', '15:30', 'DISPONIBLE', 0);
