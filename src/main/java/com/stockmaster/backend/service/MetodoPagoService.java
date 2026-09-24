package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.MetodoPagoDto;
import com.stockmaster.backend.entity.MetodoPago;
import com.stockmaster.backend.exception.BadRequestException;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.MetodoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;

    public List<MetodoPagoDto.Response> findAll() {
        return metodoPagoRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public MetodoPago findEntityById(Long id) {
        if (id == null) return null;
        return metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no encontrado con ID: " + id));
    }

    @Transactional
    public MetodoPagoDto.Response create(MetodoPagoDto.Request request) {
        if (metodoPagoRepository.existsByNombre(request.getNombre().trim())) {
            throw new BadRequestException("Ya existe un método de pago con el nombre: " + request.getNombre());
        }

        MetodoPago metodoPago = MetodoPago.builder()
                .nombre(request.getNombre().trim())
                .build();

        return mapToDto(metodoPagoRepository.save(metodoPago));
    }

    public MetodoPagoDto.Response mapToDto(MetodoPago mp) {
        return MetodoPagoDto.Response.builder()
                .id(mp.getId())
                .nombre(mp.getNombre())
                .build();
    }
}
