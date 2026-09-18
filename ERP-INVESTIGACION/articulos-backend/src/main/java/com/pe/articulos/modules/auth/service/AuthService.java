package com.pe.articulos.modules.auth.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.core.exception.AuthException;
import com.pe.articulos.core.exception.ValidationException;
import com.pe.articulos.core.reports.audit.service.AuditService;
import com.pe.articulos.core.security.entity.FailureReason;
import com.pe.articulos.core.security.service.SecurityMonitorService;
import com.pe.articulos.core.security.util.HttpUtils;
import com.pe.articulos.modules.auth.service.dto.AccesoDto;
import com.pe.articulos.modules.auth.service.dto.LoginRequest;
import com.pe.articulos.modules.auth.service.dto.LoginResponse;
import com.pe.articulos.modules.auth.service.dto.RegisterRequest;
import com.pe.articulos.modules.auth.service.dto.RegisterResponse;
import com.pe.articulos.modules.auth.service.dto.TokenInfoResponse;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.roles.entity.Role;
import com.pe.articulos.modules.roles.repository.RoleRepository;
import com.pe.articulos.modules.accesos.repository.AccesoMainRepository;
import com.pe.articulos.modules.notificaciones.service.AlertaSchedulerService;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("null")
public class AuthService {

    private final DatosPersonalesRepository datosPersonalesRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityMonitorService securityMonitorService;
    private final AuditService auditService;
    private final AccesoMainRepository accesoMainRepository;
    private final AlertaSchedulerService alertaSchedulerService;

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = HttpUtils.getClientIpAddress(httpRequest);
        String userAgent = HttpUtils.getUserAgent(httpRequest);

        log.info("Login attempt for user: {} from IP: {}", request.getLogin(), ipAddress);


