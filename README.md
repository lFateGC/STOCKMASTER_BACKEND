# StockMaster Backend (Spring Boot API) 🚀

API REST empresarial para el sistema **StockMaster**, desarrollada con **Java 21**, **Spring Boot 4**, **Spring Data JPA**, **Spring Security (JWT)** y base de datos relacional (H2 en memoria para desarrollo y PostgreSQL para producción).

---

## 🏗 Arquitectura y Paquetes

```text
com.stockmaster.backend/
├── config/         # Configuración de beans y beans generales
├── controller/     # Controladores REST (/api/auth, /api/productos, /api/ventas, etc.)
├── dto/            # Data Transfer Objects para requests y responses
├── entity/         # Entidades JPA (Usuario, Producto, Categoria, Venta, Compra, etc.)
├── exception/      # GlobalExceptionHandler y excepciones de negocio
├── repository/     # Repositorios Spring Data JPA
├── security/       # JWT Token Provider, Filtro JWT, UserDetailsService, SecurityConfig
└── service/        # Lógica de negocio (control transaccional, stock, cálculos fiscales)
```

---

## 🔐 Endpoints Principales

### Autenticación (`/api/auth`)
* `POST /api/auth/login`: Autentica credenciales y devuelve token JWT con datos del usuario.
* `POST /api/auth/register`: Registra un nuevo usuario en el sistema.

### Productos (`/api/productos`)
* `GET /api/productos`: Lista productos activos con cálculo automático de margen y alertas de stock.
* `GET /api/productos?all=true`: Lista todos los productos (incluye inactivos).
* `GET /api/productos/{id}`: Obtiene un producto por ID.
* `GET /api/productos/low-stock`: Obtiene productos con stock bajo o nulo.
* `POST /api/productos`: Crea un nuevo producto (Admin).
* `PUT /api/productos/{id}`: Actualiza un producto existente (Admin).
* `DELETE /api/productos/{id}`: Desactivación lógica del producto (Admin).

### Categorías (`/api/categorias`)
* `GET /api/categorias`: Lista todas las categorías.
* `POST /api/categorias`: Crea una categoría (Admin).
* `PUT /api/categorias/{id}`: Actualiza una categoría (Admin).
* `DELETE /api/categorias/{id}`: Elimina una categoría (Admin).

### Ventas - POS (`/api/ventas`)
* `GET /api/ventas`: Historial de ventas con filtros de fecha y vendedor.
* `POST /api/ventas`: Registra una venta, descuenta stock atómicamente, genera boleta fiscal y registra el movimiento de stock.
* `PATCH /api/ventas/{id}/anular`: Anula la venta y reingresa el stock al inventario.

### Compras (`/api/compras`)
* `GET /api/compras`: Historial de compras a proveedores.
* `POST /api/compras`: Registra compra de mercadería, incrementa stock y actualiza costo unitario.

### Movimientos de Inventario (`/api/movimientos`)
* `GET /api/movimientos`: Kardex / Historial de movimientos.
* `POST /api/movimientos`: Registro de entradas, salidas o ajustes manuales de stock.

### Proveedores (`/api/proveedores`)
* `GET /api/proveedores`: Listado de proveedores.
* `POST /api/proveedores`: Creación de proveedores.

### Métodos de Pago (`/api/metodos-pago`)
* `GET /api/metodos-pago`: Listado de medios de pago disponibles (Efectivo, Tarjeta, Yape, Plin).

---

## 🧪 Pruebas Automatizadas

El proyecto cuenta con una suite completa de pruebas unitarias y de integración que cubren los servicios principales y controladores:

```bash
./gradlew test
```

### Casos de prueba incluidos:
1. `ProductoServiceTest`: Validación de stock, SKU duplicado, desactivación lógica y búsquedas.
2. `VentaServiceTest`: Descuento de stock en venta, excepción `InsufficientStockException`, anulación con restauración de stock y cálculo de IGV.
3. `CompraServiceTest`: Incremento de stock y actualización de precio de compra.
4. `AuthServiceTest`: Generación de token JWT, autenticación y unicidad de correo.
5. `ProductoControllerTest`: Pruebas de endpoint con MockMvc y respuestas JSON.
