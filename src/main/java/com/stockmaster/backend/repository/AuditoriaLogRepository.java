package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {

    List<AuditoriaLog> findAllByOrderByFechaDesc();

    List<AuditoriaLog> findByModuloIgnoreCaseOrderByFechaDesc(String modulo);

    List<AuditoriaLog> findByFechaBetweenOrderByFechaDesc(LocalDateTime from, LocalDateTime to);
}
