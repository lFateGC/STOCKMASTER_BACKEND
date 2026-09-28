package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    List<Devolucion> findAllByOrderByFechaDevolucionDesc();

    List<Devolucion> findByVentaIdOrderByFechaDevolucionDesc(Long ventaId);

    List<Devolucion> findByFechaDevolucionBetweenOrderByFechaDevolucionDesc(LocalDateTime from, LocalDateTime to);
}
