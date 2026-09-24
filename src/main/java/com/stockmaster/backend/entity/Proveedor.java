package com.stockmaster.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(unique = true, length = 20)
    private String ruc;

    @Convert(converter = com.stockmaster.backend.security.EncryptedStringConverter.class)
    @Column(name = "telefono", length = 255)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Convert(converter = com.stockmaster.backend.security.EncryptedStringConverter.class)
    @Column(name = "direccion", length = 500)
    private String direccion;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
