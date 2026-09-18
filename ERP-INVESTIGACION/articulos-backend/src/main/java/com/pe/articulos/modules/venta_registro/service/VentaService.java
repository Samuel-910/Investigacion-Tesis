package com.pe.articulos.modules.venta_registro.service;

import java.time.LocalDate;
import java.util.List;

import com.pe.articulos.modules.venta_registro.dto.CrearVentaRequest;
import com.pe.articulos.modules.venta_registro.dto.VentaRegistroDTO;
import com.pe.articulos.core.shared.dto.ApiResponse;

public interface VentaService {

        VentaRegistroDTO crearVenta(CrearVentaRequest request);

        VentaRegistroDTO obtenerVentaPorId(Long idVenta);

        List<VentaRegistroDTO> listarTodasVentas();

        List<VentaRegistroDTO> listarVentasPorEstado(com.pe.articulos.core.enums.EstadoGeneral estado);

        List<VentaRegistroDTO> listarVentasPorFecha(LocalDate fecha);

        List<VentaRegistroDTO> listarVentasPorPaciente(String idPersonal);

        List<VentaRegistroDTO> listarVentasPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

        VentaRegistroDTO anularVenta(Long idVenta, String motivo);

        VentaRegistroDTO anularCotizacion(Long idVenta);

        VentaRegistroDTO generarComprobanteReverso(Long idVenta, String motivo);

        VentaRegistroDTO obtenerVentaPorNumdoc(String numdoc);

        org.springframework.data.domain.Page<VentaRegistroDTO> listarVentasPaginado(
                        Long idPunto,
                        org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<VentaRegistroDTO> listarVentasPorEstadoPaginado(com.pe.articulos.core.enums.EstadoGeneral estado,
                        Long idPunto,
                        org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<VentaRegistroDTO> buscarVentas(String q,
                        Long idPunto,
                        org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<VentaRegistroDTO> buscarVentasAvanzado(
                        String serie,
                        String numero,
                        Integer numeroDesde,
                        Integer numeroHasta,
                        java.time.LocalDate fechaDesde,
                        java.time.LocalDate fechaHasta,
                        String idVendedor,
                        String condicionPago,
                        com.pe.articulos.core.enums.EstadoGeneral estado,
                        Long puntoId,
                        Long sucursalId,
                        String tipoDoc,
                        org.springframework.data.domain.Pageable pageable);

        VentaRegistroDTO reimprimir(Long idVenta, String ip, String motivo);

        List<com.pe.articulos.modules.venta_registro.dto.VentaReimpresionLogDTO> obtenerHistorialReimpresiones(Long idVenta);

        List<com.pe.articulos.modules.venta_registro.dto.CorrelatividadDTO> obtenerCorrelatividad(
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            Long idSucursal,
            Long puntoId,
            String tipoDoc);

        List<VentaRegistroDTO> obtenerDetalleCorrelatividad(
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            Long idSucursal,
            Long puntoId,
            String tipoDoc,
            String serie);

        VentaRegistroDTO realizarDevolucion(com.pe.articulos.modules.venta_registro.dto.DevolucionRequest request);

        java.io.ByteArrayInputStream exportarCorrelatividad(
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            Long idSucursal,
            Long puntoId,
            String tipoDoc);

        byte[] obtenerXmlVenta(Long idVenta);

        String obtenerHtmlVenta(Long idVenta);

        void enviarPorEmail(Long idVenta, String email, Long idPlantilla);

        void enviarPorWhatsApp(Long idVenta, String telefono, Long idPlantilla);

        byte[] obtenerPdfVenta(Long idVenta, Long idPlantilla);
}

