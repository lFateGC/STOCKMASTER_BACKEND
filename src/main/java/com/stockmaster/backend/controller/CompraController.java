package com.stockmaster.backend.controller;

import com.stockmaster.backend.dto.CompraDto;
import com.stockmaster.backend.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CompraDto.Response>> getAll(
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (proveedorId != null) {
            return ResponseEntity.ok(compraService.findByProveedor(proveedorId));
        }
        if (from != null && to != null) {
            return ResponseEntity.ok(compraService.findByDateRange(from, to));
        }
        return ResponseEntity.ok(compraService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompraDto.Response> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String correo = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(compraService.findById(id, correo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompraDto.Response> create(
            @Valid @RequestBody CompraDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compraService.createCompra(request, userDetails.getUsername()));
    }
}
