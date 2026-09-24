package com.stockmaster.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductoDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "La categoría es obligatoria")
        private Long categoriaId;

        private String sku;

        @NotBlank(message = "El nombre del producto es obligatorio")
        private String nombre;

        private String descripcion;
        private String imagenUrl;

        @NotNull(message = "El precio de compra es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio de compra debe ser >= 0")
        private BigDecimal precioCompra;

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio de venta debe ser >= 0")
        private BigDecimal precioVenta;

        @Min(value = 0, message = "El stock debe ser >= 0")
        private Integer stock;

        @Min(value = 0, message = "El stock mínimo debe ser >= 0")
        private Integer stockMinimo;

        private String estado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long categoriaId;
        private String categoriaNombre;
        private String sku;
        private String nombre;
        private String descripcion;
        private String imagenUrl;
        private BigDecimal precioCompra;
        private BigDecimal precioVenta;
        private Integer stock;
        private Integer stockMinimo;
        private String estado;
        private Double margen;
        private String stockLabel;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;

        // Compatibility aliases for frontend
        public String getName() { return nombre; }
        public String getCategory() { return categoriaNombre; }
        public BigDecimal getPrice() { return precioVenta; }
        public BigDecimal getCostPrice() { return precioCompra; }
        public Integer getMinStock() { return stockMinimo; }
        public String getImage() { return imagenUrl; }
        public String getImageUrl() { return imagenUrl; }
    }
}
