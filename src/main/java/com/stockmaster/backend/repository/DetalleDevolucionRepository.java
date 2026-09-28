package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.DetalleDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleDevolucionRepository extends JpaRepository<DetalleDevolucion, Long> {

    List<DetalleDevolucion> findByDevolucionId(Long devolucionId);
}
