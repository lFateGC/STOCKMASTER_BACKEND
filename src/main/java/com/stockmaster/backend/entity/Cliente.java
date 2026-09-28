package com.stockmaster.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", unique = true, length = 20)
    private String numeroDocumento;

    @Convert(converter = com.stockmaster.backend.security.EncryptedStringConverter.class)
    @Column(length = 255)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Convert(converter = com.stockmaster.backend.security.EncryptedStringConverter.class)
    @Column(length = 500)
    private String direccion;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "activo";

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = "activo";
        }
        if (this.tipoDocumento == null) {
            this.tipoDocumento = "DNI";
        }
    }
}
