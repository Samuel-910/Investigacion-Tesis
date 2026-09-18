package com.pe.articulos.modules.finanzas;

import com.pe.articulos.modules.compras.entity.Compra;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.modules.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FlujoAprobacionService {
    void solicitarPagoCompra(Compra compra, String userId);
}

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
class FlujoAprobacionServiceImpl implements FlujoAprobacionService {

    private final FlujoAprobacionRepository repository;
    private final DatosPersonalesRepository datosPersonalesRepository;

    @Override
    @Transactional
    public void solicitarPagoCompra(Compra compra, String userId) {

        DatosPersonales solicitante = datosPersonalesRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("Solicitante no encontrado"));

        BigDecimal montoASolicitar = compra.getTotalPagar();

        FlujoAprobacion solicitud = FlujoAprobacion.builder()
                .solicitante(solicitante)
                .tipoAccion("SOLICITUD_PAGO_COMPRA")
                .descripcion("Pago de Compra: " + compra.getTipoComprobante() + " " + compra.getSerie() + "-"
                        + compra.getCorrelativo() +
                        " del proveedor " + compra.getProveedor().getRazonSocial())
                .monto(montoASolicitar)
                .datosContexto("{\"idCompra\":" + compra.getId() + "}")
                .estado(EstadoAprobacion.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .active(true)
                .build();

        repository.save(solicitud);
    }
}
