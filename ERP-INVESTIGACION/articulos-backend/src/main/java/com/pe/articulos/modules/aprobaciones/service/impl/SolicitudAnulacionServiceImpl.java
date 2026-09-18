package com.pe.articulos.modules.aprobaciones.service.impl;

import com.pe.articulos.core.shared.dto.ApiResponse;
import com.pe.articulos.modules.aprobaciones.dto.SolicitudAnulacionDTO;
import com.pe.articulos.modules.aprobaciones.entity.SolicitudesAnulacion;
import com.pe.articulos.modules.aprobaciones.repository.SolicitudAnulacionRepository;
import com.pe.articulos.modules.aprobaciones.service.SolicitudAnulacionService;
import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.compras.repository.CompraRepository;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import com.pe.articulos.modules.venta_registro.entity.VentaRegistro;
import com.pe.articulos.modules.venta_registro.repository.VentaRegistroRepository;
import com.pe.articulos.modules.venta_registro.service.VentaService;
import com.pe.articulos.modules.productos.entity.MovimientoDiverso;
import com.pe.articulos.modules.productos.repository.MovimientoDiversoRepository;
import com.pe.articulos.modules.productos.services.InventarioService;
import com.pe.articulos.core.enums.EstadoGeneral;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class SolicitudAnulacionServiceImpl implements SolicitudAnulacionService {

    private final SolicitudAnulacionRepository repository;
    private final VentaRegistroRepository ventaRepository;
    private final CompraRepository compraRepository;
    private final VentaService ventaService;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final MovimientoDiversoRepository movimientoDiversoRepository;
    private final InventarioService inventarioService;

    @Override
    @Transactional
    public ApiResponse<SolicitudAnulacionDTO> solicitar(
            SolicitudesAnulacion.TipoSolicitud tipo,
            Long referenciaId,
            String motivo) {

        // Verificar si ya existe una solicitud pendiente
        repository
                .findByTipoAndReferenciaIdAndEstado(tipo, referenciaId, SolicitudesAnulacion.EstadoSolicitud.PENDIENTE)
                .ifPresent(s -> {
                    throw new RuntimeException("Ya existe una solicitud de anulación pendiente para este documento.");
                });

        SolicitudesAnulacion solicitud = SolicitudesAnulacion.builder()
                .tipo(tipo)
                .referenciaId(referenciaId)
                .motivo(motivo)
                .estado(SolicitudesAnulacion.EstadoSolicitud.PENDIENTE)
                .build();

        // Cambiar estado del documento original a "PENDIENTE_ANULACION"
        if (tipo == SolicitudesAnulacion.TipoSolicitud.VENTA) {
            VentaRegistro venta = ventaRepository.findById(referenciaId)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
            venta.setEstado(EstadoGeneral.PENDIENTE_ANULACION);
            ventaRepository.save(venta);
        } else if (tipo == SolicitudesAnulacion.TipoSolicitud.COMPRA) {
            Compra compra = compraRepository.findById(referenciaId)
                    .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
            compra.setEstado(EstadoGeneral.PENDIENTE_ANULACION);
            compraRepository.save(compra);
        } else if (tipo == SolicitudesAnulacion.TipoSolicitud.MOVIMIENTO_DIVERSO) {
            MovimientoDiverso mov = movimientoDiversoRepository.findById(referenciaId)
                    .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
            mov.setEstado(EstadoGeneral.PENDIENTE_ANULACION);
            movimientoDiversoRepository.save(mov);
        }

        repository.save(solicitud);
        return new ApiResponse<>("Solicitud enviada correctamente", mapToDTO(solicitud), HttpStatus.OK.value(), true);
    }

    @Override
    public ApiResponse<List<SolicitudAnulacionDTO>> listarPendientes(String q, String type, Integer page,
            Integer size, Long idSucursal, Long idPuntoVenta) {
        List<SolicitudAnulacionDTO> dtos = repository.findByEstado(SolicitudesAnulacion.EstadoSolicitud.PENDIENTE)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        dtos = filtrarDTOS(dtos, q, type, idSucursal, idPuntoVenta);
        return new ApiResponse<>("Solicitudes obtenidas", dtos, HttpStatus.OK.value(), true);
    }

    @Override
    @Transactional
    public ApiResponse<Void> atender(Long id, boolean aprobada, String observacion) {
        SolicitudesAnulacion solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (solicitud.getEstado() != SolicitudesAnulacion.EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException("La solicitud ya ha sido atendida.");
        }

        DatosPersonales usuarioAtiende = getUsuarioActual();
        solicitud.setEstado(
                aprobada ? SolicitudesAnulacion.EstadoSolicitud.APROBADA
                        : SolicitudesAnulacion.EstadoSolicitud.RECHAZADA);
        solicitud.setObservacionAtiende(observacion);

        if (aprobada) {
            // Ejecutar anulación real
            if (solicitud.getTipo() == SolicitudesAnulacion.TipoSolicitud.VENTA) {
                // Necesitamos forzar la anulación aunque el estado sea 'P'
                // Modificamos temporalmente el estado para que anularVenta no falle si tiene
                // validaciones
                VentaRegistro venta = ventaRepository.findById(solicitud.getReferenciaId()).get();
                venta.setEstado(EstadoGeneral.VIGENTE); // Resetear temporalmente a Vigente para que la lógica de
                                                        // anulación proceda
                ventaRepository.save(venta);
                ventaService.anularVenta(solicitud.getReferenciaId(),
                        solicitud.getMotivo() + " (Aprobado por: " + usuarioAtiende.getLogin() + ")");
            } else if (solicitud.getTipo() == SolicitudesAnulacion.TipoSolicitud.COMPRA) {
                Compra compra = compraRepository.findById(solicitud.getReferenciaId())
                        .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
                compra.setEstado(EstadoGeneral.ANULADO);
                compraRepository.save(compra);
            } else if (solicitud.getTipo() == SolicitudesAnulacion.TipoSolicitud.MOVIMIENTO_DIVERSO) {
                MovimientoDiverso movOriginal = movimientoDiversoRepository.findById(solicitud.getReferenciaId()).orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
                java.time.LocalDate hoy = java.time.LocalDate.now();
                java.time.LocalDate fechaDoc = movOriginal.getFecha().toLocalDate();
                
                boolean pasoUnDia = fechaDoc.isBefore(hoy);
                
                inventarioService.revertirMovimientoDiverso(solicitud.getReferenciaId(),
                        solicitud.getMotivo() + " (Aprobado por: " + usuarioAtiende.getLogin() + ")",
                        usuarioAtiende.getId(),
                        !pasoUnDia); // si pasó un día, anularOriginal es FALSE, si es de hoy, es TRUE
            }
        } else {
            // Restaurar estado original si se rechaza
            if (solicitud.getTipo() == SolicitudesAnulacion.TipoSolicitud.VENTA) {
                VentaRegistro venta = ventaRepository.findById(solicitud.getReferenciaId()).get();
                venta.setEstado(EstadoGeneral.VIGENTE);
                ventaRepository.save(venta);
            } else if (solicitud.getTipo() == SolicitudesAnulacion.TipoSolicitud.COMPRA) {
                Compra compra = compraRepository.findById(solicitud.getReferenciaId()).get();
                compra.setEstado(EstadoGeneral.REGISTRADO);
                compraRepository.save(compra);
            } else if (solicitud.getTipo() == SolicitudesAnulacion.TipoSolicitud.MOVIMIENTO_DIVERSO) {
                MovimientoDiverso mov = movimientoDiversoRepository.findById(solicitud.getReferenciaId()).get();
                mov.setEstado(EstadoGeneral.ACTIVO);
                movimientoDiversoRepository.save(mov);
            }
        }

        repository.save(solicitud);
        return new ApiResponse<>("Solicitud " + (aprobada ? "aprobada" : "rechazada") + " correctamente", null,
                HttpStatus.OK.value(), true);
    }

    @Override
    public ApiResponse<List<SolicitudAnulacionDTO>> listarHistorial(String q, String type, Integer page, Integer size, Long idSucursal, Long idPuntoVenta) {
        List<SolicitudAnulacionDTO> dtos = repository.findAll().stream()
                .filter(s -> s.getEstado() == SolicitudesAnulacion.EstadoSolicitud.APROBADA || s.getEstado() == SolicitudesAnulacion.EstadoSolicitud.RECHAZADA)
                .map(this::mapToDTO).collect(Collectors.toList());
        dtos = filtrarDTOS(dtos, q, type, idSucursal, idPuntoVenta);
        return new ApiResponse<>("Historial obtenido", dtos, HttpStatus.OK.value(), true);
    }

    private List<SolicitudAnulacionDTO> filtrarDTOS(List<SolicitudAnulacionDTO> dtos, String query, String type, Long idSucursal, Long idPuntoVenta) {
        if (idSucursal != null && idSucursal != 0 && idSucursal != -1) {
            dtos = dtos.stream().filter(d -> d.getSucursalId() != null && d.getSucursalId().equals(idSucursal)).collect(Collectors.toList());
        }
        if (idPuntoVenta != null && idPuntoVenta != 0 && idPuntoVenta != -1) {
            dtos = dtos.stream().filter(d -> d.getPuntoId() != null && d.getPuntoId().equals(idPuntoVenta)).collect(Collectors.toList());
        }
        
        if (query == null || query.trim().isEmpty()) {
            return dtos;
        }
        String q = query.toLowerCase().trim();
        return dtos.stream().filter(dto -> {
            if ("DOCUMENTO".equalsIgnoreCase(type)) {
                return dto.getDocumentoReferencia() != null && dto.getDocumentoReferencia().toLowerCase().contains(q);
            } else if ("CLIENTE".equalsIgnoreCase(type)) {
                return dto.getClienteProveedor() != null && dto.getClienteProveedor().toLowerCase().contains(q);
            } else if ("MOTIVO".equalsIgnoreCase(type)) {
                return dto.getMotivo() != null && dto.getMotivo().toLowerCase().contains(q);
            } else if ("TIPO".equalsIgnoreCase(type)) {
                return dto.getTipo() != null && dto.getTipo().toString().toLowerCase().contains(q);
            } else if ("USUARIO".equalsIgnoreCase(type)) {
                return dto.getUsuarioAtiende() != null && dto.getUsuarioAtiende().toLowerCase().contains(q);
            } else if ("OBSERVACION".equalsIgnoreCase(type)) {
                return dto.getObservacionAtiende() != null && dto.getObservacionAtiende().toLowerCase().contains(q);
            } else if ("ESTADO".equalsIgnoreCase(type)) {
                return dto.getEstado() != null && dto.getEstado().toString().toLowerCase().contains(q);
            } else {
                // ALL or default
                return (dto.getDocumentoReferencia() != null && dto.getDocumentoReferencia().toLowerCase().contains(q))
                        ||
                        (dto.getClienteProveedor() != null && dto.getClienteProveedor().toLowerCase().contains(q)) ||
                        (dto.getMotivo() != null && dto.getMotivo().toLowerCase().contains(q)) ||
                        (dto.getTipo() != null && dto.getTipo().toString().toLowerCase().contains(q)) ||
                        (dto.getUsuarioAtiende() != null && dto.getUsuarioAtiende().toLowerCase().contains(q)) ||
                        (dto.getObservacionAtiende() != null && dto.getObservacionAtiende().toLowerCase().contains(q))
                        ||
                        (dto.getEstado() != null && dto.getEstado().toString().toLowerCase().contains(q));
            }
        }).collect(Collectors.toList());
    }

    private DatosPersonales getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = (auth != null) ? auth.getName() : "SISTEMA";
        return datosPersonalesRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));
    }

    private SolicitudAnulacionDTO mapToDTO(SolicitudesAnulacion s) {
        SolicitudAnulacionDTO dto = SolicitudAnulacionDTO.builder()
                .id(s.getId())
                .tipo(s.getTipo())
                .referenciaId(s.getReferenciaId())
                .motivo(s.getMotivo())
                .usuarioSolicita(s.getUsuarioCreacion())
                .fechaSolicitud(s.getFechaCreacion())
                .estado(s.getEstado())
                .usuarioAtiende(s.getUsuarioModificacion())
                .fechaAtiende(s.getFechaModificacion())
                .observacionAtiende(s.getObservacionAtiende())
                .build();

        // Completar información descriptiva
        try {
            if (s.getTipo() == SolicitudesAnulacion.TipoSolicitud.VENTA) {
                ventaRepository.findById(s.getReferenciaId()).ifPresent(v -> {
                    dto.setDocumentoReferencia(
                            v.getSerie() + "-" + (v.getNumero() != null ? v.getNumero() : v.getNumdoc()));
                    dto.setClienteProveedor(v.getRazon() != null ? v.getRazon() : v.getNombrePac());
                    dto.setMonto(v.getTotal());
                    dto.setSucursalId(v.getIdSucursal());
                    dto.setPuntoId(v.getPunto());

                    String tipoDoc = v.getTipoDoc();
                    if ("01".equals(tipoDoc)) {
                        dto.setTipoDocumento("Factura");
                    } else if ("03".equals(tipoDoc)) {
                        dto.setTipoDocumento("Boleta");
                    } else if ("07".equals(tipoDoc)) {
                        dto.setTipoDocumento("Nota de Crédito");
                    } else if ("08".equals(tipoDoc)) {
                        dto.setTipoDocumento("Nota de Débito");
                    } else if ("NV".equals(tipoDoc)) {
                        dto.setTipoDocumento("Nota de Venta");
                    } else {
                        dto.setTipoDocumento("Venta");
                    }
                });
            } else if (s.getTipo() == SolicitudesAnulacion.TipoSolicitud.COMPRA) {
                compraRepository.findById(s.getReferenciaId()).ifPresent(c -> {
                    dto.setDocumentoReferencia(c.getSerie() + "-" + c.getCorrelativo());
                    dto.setClienteProveedor(c.getProveedor().getRazonSocial());
                    dto.setMonto(c.getTotalPagar());
                    dto.setTipoDocumento("Compra");
                    if (c.getIdSucursal() != null) {
                        dto.setSucursalId(c.getIdSucursal());
                    }
                });
            } else if (s.getTipo() == SolicitudesAnulacion.TipoSolicitud.MOVIMIENTO_DIVERSO) {
                movimientoDiversoRepository.findById(s.getReferenciaId()).ifPresent(m -> {
                    dto.setDocumentoReferencia(m.getNumDocumento());
                    dto.setClienteProveedor(m.getMotivo());
                    if (m.getSucursal() != null) {
                        dto.setSucursalId(m.getSucursal().getIdSucursal());
                    }
                    java.math.BigDecimal total = java.math.BigDecimal.ZERO;
                    if (m.getDetalles() != null) {
                        for (var detalle : m.getDetalles()) {
                            if (detalle.getCantidad() != null && detalle.getCostoUnitario() != null) {
                                total = total.add(detalle.getCantidad().multiply(detalle.getCostoUnitario()));
                            }
                        }
                    }
                    dto.setMonto(total);
                    dto.setTipoDocumento("Movimiento");
                });
            }
        } catch (Exception e) {
            // Ignorar errores en el mapeo de info extra para no romper el listado
        }

        return dto;
    }
}
