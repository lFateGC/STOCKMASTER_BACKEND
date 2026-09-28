package com.stockmaster.backend.controller;

import com.stockmaster.backend.dto.ProductoDto;
import com.stockmaster.backend.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoDto.Response>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        if (all) {
            return ResponseEntity.ok(productoService.findAll());
        }
        return ResponseEntity.ok(productoService.findActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto.Response> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productoService.findById(id, userDetails != null ? userDetails.getUsername() : null));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductoDto.Response>> search(@RequestParam String q) {
        return ResponseEntity.ok(productoService.search(q));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductoDto.Response>> getLowStock() {
        return ResponseEntity.ok(productoService.findLowStock());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDto.Response> create(
            @Valid @RequestBody ProductoDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.create(request, userDetails != null ? userDetails.getUsername() : null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDto.Response> update(
            @PathVariable Long id,
            @RequestBody ProductoDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productoService.update(id, request, userDetails != null ? userDetails.getUsername() : null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductoDto.Response> patchUpdate(
            @PathVariable Long id,
            @RequestBody ProductoDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productoService.update(id, request, userDetails != null ? userDetails.getUsername() : null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productoService.delete(id, userDetails != null ? userDetails.getUsername() : null);
        return ResponseEntity.noContent().build();
    }
}
