-- Este archivo se ejecuta DESPUÉS de que Hibernate (ddl-auto=create) genera las tablas.
-- Aquí colocamos únicamente las estructuras complejas que Hibernate NO puede generar por sí solo.

-- ==========================================
-- 1. ÍNDICES PARCIALES ÚNICOS (SOFT DELETE)
-- ==========================================

CREATE UNIQUE INDEX IF NOT EXISTS idx_almacen_codigo_unico ON almacen (codigo) WHERE estado != 3;
CREATE UNIQUE INDEX IF NOT EXISTS idx_sucursal_nombre_unico ON sucursal (nombre) WHERE estado != 3;
CREATE UNIQUE INDEX IF NOT EXISTS idx_catalogo_codigo_unico ON catalogo (codigo) WHERE estado != 3;
CREATE UNIQUE INDEX IF NOT EXISTS idx_tipo_atencion_codigo_unico ON tipos_atencion (codigo) WHERE estado != 3;
CREATE UNIQUE INDEX IF NOT EXISTS idx_unidad_medida_nombre_unico ON unidad_medida (nombre) WHERE estado != 3;
CREATE UNIQUE INDEX IF NOT EXISTS idx_clinica_ruc_unico ON datos_clinica (ruc) WHERE estado != 3;
CREATE UNIQUE INDEX IF NOT EXISTS idx_datos_medico_cmp_unico ON datos_medico (nro_cmp) WHERE estado != 3;
CREATE UNIQUE INDEX IF NOT EXISTS idx_proveedor_doc_unico ON proveedores (num_doc_ident) WHERE estado != 0;
CREATE UNIQUE INDEX IF NOT EXISTS idx_role_name_unico ON roles (name) WHERE active = true;
CREATE UNIQUE INDEX IF NOT EXISTS idx_permission_name_unico ON permissions (name) WHERE active = true;
CREATE UNIQUE INDEX IF NOT EXISTS idx_tipo_doc_codigo_unico ON tipo_documento (codigo) WHERE activo = true;
