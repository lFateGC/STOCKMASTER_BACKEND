package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByNumeroBoleta(String numeroBoleta);
    boolean existsByNumeroBoleta(String numeroBoleta);
    List<Venta> findAllByOrderByFechaVentaDesc();
    List<Venta> findByVendedorIdOrderByFechaVentaDesc(Long vendedorId);
    List<Venta> findByFechaVentaBetweenOrderByFechaVentaDesc(LocalDateTime from, LocalDateTime to);
}
