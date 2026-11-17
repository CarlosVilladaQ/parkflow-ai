-- 1. Tabla de configuración general del parqueadero
CREATE TABLE configuracion_parqueadero (
    id SERIAL PRIMARY KEY,
    nombre_parqueadero VARCHAR(100) NOT NULL,
    cupos_totales_carros INT NOT NULL,
    cupos_totales_motos INT NOT NULL
);

-- 2. Tabla de tarifas por tipo de vehículo
CREATE TABLE tarifas (
    id SERIAL PRIMARY KEY,
    tipo_vehiculo VARCHAR(20) NOT NULL UNIQUE,
    valor_hora NUMERIC(10, 2) NOT NULL,
    valor_fraccion NUMERIC(10, 2) NOT NULL,
    valor_dia NUMERIC(10, 2) NOT NULL,
    activo BOOLEAN DEFAULT TRUE
);

-- 3. Tabla de tickets (registros reales de entrada y salida)
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(10) NOT NULL,
    tipo_vehiculo VARCHAR(20) NOT NULL,
    hora_entrada TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hora_salida TIMESTAMP WITH TIME ZONE,
    total_pagado NUMERIC(10, 2),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
);

-- 4. Tabla de reservas (gestionadas por el agente de IA o recepción)
CREATE TABLE reservas (
    id BIGSERIAL PRIMARY KEY,
    placa VARCHAR(10) NOT NULL,
    tipo_vehiculo VARCHAR(20) NOT NULL,
    cliente_telefono VARCHAR(20) NOT NULL,
    fecha_hora_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA'
);

-- Datos iniciales de prueba
INSERT INTO configuracion_parqueadero (nombre_parqueadero, cupos_totales_carros, cupos_totales_motos)
VALUES ('Parqueadero Central ParkFlow', 30, 50);

INSERT INTO tarifas (tipo_vehiculo, valor_hora, valor_fraccion, valor_dia, activo)
VALUES 
    ('CARRO', 4000.00, 1500.00, 25000.00, TRUE),
    ('MOTO', 2000.00, 800.00, 12000.00, TRUE);
