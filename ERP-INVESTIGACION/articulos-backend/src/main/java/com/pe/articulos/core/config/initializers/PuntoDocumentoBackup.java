package com.pe.articulos.core.config.initializers;

import com.pe.articulos.modules.documentos.entities.Modulo;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.puntos.entity.PuntoDocumento;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.modules.puntos.repository.PuntoDocumentoRepository;
import com.pe.articulos.modules.puntos.repository.PuntoRepository;
import com.pe.articulos.modules.venta_registro.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.pe.articulos.modules.documentos.repositories.PlantillaRepository;
import com.pe.articulos.modules.documentos.entities.Plantilla;

@Component
@RequiredArgsConstructor
public class PuntoDocumentoBackup {

    private final PuntoRepository puntoRepository;
    private final PuntoDocumentoRepository puntoDocumentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final PlantillaRepository plantillaRepository;

    @Transactional
    public void run() {
        // 0. Limpiar duplicados accidentales (NC y otros) creados por reinicios
        List<PuntoDocumento> todos = puntoDocumentoRepository.findAll();
        for (PuntoDocumento d : todos) {
            if (d.getNumero() == 0) {
                String s = d.getSerie();
                // Si la serie tiene un nmero mayor a 01, es un duplicado no usado de los reinicios
                if (s != null && s.length() > 2) {
                    try {
                        String numPart = s.replaceAll("[^0-9]", "");
                        if (!numPart.isEmpty() && Integer.parseInt(numPart) > 1) { // Ej: FC002, B002
                            puntoDocumentoRepository.delete(d);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        // 1. Corregir documentos antiguos si existen
        fixExistingDocumentCodes();

        // 2. Buscar el punto de Admisión (ID 5 según tu SQL) para insertar los nuevos
        puntoRepository.findAll().stream()
                .filter(p -> p.getPunto() == 5L)
                .findFirst()
                .ifPresent(this::seedExtraDocuments);

        // 3. Crear series de Nota de Crédito (FC01 y BC01) para TODOS los puntos de venta
        puntoRepository.findAll().forEach(this::seedCreditNotes);
    }
    
    @Transactional
    public void asignarDocumentosPorDefecto(Punto punto) {
        seedExtraDocuments(punto);
        seedCreditNotes(punto);
    }

    private void fixExistingDocumentCodes() {
        updateTipoDoc("FACTURA", "01");
        updateTipoDoc("BOLETA", "03");
        updateTipoDoc("NOTA CREDITO", "07");
    }

    private void updateTipoDoc(String nombreBusqueda, String codigoSunat) {
        List<PuntoDocumento> docs = puntoDocumentoRepository.findByTipoDocumentoTipoDocIgnoreCase(nombreBusqueda);
        if (!docs.isEmpty()) {
            tipoDocumentoRepository.findByTipoDoc(codigoSunat).ifPresent(td -> {
                docs.forEach(d -> d.setTipoDocumento(td));
                puntoDocumentoRepository.saveAll(docs);
            });
        }
    }

    private String generateNextSerie(String prefix, String tipoDoc) {
        List<PuntoDocumento> docs = puntoDocumentoRepository.findByTipoDocumentoTipoDoc(tipoDoc);
        int max = 0;
        for (PuntoDocumento doc : docs) {
            String s = doc.getSerie();
            if (s != null && s.startsWith(prefix) && s.length() > prefix.length()) {
                try {
                    int num = Integer.parseInt(s.substring(prefix.length()));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        }
        max++;
        return prefix + String.format("%03d", max);
    }

    private Long getPlantillaId(String nombreBase, Punto punto) {
        Plantilla p = plantillaRepository.findByNombre(nombreBase).orElse(null);
        return p != null ? p.getId() : null;
    }

    private void seedExtraDocuments(Punto punto) {
        if (!puntoDocumentoRepository.buscarPorCriterios(punto.getPunto(), "01", null).isEmpty()) {
            return;
        }
        Long idFactura = getPlantillaId("defecto_factura_venta_a4", punto);
        Long idBoleta = getPlantillaId("defecto_factura_venta_a4", punto); // Usa misma plantilla que factura
        Long idTicket = getPlantillaId("defecto_venta_ticket", punto);
        Long idIngreso = getPlantillaId("defecto_movimientos_ingresos_ticket", punto);
        Long idSalida = getPlantillaId("defecto_movimientos_salidas_ticket", punto);

        String serieFactura = generateNextSerie("F", "01");
        registrarDocumento(punto, "01", serieFactura, 1, idFactura, Modulo.VENTA, "asd");

        String serieBoleta = generateNextSerie("B", "03");
        registrarDocumento(punto, "03", serieBoleta, 1, idBoleta, Modulo.VENTA, "asd");

        String serieTicket = generateNextSerie("T", "12");
        registrarDocumento(punto, "12", serieTicket, 2, idTicket, Modulo.VENTA, "123");

        String serieIngreso = generateNextSerie("ID", "12");
        registrarDocumento(punto, "12", serieIngreso, 4, idIngreso, Modulo.INGRESOS_DIVERSOS, "123");

        String serieSalida = generateNextSerie("SD", "12");
        registrarDocumento(punto, "12", serieSalida, 4, idSalida, Modulo.SALIDAS_DIVERSAS, "123");
    }

    private void seedCreditNotes(Punto punto) {
        if (!puntoDocumentoRepository.buscarPorCriterios(punto.getPunto(), "07", null).isEmpty()) {
            return;
        }
        Long idNC = getPlantillaId("defecto_nota_credito_a4", punto);

        String serieNCFactura = generateNextSerie("FC", "07");
        registrarDocumento(punto, "07", serieNCFactura, 1, idNC, Modulo.VENTA, "localhost");

        String serieNCBoleta = generateNextSerie("BC", "07");
        registrarDocumento(punto, "07", serieNCBoleta, 1, idNC, Modulo.VENTA, "localhost");
    }

    private void registrarDocumento(Punto punto, String codTipoDoc, String serie,
            Integer idDocimp, Long idPlantilla, Modulo modulo, String ip) {

        tipoDocumentoRepository.findByTipoDoc(codTipoDoc).ifPresent(tipo -> {
            PuntoDocumento doc = new PuntoDocumento();
            doc.setPunto(punto);
            doc.setTipoDocumento(tipo);
            doc.setSerie(serie);
            doc.setNumero(0); // Según tu SQL es 0
            doc.setIdDocimp(idDocimp);
            doc.setIdPlantilla(idPlantilla);
            doc.setModulo(modulo);
            doc.setEstado(EstadoGeneral.ACTIVO);
            doc.setIp(ip); // En tu SQL 'asd' y '123' están en la columna IP

            // Campos por defecto según tu SQL (vacíos)
            doc.setRefact("");
            doc.setSelecc("");
            doc.setDetNc("");
            doc.setX("");
            doc.setNota("");
            doc.setLpt("");
            doc.setRefactDia(0);

            puntoDocumentoRepository.save(doc);
        });
    }
}
