package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.DashboardStatsDto;
import com.stockmaster.backend.entity.Compra;
import com.stockmaster.backend.entity.Producto;
import com.stockmaster.backend.entity.Venta;
import com.stockmaster.backend.repository.CompraRepository;
import com.stockmaster.backend.repository.ProductoRepository;
import com.stockmaster.backend.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final CompraRepository compraRepository;

    public DashboardStatsDto getStats() {
        List<Producto> productos = productoRepository.findByEstado("activo");
        long totalProductos = productos.size();
        long lowStock = productos.stream().filter(p -> p.getStock() > 0 && p.getStock() <= p.getStockMinimo()).count();
        long outOfStock = productos.stream().filter(p -> p.getStock() <= 0).count();

        List<Venta> ventas = ventaRepository.findAll();
        long totalVentas = ventas.size();
        BigDecimal totalIngresos = ventas.stream()
                .filter(v -> "completada".equalsIgnoreCase(v.getEstado()))
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Compra> compras = compraRepository.findAll();
        long totalCompras = compras.size();
        BigDecimal totalEgresos = compras.stream()
                .map(Compra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStatsDto.builder()
                .totalProductos(totalProductos)
                .productosBajoStock(lowStock)
                .productosSinStock(outOfStock)
                .totalVentas(totalVentas)
                .totalIngresos(totalIngresos)
                .totalCompras(totalCompras)
                .totalEgresos(totalEgresos)
                .build();
    }
}
