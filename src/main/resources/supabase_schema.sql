-- ==============================================================================
-- STOCKMASTER - ESQUEMA DE BASE DE DATOS PARA SUPABASE (POSTGRESQL)
-- ==============================================================================
-- Este script crea todas las tablas, relaciones, triggers de stock, perfiles,
-- políticas de seguridad RLS y datos iniciales.
-- Diseñado para ejecutarse 100% limpio en el SQL Editor de Supabase sin advertencias.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. EXTENSIONES
-- ------------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ------------------------------------------------------------------------------
-- 2. TABLA: perfiles (Extensión vinculada a auth.users de Supabase)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.perfiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    nombre_completo TEXT NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'vendedor' CHECK (rol IN ('admin', 'vendedor')),
    correo TEXT UNIQUE NOT NULL,
    avatar_url TEXT,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo')),
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

COMMENT ON TABLE public.perfiles IS 'Perfiles de usuario vinculados al sistema de autenticación de Supabase (auth.users)';

-- ------------------------------------------------------------------------------
-- 3. TABLA: categorias
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.categorias (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 4. TABLA: proveedores
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.proveedores (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc VARCHAR(20) UNIQUE,
    telefono VARCHAR(255),
    correo VARCHAR(150),
    direccion VARCHAR(500),
    fecha_creacion TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 5. TABLA: metodos_pago
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.metodos_pago (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- ------------------------------------------------------------------------------
-- 6. TABLA: productos
-- ------------------------------------------------------------------------------
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

-- ------------------------------------------------------------------------------
-- 7. TABLA: ventas (Cabecera)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.ventas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_boleta VARCHAR(50) UNIQUE NOT NULL,
    vendedor_id UUID NOT NULL REFERENCES public.perfiles(id) ON DELETE RESTRICT,
    cliente VARCHAR(150) NOT NULL,
    metodo_pago_id BIGINT REFERENCES public.metodos_pago(id) ON DELETE SET NULL,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    igv NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (igv >= 0),
    descuento NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (descuento >= 0),
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(20) NOT NULL DEFAULT 'completada' CHECK (estado IN ('completada', 'anulada')),
    observaciones TEXT,
    fecha_venta TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 8. TABLA: detalle_ventas
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.detalle_ventas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES public.ventas(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0)
);

-- ------------------------------------------------------------------------------
-- 9. TABLA: compras (Cabecera)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.compras (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    proveedor_id BIGINT NOT NULL REFERENCES public.proveedores(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES public.perfiles(id) ON DELETE RESTRICT,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    igv NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (igv >= 0),
    total NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    fecha_compra TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ------------------------------------------------------------------------------
-- 10. TABLA: detalle_compras
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.detalle_compras (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES public.compras(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id) ON DELETE RESTRICT,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    costo_unitario NUMERIC(12, 2) NOT NULL CHECK (costo_unitario >= 0),
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0)
);

-- ------------------------------------------------------------------------------
-- 11. TABLA: movimientos_stock (Kardex de Inventario)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.movimientos_stock (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES public.perfiles(id) ON DELETE RESTRICT,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('entrada', 'salida', 'ajuste')),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    motivo TEXT,
    fecha_movimiento TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- ==============================================================================
-- 12. TRIGGERS Y FUNCIONES DE BASE DE DATOS
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

-- B. Sincronizar automáticamente nuevo usuario desde auth.users a public.perfiles
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

-- C. Descontar Stock automáticamente al registrar Detalle de Venta
CREATE OR REPLACE FUNCTION public.fn_descontar_stock_venta()
RETURNS TRIGGER AS $$
DECLARE
    v_stock_actual INTEGER;
    v_nombre_prod TEXT;
BEGIN
    SELECT stock, nombre INTO v_stock_actual, v_nombre_prod
    FROM public.productos
    WHERE id = NEW.producto_id;

    IF v_stock_actual < NEW.cantidad THEN
        RAISE EXCEPTION 'Stock insuficiente para el producto % (ID: %). Disponible: %, Solicitado: %',
            v_nombre_prod, NEW.producto_id, v_stock_actual, NEW.cantidad;
    END IF;

    UPDATE public.productos
    SET stock = stock - NEW.cantidad
    WHERE id = NEW.producto_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER tr_descontar_stock_venta
AFTER INSERT ON public.detalle_ventas
FOR EACH ROW
EXECUTE FUNCTION public.fn_descontar_stock_venta();

-- D. Restaurar Stock automáticamente al anular una venta
CREATE OR REPLACE FUNCTION public.fn_restaurar_stock_anulacion()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.estado = 'anulada' AND OLD.estado != 'anulada' THEN
        UPDATE public.productos p
        SET stock = p.stock + dv.cantidad
        FROM public.detalle_ventas dv
        WHERE dv.venta_id = NEW.id AND p.id = dv.producto_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER tr_restaurar_stock_anulacion
AFTER UPDATE OF estado ON public.ventas
FOR EACH ROW
EXECUTE FUNCTION public.fn_restaurar_stock_anulacion();

-- E. Incrementar Stock y actualizar costo al registrar Detalle de Compra
CREATE OR REPLACE FUNCTION public.fn_incrementar_stock_compra()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE public.productos
    SET stock = stock + NEW.cantidad,
        precio_compra = NEW.costo_unitario
    WHERE id = NEW.producto_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER tr_incrementar_stock_compra
AFTER INSERT ON public.detalle_compras
FOR EACH ROW
EXECUTE FUNCTION public.fn_incrementar_stock_compra();

-- F. Aplicar ajustes manuales de movimientos_stock al inventario
CREATE OR REPLACE FUNCTION public.fn_aplicar_movimiento_stock()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tipo = 'entrada' THEN
        UPDATE public.productos SET stock = stock + NEW.cantidad WHERE id = NEW.producto_id;
    ELSIF NEW.tipo = 'salida' THEN
        IF (SELECT stock FROM public.productos WHERE id = NEW.producto_id) < NEW.cantidad THEN
            RAISE EXCEPTION 'Stock insuficiente para salida en movimiento ID %', NEW.id;
        END IF;
        UPDATE public.productos SET stock = stock - NEW.cantidad WHERE id = NEW.producto_id;
    ELSIF NEW.tipo = 'ajuste' THEN
        UPDATE public.productos SET stock = NEW.cantidad WHERE id = NEW.producto_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER tr_aplicar_movimiento_stock
AFTER INSERT ON public.movimientos_stock
FOR EACH ROW
EXECUTE FUNCTION public.fn_aplicar_movimiento_stock();

-- ==============================================================================
-- 13. ROW LEVEL SECURITY (RLS) Y POLÍTICAS DE ACCESO
-- ==============================================================================
ALTER TABLE public.perfiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.proveedores ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.metodos_pago ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ventas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalle_ventas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.compras ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.detalle_compras ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.movimientos_stock ENABLE ROW LEVEL SECURITY;

-- Función helper para verificar si el usuario autenticado es admin
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.perfiles
        WHERE id = auth.uid() AND rol = 'admin' AND estado = 'activo'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Bloque seguro para crear políticas solo si no existen previamente
DO $$
BEGIN
    -- Políticas de lectura para usuarios autenticados
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir lectura para usuarios autenticados' AND tablename = 'perfiles') THEN
        CREATE POLICY "Permitir lectura para usuarios autenticados" ON public.perfiles FOR SELECT TO authenticated USING (true);
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

    -- Políticas de ventas
    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir crear ventas' AND tablename = 'ventas') THEN
        CREATE POLICY "Permitir crear ventas" ON public.ventas FOR INSERT TO authenticated WITH CHECK (auth.uid() = vendedor_id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir crear detalle_ventas' AND tablename = 'detalle_ventas') THEN
        CREATE POLICY "Permitir crear detalle_ventas" ON public.detalle_ventas FOR INSERT TO authenticated WITH CHECK (true);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Permitir anular ventas admin o creador' AND tablename = 'ventas') THEN
        CREATE POLICY "Permitir anular ventas admin o creador" ON public.ventas FOR UPDATE TO authenticated USING (public.is_admin() OR auth.uid() = vendedor_id);
    END IF;

    -- Políticas de administración
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

    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin gestiona usuarios' AND tablename = 'perfiles') THEN
        CREATE POLICY "Admin gestiona usuarios" ON public.perfiles FOR ALL TO authenticated USING (public.is_admin());
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_policies WHERE policyname = 'Admin registra movimientos stock' AND tablename = 'movimientos_stock') THEN
        CREATE POLICY "Admin registra movimientos stock" ON public.movimientos_stock FOR INSERT TO authenticated WITH CHECK (true);
    END IF;
END $$;

-- ==============================================================================
-- 14. DATOS INICIALES (SEED DATA)
-- ==============================================================================

-- Métodos de pago
INSERT INTO public.metodos_pago (nombre) VALUES
    ('Efectivo'),
    ('Tarjeta de Débito'),
    ('Tarjeta de Crédito'),
    ('Yape'),
    ('Plin'),
    ('Transferencia Bancaria')
ON CONFLICT (nombre) DO NOTHING;

-- Categorías
INSERT INTO public.categorias (nombre, descripcion) VALUES
    ('Lácteos', 'Leches, quesos, yogures y derivados'),
    ('Bebidas', 'Gaseosas, aguas minerales, jugos e isotónicas'),
    ('Abarrotes', 'Arroz, azúcar, fideos, aceites y conservas'),
    ('Limpieza', 'Detergentes, desinfectantes, jabones y papel'),
    ('Snacks', 'Galletas, chocolates, golosinas y piqueos')
ON CONFLICT (nombre) DO NOTHING;

-- Proveedores
INSERT INTO public.proveedores (razon_social, ruc, telefono, correo, direccion) VALUES
    ('Distribuidora Gloria S.A.', '20100190797', '014707070', 'contacto@gloria.com.pe', 'Av. República de Panamá 2461, Santa Catalina, Lima'),
    ('Alicorp S.A.A.', '20100055237', '013150800', 'ventas@alicorp.com.pe', 'Av. Argentina 4793, Callao'),
    ('Unilever Andina Perú S.A.', '20100021332', '016186000', 'atencion@unilever.com', 'Av. Paseo de la República 5895, Miraflores, Lima')
ON CONFLICT (ruc) DO NOTHING;

-- Productos
INSERT INTO public.productos (categoria_id, sku, nombre, descripcion, imagen_url, precio_compra, precio_venta, stock, stock_minimo, estado)
SELECT c.id, 'LAC-001', 'Leche Gloria Azul 400g', 'Leche evaporada entera de tarro', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400', 3.20, 4.50, 50, 10, 'activo'
FROM public.categorias c WHERE c.nombre = 'Lácteos'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO public.productos (categoria_id, sku, nombre, descripcion, imagen_url, precio_compra, precio_venta, stock, stock_minimo, estado)
SELECT c.id, 'LAC-002', 'Yogurt Gloria Fresa 1L', 'Yogurt batido sabor fresa botella', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400', 5.00, 6.80, 4, 5, 'activo'
FROM public.categorias c WHERE c.nombre = 'Lácteos'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO public.productos (categoria_id, sku, nombre, descripcion, imagen_url, precio_compra, precio_venta, stock, stock_minimo, estado)
SELECT c.id, 'BEB-001', 'Inca Kola 500ml', 'Gaseosa sabor nacional botella no retornable', 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400', 2.00, 3.00, 40, 8, 'activo'
FROM public.categorias c WHERE c.nombre = 'Bebidas'
ON CONFLICT (sku) DO NOTHING;

INSERT INTO public.productos (categoria_id, sku, nombre, descripcion, imagen_url, precio_compra, precio_venta, stock, stock_minimo, estado)
SELECT c.id, 'ABA-001', 'Arroz Costeño Extra 1kg', 'Arroz superior seleccionado bolsa', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400', 3.80, 4.90, 0, 10, 'activo'
FROM public.categorias c WHERE c.nombre = 'Abarrotes'
ON CONFLICT (sku) DO NOTHING;
