package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.ProveedorDto;
import com.stockmaster.backend.entity.Proveedor;
import com.stockmaster.backend.exception.BadRequestException;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<ProveedorDto.Response> findAll() {
        return proveedorRepository.findAllByOrderByRazonSocialAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProveedorDto.Response findById(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
        return mapToDto(proveedor);
    }

    public Proveedor findEntityById(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
    }

    @Transactional
    public ProveedorDto.Response create(ProveedorDto.Request request) {
        if (request.getRuc() != null && !request.getRuc().isBlank()) {
            if (proveedorRepository.existsByRuc(request.getRuc().trim())) {
                throw new BadRequestException("Ya existe un proveedor con el RUC: " + request.getRuc());
            }
        }

        Proveedor proveedor = Proveedor.builder()
                .razonSocial(request.getRazonSocial().trim())
                .ruc(request.getRuc() != null && !request.getRuc().isBlank() ? request.getRuc().trim() : null)
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .direccion(request.getDireccion())
                .build();

        return mapToDto(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorDto.Response update(Long id, ProveedorDto.Request request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        if (request.getRuc() != null && !request.getRuc().isBlank()) {
            if (!request.getRuc().equalsIgnoreCase(proveedor.getRuc()) &&
                    proveedorRepository.existsByRuc(request.getRuc().trim())) {
                throw new BadRequestException("Ya existe un proveedor con el RUC: " + request.getRuc());
            }
            proveedor.setRuc(request.getRuc().trim());
        }

        proveedor.setRazonSocial(request.getRazonSocial().trim());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setDireccion(request.getDireccion());

        return mapToDto(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void delete(Long id) {
        if (!proveedorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + id);
        }
        proveedorRepository.deleteById(id);
    }

    public ProveedorDto.Response mapToDto(Proveedor p) {
        return ProveedorDto.Response.builder()
                .id(p.getId())
                .razonSocial(p.getRazonSocial())
                .ruc(p.getRuc())
                .telefono(p.getTelefono())
                .correo(p.getCorreo())
                .direccion(p.getDireccion())
                .fechaCreacion(p.getFechaCreacion())
                .build();
    }
}
