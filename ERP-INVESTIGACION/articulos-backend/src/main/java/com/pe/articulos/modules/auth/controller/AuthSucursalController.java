package com.pe.articulos.modules.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.auth.service.AuthSucursalService;
import com.pe.articulos.modules.auth.service.JwtService;
import com.pe.articulos.modules.auth.service.dto.LoginResponse;
import com.pe.articulos.modules.auth.service.dto.SelectSucursalRequest;
import com.pe.articulos.modules.puntos.dto.PuntoDto;
import com.pe.articulos.modules.sucursal.dto.SucursalDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/sucursales")
@RequiredArgsConstructor
public class AuthSucursalController {

        private final AuthSucursalService authSucursalService;
        private final JwtService jwtService;

        @GetMapping("/{userId}")
        public ResponseEntity<ApiResponse<List<SucursalDto>>> obtenerSucursalesDisponibles(@PathVariable Long userId) {
                List<SucursalDto> sucursales = authSucursalService.obtenerSucursalesDisponibles(userId);

                return ResponseEntity.ok(ApiResponse.<List<SucursalDto>>builder()
                                .success(true)
                                .message("Sucursales obtenidas exitosamente")
                                .data(sucursales)
                                .build());
        }

        @GetMapping("/{userId}/puntos")
        public ResponseEntity<ApiResponse<List<PuntoDto>>> obtenerPuntosDeSucursalActual(
                        @PathVariable Long userId,
                        HttpServletRequest request) {

                String authHeader = request.getHeader("Authorization");
                Long sucursalIdFromToken = null;

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        sucursalIdFromToken = jwtService.extractSucursalId(token);
                }

                List<PuntoDto> puntos = authSucursalService.obtenerPuntosDeSucursalActual(userId, sucursalIdFromToken);

                return ResponseEntity.ok(ApiResponse.<List<PuntoDto>>builder()
                                .success(true)
                                .message("Puntos de venta obtenidos exitosamente")
                                .data(puntos)
                                .build());
        }

        @PostMapping("/select")
        public ResponseEntity<ApiResponse<LoginResponse>> seleccionarSucursal(
                        @Valid @RequestBody SelectSucursalRequest request,
                        HttpServletRequest httpRequest) {

                LoginResponse response = authSucursalService.seleccionarSucursal(
                                request.getUserId(),
                                request.getSucursalId(),
                                request.getPassword(),
                                httpRequest);

                return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                                .success(true)
                                .message("Sucursal seleccionada exitosamente")
                                .data(response)
                                .build());
        }
}
