package com.stockmaster.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "El nombre del cliente es obligatorio")
        private String cliente;

        private Long metodoPagoId;

        @DecimalMin(value = "0.0", inclusive = true, message = "El descuento debe ser >= 0")
        @Builder.Default
        private BigDecimal descuento = BigDecimal.ZERO;

        private String observaciones;

        @NotEmpty(message = "La venta debe incluir al menos un producto")
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

        private BigDecimal precioUnitario; // If null, service uses current product precioVenta
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String numeroBoleta;
        private Long vendedorId;
        private String vendedorNombre;
        private String cliente;
        private Long metodoPagoId;
        private String metodoPagoNombre;
        private BigDecimal subtotal;
        private BigDecimal igv;
        private BigDecimal descuento;
        private BigDecimal total;
        private String estado;
        private LocalDateTime fechaVenta;
        private String observaciones;
        private List<DetalleResponse> detalles;

        public String getCustomerName() { return cliente; }
        public String getDate() { return fechaVenta != null ? fechaVenta.toString() : null; }
        public boolean isCompletada() { return "completada".equalsIgnoreCase(estado); }
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
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public String getProductName() { return productoNombre; }
        public BigDecimal getUnitPrice() { return precioUnitario; }
    }
}
