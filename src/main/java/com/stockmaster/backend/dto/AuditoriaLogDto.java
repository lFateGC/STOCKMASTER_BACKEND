package com.stockmaster.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AuditoriaLogDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String nombreRecurso;
        private String accion;
        private String quienLoHizo;
        private String usuarioCorreo;
        private String modulo;
        private String detalle;
        private LocalDateTime fecha;
    }
}
