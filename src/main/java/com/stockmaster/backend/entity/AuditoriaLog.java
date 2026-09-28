package com.stockmaster.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_recurso", nullable = false, length = 200)
    private String nombreRecurso;

    @Column(nullable = false, length = 50)
    private String accion; // CREAR, EDITAR, VER, ELIMINAR

    @Column(name = "quien_lo_hizo", nullable = false, length = 150)
    private String quienLoHizo;

    @Column(name = "usuario_correo", length = 150)
    private String usuarioCorreo;

    @Column(nullable = false, length = 50)
    private String modulo; // CLIENTES, VENTAS, DEVOLUCIONES, PRODUCTOS, COMPRAS, USUARIOS

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }
}
