package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.ClienteDto;
import com.stockmaster.backend.entity.Cliente;
import com.stockmaster.backend.exception.BadRequestException;
import com.stockmaster.backend.exception.ResourceNotFoundException;
import com.stockmaster.backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    public List<ClienteDto.Response> findAll() {
        return clienteRepository.findAllByOrderByNombreAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ClienteDto.Response> findByEstado(String estado) {
        return clienteRepository.findByEstadoOrderByNombreAsc(estado).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ClienteDto.Response> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String q = query.trim();
        return clienteRepository.findByNombreContainingIgnoreCaseOrNumeroDocumentoContainingIgnoreCaseOrderByNombreAsc(q, q).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ClienteDto.Response findById(Long id, String userCorreo) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        auditoriaService.registrar(
                userCorreo,
                "VER",
                "CLIENTES",
                c.getNombre(),
                "Visualización de ficha de cliente (" + c.getTipoDocumento() + ": " + c.getNumeroDocumento() + ")"
        );

        return mapToDto(c);
    }

    @Transactional
    public ClienteDto.Response create(ClienteDto.Request request, String userCorreo) {
        if (request.getNumeroDocumento() == null || request.getNumeroDocumento().isBlank()) {
            throw new BadRequestException("El número de documento (DNI o RUC) es obligatorio y no puede estar vacío.");
        }

        String doc = request.getNumeroDocumento().trim();
        String tipo = request.getTipoDocumento() != null && !request.getTipoDocumento().isBlank()
                ? request.getTipoDocumento().toUpperCase().trim() : "DNI";

        if ("DNI".equalsIgnoreCase(tipo) && !doc.matches("^\\d{8}$")) {
            throw new BadRequestException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        if ("RUC".equalsIgnoreCase(tipo) && !doc.matches("^\\d{11}$")) {
            throw new BadRequestException("El RUC debe tener exactamente 11 dígitos numéricos.");
        }

        if (clienteRepository.existsByNumeroDocumento(doc)) {
            throw new BadRequestException("Ya existe un cliente registrado con el documento: " + doc);
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre().trim())
                .tipoDocumento(tipo)
                .numeroDocumento(doc)
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .direccion(request.getDireccion())
                .estado(request.getEstado() != null && !request.getEstado().isBlank() ? request.getEstado() : "activo")
                .build();

        Cliente guardado = clienteRepository.save(cliente);

        auditoriaService.registrar(
                userCorreo,
                "CREAR",
                "CLIENTES",
                guardado.getNombre(),
                "Registro de nuevo cliente (" + tipo + ": " + doc + ")"
        );

        return mapToDto(guardado);
    }

    @Transactional
    public ClienteDto.Response update(Long id, ClienteDto.Request request, String userCorreo) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (request.getNumeroDocumento() == null || request.getNumeroDocumento().isBlank()) {
            throw new BadRequestException("El número de documento (DNI o RUC) no puede quedar vacío.");
        }

        String doc = request.getNumeroDocumento().trim();
        String tipo = request.getTipoDocumento() != null && !request.getTipoDocumento().isBlank()
                ? request.getTipoDocumento().toUpperCase().trim() : cliente.getTipoDocumento();

        if ("DNI".equalsIgnoreCase(tipo) && !doc.matches("^\\d{8}$")) {
            throw new BadRequestException("El DNI debe tener exactamente 8 dígitos numéricos.");
        }
        if ("RUC".equalsIgnoreCase(tipo) && !doc.matches("^\\d{11}$")) {
            throw new BadRequestException("El RUC debe tener exactamente 11 dígitos numéricos.");
        }

        if (!doc.equalsIgnoreCase(cliente.getNumeroDocumento()) && clienteRepository.existsByNumeroDocumento(doc)) {
            throw new BadRequestException("Ya existe otro cliente registrado con el documento: " + doc);
        }

        cliente.setNombre(request.getNombre().trim());
        cliente.setTipoDocumento(tipo);
        cliente.setNumeroDocumento(doc);
        cliente.setTelefono(request.getTelefono());
        cliente.setCorreo(request.getCorreo());
        cliente.setDireccion(request.getDireccion());
        if (request.getEstado() != null) {
            cliente.setEstado(request.getEstado());
        }

        Cliente actualizado = clienteRepository.save(cliente);

        auditoriaService.registrar(
                userCorreo,
                "EDITAR",
                "CLIENTES",
                actualizado.getNombre(),
                "Actualización de cliente (" + tipo + ": " + doc + ")"
        );

        return mapToDto(actualizado);
    }

    @Transactional
    public void delete(Long id, String userCorreo) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        cliente.setEstado("inactivo");
        clienteRepository.save(cliente);

        auditoriaService.registrar(
                userCorreo,
                "ELIMINAR",
                "CLIENTES",
                cliente.getNombre(),
                "Inactivación de cliente (" + cliente.getTipoDocumento() + ": " + cliente.getNumeroDocumento() + ")"
        );
    }

    public ClienteDto.Response mapToDto(Cliente c) {
        return ClienteDto.Response.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .tipoDocumento(c.getTipoDocumento())
                .numeroDocumento(c.getNumeroDocumento())
                .telefono(c.getTelefono())
                .correo(c.getCorreo())
                .direccion(c.getDireccion())
                .estado(c.getEstado())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}
