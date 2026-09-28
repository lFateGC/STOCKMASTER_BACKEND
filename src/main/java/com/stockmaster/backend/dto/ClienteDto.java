package com.stockmaster.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ClienteDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "El nombre o razón social es obligatorio")
        private String nombre;

        private String tipoDocumento;

        private String numeroDocumento;

        private String telefono;

        private String correo;

        private String direccion;

        private String estado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String nombre;
        private String tipoDocumento;
        private String numeroDocumento;
        private String telefono;
        private String correo;
        private String direccion;
        private String estado;
        private LocalDateTime fechaCreacion;
    }
}
