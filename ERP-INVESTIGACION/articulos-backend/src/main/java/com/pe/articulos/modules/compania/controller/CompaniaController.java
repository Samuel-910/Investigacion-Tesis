package com.pe.articulos.modules.compania.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pe.articulos.modules.compania.dto.CompaniaRegistroDTO;
import com.pe.articulos.modules.compania.dto.VinculoSeguroDTO;
import com.pe.articulos.modules.compania.dto.VinculoPacienteDniDTO;
import com.pe.articulos.modules.compania.dto.VinculoPacienteManualDTO;
import com.pe.articulos.modules.compania.dto.CompaniaResponseDTO;
import com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO;
import com.pe.articulos.modules.compania.service.CompaniaService;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.core.shared.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companias")
@RequiredArgsConstructor
public class CompaniaController {

    private final CompaniaService companiaService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompaniaResponseDTO>> registrarCompania(@RequestBody CompaniaRegistroDTO dto) {
        CompaniaResponseDTO nuevaCompania = companiaService.registrarCompania(dto);
        return new ResponseEntity<>(ApiResponse.success(nuevaCompania), HttpStatus.CREATED);
    }

    @PostMapping("/{idCompania}/vincular")
    public ResponseEntity<ApiResponse<CompaniaPersonaVinculoResponseDTO>> vincularPaciente(
            @PathVariable Long idCompania,
            @RequestParam Long idPaciente,
            @RequestBody VinculoSeguroDTO dto) {

        CompaniaPersonaVinculoResponseDTO vinculo = companiaService.vincularPacienteASeguro(idCompania, idPaciente, dto);
        return new ResponseEntity<>(ApiResponse.success(vinculo), HttpStatus.CREATED);
    }

    @PostMapping("/{idCompania}/vincular-masivo")
    public ResponseEntity<ApiResponse<PageResponse<CompaniaPersonaVinculoResponseDTO>>> vincularPacientesMasivo(
            @PathVariable Long idCompania,
            @RequestParam List<Long> idPacientes,
            @RequestBody VinculoSeguroDTO dto) {

        return new ResponseEntity<>(ApiResponse.success(companiaService.vincularPacientesMasivo(idCompania, idPacientes, dto)),
                HttpStatus.CREATED);
    }

    @PostMapping("/{idCompania}/vincular-masivo-detalle")
    public ResponseEntity<ApiResponse<PageResponse<CompaniaPersonaVinculoResponseDTO>>> vincularPacientesMasivoDetalle(
            @PathVariable Long idCompania,
            @RequestBody List<VinculoPacienteManualDTO> vinculos) {

        return new ResponseEntity<>(ApiResponse.success(companiaService.vincularPacientesMasivoDetalle(idCompania, vinculos)),
                HttpStatus.CREATED);
    }

    @PostMapping("/{idCompania}/vincular-dnis")
    public ResponseEntity<ApiResponse<PageResponse<CompaniaPersonaVinculoResponseDTO>>> vincularPacientesPorDni(
            @PathVariable Long idCompania,
            @RequestParam List<String> dnis,
            @RequestBody VinculoSeguroDTO dto) {

        return new ResponseEntity<>(ApiResponse.success(companiaService.vincularPacientesPorDni(idCompania, dnis, dto)), HttpStatus.CREATED);
    }

    @PostMapping("/{idCompania}/vincular-dnis-detalle")
    public ResponseEntity<ApiResponse<PageResponse<CompaniaPersonaVinculoResponseDTO>>> vincularPacientesPorDniDetalle(
            @PathVariable Long idCompania,
            @RequestBody List<VinculoPacienteDniDTO> vinculos) {

        return new ResponseEntity<>(ApiResponse.success(companiaService.vincularPacientesPorDniDetalle(idCompania, vinculos)), HttpStatus.CREATED);
    }

    @GetMapping("/{idCompania}/vinculos")
    public ResponseEntity<ApiResponse<PageResponse<CompaniaPersonaVinculoResponseDTO>>> listarVinculos(
            @PathVariable Long idCompania,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaInicio") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction != null ? direction : "DESC");
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(ApiResponse.success(companiaService.listarVinculos(idCompania, pageable)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CompaniaResponseDTO>>> listarCompanias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction != null ? direction : "ASC");
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(ApiResponse.success(companiaService.listarCompanias(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompaniaResponseDTO>> obtenerCompania(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(companiaService.obtenerCompaniaPorId(id)));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompaniaResponseDTO>> actualizarCompania(@PathVariable Long id,
            @RequestBody CompaniaRegistroDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(companiaService.actualizarCompania(id, dto)));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> eliminarCompania(@PathVariable Long id) {
        companiaService.eliminarCompania(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- ACCIONES DE VÍNCULOS ---

    @org.springframework.web.bind.annotation.PutMapping("/vinculos/{idVinculo}")
    public ResponseEntity<ApiResponse<CompaniaPersonaVinculoResponseDTO>> actualizarVinculo(
            @PathVariable Long idVinculo,
            @RequestBody VinculoSeguroDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(companiaService.actualizarVinculo(idVinculo, dto)));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/vinculos/{idVinculo}")
    public ResponseEntity<ApiResponse<Void>> eliminarVinculo(@PathVariable Long idVinculo) {
        companiaService.eliminarVinculo(idVinculo);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
