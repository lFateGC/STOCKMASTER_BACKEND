package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findAllByOrderByFechaMovimientoDesc();
    List<MovimientoStock> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);
    List<MovimientoStock> findByTipoOrderByFechaMovimientoDesc(String tipo);
    List<MovimientoStock> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(LocalDateTime from, LocalDateTime to);
}
