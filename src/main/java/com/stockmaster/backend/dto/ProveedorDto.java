package com.stockmaster.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ProveedorDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "La razón social es obligatoria")
        private String razonSocial;
        private String ruc;
        private String telefono;
        private String correo;
        private String direccion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String razonSocial;
        private String ruc;
        private String telefono;
        private String correo;
        private String direccion;
        private LocalDateTime fechaCreacion;

        public String getLabel() {
            return ruc != null && !ruc.isBlank() ? razonSocial + " (" + ruc + ")" : razonSocial;
        }

        public String getSupplier() {
            return razonSocial;
        }
    }
}
