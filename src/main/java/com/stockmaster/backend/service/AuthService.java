package com.stockmaster.backend.service;

import com.stockmaster.backend.dto.AuthDto;
import com.stockmaster.backend.entity.Rol;
import com.stockmaster.backend.entity.Usuario;
import com.stockmaster.backend.exception.BadRequestException;
import com.stockmaster.backend.repository.UsuarioRepository;
import com.stockmaster.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        String username = usuario.getCorreo().contains("@")
                ? usuario.getCorreo().substring(0, usuario.getCorreo().indexOf("@"))
                : usuario.getCorreo();

        String roleLower = usuario.getRol().name().toLowerCase();

        return AuthDto.LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(usuario.getId())
                .correo(usuario.getCorreo())
                .username(username)
                .displayName(usuario.getNombreCompleto())
                .rol(roleLower)
                .role(roleLower)
                .avatarUrl(usuario.getAvatarUrl())
                .build();
    }

    @Transactional
    public AuthDto.LoginResponse register(AuthDto.RegisterRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BadRequestException("El correo ya está registrado: " + request.getCorreo());
        }

        Usuario usuario = Usuario.builder()
                .correo(request.getCorreo())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .rol(request.getRol() != null ? request.getRol() : Rol.VENDEDOR)
                .estado("activo")
                .build();

        usuarioRepository.save(usuario);

        return login(new AuthDto.LoginRequest(request.getCorreo(), request.getPassword()));
    }
}
