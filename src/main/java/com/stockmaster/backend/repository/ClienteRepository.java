package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByNombreAsc();

    List<Cliente> findByEstadoOrderByNombreAsc(String estado);

    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    boolean existsByNumeroDocumento(String numeroDocumento);

    List<Cliente> findByNombreContainingIgnoreCaseOrNumeroDocumentoContainingIgnoreCaseOrderByNombreAsc(
            String nombre, String numeroDocumento
    );
}
