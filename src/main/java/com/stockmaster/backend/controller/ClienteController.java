package com.stockmaster.backend.controller;

import com.stockmaster.backend.dto.ClienteDto;
import com.stockmaster.backend.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDto.Response>> getAll(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String q) {

        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(clienteService.search(q));
        }
        if (estado != null && !estado.isBlank()) {
            return ResponseEntity.ok(clienteService.findByEstado(estado));
        }
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto.Response> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(clienteService.findById(id, userDetails != null ? userDetails.getUsername() : null));
    }

    @PostMapping
    public ResponseEntity<ClienteDto.Response> create(
            @Valid @RequestBody ClienteDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.create(request, userDetails != null ? userDetails.getUsername() : null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody ClienteDto.Request request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(clienteService.update(id, request, userDetails != null ? userDetails.getUsername() : null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        clienteService.delete(id, userDetails != null ? userDetails.getUsername() : null);
        return ResponseEntity.noContent().build();
    }
}
