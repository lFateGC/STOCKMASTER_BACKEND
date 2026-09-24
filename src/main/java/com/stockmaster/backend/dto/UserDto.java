package com.stockmaster.backend.dto;

import com.stockmaster.backend.entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {
        private Long id;
        private String correo;
        private String nombreCompleto;
        private String rol;
        private String estado;
        private String avatarUrl;
        private LocalDateTime fechaCreacion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateUserRequest {
        private String nombreCompleto;
        private Rol rol;
        private String estado;
        private String password;
        private String avatarUrl;
    }
}
