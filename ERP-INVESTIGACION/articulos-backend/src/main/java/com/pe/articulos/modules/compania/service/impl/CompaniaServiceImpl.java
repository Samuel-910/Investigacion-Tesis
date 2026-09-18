package com.pe.articulos.modules.compania.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.modules.compania.dto.CompaniaRegistroDTO;
import com.pe.articulos.modules.compania.dto.VinculoSeguroDTO;
import com.pe.articulos.modules.compania.entity.Compania;
import com.pe.articulos.modules.compania.entity.CompaniaPersonaVinculo;
import com.pe.articulos.modules.compania.repository.CompaniaPersonaVinculoRepository;
import com.pe.articulos.modules.compania.repository.CompaniaRepository;
import com.pe.articulos.modules.compania.service.CompaniaService;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import java.util.List;
import java.util.ArrayList;

import com.pe.articulos.modules.compania.dto.VinculoPacienteDniDTO;
import com.pe.articulos.modules.compania.dto.VinculoPacienteManualDTO;
import com.pe.articulos.core.enums.EstadoGeneral;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pe.articulos.core.shared.dto.PageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompaniaServiceImpl implements CompaniaService {

        private final CompaniaRepository companiaRepository;
        private final CompaniaPersonaVinculoRepository vinculoRepository;
        private final DatosPersonalesRepository datosPersonalesRepository;
        private final com.pe.articulos.core.reports.audit.service.AuditService auditService;
        private final com.pe.articulos.modules.compania.mapper.CompaniaMapper companiaMapper;

        @Override
        @Transactional
        @SuppressWarnings("null")
        public com.pe.articulos.modules.compania.dto.CompaniaResponseDTO registrarCompania(CompaniaRegistroDTO dto) {
                // 1. Create/Save DatosPersonales (As Persona Juridica)
                DatosPersonales persona = DatosPersonales.builder()
                                .active(true)
                                .login(dto.getUsername()) // Mapped to 'login' field
                                .nombre(dto.getNombre())
                                .direcc(dto.getDirecc())
                                .fonLocal(dto.getFonLocal())
                                .ruc(dto.getRuc())
                                .tipodoc(dto.getTipodoc())
                                .numdoc(dto.getNumdoc())
                                .apepat("") // Required by DB constraint? Usually yes.
                                .apemat("")
                                .sexo("J") // Juridica? Or generic.
                                .email(dto.getEmail())
                                .passwd("123456")
                                .build();

                // NOTE: Password encoding and proper user setup should be here if this
                // 'Compania' logs in.
                // Assuming simple creation for now as per prompt scope.

                DatosPersonales savedPersona = datosPersonalesRepository.save(persona);

                // 2. Create Compania with MapsId
                Compania compania = Compania.builder()
                                .personaBase(savedPersona) // MapsId will use savedPersona.id
                                .codigo(dto.getCodigo())
                                .codigoIafa(dto.getCodigoIafa())
                                .codGales(dto.getCodGales())
                                .codEps(dto.getCodEps())
                                .codCtr(dto.getCodCtr())
                                .codSec(dto.getCodSec())
                                .idTipoCia(dto.getIdTipoCia())
                                .representante(dto.getRepresentante())
                                .auditor(dto.getAuditor())
                                .liquidador(dto.getLiquidador())
                                .financieroNombre(dto.getFinancieroNombre())
                                .idPersonalUser(dto.getIdPersonalUser())
                                .reqAmb(dto.getReqAmb())
                                .reqHos(dto.getReqHos())
                                .reqEme(dto.getReqEme())
                                .reqAcc(dto.getReqAcc())
                                .daysWaiting(dto.getDaysWaiting())
                                .ctaCte(dto.getCtaCte())
                                .pluctc(dto.getPluctc())
                                .diasPlazo(dto.getDiasPlazo())
                                .tipoTarif(dto.getTipoTarif())
                                .trabajaCpm(dto.getTrabajaCpm())
                                .importeCpm(dto.getImporteCpm())
                                .afectaRecargoEspecial(dto.getAfectaRecargoEspecial())
                                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoGeneral.ACTIVO)
                                .nombre(dto.getNombre())
                                .nomCorto(dto.getNomCorto())
                                .tipoPac(dto.getTipoPac())
                                .chana(dto.getChana())
                                .manejaCob(dto.getManejaCob())
                                .tiptra(dto.getTiptra())
                                .borrar(dto.getBorrar())
                                .mostrar(dto.getMostrar())
                                .mostrarDirec(dto.getMostrarDirec())
                                .mostrarDiasPlazo(dto.getMostrarDiasPlazo())
                                .razSol(dto.getRazSol())
                                .facturar(dto.getFacturar())
                                .contador(dto.getContador())
                                .clasifRepCobranzas(dto.getClasifRepCobranzas())
                                .build();

                Compania saved = companiaRepository.save(compania);

                auditService.logUserAction("COMPANIA", "INSERT",
                                "Creó compañía: " + saved.getNombre() + " (RUC: " + saved.getPersonaBase().getRuc()
                                                + ")");

                return companiaMapper.toResponse(saved);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO vincularPacienteASeguro(Long idCompania, Long idPaciente, VinculoSeguroDTO dto) {

                Compania compania = companiaRepository.findById(idCompania)
                                .orElseThrow(() -> new RuntimeException(
                                                "Compania no encontrada con ID: " + idCompania));
                DatosPersonales paciente = datosPersonalesRepository.findById(idPaciente)
                                .orElseThrow(() -> new RuntimeException(
                                                "Paciente no encontrado con IdPersonal: " + idPaciente));

                // 3. Create Link
                CompaniaPersonaVinculo vinculo = CompaniaPersonaVinculo.builder()
                                .compania(compania)
                                .persona(paciente)
                                .nroPoliza(dto.getNroPoliza())
                                .tipoAfiliacion(dto.getTipoAfiliacion())
                                .parentesco(dto.getParentesco())
                                .fechaInicio(dto.getFechaInicio())
                                .fechaVencimiento(dto.getFechaVencimiento())
                                .estado(dto.getActivo() != null && dto.getActivo() ? EstadoGeneral.ACTIVO : EstadoGeneral.INACTIVO)
                                .build();

                CompaniaPersonaVinculo saved = vinculoRepository.save(vinculo);

                auditService.logUserAction("COMPANIA", "VINCULO",
                                "Vinculó paciente: " + paciente.getNombre() + " a seguro: " + compania.getNombre());

                return companiaMapper.toVinculoResponse(saved);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public PageResponse<com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO> vincularPacientesMasivo(Long idCompania,
                        List<Long> idPacientes, VinculoSeguroDTO dto) {
                Compania compania = companiaRepository.findById(idCompania)
                                .orElseThrow(() -> new RuntimeException(
                                                "Compania no encontrada con ID: " + idCompania));

                List<CompaniaPersonaVinculo> vinculos = new ArrayList<>();

                for (Long idPaciente : idPacientes) {
                        try {
                                DatosPersonales paciente = datosPersonalesRepository.findById(idPaciente)
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Paciente no encontrado: " + idPaciente));

                                CompaniaPersonaVinculo vinculo = CompaniaPersonaVinculo.builder()
                                                .compania(compania)
                                                .persona(paciente)
                                                .nroPoliza(dto.getNroPoliza())
                                                .tipoAfiliacion(dto.getTipoAfiliacion())
                                                .parentesco(dto.getParentesco())
                                                .fechaInicio(dto.getFechaInicio())
                                                .fechaVencimiento(dto.getFechaVencimiento())
                                                .estado(dto.getActivo() != null && dto.getActivo() ? EstadoGeneral.ACTIVO : EstadoGeneral.INACTIVO)
                                                .build();

                                vinculos.add(vinculoRepository.save(vinculo));
                        } catch (Exception e) {
                                // Log error but continue with others
                                log.error("Error vinculando paciente " + idPaciente + ": " + e.getMessage());
                        }
                }

                auditService.logUserAction("COMPANIA", "VINCULO_MASIVO",
                                "Vinculó " + vinculos.size() + " pacientes a seguro: " + compania.getNombre());

                return new PageResponse<>(vinculos.stream().map(companiaMapper::toVinculoResponse).toList(), vinculos.size(), 1, vinculos.size(), 0);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public PageResponse<com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO> vincularPacientesMasivoDetalle(Long idCompania,
                        List<VinculoPacienteManualDTO> vinculosDTO) {
                Compania compania = companiaRepository.findById(idCompania)
                                .orElseThrow(() -> new RuntimeException(
                                                "Compania no encontrada con ID: " + idCompania));

                List<CompaniaPersonaVinculo> creados = new ArrayList<>();

                for (VinculoPacienteManualDTO dto : vinculosDTO) {
                        try {
                                DatosPersonales paciente = datosPersonalesRepository.findById(dto.getIdPaciente())
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Paciente no encontrado: " + dto.getIdPaciente()));

                                CompaniaPersonaVinculo vinculo = CompaniaPersonaVinculo.builder()
                                                .compania(compania)
                                                .persona(paciente)
                                                .nroPoliza(dto.getNroPoliza())
                                                .tipoAfiliacion(dto.getTipoAfiliacion())
                                                .parentesco(dto.getParentesco())
                                                .fechaInicio(dto.getFechaInicio())
                                                .fechaVencimiento(dto.getFechaVencimiento())
                                                .estado(dto.getActivo() != null && dto.getActivo() ? EstadoGeneral.ACTIVO : EstadoGeneral.INACTIVO)
                                                .build();

                                creados.add(vinculoRepository.save(vinculo));
                        } catch (Exception e) {
                                log.error("Error vinculando paciente " + dto.getIdPaciente() + ": "
                                                + e.getMessage());
                        }
                }

                auditService.logUserAction("COMPANIA", "VINCULO_DETALLADO",
                                "Vinculó " + creados.size() + " pacientes con datos específicos a seguro: "
                                                + compania.getNombre());

                return new PageResponse<>(creados.stream().map(companiaMapper::toVinculoResponse).toList(), creados.size(), 1, creados.size(), 0);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public PageResponse<com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO> vincularPacientesPorDni(Long idCompania,
                        List<String> dnis,
                        VinculoSeguroDTO dto) {
                Compania compania = companiaRepository.findById(idCompania)
                                .orElseThrow(() -> new RuntimeException(
                                                "Compania no encontrada con ID: " + idCompania));

                List<CompaniaPersonaVinculo> vinculos = new ArrayList<>();

                for (String dni : dnis) {
                        try {
                                DatosPersonales paciente = datosPersonalesRepository.findByNumdoc(dni)
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Paciente no encontrado con DNI: " + dni));

                                CompaniaPersonaVinculo vinculo = CompaniaPersonaVinculo.builder()
                                                .compania(compania)
                                                .persona(paciente)
                                                .nroPoliza(dto.getNroPoliza())
                                                .tipoAfiliacion(dto.getTipoAfiliacion())
                                                .parentesco(dto.getParentesco())
                                                .fechaInicio(dto.getFechaInicio())
                                                .fechaVencimiento(dto.getFechaVencimiento())
                                                .estado(dto.getActivo() != null && dto.getActivo() ? EstadoGeneral.ACTIVO : EstadoGeneral.INACTIVO)
                                                .build();

                                vinculos.add(vinculoRepository.save(vinculo));
                        } catch (Exception e) {
                                log.error("Error vinculando DNI " + dni + ": " + e.getMessage());
                        }
                }

                auditService.logUserAction("COMPANIA", "VINCULO_EXCEL",
                                "Vinculó " + vinculos.size() + " pacientes vía DNI a seguro: " + compania.getNombre());

                return new PageResponse<>(vinculos.stream().map(companiaMapper::toVinculoResponse).toList(), vinculos.size(), 1, vinculos.size(), 0);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public PageResponse<com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO> vincularPacientesPorDniDetalle(Long idCompania,
                        List<VinculoPacienteDniDTO> vinculosDTO) {
                Compania compania = companiaRepository.findById(idCompania)
                                .orElseThrow(() -> new RuntimeException(
                                                "Compania no encontrada con ID: " + idCompania));

                List<CompaniaPersonaVinculo> creados = new ArrayList<>();

                for (VinculoPacienteDniDTO dto : vinculosDTO) {
                        try {
                                if (dto.getDni() == null || dto.getDni().trim().isEmpty()) {
                                        continue;
                                }
                                DatosPersonales paciente = datosPersonalesRepository.findByNumdoc(dto.getDni().trim())
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Paciente no encontrado con DNI: " + dto.getDni()));

                                CompaniaPersonaVinculo vinculo = CompaniaPersonaVinculo.builder()
                                                .compania(compania)
                                                .persona(paciente)
                                                .nroPoliza(dto.getNroPoliza())
                                                .tipoAfiliacion(dto.getTipoAfiliacion())
                                                .parentesco(dto.getParentesco() != null ? dto.getParentesco() : "TITULAR")
                                                .fechaInicio(dto.getFechaInicio())
                                                .fechaVencimiento(dto.getFechaVencimiento())
                                                .estado(dto.getActivo() != null && dto.getActivo() ? EstadoGeneral.ACTIVO : EstadoGeneral.INACTIVO)
                                                .build();

                                creados.add(vinculoRepository.save(vinculo));
                        } catch (Exception e) {
                                log.error("Error vinculando paciente con DNI " + dto.getDni() + ": "
                                                + e.getMessage());
                        }
                }

                auditService.logUserAction("COMPANIA", "VINCULO_EXCEL_DETALLE",
                                "Vinculó " + creados.size() + " pacientes con detalles específicos vía DNI a seguro: "
                                                + compania.getNombre());

                return new PageResponse<>(creados.stream().map(companiaMapper::toVinculoResponse).toList(), creados.size(), 1, creados.size(), 0);
        }

        @Override
        @SuppressWarnings("null")
        public PageResponse<com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO> listarVinculos(Long idCompania, Pageable pageable) {
                Page<CompaniaPersonaVinculo> page = vinculoRepository.findByCompaniaIdAndEstado(idCompania, EstadoGeneral.ACTIVO, pageable);
                return PageResponse.fromPage(page.map(companiaMapper::toVinculoResponse));
        }

        @Override
        @SuppressWarnings("null")
        public PageResponse<com.pe.articulos.modules.compania.dto.CompaniaResponseDTO> listarCompanias(Pageable pageable) {
                Page<Compania> page = companiaRepository.findByEstado(EstadoGeneral.ACTIVO, pageable);
                return PageResponse.fromPage(page.map(companiaMapper::toResponse));
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public com.pe.articulos.modules.compania.dto.CompaniaPersonaVinculoResponseDTO actualizarVinculo(Long idVinculo, VinculoSeguroDTO dto) {
                CompaniaPersonaVinculo vinculo = vinculoRepository.findById(idVinculo)
                                .orElseThrow(() -> new RuntimeException("Vínculo no encontrado con ID: " + idVinculo));

                vinculo.setNroPoliza(dto.getNroPoliza());
                vinculo.setTipoAfiliacion(dto.getTipoAfiliacion());
                vinculo.setParentesco(dto.getParentesco());
                vinculo.setFechaInicio(dto.getFechaInicio());
                vinculo.setFechaVencimiento(dto.getFechaVencimiento());
                vinculo.setEstado(dto.getActivo() != null && dto.getActivo() ? EstadoGeneral.ACTIVO : EstadoGeneral.INACTIVO);

                CompaniaPersonaVinculo actualizado = vinculoRepository.save(vinculo);

                auditService.logUserAction("COMPANIA", "ACTUALIZAR_VINCULO",
                                "Actualizó vínculo ID: " + idVinculo + " para paciente: "
                                                + actualizado.getPersona().getNombreCompleto());

                return companiaMapper.toVinculoResponse(actualizado);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public void eliminarVinculo(Long idVinculo) {
                CompaniaPersonaVinculo vinculo = vinculoRepository.findById(idVinculo)
                                .orElseThrow(() -> new RuntimeException("Vínculo no encontrado con ID: " + idVinculo));

                String nombrePaciente = vinculo.getPersona().getNombreCompleto();
                String nombreCompania = vinculo.getCompania().getNombre();

                vinculo.setEstado(EstadoGeneral.ELIMINADO);
                vinculoRepository.save(vinculo);

                auditService.logUserAction("COMPANIA", "ELIMINAR_VINCULO",
                                "Eliminó vínculo de paciente: " + nombrePaciente + " del convenio: " + nombreCompania);
        }

        @Override
        @SuppressWarnings("null")
        public com.pe.articulos.modules.compania.dto.CompaniaResponseDTO obtenerCompaniaPorId(Long id) {
                Compania compania = companiaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Compañía no encontrada"));
                return companiaMapper.toResponse(compania);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public com.pe.articulos.modules.compania.dto.CompaniaResponseDTO actualizarCompania(Long id, CompaniaRegistroDTO dto) {
                Compania compania = companiaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Compania no encontrada con ID: " + id));

                String nombreAnterior = compania.getNombre();

                // Update DatosPersonales fields
                DatosPersonales persona = compania.getPersonaBase();
                if (persona != null) {
                        persona.setNombre(dto.getNombre());
                        persona.setLogin(dto.getUsername());
                        persona.setEmail(dto.getEmail());
                        // Update other persona fields
                        persona.setDirecc(dto.getDirecc());
                        persona.setFonLocal(dto.getFonLocal());
                        persona.setRuc(dto.getRuc());
                        persona.setTipodoc(dto.getTipodoc());
                        persona.setNumdoc(dto.getNumdoc());

                        datosPersonalesRepository.save(persona);
                }

                // Update Compania fields
                compania.setCodigo(dto.getCodigo());
                compania.setCodigoIafa(dto.getCodigoIafa());
                compania.setCodGales(dto.getCodGales());
                compania.setCodEps(dto.getCodEps());
                compania.setCodCtr(dto.getCodCtr());
                compania.setCodSec(dto.getCodSec());
                compania.setIdTipoCia(dto.getIdTipoCia());
                compania.setRepresentante(dto.getRepresentante());
                compania.setAuditor(dto.getAuditor());
                compania.setLiquidador(dto.getLiquidador());
                compania.setFinancieroNombre(dto.getFinancieroNombre());
                compania.setIdPersonalUser(dto.getIdPersonalUser());
                compania.setReqAmb(dto.getReqAmb());
                compania.setReqHos(dto.getReqHos());
                compania.setReqEme(dto.getReqEme());
                compania.setReqAcc(dto.getReqAcc());
                compania.setDaysWaiting(dto.getDaysWaiting());
                compania.setCtaCte(dto.getCtaCte());
                compania.setPluctc(dto.getPluctc());
                compania.setDiasPlazo(dto.getDiasPlazo());
                compania.setTipoTarif(dto.getTipoTarif());
                compania.setTrabajaCpm(dto.getTrabajaCpm());
                compania.setImporteCpm(dto.getImporteCpm());
                compania.setAfectaRecargoEspecial(dto.getAfectaRecargoEspecial());
                compania.setEstado(dto.getEstado());
                compania.setNombre(dto.getNombre());
                compania.setNomCorto(dto.getNomCorto());
                compania.setTipoPac(dto.getTipoPac());
                compania.setChana(dto.getChana());
                compania.setManejaCob(dto.getManejaCob());
                compania.setTiptra(dto.getTiptra());
                compania.setBorrar(dto.getBorrar());
                compania.setMostrar(dto.getMostrar());
                compania.setMostrarDirec(dto.getMostrarDirec());
                compania.setMostrarDiasPlazo(dto.getMostrarDiasPlazo());
                compania.setRazSol(dto.getRazSol());
                compania.setFacturar(dto.getFacturar());
                compania.setContador(dto.getContador());
                compania.setClasifRepCobranzas(dto.getClasifRepCobranzas());

                Compania updated = companiaRepository.save(compania);

                auditService.logUserAction("COMPANIA", "UPDATE",
                                "Actualizó compañía: " + nombreAnterior + " -> " + updated.getNombre() + " (ID: " + id
                                                + ")");

                return companiaMapper.toResponse(updated);
        }

        @Override
        @Transactional
        @SuppressWarnings("null")
        public void eliminarCompania(Long id) {
                if (!companiaRepository.existsById(id)) {
                        throw new RuntimeException("Compania no encontrada con ID: " + id);
                }
                Compania compania = companiaRepository.findById(id).get();
                compania.setEstado(EstadoGeneral.ELIMINADO);
                companiaRepository.save(compania);

                auditService.logUserAction("COMPANIA", "DELETE",
                                "Eliminó compañía con ID: " + id);
        }
}
