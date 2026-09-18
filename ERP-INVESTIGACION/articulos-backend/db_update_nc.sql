-- Script para insertar las series de Nota de Crédito (07) para los puntos de venta existentes

-- Insertar serie para Facturas (FC01) si no existe en los puntos
INSERT INTO puntos_documento (punto, id_tipo_documento, serie, modulo, estado, activo, ultimo_numero, numero_actual)
SELECT p.punto, '07', 'FC01', 'VENTA', 'V', true, 0, 1
FROM puntos p
WHERE NOT EXISTS (
    SELECT 1 FROM puntos_documento pd 
    WHERE pd.punto = p.punto AND pd.id_tipo_documento = '07' AND pd.serie = 'FC01'
);

-- Insertar serie para Boletas (BC01) si no existe en los puntos
INSERT INTO puntos_documento (punto, id_tipo_documento, serie, modulo, estado, activo, ultimo_numero, numero_actual)
SELECT p.punto, '07', 'BC01', 'VENTA', 'V', true, 0, 1
FROM puntos p
WHERE NOT EXISTS (
    SELECT 1 FROM puntos_documento pd 
    WHERE pd.punto = p.punto AND pd.id_tipo_documento = '07' AND pd.serie = 'BC01'
);
