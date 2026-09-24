package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.CompraDto;
import com.stockmaster.backend.entity.*;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.CompraRepository;
import com.stockmaster.backend.repository.MovimientoStockRepository;
import com.stockmaster.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ProveedorService proveedorService;
    private final UsuarioService usuarioService;

    private static final BigDecimal IGV_RATE = BigDecimal.valueOf(0.18);

    public List<CompraDto.Response> findAll() {
        return compraRepository.findAllByOrderByFechaCompraDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CompraDto.Response> findByProveedor(Long proveedorId) {
        return compraRepository.findByProveedorIdOrderByFechaCompraDesc(proveedorId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CompraDto.Response> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return compraRepository.findByFechaCompraBetweenOrderByFechaCompraDesc(from, to).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CompraDto.Response findById(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
        return mapToDto(compra);
    }

    @Transactional
    public CompraDto.Response createCompra(CompraDto.Request request, String userCorreo) {
        Proveedor proveedor = proveedorService.findEntityById(request.getProveedorId());
        Usuario usuario = usuarioService.findEntityByCorreo(userCorreo);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetalleCompra> detalles = new ArrayList<>();
        List<MovimientoStock> movimientos = new ArrayList<>();

        for (CompraDto.DetalleItemRequest item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + item.getProductoId()));

            // Aumentar stock
            producto.setStock(producto.getStock() + item.getCantidad());
            // Actualizar precio de costo
            producto.setPrecioCompra(item.getCostoUnitario());
            productoRepository.save(producto);

            BigDecimal itemSubtotal = item.getCostoUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemSubtotal);

            DetalleCompra detalle = DetalleCompra.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .costoUnitario(item.getCostoUnitario())
                    .subtotal(itemSubtotal)
                    .build();
            detalles.add(detalle);

            MovimientoStock mov = MovimientoStock.builder()
                    .producto(producto)
                    .usuario(usuario)
                    .tipo("entrada")
                    .cantidad(item.getCantidad())
                    .motivo("Compra a proveedor: " + proveedor.getRazonSocial())
                    .build();
            movimientos.add(mov);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);

        Compra compra = Compra.builder()
                .proveedor(proveedor)
                .usuario(usuario)
                .subtotal(subtotal)
                .igv(igv)
                .total(total)
                .build();

        for (DetalleCompra d : detalles) {
            compra.addDetalle(d);
        }

        Compra guardada = compraRepository.save(compra);
        movimientoStockRepository.saveAll(movimientos);

        return mapToDto(guardada);
    }

    public CompraDto.Response mapToDto(Compra c) {
        List<CompraDto.DetalleResponse> detallesDto = c.getDetalles().stream()
                .map(d -> CompraDto.DetalleResponse.builder()
                        .id(d.getId())
                        .productoId(d.getProducto().getId())
                        .productoNombre(d.getProducto().getNombre())
                        .sku(d.getProducto().getSku())
                        .cantidad(d.getCantidad())
                        .costoUnitario(d.getCostoUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return CompraDto.Response.builder()
                .id(c.getId())
                .proveedorId(c.getProveedor().getId())
                .proveedorNombre(c.getProveedor().getRazonSocial())
                .usuarioId(c.getUsuario().getId())
                .usuarioNombre(c.getUsuario().getNombreCompleto())
                .subtotal(c.getSubtotal())
                .igv(c.getIgv())
                .total(c.getTotal())
                .fechaCompra(c.getFechaCompra())
                .detalles(detallesDto)
                .build();
    }
}
