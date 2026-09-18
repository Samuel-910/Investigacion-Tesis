package com.pe.articulos.modules.niveles.service.impl;

import com.pe.articulos.modules.niveles.dto.*;
import com.pe.articulos.modules.niveles.entity.Nivel;
import com.pe.articulos.modules.niveles.mapper.NivelMapper;
import com.pe.articulos.modules.niveles.repository.NivelRepository;
import com.pe.articulos.modules.niveles.service.NivelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("null")
public class NivelServiceImpl implements NivelService {

        private final NivelRepository nivelRepository;
        private final NivelMapper nivelMapper;
        private final com.pe.articulos.core.reports.audit.service.AuditService auditService;
        private final CodigoNivelGenerator codigoNivelGenerator;

        @Override
        public NivelDto crear(NivelCreateDto dto) {
                if (nivelRepository.existsByNombreIgnoreCaseAndIdNivelPadre(
                                dto.getNombre(), dto.getIdNivelPadre())) {
                        throw new IllegalArgumentException(
                                        "Ya existe un nivel con ese nombre en el mismo nivel padre");
                }
                String codigoGenerado = codigoNivelGenerator.generarCodigo(
                                dto.getIdNivelPadre(),
                                dto.getNumNivel());

                Nivel nivel = nivelMapper.toEntity(dto);
                nivel.setNumNivel(codigoGenerado);

                Nivel guardado = nivelRepository.save(nivel);
                auditService.logUserAction("NIVELES", "INSERT",
                                "Creó nivel: " + guardado.getNombre() + " (ID: " + guardado.getIdNivel() + ", Código: "
                                                + guardado.getNumNivel() + ")");

                return nivelMapper.toDto(guardado);
        }

        @Override
        public NivelDto actualizar(Long id, NivelDto dto) {
                Nivel nivel = nivelRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

                String nombreAnterior = nivel.getNombre();
                String codigoAnterior = nivel.getNumNivel();

                if (dto.getNumNivel() != null && !dto.getNumNivel().equals(codigoAnterior)) {
                        throw new IllegalArgumentException(
                                        "No se puede modificar el código de un nivel existente. " +
                                                        "El código forma parte de la estructura jerárquica.");
                }

                nivelMapper.updateEntity(dto, nivel);

                Nivel actualizado = nivelRepository.save(nivel);

                auditService.logUserAction("NIVELES", "UPDATE",
                                "Actualizó nivel: " + nombreAnterior + " -> " + actualizado.getNombre() + " (ID: " + id
                                                + ")");

                return nivelMapper.toDto(actualizado);
        }

