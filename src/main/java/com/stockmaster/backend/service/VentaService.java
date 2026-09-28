package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.VentaDto;
import com.stockmaster.backend.entity.*;
import com.stockmaster.backend.exception.BadRequestException;
import com.stockmaster.backend.exception.InsufficientStockException;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.MovimientoStockRepository;
import com.stockmaster.backend.repository.ProductoRepository;
import com.stockmaster.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final MetodoPagoService metodoPagoService;
    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    private static final BigDecimal IGV_RATE = BigDecimal.valueOf(0.18);

    public List<VentaDto.Response> findAll() {
        return ventaRepository.findAllByOrderByFechaVentaDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<VentaDto.Response> findByVendedor(Long vendedorId) {
        return ventaRepository.findByVendedorIdOrderByFechaVentaDesc(vendedorId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<VentaDto.Response> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return ventaRepository.findByFechaVentaBetweenOrderByFechaVentaDesc(from, to).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public VentaDto.Response findById(Long id) {
        return findById(id, null);
    }

    public VentaDto.Response findById(Long id, String userCorreo) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));

        if (userCorreo != null) {
            auditoriaService.registrar(
                    userCorreo,
                    "VER",
                    "VENTAS",
                    venta.getNumeroBoleta(),
                    "Visualización de comprobante de venta"
            );
        }

        return mapToDto(venta);
    }

    @Transactional
    public VentaDto.Response createVenta(VentaDto.Request request, String userCorreo) {
        Usuario vendedor = usuarioService.findEntityByCorreo(userCorreo);

        MetodoPago metodoPago = null;
        if (request.getMetodoPagoId() != null) {
            metodoPago = metodoPagoService.findEntityById(request.getMetodoPagoId());
        }

        String numeroBoleta = generarNumeroBoleta();

        BigDecimal subtotalBase = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();
        List<MovimientoStock> movimientos = new ArrayList<>();

        for (VentaDto.DetalleItemRequest item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + item.getProductoId()));

            if (!"activo".equalsIgnoreCase(producto.getEstado())) {
                throw new BadRequestException("El producto " + producto.getNombre() + " no está activo para venta");
            }

            if (producto.getStock() < item.getCantidad()) {
                throw new InsufficientStockException("Stock insuficiente para '" + producto.getNombre() +
                        "'. Disponible: " + producto.getStock() + ", Solicitado: " + item.getCantidad());
            }

            // Descontar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            BigDecimal precioUnitario = item.getPrecioUnitario() != null
                    ? item.getPrecioUnitario()
                    : producto.getPrecioVenta();

            BigDecimal itemSubtotal = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            subtotalBase = subtotalBase.add(itemSubtotal);

            DetalleVenta detalle = DetalleVenta.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(itemSubtotal)
                    .build();

            detalles.add(detalle);

            // Registrar movimiento de salida por venta (cantidad negativa)
            MovimientoStock mov = MovimientoStock.builder()
                    .producto(producto)
                    .usuario(vendedor)
                    .tipo("venta")
                    .cantidad(-Math.abs(item.getCantidad()))
                    .motivo("Venta: Boleta " + numeroBoleta)
                    .build();
            movimientos.add(mov);
        }

        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal subtotal = subtotalBase.subtract(descuento);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        Venta venta = Venta.builder()
                .numeroBoleta(numeroBoleta)
                .vendedor(vendedor)
                .cliente(request.getCliente().trim())
                .metodoPago(metodoPago)
                .subtotal(subtotal)
                .igv(igv)
                .descuento(descuento)
                .total(total)
                .estado("completada")
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .build();

        for (DetalleVenta d : detalles) {
            venta.addDetalle(d);
        }

        Venta guardada = ventaRepository.save(venta);
        movimientoStockRepository.saveAll(movimientos);

        auditoriaService.registrar(
                userCorreo,
                "CREAR",
                "VENTAS",
                numeroBoleta,
                "Comprobante emitido a " + guardada.getCliente() + " por un total de S/ " + guardada.getTotal()
        );

        return mapToDto(guardada);
    }

    @Transactional
    public VentaDto.Response anularVenta(Long id, String userCorreo) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + id));

        if ("anulada".equalsIgnoreCase(venta.getEstado())) {
            throw new BadRequestException("La venta ya se encuentra anulada");
        }

        Usuario usuario = usuarioService.findEntityByCorreo(userCorreo);
        List<MovimientoStock> movimientos = new ArrayList<>();

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);

            MovimientoStock mov = MovimientoStock.builder()
                    .producto(producto)
                    .usuario(usuario)
                    .tipo("devolucion")
                    .cantidad(Math.abs(detalle.getCantidad()))
                    .motivo("Devolución por Anulación: Boleta " + venta.getNumeroBoleta())
                    .build();
            movimientos.add(mov);
        }

        venta.setEstado("anulada");
        Venta guardada = ventaRepository.save(venta);
        movimientoStockRepository.saveAll(movimientos);

        auditoriaService.registrar(
                userCorreo,
                "ELIMINAR",
                "VENTAS",
                venta.getNumeroBoleta(),
                "Comprobante anulado. Stock devuelto a inventario."
        );

        return mapToDto(guardada);
    }

    private String generarNumeroBoleta() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "B" + fecha + "-" + rand;
    }

    public VentaDto.Response mapToDto(Venta v) {
        List<VentaDto.DetalleResponse> detallesDto = v.getDetalles().stream()
                .map(d -> VentaDto.DetalleResponse.builder()
                        .id(d.getId())
                        .productoId(d.getProducto().getId())
                        .productoNombre(d.getProducto().getNombre())
                        .sku(d.getProducto().getSku())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return VentaDto.Response.builder()
                .id(v.getId())
                .numeroBoleta(v.getNumeroBoleta())
                .vendedorId(v.getVendedor().getId())
                .vendedorNombre(v.getVendedor().getNombreCompleto())
                .cliente(v.getCliente())
                .metodoPagoId(v.getMetodoPago() != null ? v.getMetodoPago().getId() : null)
                .metodoPagoNombre(v.getMetodoPago() != null ? v.getMetodoPago().getNombre() : "Efectivo")
                .subtotal(v.getSubtotal())
                .igv(v.getIgv())
                .descuento(v.getDescuento())
                .total(v.getTotal())
                .estado(v.getEstado())
                .fechaVenta(v.getFechaVenta())
                .observaciones(v.getObservaciones())
                .detalles(detallesDto)
                .build();
    }
}
