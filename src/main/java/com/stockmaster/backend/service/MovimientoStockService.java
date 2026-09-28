package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.MovimientoStockDto;
import com.stockmaster.backend.entity.MovimientoStock;
import com.stockmaster.backend.entity.Producto;
import com.stockmaster.backend.entity.Usuario;
import com.stockmaster.backend.exception.InsufficientStockException;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.MovimientoStockRepository;
import com.stockmaster.backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;

    public List<MovimientoStockDto.Response> findAll() {
        return movimientoStockRepository.findAllByOrderByFechaMovimientoDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<MovimientoStockDto.Response> findByProducto(Long productoId) {
        return movimientoStockRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<MovimientoStockDto.Response> findByTipo(String tipo) {
        return movimientoStockRepository.findByTipoOrderByFechaMovimientoDesc(tipo).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<MovimientoStockDto.Response> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return movimientoStockRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(from, to).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MovimientoStockDto.Response createMovimiento(MovimientoStockDto.Request request, String userCorreo) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + request.getProductoId()));

        Usuario usuario = usuarioService.findEntityByCorreo(userCorreo);

        String tipo = request.getTipo().toLowerCase();
        int cantidad = Math.abs(request.getCantidad());
        int cantidadGuardar = cantidad;

        switch (tipo) {
            case "compra":
            case "devolucion":
            case "entrada":
                producto.setStock(producto.getStock() + cantidad);
                cantidadGuardar = cantidad;
                break;
            case "venta":
            case "salida":
                if (producto.getStock() < cantidad) {
                    throw new InsufficientStockException("Stock insuficiente para salida. Disponible: " +
                            producto.getStock() + ", Solicitado: " + cantidad);
                }
                producto.setStock(producto.getStock() - cantidad);
                cantidadGuardar = -cantidad;
                break;
            case "ajuste":
                int diferencia = cantidad - producto.getStock();
                producto.setStock(cantidad);
                cantidadGuardar = diferencia;
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento inválido: " + tipo);
        }

        productoRepository.save(producto);

        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .usuario(usuario)
                .tipo(tipo)
                .cantidad(cantidadGuardar)
                .motivo(request.getMotivo() != null ? request.getMotivo().trim() : null)
                .build();

        MovimientoStock guardado = movimientoStockRepository.save(movimiento);
        return mapToDto(guardado);
    }

    public MovimientoStockDto.Response mapToDto(MovimientoStock m) {
        return MovimientoStockDto.Response.builder()
                .id(m.getId())
                .productoId(m.getProducto().getId())
                .productoNombre(m.getProducto().getNombre())
                .sku(m.getProducto().getSku())
                .usuarioId(m.getUsuario().getId())
                .usuarioNombre(m.getUsuario().getNombreCompleto())
                .tipo(m.getTipo())
                .cantidad(m.getCantidad())
                .motivo(m.getMotivo())
                .deDonde(m.getMotivo() != null ? m.getMotivo() : "Operación directa")
                .fechaMovimiento(m.getFechaMovimiento())
                .build();
    }
}
