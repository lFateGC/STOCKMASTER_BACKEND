package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.AuditoriaLogDto;
import com.stockmaster.backend.entity.AuditoriaLog;
import com.stockmaster.backend.entity.Usuario;
import com.stockmaster.backend.repository.AuditoriaLogRepository;
import com.stockmaster.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final AuditoriaLogRepository auditoriaLogRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrar(String userCorreo, String accion, String modulo, String nombreRecurso, String detalle) {
        try {
            String quien = userCorreo != null ? userCorreo : "Sistema";
            if (userCorreo != null) {
                Usuario u = usuarioRepository.findByCorreo(userCorreo).orElse(null);
                if (u != null) {
                    quien = u.getNombreCompleto();
                }
            }

            AuditoriaLog logEntry = AuditoriaLog.builder()
                    .usuarioCorreo(userCorreo)
                    .quienLoHizo(quien)
                    .accion(accion != null ? accion.toUpperCase() : "ACCION")
                    .modulo(modulo != null ? modulo.toUpperCase() : "GENERAL")
                    .nombreRecurso(nombreRecurso != null ? nombreRecurso : "Sin especificar")
                    .detalle(detalle)
                    .build();

            auditoriaLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Error al registrar auditoría: {}", e.getMessage());
        }
    }

    public List<AuditoriaLogDto.Response> findAll() {
        return auditoriaLogRepository.findAllByOrderByFechaDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AuditoriaLogDto.Response> findByModulo(String modulo) {
        return auditoriaLogRepository.findByModuloIgnoreCaseOrderByFechaDesc(modulo).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AuditoriaLogDto.Response> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return auditoriaLogRepository.findByFechaBetweenOrderByFechaDesc(from, to).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AuditoriaLogDto.Response mapToDto(AuditoriaLog log) {
        return AuditoriaLogDto.Response.builder()
                .id(log.getId())
                .nombreRecurso(log.getNombreRecurso())
                .accion(log.getAccion())
                .quienLoHizo(log.getQuienLoHizo())
                .usuarioCorreo(log.getUsuarioCorreo())
                .modulo(log.getModulo())
                .detalle(log.getDetalle())
                .fecha(log.getFecha())
                .build();
    }
}
