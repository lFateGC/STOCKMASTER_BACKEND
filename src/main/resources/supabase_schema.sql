-- ==============================================================================
-- STOCKMASTER - ESQUEMA DE BASE DE DATOS SUPABASE (POSTGRESQL)
-- ==============================================================================
-- Esquema relacional completo para inventario, ventas, compras, clientes,
-- devoluciones, trazabilidad de movimientos (Kardex) y auditoría de eventos.
-- ==============================================================================

-- 1. EXTENSIONES
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 2. TABLA: perfiles (Vinculada a auth.users de Supabase)
CREATE TABLE IF NOT EXISTS public.perfiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    nombre_completo TEXT NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'vendedor' CHECK (rol IN ('admin', 'vendedor')),
    correo TEXT UNIQUE NOT NULL,
    avatar_url TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo')),
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

COMMENT ON TABLE public.perfiles IS 'Perfiles de usuario y roles vinculados a auth.users de Supabase';

-- 3. TABLA: categorias
CREATE TABLE IF NOT EXISTS public.categorias (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 4. TABLA: proveedores
CREATE TABLE IF NOT EXISTS public.proveedores (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    telefono VARCHAR(255),
    correo VARCHAR(150),
    direccion VARCHAR(500),
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 5. TABLA: metodos_pago
CREATE TABLE IF NOT EXISTS public.metodos_pago (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- 6. TABLA: productos
CREATE TABLE IF NOT EXISTS public.productos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    categoria_id BIGINT NOT NULL REFERENCES public.categorias(id) ON DELETE RESTRICT,
    sku VARCHAR(50) UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    imagen_url TEXT,
    precio_compra NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (precio_compra >= 0),
    precio_venta NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (precio_venta >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo INTEGER NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo')),
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL,
    fecha_actualizacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 7. TABLA: clientes (Registro estricto con DNI 8 dígitos o RUC 11 dígitos, sin duplicados)
CREATE TABLE IF NOT EXISTS public.clientes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL DEFAULT 'DNI' CHECK (tipo_documento IN ('DNI', 'RUC')),
    numero_documento VARCHAR(20) UNIQUE NOT NULL,
    telefono VARCHAR(255),
    correo VARCHAR(150),
    direccion VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo')),
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 8. TABLA: ventas (Cabecera comercial)
CREATE TABLE IF NOT EXISTS public.ventas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_boleta VARCHAR(50) UNIQUE NOT NULL,
    vendedor_id UUID NOT NULL REFERENCES public.perfiles(id) ON DELETE RESTRICT,
    cliente VARCHAR(150) NOT NULL,
    cliente_id BIGINT REFERENCES public.clientes(id) ON DELETE SET NULL,
    metodo_pago_id BIGINT REFERENCES public.metodos_pago(id) ON DELETE SET NULL,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    igv NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (igv >= 0),
    descuento NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (descuento >= 0),
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'completada' CHECK (estado IN ('completada', 'anulada')),
    observaciones TEXT,
    fecha_venta TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 9. TABLA: detalle_ventas
CREATE TABLE IF NOT EXISTS public.detalle_ventas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES public.ventas(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0)
);

-- 10. TABLA: compras (Cabecera abastecimiento)
CREATE TABLE IF NOT EXISTS public.compras (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    proveedor_id BIGINT NOT NULL REFERENCES public.proveedores(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES public.perfiles(id) ON DELETE RESTRICT,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    igv NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (igv >= 0),
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    fecha_compra TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 11. TABLA: detalle_compras
CREATE TABLE IF NOT EXISTS public.detalle_compras (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES public.compras(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(12, 2) NOT NULL CHECK (costo_unitario >= 0),
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0)
);

-- 12. TABLA: movimientos_stock (Kardex integral: compras, ventas, devoluciones, ajustes)
CREATE TABLE IF NOT EXISTS public.movimientos_stock (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES public.perfiles(id) ON DELETE RESTRICT,
    tipo VARCHAR(50) NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad != 0),
    motivo TEXT,
    fecha_movimiento TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 13. TABLA: devoluciones (Gestión de notas de crédito y reversiones)
CREATE TABLE IF NOT EXISTS public.devoluciones (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_devolucion VARCHAR(50) UNIQUE NOT NULL,
    venta_id BIGINT NOT NULL REFERENCES public.ventas(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES public.perfiles(id) ON DELETE RESTRICT,
    cliente VARCHAR(150) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    destino_stock VARCHAR(50) NOT NULL DEFAULT 'reingreso' CHECK (destino_stock IN ('reingreso', 'merma')),
    metodo_reembolso VARCHAR(50) NOT NULL DEFAULT 'efectivo',
    monto_total NUMERIC(12, 2) NOT NULL CHECK (monto_total >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'completada' CHECK (estado IN ('completada', 'anulada')),
    observaciones TEXT,
    fecha_devolucion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 14. TABLA: detalle_devoluciones
CREATE TABLE IF NOT EXISTS public.detalle_devoluciones (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    devolucion_id BIGINT NOT NULL REFERENCES public.devoluciones(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0)
);

-- 15. TABLA: auditoria_logs (Registro de operaciones: CREAR, EDITAR, VER, ELIMINAR)
CREATE TABLE IF NOT EXISTS public.auditoria_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_recurso VARCHAR(200) NOT NULL,
    accion VARCHAR(50) NOT NULL CHECK (accion IN ('CREAR', 'EDITAR', 'VER', 'ELIMINAR')),
    quien_lo_hizo VARCHAR(150) NOT NULL,
    usuario_correo VARCHAR(150),
    modulo VARCHAR(50) NOT NULL,
    detalle TEXT,
    fecha TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ==============================================================================
-- 16. TRIGGERS Y FUNCIONES AUTOMÁTICAS
-- ==============================================================================

-- A. Actualizar automáticamente fecha_actualizacion en productos
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = timezone('utc'::text, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER tr_productos_updated_at
BEFORE UPDATE ON public.productos
FOR EACH ROW
EXECUTE FUNCTION public.handle_updated_at();

-- B. Sincronizar nuevo usuario desde auth.users a public.perfiles
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.perfiles (id, nombre_completo, rol, correo, estado)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'nombre_completo', split_part(NEW.email, '@', 1)),
        COALESCE(NEW.raw_user_meta_data->>'rol', 'vendedor'),
        NEW.email,
        'activo'
    )
    ON CONFLICT (id) DO UPDATE
    SET correo = EXCLUDED.correo;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ==============================================================================
-- 17. ROW LEVEL SECURITY (RLS)
-- ==============================================================================
ALTER TABLE public.perfiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.proveedores ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.metodos_pago ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ventas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalle_ventas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.compras ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalle_compras ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.movimientos_stock ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.devoluciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalle_devoluciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.auditoria_logs ENABLE ROW LEVEL SECURITY;

-- Función helper para verificar rol admin
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.perfiles
        WHERE id = auth.uid() AND rol = 'admin' AND estado = 'activo'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DO $$
BEGIN
    -- Lectura pública para usuarios autenticados
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura perfiles' AND tablename = 'perfiles') THEN
        CREATE POLICY "Permitir lectura perfiles" ON public.perfiles FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura categorias' AND tablename = 'categorias') THEN
        CREATE POLICY "Permitir lectura categorias" ON public.categorias FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura proveedores' AND tablename = 'proveedores') THEN
        CREATE POLICY "Permitir lectura proveedores" ON public.proveedores FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura productos' AND tablename = 'productos') THEN
        CREATE POLICY "Permitir lectura productos" ON public.productos FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura metodos_pago' AND tablename = 'metodos_pago') THEN
        CREATE POLICY "Permitir lectura metodos_pago" ON public.metodos_pago FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura clientes' AND tablename = 'clientes') THEN
        CREATE POLICY "Permitir lectura clientes" ON public.clientes FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura ventas' AND tablename = 'ventas') THEN
        CREATE POLICY "Permitir lectura ventas" ON public.ventas FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura detalle_ventas' AND tablename = 'detalle_ventas') THEN
        CREATE POLICY "Permitir lectura detalle_ventas" ON public.detalle_ventas FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura compras' AND tablename = 'compras') THEN
        CREATE POLICY "Permitir lectura compras" ON public.compras FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura detalle_compras' AND tablename = 'detalle_compras') THEN
        CREATE POLICY "Permitir lectura detalle_compras" ON public.detalle_compras FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura movimientos_stock' AND tablename = 'movimientos_stock') THEN
        CREATE POLICY "Permitir lectura movimientos_stock" ON public.movimientos_stock FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura devoluciones' AND tablename = 'devoluciones') THEN
        CREATE POLICY "Permitir lectura devoluciones" ON public.devoluciones FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura detalle_devoluciones' AND tablename = 'detalle_devoluciones') THEN
        CREATE POLICY "Permitir lectura detalle_devoluciones" ON public.detalle_devoluciones FOR SELECT TO authenticated USING (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura auditoria_logs' AND tablename = 'auditoria_logs') THEN
        CREATE POLICY "Permitir lectura auditoria_logs" ON public.auditoria_logs FOR SELECT TO authenticated USING (public.is_admin());
    END IF;

    -- Gestión de clientes por usuarios autenticados
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir gestionar clientes' AND tablename = 'clientes') THEN
        CREATE POLICY "Permitir gestionar clientes" ON public.clientes FOR ALL TO authenticated USING (true) WITH CHECK (true);
    END IF;

    -- Ventas y devoluciones por usuarios autenticados
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir crear ventas' AND tablename = 'ventas') THEN
        CREATE POLICY "Permitir crear ventas" ON public.ventas FOR INSERT TO authenticated WITH CHECK (auth.uid() = vendedor_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir crear detalle_ventas' AND tablename = 'detalle_ventas') THEN
        CREATE POLICY "Permitir crear detalle_ventas" ON public.detalle_ventas FOR INSERT TO authenticated WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir anular ventas' AND tablename = 'ventas') THEN
        CREATE POLICY "Permitir anular ventas" ON public.ventas FOR UPDATE TO authenticated USING (public.is_admin() OR auth.uid() = vendedor_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir crear devoluciones' AND tablename = 'devoluciones') THEN
        CREATE POLICY "Permitir crear devoluciones" ON public.devoluciones FOR INSERT TO authenticated WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir crear detalle_devoluciones' AND tablename = 'detalle_devoluciones') THEN
        CREATE POLICY "Permitir crear detalle_devoluciones" ON public.detalle_devoluciones FOR INSERT TO authenticated WITH CHECK (true);
    END IF;

    -- Políticas administrativas
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin gestiona categorias' AND tablename = 'categorias') THEN
        CREATE POLICY "Admin gestiona categorias" ON public.categorias FOR ALL TO authenticated USING (public.is_admin());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin gestiona proveedores' AND tablename = 'proveedores') THEN
        CREATE POLICY "Admin gestiona proveedores" ON public.proveedores FOR ALL TO authenticated USING (public.is_admin());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin gestiona productos' AND tablename = 'productos') THEN
        CREATE POLICY "Admin gestiona productos" ON public.productos FOR ALL TO authenticated USING (public.is_admin());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin gestiona compras' AND tablename = 'compras') THEN
        CREATE POLICY "Admin gestiona compras" ON public.compras FOR ALL TO authenticated USING (public.is_admin());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin gestiona detalle_compras' AND tablename = 'detalle_compras') THEN
        CREATE POLICY "Admin gestiona detalle_compras" ON public.detalle_compras FOR ALL TO authenticated USING (public.is_admin());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin gestiona perfiles' AND tablename = 'perfiles') THEN
        CREATE POLICY "Admin gestiona perfiles" ON public.perfiles FOR ALL TO authenticated USING (public.is_admin());
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir insertar movimientos stock' AND tablename = 'movimientos_stock') THEN
        CREATE POLICY "Permitir insertar movimientos stock" ON public.movimientos_stock FOR INSERT TO authenticated WITH CHECK (true);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir registrar auditoria' AND tablename = 'auditoria_logs') THEN
        CREATE POLICY "Permitir registrar auditoria" ON public.auditoria_logs FOR INSERT TO authenticated WITH CHECK (true);
    END IF;
END $$;

-- ==============================================================================
-- 18. CONFIGURACIÓN BÁSICA DE MÉTODOS DE PAGO (SIN DATOS DE PRUEBA)
-- ==============================================================================
INSERT INTO public.metodos_pago (nombre) VALUES
    ('Efectivo'),
    ('Tarjeta de Débito'),
    ('Tarjeta de Crédito'),
    ('Yape'),
    ('Plin'),
    ('Transferencia Bancaria')
ON CONFLICT (nombre) DO NOTHING;
