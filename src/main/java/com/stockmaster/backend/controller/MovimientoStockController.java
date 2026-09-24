package com.stockmaster.backend.controller;

import com.stockmaster.backend.dto.MovimientoStockDto;
import com.stockmaster.backend.service.MovimientoStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    @GetMapping
    public ResponseEntity<List<MovimientoStockDto.Response>> getAll(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (productoId != null) {
            return ResponseEntity.ok(movimientoStockService.findByProducto(productoId));
        }
        if (tipo != null && !tipo.isBlank()) {
            return ResponseEntity.ok(movimientoStockService.findByTipo(tipo.toLowerCase()));
        }
        if (from != null && to != null) {
            return ResponseEntity.ok(movimientoStockService.findByDateRange(from, to));
        }
        return ResponseEntity.ok(movimientoStockService.findAll());
    }

    @PostMapping
    public ResponseEntity<MovimientoStockDto.Response> create(
            @Valid @RequestBody MovimientoStockDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoStockService.createMovimiento(request, userDetails.getUsername()));
    }
}
