package com.stockmaster.backend.repository;

import com.stockmaster.backend.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByEstado(String estado);
    List<Producto> findByCategoriaId(Long categoriaId);
    Optional<Producto> findBySku(String sku);
    boolean existsBySku(String sku);

    @Query("SELECT p FROM Producto p WHERE p.estado = 'activo' AND p.stock <= p.stockMinimo")
    List<Producto> findLowStockProducts();

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Producto> searchProducts(String query);
}
