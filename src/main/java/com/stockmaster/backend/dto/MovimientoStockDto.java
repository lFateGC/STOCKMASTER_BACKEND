package com.stockmaster.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MovimientoStockDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotBlank(message = "El tipo de movimiento es obligatorio")
        @Pattern(regexp = "^(venta|compra|devolucion|entrada|salida|ajuste)$", message = "El tipo debe ser venta, compra, devolucion, entrada, salida o ajuste")
        private String tipo;

        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;

        private String motivo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long productoId;
        private String productoNombre;
        private String sku;
        private Long usuarioId;
        private String usuarioNombre;
        private String tipo;
        private Integer cantidad;
        private String motivo;
        private String deDonde;
        private LocalDateTime fechaMovimiento;

        // Compatibility aliases for frontend
        public String getProductName() { return productoNombre; }
        public String getType() { return tipo; }
        public Integer getQuantity() { return cantidad; }
        public String getReason() { return motivo; }
        public String getDeDonde() { return deDonde != null ? deDonde : (motivo != null ? motivo : "Operación directa"); }
        public String getUser() { return usuarioNombre; }
        public String getDate() { return fechaMovimiento != null ? fechaMovimiento.toString() : null; }
    }
}
