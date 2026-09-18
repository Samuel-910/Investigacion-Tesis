package com.pe.articulos.modules.auth.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.exception.AuthException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.core.reports.audit.service.AuditService;
import com.pe.articulos.core.security.util.HttpUtils;
import com.pe.articulos.modules.auth.service.dto.LoginResponse;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.puntos.dto.PuntoDto;
import com.pe.articulos.modules.puntos.repository.PuntoRepository;
import com.pe.articulos.modules.sucursal.dto.SucursalDto;
import com.pe.articulos.modules.sucursal.repository.SucursalRepository;
import com.pe.articulos.modules.sucursal.service.SucursalService;
import com.pe.articulos.modules.notificaciones.service.AlertaSchedulerService;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class AuthSucursalService {

        private final DatosPersonalesRepository datosPersonalesRepository;
        private final SucursalService sucursalService;
        private final SucursalRepository sucursalRepository;
        private final PuntoRepository puntoRepository;
        private final JwtService jwtService;
        private final AuditService auditService;
        private final PasswordEncoder passwordEncoder;
        private final AlertaSchedulerService alertaSchedulerService;

        public List<SucursalDto> obtenerSucursalesDisponibles(Long userId) {
                DatosPersonales user = datosPersonalesRepository.findByidWithRoles(userId)
                                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

                return obtenerSucursalesAutorizadas(user);
        }

        public List<PuntoDto> obtenerPuntosDeSucursalActual(Long userId, Long sucursalId) {
                Long idFinal = sucursalId;


                if (idFinal == null) {
                        DatosPersonales user = datosPersonalesRepository.findById(userId)
                                        .orElseThrow(() -> new AuthException("Usuario no encontrado"));

                        if (user.getSucursalActual() == null) {
                                return List.of();
                        }
                        idFinal = user.getSucursalActual().getIdSucursal();
                }

                return puntoRepository.findByIdSucursal(idFinal.intValue()).stream()
                                .map(p -> PuntoDto.builder()
                                                .id(p.getPunto())
                                                .nombre(p.getNombre())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional
        public LoginResponse seleccionarSucursal(Long userId, Long sucursalId, String password,
                        HttpServletRequest httpRequest) {
                String ipAddress = HttpUtils.getClientIpAddress(httpRequest);
                String userAgent = HttpUtils.getUserAgent(httpRequest);


                DatosPersonales user = datosPersonalesRepository.findByidWithRoles(userId)
                                .orElseThrow(() -> new AuthException("Usuario no encontrado"));


                String authenticatedUsername = org.springframework.security.core.context.SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                if (!user.getLogin().equals(authenticatedUsername)) {
                        log.warn("Intento de cambio de sucursal con usuario diferente. Autenticado: {}, Ingresado: {}",
                                        authenticatedUsername, user.getLogin());
                        throw new AuthException("Debe usar las credenciales con las que inició sesión");
                }


                if (!passwordEncoder.matches(password, user.getPassword())) {
                        log.warn("Intento de cambio de sucursal con contraseña incorrecta para usuario: {}",
                                        user.getLogin());
                        throw new AuthException("Contraseña incorrecta");
                }


                List<SucursalDto> autorizadas = obtenerSucursalesAutorizadas(user);
                boolean autorizada = autorizadas.stream()
                                .anyMatch(s -> s.getIdSucursal().equals(sucursalId));

                if (!autorizada) {
                        throw new AuthException("No tiene autorización para acceder a esta sucursal");
                }


                SucursalDto sucursalDto = sucursalService.obtenerPorId(sucursalId)
                                .orElseThrow(() -> new ValidationException("Sucursal no encontrada"));


                Sucursal sucursalEntity = sucursalRepository
                                .findById(sucursalId)
                                .orElseThrow(() -> new ValidationException("Sucursal no encontrada"));
                user.setSucursalActual(sucursalEntity);
                datosPersonalesRepository.save(user);


                String token = jwtService.generateTokenWithSucursal(user, sucursalId, sucursalDto.getNombreSucursal());


                registrarSeleccionSucursal(user, sucursalId, sucursalDto.getNombreSucursal(), ipAddress, userAgent);


                return construirLoginResponse(user, token, sucursalId, sucursalDto.getNombreSucursal());
        }

        @Transactional
        public LoginResponse seleccionarPunto(Long userId, Long puntoId, String password,
                        HttpServletRequest httpRequest) {
                DatosPersonales user = datosPersonalesRepository.findById(userId)
                                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

                if (!passwordEncoder.matches(password, user.getPasswd())) {
                        throw new AuthException("Contraseña incorrecta");
                }

                Punto punto = puntoRepository.findById(puntoId)
                                .orElseThrow(() -> new ValidationException("Punto de venta no encontrado"));


                user.setPunto(punto);

                if (punto.getIdSucursal() != null) {
                        Sucursal sucursal = sucursalRepository.findById(punto.getIdSucursal().longValue())
                                        .orElse(user.getSucursalActual());
                        user.setSucursalActual(sucursal);
                }
                datosPersonalesRepository.save(user);

                String sucursalNombre = user.getSucursalActual() != null ? user.getSucursalActual().getNombreSucursal()
                                : "N/A";
                Long sucursalId = user.getSucursalActual() != null ? user.getSucursalActual().getIdSucursal() : 1L;

                String token = jwtService.generateTokenWithSucursal(user, sucursalId, sucursalNombre);

                final Long finalSucursalId = sucursalId;
                CompletableFuture.runAsync(() -> {
                    try {
                        alertaSchedulerService.generarAlertasParaSucursal(finalSucursalId);
                    } catch (Exception e) {
                        log.error("Error generating alerts async for sucursal " + finalSucursalId, e);
                    }
                });

                auditService.logAction(
                                "AUTH",
                                "SELECT_PUNTO",
                                "Cambio de punto de venta a: " + punto.getNombre(),
                                user.getUsername(),
                                user.getId(),
                                HttpUtils.getClientIpAddress(httpRequest),
                                "Éxito",
                                HttpUtils.getUserAgent(httpRequest),
                                "/api/auth/puntos/select",
                                "POST");

                return construirLoginResponse(user, token, sucursalId, sucursalNombre);
        }

        private List<SucursalDto> obtenerSucursalesAutorizadas(DatosPersonales user) {

                if (user.hasRole("ADMIN") || user.hasRole("ADMINISTRADOR")) {
                        return sucursalService.obtenerActivasAsList();
                }


                if (user.getSucursalesAsignadas() != null && !user.getSucursalesAsignadas().isEmpty()) {
                        return user.getSucursalesAsignadas().stream()
                                        .map(sucursal -> {
                                                SucursalDto dto = new SucursalDto();
                                                dto.setIdSucursal(sucursal.getIdSucursal());
                                                dto.setNombreSucursal(sucursal.getNombreSucursal());
                                                dto.setEstado(sucursal.getEstado());
                                                return dto;
                                        })
                                        .collect(Collectors.toList());
                }


                return List.of();
        }

        private LoginResponse construirLoginResponse(DatosPersonales user, String token, Long sucursalId,
                        String sucursalNombre) {
                Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

                Set<String> permissions = authorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(auth -> !auth.startsWith("ROLE_"))
                                .collect(Collectors.toSet());

                Set<String> roles = authorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(auth -> auth.startsWith("ROLE_"))
                                .map(role -> role.replace("ROLE_", ""))
                                .collect(Collectors.toSet());

                return LoginResponse.builder()
                                .token(token)
                                .type("Bearer")
                                .userId(user.getId())
                                .idPersonal(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .firstName(user.getNombre())
                                .sexo(user.getSexo())
                                .lastName(user.getApepat() + " " + user.getApemat())
                                .roles(roles)
                                .permissions(permissions)
                                .sucursalId(sucursalId)
                                .sucursalNombre(sucursalNombre)
                                .build();
        }

        private void registrarSeleccionSucursal(DatosPersonales user, Long sucursalId, String sucursalNombre,
                        String ipAddress, String userAgent) {
                try {
                        String details = String.format("{\"sucursal_id\": %d, \"sucursal_nombre\": \"%s\"}", sucursalId,
                                        sucursalNombre);

                        auditService.logAction(
                                        "SESION",
                                        "SELECCION_SUCURSAL",
                                        details,
                                        user.getUsername(),
                                        user.getId(),
                                        ipAddress,
                                        "Éxito",
                                        userAgent,
                                        "/api/auth/select-sucursal",
                                        "POST");
                } catch (Exception e) {
                        log.error("Error al registrar auditoría de selección de sucursal", e);
                }
        }
}
