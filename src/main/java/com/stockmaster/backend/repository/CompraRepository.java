package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findAllByOrderByFechaCompraDesc();
    List<Compra> findByProveedorIdOrderByFechaCompraDesc(Long proveedorId);
    List<Compra> findByFechaCompraBetweenOrderByFechaCompraDesc(LocalDateTime from, LocalDateTime to);
}
