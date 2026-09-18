package com.pe.articulos.modules.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.auth.service.AuthService;
import com.pe.articulos.modules.auth.service.AuthSucursalService;
import com.pe.articulos.modules.auth.service.dto.LoginRequest;
import com.pe.articulos.modules.auth.service.dto.LoginResponse;
import com.pe.articulos.modules.auth.service.dto.RegisterRequest;
import com.pe.articulos.modules.auth.service.dto.RegisterResponse;
import com.pe.articulos.modules.auth.service.dto.SelectPuntoRequest;
import com.pe.articulos.modules.auth.service.dto.TokenInfoResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final AuthSucursalService authSucursalService;

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest httpRequest) {

                LoginResponse loginResponse = authService.login(request, httpRequest);

                return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                                .success(true)
                                .message("Login exitoso")
                                .data(loginResponse)
                                .build());
        }

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {

                RegisterResponse registerResponse = authService.register(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<RegisterResponse>builder()
                                                .success(true)
                                                .message("Usuario registrado exitosamente")
                                                .data(registerResponse)
                                                .build());
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestHeader("Authorization") String token,
                        HttpServletRequest httpRequest) {

                String jwtToken = token.substring(7);
                authService.logout(jwtToken, httpRequest);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Logout exitoso")
                                .build());
        }

        @GetMapping("/health")
        public ResponseEntity<ApiResponse<String>> health() {
                return ResponseEntity.ok(ApiResponse.<String>builder()
                                .success(true)
                                .message("Auth service is running")
                                .data("OK")
                                .build());
        }

        @GetMapping("/token-info")
        public ResponseEntity<ApiResponse<TokenInfoResponse>> getTokenInfo(
                        @RequestHeader("Authorization") String token) {

                String jwtToken = token.substring(7);
                var tokenInfo = authService.getTokenInfo(jwtToken);

                return ResponseEntity
                                .ok(ApiResponse.<TokenInfoResponse>builder()
                                                .success(true)
                                                .message("Información del token obtenida")
                                                .data(tokenInfo)
                                                .build());
        }

        @PostMapping("/refresh-token")
        public ResponseEntity<ApiResponse<TokenInfoResponse>> refreshToken(
                        @RequestHeader("Authorization") String token) {

                String jwtToken = token.substring(7);
                var tokenInfo = authService.refreshToken(jwtToken);

                return ResponseEntity
                                .ok(ApiResponse.<TokenInfoResponse>builder()
                                                .success(true)
                                                .message("Token renovado exitosamente")
                                                .data(tokenInfo)
                                                .build());
        }

        @PostMapping("/puntos/select")
        public ResponseEntity<ApiResponse<LoginResponse>> seleccionarPunto(
                        @Valid @RequestBody SelectPuntoRequest request,
                        HttpServletRequest httpRequest) {

                LoginResponse response = authSucursalService.seleccionarPunto(
                                request.getUserId(),
                                request.getPuntoId(),
                                request.getPassword(),
                                httpRequest);

                return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                                .success(true)
                                .message("Punto de venta seleccionado exitosamente")
                                .data(response)
                                .build());
        }
}