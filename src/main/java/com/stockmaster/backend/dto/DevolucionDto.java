package com.stockmaster.backend.dto;

import jakarta.validation.Valid;
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

public class DevolucionDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "El ID de la venta es obligatorio")
        private Long ventaId;

        @NotBlank(message = "El motivo de la devolución es obligatorio")
        private String motivo;

        private String destinoStock; // "reingreso" o "merma"

        private String metodoReembolso; // "efectivo", "nota_credito", "transferencia"

        private String observaciones;

        @NotEmpty(message = "Debe incluir al menos un producto a devolver")
        @Valid
        private List<ItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemRequest {
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad a devolver debe ser al menos 1")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        private BigDecimal precioUnitario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String numeroDevolucion;
        private Long ventaId;
        private String numeroBoleta;
        private String cliente;
        private String usuarioNombre;
        private String motivo;
        private String destinoStock;
        private String metodoReembolso;
        private BigDecimal montoTotal;
        private String estado;
        private String observaciones;
        private LocalDateTime fechaDevolucion;
        private List<ItemResponse> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemResponse {
        private Long id;
        private Long productoId;
        private String productoNombre;
        private String sku;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
