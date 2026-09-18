package com.pe.articulos.modules.compania.service;

import java.util.List;
import com.pe.articulos.modules.compania.dto.CompaniaRegistroDTO;
import com.pe.articulos.modules.compania.dto.VinculoPacienteDniDTO;
import com.pe.articulos.modules.compania.dto.VinculoPacienteManualDTO;
import com.pe.articulos.modules.compania.dto.VinculoSeguroDTO;
import com.pe.articulos.modules.compania.dto.CompaniaResponseDTO;
import com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

public interface CompaniaService {
        CompaniaResponseDTO registrarCompania(CompaniaRegistroDTO dto);

        CompaniaResponseDTO actualizarCompania(Long id, CompaniaRegistroDTO dto);

        void eliminarCompania(Long id);

        CompaniaPersonaVinculoResponseDTO vincularPacienteASeguro(Long idCompania, Long idPaciente,
                        VinculoSeguroDTO dto);

        PageResponse<CompaniaPersonaVinculoResponseDTO> vincularPacientesMasivo(Long idCompania,
                        List<Long> idPacientes,
                        VinculoSeguroDTO dto);

        PageResponse<CompaniaPersonaVinculoResponseDTO> vincularPacientesMasivoDetalle(Long idCompania,
                        List<VinculoPacienteManualDTO> vinculos);

        PageResponse<CompaniaPersonaVinculoResponseDTO> vincularPacientesPorDni(Long idCompania, List<String> dnis,
                        VinculoSeguroDTO dto);

        PageResponse<CompaniaPersonaVinculoResponseDTO> vincularPacientesPorDniDetalle(Long idCompania,
                        List<VinculoPacienteDniDTO> vinculos);

        PageResponse<CompaniaPersonaVinculoResponseDTO> listarVinculos(Long idCompania, Pageable pageable);

        PageResponse<CompaniaResponseDTO> listarCompanias(Pageable pageable);

        CompaniaPersonaVinculoResponseDTO actualizarVinculo(Long idVinculo, VinculoSeguroDTO dto);

        void eliminarVinculo(Long idVinculo);

        CompaniaResponseDTO obtenerCompaniaPorId(Long id);

}
