package com.pe.articulos.modules.notificaciones.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.articulos.modules.notificaciones.entity.Notificacion;
import com.pe.articulos.modules.notificaciones.repository.NotificacionRepository;
import com.pe.articulos.modules.notificaciones.dto.NotificacionDTO;
import com.pe.articulos.core.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNoLeidas() {
        return notificacionRepository.findByLeidoFalseOrderByIdNotificacionDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNoLeidasPorSucursal(Long idSucursal) {
        if (idSucursal == null) {
            return obtenerNoLeidas();
        }
        return notificacionRepository.findByLeidoFalseAndIdSucursalOrderByIdNotificacionDesc(idSucursal).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void marcarComoLeida(Long idNotificacion) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        
        notificacion.setLeido(true);
        notificacionRepository.save(notificacion);
        log.info("Notificación {} marcada como leída", idNotificacion);
    }

    @Transactional
    public void crearNotificacion(String tipo, String titulo, String mensaje, String referenciaId, Long idSucursal) {
        // Evitar duplicados para la misma referencia y tipo si aǧn no ha sido leída
        if (referenciaId != null) {
            Optional<Notificacion> existente = notificacionRepository.findByTipoAndReferenciaId(tipo, referenciaId);
            if (existente.isPresent() && !existente.get().getLeido()) {
                // Actualizar el mensaje de la existente en vez de crear una nueva
                Notificacion notif = existente.get();
                notif.setMensaje(mensaje);
                notif.setTitulo(titulo);
                notificacionRepository.save(notif);
                return;
            }
        }

        Notificacion notificacion = Notificacion.builder()
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .referenciaId(referenciaId)
                .idSucursal(idSucursal)
                .build();
                
        notificacionRepository.save(notificacion);
    }

    private NotificacionDTO mapToDTO(Notificacion entidad) {
        return NotificacionDTO.builder()
                .idNotificacion(entidad.getIdNotificacion())
                .tipo(entidad.getTipo())
                .titulo(entidad.getTitulo())
                .mensaje(entidad.getMensaje())
                .referenciaId(entidad.getReferenciaId())
                .leido(entidad.getLeido())
                .fechaCreacion(entidad.getFechaCreacion())
                .idSucursal(entidad.getIdSucursal())
                .build();
    }
}