        @Override
        public void eliminar(Long id) {
                Nivel nivel = nivelRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));
                Long hijos = nivelRepository.countByIdNivelPadre(id);
                if (hijos > 0) {
                        throw new IllegalArgumentException(
                                        "No se puede eliminar el nivel porque tiene " + hijos + " subniveles");
                }

                String nombreEliminado = nivel.getNombre();
                String codigoEliminado = nivel.getNumNivel();

                nivelRepository.deleteById(id);
                log.info("Nivel eliminado: {} (código: {})", id, codigoEliminado);

                auditService.logUserAction("NIVELES", "DELETE",
                                "Eliminó nivel: " + nombreEliminado + " (ID: " + id + ", Código: " + codigoEliminado
                                                + ")");
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<NivelDto> obtenerPorId(Long id) {
                return nivelRepository.findById(id)
                                .map(nivelMapper::toDto);
        }

        @Override
        @Transactional(readOnly = true)
        public List<NivelDto> obtenerTodos() {
                return nivelRepository.findAll().stream()
                                .map(nivelMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<NivelDto> obtenerTodosPaginado(Pageable pageable) {
                return nivelRepository.findAll(pageable)
                                .map(nivelMapper::toDto);
        }

        @Override
        @Transactional(readOnly = true)
        public List<NivelDto> obtenerNivelesRaiz() {
                return nivelRepository.findByIdNivelPadreIsNullOrderByOrden().stream()
                                .map(nivelMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<NivelDto> obtenerNivelesRaizPaginado(Pageable pageable) {
                return nivelRepository.findByIdNivelPadreIsNull(pageable)
                                .map(nivelMapper::toDto);
        }

        @Override
        @Transactional(readOnly = true)
        public List<NivelDto> obtenerHijos(Long idNivelPadre) {
                return nivelRepository.findByIdNivelPadreOrderByOrden(idNivelPadre).stream()
                                .map(nivelMapper::toDto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<NivelTreeDto> obtenerArbolCompleto() {
                List<Nivel> raices = nivelRepository.findByIdNivelPadreIsNullOrderByOrden();
                return raices.stream()
                                .map(this::construirArbol)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<NivelTreeDto> obtenerArbolDesde(Long idNivel) {
                Nivel nivel = nivelRepository.findById(idNivel)
                                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

                List<NivelTreeDto> arbol = new ArrayList<>();
                arbol.add(construirArbol(nivel));
                return arbol;
        }

        @Override
        public void moverNivel(NivelMoverDto dto) {
                log.info("Moviendo nivel {} a nuevo padre {}", dto.getIdNivel(), dto.getIdNivelPadreNuevo());
                log.warn("ADVERTENCIA: Mover niveles requiere regenerar códigos de toda la rama");
                throw new IllegalArgumentException(
                                "No se puede mover niveles una vez creados porque esto requeriría " +
                                                "regenerar los códigos de toda la rama jerárquica. " +
                                                "Si necesita reorganizar, considere eliminar y recrear la estructura.");

                // Si en el futuro se desea implementar esta funcionalidad:
                // 1. Validar que no se mueva a sí mismo o a sus descendientes
                // 2. Regenerar código del nivel movido
                // 3. Regenerar códigos de TODOS los descendientes recursivamente
                // 4. Actualizar jerarquía
        }

        @Override
        public void cambiarOrden(Long idNivel, Integer nuevoOrden) {
                Nivel nivel = nivelRepository.findById(idNivel)
                                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

                nivel.setOrden(nuevoOrden);
                nivelRepository.save(nivel);

                log.info("Orden actualizado para nivel {} (código: {}): {}",
                                idNivel, nivel.getNumNivel(), nuevoOrden);
        }

        @Override
        @Transactional(readOnly = true)
        public EstadisticasNivelDto obtenerEstadisticas() {
                EstadisticasNivelDto stats = new EstadisticasNivelDto();

                stats.setTotalNiveles(nivelRepository.count());
                stats.setNivelesActivos(
                                nivelRepository.countByEstado(com.pe.articulos.core.enums.EstadoGeneral.ACTIVO));
                stats.setNivelesInactivos(
                                nivelRepository.countByEstado(com.pe.articulos.core.enums.EstadoGeneral.INACTIVO));
                stats.setNivelMaximoJerarquia(nivelRepository.obtenerNivelMaximoJerarquia());
                stats.setTotalNivelesRaiz((long) nivelRepository.findByIdNivelPadreIsNullOrderByOrden().size());
                stats.setTotalNivelesConHijos((long) nivelRepository.findNivelesConHijos().size());
                stats.setTotalNivelesSinHijos(stats.getTotalNiveles() - stats.getTotalNivelesConHijos());

                return stats;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<NivelDto> buscarConFiltros(String nombre, String tipo,
                        com.pe.articulos.core.enums.EstadoGeneral estado,
                        Long idNivelPadre, Pageable pageable) {
                return nivelRepository.buscarConFiltros(nombre, tipo, estado, idNivelPadre, pageable)
                                .map(nivelMapper::toDto);
        }

        // MÉTODOS AUXILIARES

        private NivelTreeDto construirArbol(Nivel nivel) {
                NivelTreeDto dto = nivelMapper.toTreeDto(nivel);

                List<Nivel> hijos = nivelRepository.findByIdNivelPadreOrderByOrden(nivel.getIdNivel());
                if (!hijos.isEmpty()) {
                        dto.setHijos(hijos.stream()
                                        .map(this::construirArbol)
                                        .collect(Collectors.toList()));
                }

                return dto;
        }

}