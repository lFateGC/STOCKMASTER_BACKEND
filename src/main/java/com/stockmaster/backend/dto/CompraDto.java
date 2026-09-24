package com.stockmaster.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CompraDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "El proveedor es obligatorio")
        private Long proveedorId;

        @NotEmpty(message = "La compra debe incluir al menos un producto")
        @Valid
        private List<DetalleItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleItemRequest {
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;

        @NotNull(message = "El costo unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El costo unitario debe ser >= 0")
        private BigDecimal costoUnitario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long proveedorId;
        private String proveedorNombre;
        private Long usuarioId;
        private String usuarioNombre;
        private BigDecimal subtotal;
        private BigDecimal igv;
        private BigDecimal total;
        private LocalDateTime fechaCompra;
        private List<DetalleResponse> detalles;

        public String getSupplier() { return proveedorNombre; }
        public String getDate() { return fechaCompra != null ? fechaCompra.toString() : null; }
        public String getCreatedBy() { return usuarioNombre; }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleResponse {
        private Long id;
        private Long productoId;
        private String productoNombre;
        private String sku;
        private Integer cantidad;
        private BigDecimal costoUnitario;
        private BigDecimal subtotal;

        public String getProductName() { return productoNombre; }
        public BigDecimal getUnitCost() { return costoUnitario; }
    }
}
