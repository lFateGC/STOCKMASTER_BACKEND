package com.stockmaster.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devoluciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_devolucion", nullable = false, unique = true, length = 50)
    private String numeroDevolucion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String cliente;

    @Column(nullable = false, length = 150)
    private String motivo;

    @Column(name = "destino_stock", nullable = false, length = 50)
    @Builder.Default
    private String destinoStock = "reingreso"; // reingreso, merma

    @Column(name = "metodo_reembolso", nullable = false, length = 50)
    @Builder.Default
    private String metodoReembolso = "efectivo"; // efectivo, nota_credito, transferencia

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "completada";

    @Convert(converter = com.stockmaster.backend.security.EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_devolucion", updatable = false)
    private LocalDateTime fechaDevolucion;

    @OneToMany(mappedBy = "devolucion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetalleDevolucion> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.fechaDevolucion == null) {
            this.fechaDevolucion = LocalDateTime.now();
        }
        if (this.estado == null) this.estado = "completada";
        if (this.destinoStock == null) this.destinoStock = "reingreso";
        if (this.metodoReembolso == null) this.metodoReembolso = "efectivo";
    }

    public void addDetalle(DetalleDevolucion detalle) {
        detalles.add(detalle);
        detalle.setDevolucion(this);
    }
}
