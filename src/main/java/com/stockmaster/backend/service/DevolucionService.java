package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.DevolucionDto;
import com.stockmaster.backend.entity.*;
import com.stockmaster.backend.exception.BadRequestException;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final MovimientoStockRepository movimientoStockRepository;
    private final AuditoriaService auditoriaService;

    public List<DevolucionDto.Response> findAll() {
        return devolucionRepository.findAllByOrderByFechaDevolucionDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DevolucionDto.Response findById(Long id, String userCorreo) {
        Devolucion d = devolucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada con ID: " + id));

        auditoriaService.registrar(
                userCorreo,
                "VER",
                "DEVOLUCIONES",
                d.getNumeroDevolucion(),
                "Visualización de comprobante de devolución (Boleta ref: " + d.getVenta().getNumeroBoleta() + ")"
        );

        return mapToDto(d);
    }

    public List<DevolucionDto.Response> findByVentaId(Long ventaId) {
        return devolucionRepository.findByVentaIdOrderByFechaDevolucionDesc(ventaId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DevolucionDto.Response createDevolucion(DevolucionDto.Request request, String userCorreo) {
        Venta venta = ventaRepository.findById(request.getVentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + request.getVentaId()));

        if ("anulada".equalsIgnoreCase(venta.getEstado())) {
            throw new BadRequestException("No se puede procesar una devolución para una venta que ya ha sido anulada.");
        }

        Usuario usuario = usuarioService.findEntityByCorreo(userCorreo);
        String numeroDevolucion = generarNumeroDevolucion();
        String destino = request.getDestinoStock() != null && !request.getDestinoStock().isBlank()
                ? request.getDestinoStock().toLowerCase() : "reingreso";
        String metodoReembolso = request.getMetodoReembolso() != null && !request.getMetodoReembolso().isBlank()
                ? request.getMetodoReembolso().toLowerCase() : "efectivo";

        List<DetalleDevolucion> detalles = new ArrayList<>();
        List<MovimientoStock> movimientos = new ArrayList<>();
        BigDecimal totalReembolso = BigDecimal.ZERO;

        for (DevolucionDto.ItemRequest item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + item.getProductoId()));

            DetalleVenta detalleOriginal = venta.getDetalles().stream()
                    .filter(d -> d.getProducto().getId().equals(producto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("El producto " + producto.getNombre() + " no pertenece a la venta " + venta.getNumeroBoleta()));

            if (item.getCantidad() > detalleOriginal.getCantidad()) {
                throw new BadRequestException("La cantidad a devolver (" + item.getCantidad() +
                        ") excede la cantidad vendida (" + detalleOriginal.getCantidad() + ") para " + producto.getNombre());
            }

            BigDecimal precioUnit = item.getPrecioUnitario() != null ? item.getPrecioUnitario() : detalleOriginal.getPrecioUnitario();
            BigDecimal subtotal = precioUnit.multiply(BigDecimal.valueOf(item.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            totalReembolso = totalReembolso.add(subtotal);

            DetalleDevolucion detalleDev = DetalleDevolucion.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precioUnit)
                    .subtotal(subtotal)
                    .build();
            detalles.add(detalleDev);

            if ("reingreso".equalsIgnoreCase(destino)) {
                producto.setStock(producto.getStock() + item.getCantidad());
                productoRepository.save(producto);

                MovimientoStock mov = MovimientoStock.builder()
                        .producto(producto)
                        .usuario(usuario)
                        .tipo("devolucion")
                        .cantidad(Math.abs(item.getCantidad()))
                        .motivo("Devolución: " + numeroDevolucion + " (Ref: " + venta.getNumeroBoleta() + ") - " + request.getMotivo())
                        .build();
                movimientos.add(mov);
            } else {
                MovimientoStock mov = MovimientoStock.builder()
                        .producto(producto)
                        .usuario(usuario)
                        .tipo("devolucion")
                        .cantidad(Math.abs(item.getCantidad()))
                        .motivo("Devolución / Merma: " + numeroDevolucion + " (Ref: " + venta.getNumeroBoleta() + ") - " + request.getMotivo())
                        .build();
                movimientos.add(mov);
            }
        }

        Devolucion devolucion = Devolucion.builder()
                .numeroDevolucion(numeroDevolucion)
                .venta(venta)
                .usuario(usuario)
                .cliente(venta.getCliente())
                .motivo(request.getMotivo().trim())
                .destinoStock(destino)
                .metodoReembolso(metodoReembolso)
                .montoTotal(totalReembolso.setScale(2, RoundingMode.HALF_UP))
                .estado("completada")
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .build();

        for (DetalleDevolucion d : detalles) {
            devolucion.addDetalle(d);
        }

        Devolucion guardada = devolucionRepository.save(devolucion);
        if (!movimientos.isEmpty()) {
            movimientoStockRepository.saveAll(movimientos);
        }

        auditoriaService.registrar(
                userCorreo,
                "CREAR",
                "DEVOLUCIONES",
                numeroDevolucion,
                "Devolución de " + request.getItems().size() + " ítems para boleta " + venta.getNumeroBoleta() + " por S/ " + totalReembolso + " - Motivo: " + request.getMotivo()
        );

        return mapToDto(guardada);
    }

    private String generarNumeroDevolucion() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "DEV-" + fecha + "-" + rand;
    }

    public DevolucionDto.Response mapToDto(Devolucion d) {
        List<DevolucionDto.ItemResponse> itemsDto = d.getDetalles().stream()
                .map(det -> DevolucionDto.ItemResponse.builder()
                        .id(det.getId())
                        .productoId(det.getProducto().getId())
                        .productoNombre(det.getProducto().getNombre())
                        .sku(det.getProducto().getSku())
                        .cantidad(det.getCantidad())
                        .precioUnitario(det.getPrecioUnitario())
                        .subtotal(det.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return DevolucionDto.Response.builder()
                .id(d.getId())
                .numeroDevolucion(d.getNumeroDevolucion())
                .ventaId(d.getVenta().getId())
                .numeroBoleta(d.getVenta().getNumeroBoleta())
                .cliente(d.getCliente())
                .usuarioNombre(d.getUsuario() != null ? d.getUsuario().getNombreCompleto() : "Sistema")
                .motivo(d.getMotivo())
                .destinoStock(d.getDestinoStock())
                .metodoReembolso(d.getMetodoReembolso())
                .montoTotal(d.getMontoTotal())
                .estado(d.getEstado())
                .observaciones(d.getObservaciones())
                .fechaDevolucion(d.getFechaDevolucion())
                .items(itemsDto)
                .build();
    }
}