        if (securityMonitorService.isIpBlocked(ipAddress)) {
            log.warn("Login attempt from blocked IP: {}", ipAddress);

            int remainingAttempts = securityMonitorService.getRemainingAttempts(ipAddress);
            var unblockTime = securityMonitorService.getUnblockTime(ipAddress);

            securityMonitorService.registerFailedAttempt(
                    request.getLogin(),
                    ipAddress,
                    userAgent,
                    FailureReason.IP_BLOCKED);

            throw new AuthException(
                    "IP bloqueada temporalmente por intentos sospechosos. Intente nuevamente más tarde.",
                    remainingAttempts,
                    true,
                    unblockTime);
        }


        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getPassword()));
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {} from IP: {}", request.getLogin(), ipAddress);


            securityMonitorService.registerFailedAttempt(
                    request.getLogin(),
                    ipAddress,
                    userAgent,
                    FailureReason.INVALID_CREDENTIALS);


            int remainingAttempts = securityMonitorService.getRemainingAttempts(ipAddress);


            String message;
            if (remainingAttempts == 0) {
                message = "Credenciales inválidas. Su IP ha sido bloqueada temporalmente por intentos excesivos.";
            } else if (remainingAttempts == 1) {
                message = "Credenciales inválidas. ⚠️ ÚLTIMO INTENTO antes del bloqueo.";
            } else if (remainingAttempts <= 2) {
                message = String.format("Credenciales inválidas. Le quedan %d intentos.", remainingAttempts);
            } else {
                message = "Credenciales inválidas. Usuario o contraseña incorrectos.";
            }


            try {
                auditService.logAction(
                        "LOGIN",
                        "LOGIN_FAILED",
                        "Intento fallido de inicio de sesión. " + message,
                        request.getLogin(),
                        null,
                        ipAddress,
                        "Fallo",
                        userAgent,
                        "/api/auth/login",
                        "POST");
            } catch (Exception ex) {
                log.error("Error registrando auditoría de fallo de login: {}", ex.getMessage());
            }

            throw new AuthException(message, remainingAttempts, remainingAttempts == 0, null);
        }


        try {
            DatosPersonales user = datosPersonalesRepository.findByLoginWithRoles(request.getLogin())
                    .orElseThrow(() -> {
                        securityMonitorService.registerFailedAttempt(
                                request.getLogin(),
                                ipAddress,
                                userAgent,
                                FailureReason.USER_NOT_FOUND);
                        return new AuthException("Usuario no encontrado");
                    });


            if (!user.getActive()) {
                log.warn("Attempt to login with inactive account: {}", request.getLogin());
                securityMonitorService.registerFailedAttempt(
                        request.getLogin(),
                        ipAddress,
                        userAgent,
                        FailureReason.ACCOUNT_BLOCKED);
                throw new AuthException("La cuenta ha sido bloqueada. Contacte al administrador.");
            }

            // Validar acceso al módulo fijo "Articulos" (AccesoMain)
            String moduloRequerido = "Articulos";
            boolean hasDirectAssignment = user.getModulosAsignados().stream()
                    .anyMatch(m -> m.getNombre().equalsIgnoreCase(moduloRequerido));
            boolean hasRoleAssignment = user.getAuthoritiesByModulo(moduloRequerido).size() > 0;
            
            if (!hasDirectAssignment && !hasRoleAssignment) {
                log.warn("User {} attempted to login without access to module {}", request.getLogin(), moduloRequerido);
                securityMonitorService.registerFailedAttempt(
                        request.getLogin(),
                        ipAddress,
                        userAgent,
                        FailureReason.UNAUTHORIZED_ACCESS);
                throw new com.pe.articulos.core.exception.UnauthorizedException("No tiene permisos para acceder al módulo " + moduloRequerido + ". Este sistema es exclusivo para " + moduloRequerido + ".");
            }

            user.setLastLogin(LocalDateTime.now());
            datosPersonalesRepository.save(user);


            log.info("Generating token for user: {}", user.getUsername());


            String token;
            Long sucursalIdForAlerts = 1L;
            if (user.getSucursalActual() != null) {
                sucursalIdForAlerts = user.getSucursalActual().getIdSucursal();
                token = jwtService.generateTokenWithSucursal(
                        user,
                        sucursalIdForAlerts,
                        user.getSucursalActual().getNombreSucursal());
                log.info("Token generated with saved sucursal: {}", user.getSucursalActual().getNombreSucursal());
            } else {

                token = jwtService.generateTokenWithSucursal(user, 1L, "PRINCIPAL");
                log.info("Token generated with DEFAULT sucursal (1L)");
            }

            final Long finalSucursalId = sucursalIdForAlerts;
            CompletableFuture.runAsync(() -> {
                try {
                    alertaSchedulerService.generarAlertasParaSucursal(finalSucursalId);
                } catch (Exception e) {
                    log.error("Error generating alerts async for sucursal " + finalSucursalId, e);
                }
            });

            securityMonitorService.registerSuccessfulLogin(user, ipAddress, userAgent, token);


            try {
                auditService.logAction(
                        "LOGIN",
                        "LOGIN_SUCCESS",
                        "Usuario " + user.getUsername() + " ha iniciado sesión exitosamente.",
                        user.getUsername(),
                        user.getId(),
                        ipAddress,
                        "Éxito",
                        userAgent,
                        "/api/auth/login",
                        "POST");
            } catch (Exception e) {
                log.error("Error logging audit for login", e);
            }

            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

            Set<String> allPermissions = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .collect(Collectors.toSet());
            Set<String> roleNames = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(role -> role.replace("ROLE_", ""))
                    .collect(Collectors.toSet());
            // log.info("Permisos finales enviados al Front: {}", allPermissions);


            java.util.List<AccesoDto> accesos = accesoMainRepository.findAll().stream()
                    .map(a -> AccesoDto.builder()
                            .id(a.getId())
                            .nombre(a.getNombre())
                            .build())
                    .collect(Collectors.toList());

            return LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getNombre())
                    .sexo(user.getSexo())
                    .lastName(user.getApepat() + " " + user.getApemat())
                    .roles(roleNames)
                    .permissions(allPermissions)
                    .puntoId(user.getPunto() != null ? user.getPunto().getPunto() : null)
                    .puntoNombre(user.getPunto() != null ? user.getPunto().getNombre() : null)
                    .accesos(accesos)
                    .build();
        } catch (Exception e) {
            log.error("CRITICAL UNEXPECTED ERROR during login flow for user: " + request.getLogin(), e);
            throw e;
        }
    }

    public RegisterResponse register(RegisterRequest request) {
        log.info("Register attempt for login: {}", request.getLogin());

        if (datosPersonalesRepository.existsByLogin(request.getLogin())) {
            throw new ValidationException("El nombre de usuario ya existe. Elija otro.");
        }

        if (datosPersonalesRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("El email ya está registrado.");
        }

        DatosPersonales newUser = DatosPersonales.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .passwd(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getFirstName())
                .apepat(request.getLastName())
                .fonLocal(request.getPhone())
                .active(true)
                .roles(new HashSet<>())
                .build();

        Set<Role> assignedRoles = new HashSet<>();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName.toUpperCase())
                        .orElseThrow(() -> new ValidationException("Rol no encontrado: " + roleName));
                assignedRoles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName("PACIENTE")
                    .orElseThrow(() -> new ValidationException("Rol PACIENTE no encontrado en el sistema"));
            assignedRoles.add(defaultRole);
        }

        newUser.setRoles(assignedRoles);

        DatosPersonales savedUser = datosPersonalesRepository.save(newUser);

        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        log.info("User {} registered successfully with roles: {}", savedUser.getUsername(), roleNames);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getNombre())
                .lastName(savedUser.getApepat() + " " + savedUser.getApemat())
                .roles(roleNames)
                .message("Usuario registrado exitosamente")
                .build();
    }

    public void logout(String token, HttpServletRequest httpRequest) {
        try {
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            String ipAddress = HttpUtils.getClientIpAddress(httpRequest);
            String userAgent = HttpUtils.getUserAgent(httpRequest);

            log.info("User {} logged out successfully", username);

            try {
                auditService.logAction(
                        "LOGOUT",
                        "LOGOUT",
                        "Usuario " + username + " cerró sesión.",
                        username,
                        userId,
                        ipAddress,
                        "Éxito",
                        userAgent,
                        "/api/auth/logout",
                        "POST");
            } catch (Exception e) {
                log.error("Error logging audit for logout", e);
            }

        } catch (Exception e) {
            log.error("Error processing logout request", e);
        }
    }

    public TokenInfoResponse getTokenInfo(String token) {
        return TokenInfoResponse.builder()
                .timeRemainingMs(jwtService.getTimeRemaining(token))
                .timeRemainingSeconds(jwtService.getTimeRemainingInSeconds(token))
                .expirationTimeMs(jwtService.getExpirationTime())
                .isExpired(jwtService.isTokenExpired(token))
                .build();
    }

    public TokenInfoResponse refreshToken(String oldToken) {
        String username = jwtService.extractUsername(oldToken);

        DatosPersonales user = datosPersonalesRepository.findByLoginWithRoles(username)
                .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        if (!user.getActive()) {
            throw new AuthException("Cuenta inactiva");
        }

        String newToken;
        if (user.getSucursalActual() != null) {
            newToken = jwtService.generateTokenWithSucursal(
                    user,
                    user.getSucursalActual().getIdSucursal(),
                    user.getSucursalActual().getNombreSucursal());
        } else {

            newToken = jwtService.generateTokenWithSucursal(user, 1L, "PRINCIPAL");
        }

        return TokenInfoResponse.builder()
                .token(newToken)
                .timeRemainingMs(jwtService.getTimeRemaining(newToken))
                .timeRemainingSeconds(jwtService.getTimeRemainingInSeconds(newToken))
                .expirationTimeMs(jwtService.getExpirationTime())
                .isExpired(false)
                .build();
    }
}