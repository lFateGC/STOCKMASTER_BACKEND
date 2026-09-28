package com.stockmaster.backend.controller;

import com.stockmaster.backend.dto.AuditoriaLogDto;
import com.stockmaster.backend.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaLogDto.Response>> getAll(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (modulo != null && !modulo.isBlank()) {
            return ResponseEntity.ok(auditoriaService.findByModulo(modulo));
        }
        if (from != null && to != null) {
            return ResponseEntity.ok(auditoriaService.findByDateRange(from, to));
        }
        return ResponseEntity.ok(auditoriaService.findAll());
    }
}
