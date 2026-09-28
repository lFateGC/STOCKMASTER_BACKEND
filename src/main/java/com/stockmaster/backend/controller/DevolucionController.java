package com.stockmaster.backend.controller;

import com.stockmaster.backend.dto.DevolucionDto;
import com.stockmaster.backend.service.DevolucionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devoluciones")
@RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionService devolucionService;

    @GetMapping
    public ResponseEntity<List<DevolucionDto.Response>> getAll() {
        return ResponseEntity.ok(devolucionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevolucionDto.Response> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(devolucionService.findById(id, userDetails != null ? userDetails.getUsername() : null));
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<List<DevolucionDto.Response>> getByVentaId(@PathVariable Long ventaId) {
        return ResponseEntity.ok(devolucionService.findByVentaId(ventaId));
    }

    @PostMapping
    public ResponseEntity<DevolucionDto.Response> create(
            @Valid @RequestBody DevolucionDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(devolucionService.createDevolucion(request, userDetails != null ? userDetails.getUsername() : null));
    }
}
