package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.CategoriaDto;
import com.stockmaster.backend.entity.Categoria;
import com.stockmaster.backend.exception.BadRequestException;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaDto.Response> findAll() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CategoriaDto.Response findById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return mapToDto(categoria);
    }

    public Categoria findEntityById(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    @Transactional
    public CategoriaDto.Response create(CategoriaDto.Request request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null)
                .build();

        return mapToDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDto.Response update(Long id, CategoriaDto.Request request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre()) &&
                categoriaRepository.existsByNombre(request.getNombre())) {
            throw new BadRequestException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);

        return mapToDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    public CategoriaDto.Response mapToDto(Categoria categoria) {
        return CategoriaDto.Response.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .fechaCreacion(categoria.getFechaCreacion())
                .build();
    }
}
