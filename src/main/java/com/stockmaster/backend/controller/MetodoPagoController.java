package com.stockmaster.backend.controller;

import com.stockmaster.backend.dto.MetodoPagoDto;
import com.stockmaster.backend.service.MetodoPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
@RequiredArgsConstructor
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    @GetMapping
    public ResponseEntity<List<MetodoPagoDto.Response>> getAll() {
        return ResponseEntity.ok(metodoPagoService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetodoPagoDto.Response> create(@Valid @RequestBody MetodoPagoDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metodoPagoService.create(request));
    }
}
