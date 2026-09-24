package com.stockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalProductos;
    private long productosBajoStock;
    private long productosSinStock;
    private long totalVentas;
    private BigDecimal totalIngresos;
    private long totalCompras;
    private BigDecimal totalEgresos;
}
