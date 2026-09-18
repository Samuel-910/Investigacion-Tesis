package com.pe.articulos.modules.venta_registro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import com.pe.articulos.modules.puntos.entity.Punto;
import com.pe.articulos.modules.atributos.entity.MetodoPago;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "venta_registro")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("null")
public class VentaRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long idVenta;

    @Column(name = "id_cont")
    private Long idCont;

    @Column(name = "id_sucursal")
    private Long idSucursal;

    @Column(name = "id_ctacte")
    private Long idCtacte;

    @Column(name = "id_personal")
    private Long idPersonal;

    @Column(name = "id_almacen")
    private Long idAlmacen;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "id_docimp")
    private Integer idDocimp;

    @Column(name = "punto")
    private Long punto;

    @Column(name = "tipo_doc", length = 20)
    private String tipoDoc;

    @Column(name = "serie", length = 10)
    private String serie;

    @Column(name = "numdoc", length = 20)
    private String numdoc;

    @Column(name = "modo", length = 10)
    private String modo;

    @Column(name = "valor_afecto", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal valorAfecto = BigDecimal.ZERO;

    @Column(name = "valor_inaf", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal valorInaf = BigDecimal.ZERO;

    @Column(name = "valor_exo", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal valorExo = BigDecimal.ZERO;

    @Column(name = "valor_venta", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal valorVenta = BigDecimal.ZERO;

    @Column(name = "descuento", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "descuento_esp", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal descuentoEsp = BigDecimal.ZERO;

    @Column(name = "cobertura", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal cobertura = BigDecimal.ZERO;

    @Column(name = "base_imp", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal baseImp = BigDecimal.ZERO;

    @Column(name = "igv", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(name = "igv_descuento", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal igvDescuento = BigDecimal.ZERO;

    @Column(name = "total", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "copago", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal copago = BigDecimal.ZERO;

    @Column(name = "voucher", length = 50)
    private String voucher;

    @Column(name = "id_nivel_cont")
    private Integer idNivelCont;

    @Column(name = "id_nivel_vnt")
    private Integer idNivelVnt;

    @Column(name = "id_cuenta_cli", length = 20)
    private String idCuentaCli;

    @Column(name = "id_cuenta_igv", length = 20)
    private String idCuentaIgv;

    @Column(name = "id_cuenta_vnt", length = 20)
    private String idCuentaVnt;

    @Column(name = "id_cuenta_des", length = 20)
    private String idCuentaDes;

    @Column(name = "user_upd", length = 50)
    private String userUpd;

    @Column(name = "id_personal_user", length = 50)
    private String idPersonalUser;

    @Column(name = "ip", length = 50)
    private String ip;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @Column(name = "id_vnt_ref")
    private Long idVntRef;

    @Column(name = "tipo_pac", length = 2)
    private String tipoPac;

    @Column(name = "id_mov_vnt2")
    private Long idMovVnt2;

    @Column(name = "tipo_ope", length = 10)
    private String tipoOpe;

    @Column(name = "ret_hono", precision = 12, scale = 4)
    private BigDecimal retHono;

    @Column(name = "xid_politica")
    private Long xidPolitica;

    @Column(name = "sub_cta", length = 50)
    private String subCta;

    @Column(name = "id_tipo_ate")
    private Integer idTipoAte;

    @Column(name = "id_vnt_franq")
    private Long idVntFranq;

    @Column(name = "nombre_pac", length = 200)
    private String nombrePac;

    @Column(name = "id_personal_dig")
    private Integer idPersonalDig;

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "tipo_ingx", length = 10)
    private String tipoIngx;

    @Column(name = "id_pendiente")
    private Long idPendiente;

    @Column(name = "pac_pendiente", length = 200)
    private String pacPendiente;

    @Column(name = "ref_motivo", columnDefinition = "TEXT")
    private String refMotivo;

    @Column(name = "ref_pac", length = 200)
    private String refPac;

    @Column(name = "ref_doc", length = 50)
    private String refDoc;

    @Column(name = "ref_fono", length = 50)
    private String refFono;

    @Column(name = "ref_obs", columnDefinition = "TEXT")
    private String refObs;

    @Column(name = "id_autoriza")
    private Integer idAutoriza;

    @Column(name = "cod_afi", length = 50)
    private String codAfi;

    @Column(name = "nhc", length = 50)
    private String nhc;

    @Column(name = "direc_ruc", length = 200)
    private String direcRuc;

    @Column(name = "ruc", length = 20)
    private String ruc;

    @Column(name = "razon", length = 200)
    private String razon;

    @Column(name = "autorizado", length = 1)
    private String autorizado;

    @Column(name = "autorizador", length = 100)
    private String autorizador;

    @Column(name = "fecha_autoriza")
    private LocalDateTime fechaAutoriza;

    @Column(name = "cargo_usado", length = 50)
    private String cargoUsado;

    @Column(name = "forma_receta", length = 50)
    private String formaReceta;

    @Column(name = "tipo_receta", length = 50)
    private String tipoReceta;

    @Column(name = "ip_acceso", length = 50)
    private String ipAcceso;

    @Column(name = "ip_impresion", length = 50)
    private String ipImpresion;

    @Column(name = "concepto", columnDefinition = "TEXT")
    private String concepto;

    @Column(name = "cta_anul_nota", length = 20)
    private String ctaAnulNota;

    @Column(name = "motivo_bloq", columnDefinition = "TEXT")
    private String motivoBloq;

    @Column(name = "estado_bloq", length = 1)
    private String estadoBloq;

    @Column(name = "fecha_bloq")
    private LocalDateTime fechaBloq;

    @Column(name = "tipo_atencion", length = 50)
    private String tipoAtencion;

    @Column(name = "id_destino")
    private Long idDestino;

    @Column(name = "obs", columnDefinition = "TEXT")
    private String obs;

    @Column(name = "fecha_upd")
    private LocalDateTime fechaUpd;

    @Column(name = "id_rel_hono")
    private Long idRelHono;

    @Column(name = "motivo_anul2", columnDefinition = "TEXT")
    private String motivoAnul2;

    @Column(name = "id_user_anul")
    private Integer idUserAnul;

    @Column(name = "fecha_anul")
    private LocalDateTime fechaAnul;

    @Column(name = "serie_ticketera", length = 20)
    private String serieTicketera;

    @Column(name = "id_medico_hono")
    private Long idMedicoHono;

    @Column(name = "total_ope", precision = 12, scale = 4)
    private BigDecimal totalOpe;

    @Column(name = "bi_ivap", precision = 12, scale = 4)
    private BigDecimal biIvap;

    @Column(name = "ivap", precision = 12, scale = 4)
    private BigDecimal ivap;

    @Column(name = "estado_cp", length = 10)
    private String estadoCp;

    @Column(name = "uri", length = 500)
    private String uri;

    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @Column(name = "id_atencion")
    private Long idAtencion;

    @Column(name = "id_mov_original")
    private Long idMovOriginal;

    @Column(name = "cupones", length = 100)
    private String cupones;

    @Column(name = "id_mov")
    private Long idMov;

    @Column(name = "tipo_dni", length = 10)
    private String tipoDni;

    @Column(name = "nro_dni", length = 20)
    private String nroDni;

    @Column(name = "id_pae_cm")
    private Long idPaeCm;

    @Column(name = "pedido", length = 50)
    private String pedido;

    @Column(name = "importe", precision = 12, scale = 4)
    private BigDecimal importe;

    @Column(name = "vuelto", precision = 12, scale = 4)
    private BigDecimal vuelto;

    @Column(name = "id_orden_proc", length = 50)
    private String idOrdenProc;

    @Column(name = "ip_anul", length = 50)
    private String ipAnul;

    @Column(name = "id_acceso_lab")
    private Long idAccesoLab;

    @Column(name = "id_pae_ref")
    private Long idPaeRef;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @Column(name = "importe_pago", precision = 12, scale = 4)
    private BigDecimal importePago;

    @Column(name = "importe_soles", precision = 12, scale = 4)
    private BigDecimal importeSoles;

    @Column(name = "importe_dolares", precision = 12, scale = 4)
    private BigDecimal importeDolares;

    @Column(name = "importe_tarjeta", precision = 12, scale = 4)
    private BigDecimal importeTarjeta;

    @Column(name = "tipo_cambio_dolares", precision = 12, scale = 4)
    private BigDecimal tipoCambioDolares;

    @Column(name = "cambio_dolares", precision = 12, scale = 4)
    private BigDecimal cambioDolares;

    @Column(name = "id_ref_redondeo")
    private Long idRefRedondeo;

    @Column(name = "lote", length = 50)
    private String lote;

    @Column(name = "item")
    private Integer item;

    @Column(name = "id_inter")
    private Long idInter;

    @Column(name = "chk_cta", length = 1)
    private String chkCta;

    @Column(name = "cob_pag_anest", precision = 12, scale = 4)
    private BigDecimal cobPagAnest;

    @Column(name = "id_pae")
    private Long idPae;

    @Column(name = "arreglo_chana", columnDefinition = "TEXT")
    private String arregloChana;

    @Column(name = "id_plan_borrado")
    private Long idPlanBorrado;

    @Column(name = "tipodoc", length = 10)
    private String tipodoc;

    @Column(name = "authored_id")
    private Integer authoredId;

    @Column(name = "authored_date")
    private LocalDateTime authoredDate;

    @Column(name = "favor_cliente", precision = 12, scale = 4)
    private BigDecimal favorCliente;

    @Column(name = "total_mod", precision = 12, scale = 4)
    private BigDecimal totalMod;

    @Column(name = "r_descuento", precision = 12, scale = 4)
    private BigDecimal rDescuento;

    @Column(name = "r_igv", precision = 12, scale = 4)
    private BigDecimal rIgv;

    @Column(name = "r_valor_afecto", precision = 12, scale = 4)
    private BigDecimal rValorAfecto;

    @Column(name = "dt_hora_toma")
    private LocalDateTime dtHoraToma;

    @Column(name = "nro_hab", length = 20)
    private String nroHab;

    @Column(name = "id_regdia")
    private Long idRegdia;

    @Column(name = "cdi_id_paciente")
    private Long cdiIdPaciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo_pago")
    private MetodoPago metodoPago;

    @Column(name = "ref_metodo_pago", length = 100)
    private String refMetodoPago;

    @Column(name = "cant_dev", precision = 10, scale = 2)
    private BigDecimal cantDev;

    @Column(name = "id_user_cupon")
    private Integer idUserCupon;

    @Column(name = "fecha_cupon")
    private LocalDateTime fechaCupon;

    @Column(name = "id_plantilla")
    private Long idPlantilla;

    @Column(name = "numero")
    private Integer numero;

    @Column(name = "moneda", length = 3)
    @Builder.Default
    private String moneda = "PEN";

    @Column(name = "tc", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal tc = BigDecimal.ONE;

    @Column(name = "imp_vta", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal impVta = BigDecimal.ZERO;

    @Column(name = "id_medico")
    private Long idMedico;

    @Column(name = "id_user")
    private Integer idUser;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "enlace_pdf", length = 500)
    private String enlacePdf;

    @Column(name = "enlace_xml", length = 500)
    private String enlaceXml;

    @Column(name = "enlace_cdr", length = 500)
    private String enlaceCdr;

    @Column(name = "cadena_para_codigo_qr", length = 1000)
    private String cadenaParaCodigoQr;

    @Column(name = "codigo_hash", length = 100)
    private String codigoHash;

    @Column(name = "signature_value", length = 1000)
    private String signatureValue;

    @Column(name = "aceptada_por_sunat")
    private Boolean aceptadaPorSunat;

    @Column(name = "sunat_description", length = 500)
    private String sunatDescription;

    @Column(name = "sunat_response_code", length = 10)
    private String sunatResponseCode;

    @Column(name = "id_orden")
    private Long idOrden;

    @Column(name = "id_paquete")
    private Long idPaquete;
    @Column(name = "id_plan")
    private Long idPlan;
    @Column(name = "id_rel_paquete")
    private Long idRelPaquete;

    @Column(name = "reimpresiones")
    @Builder.Default
    private Integer reimpresiones = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto", referencedColumnName = "punto", insertable = false, updatable = false)
    private Punto puntoVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_doc", referencedColumnName = "tipo_doc", insertable = false, updatable = false)
    private TipoDocumento tipoDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_pac", referencedColumnName = "codigo", insertable = false, updatable = false)
    private TipoPaciente tipoPaciente;

    @OneToMany(mappedBy = "ventaRegistro", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VentaDetalle> detalles = new ArrayList<>();

    public void calcularTotales() {
        if (estado == null) {
            estado = EstadoGeneral.ACTIVO;
        }
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        if (tipoDoc != null && (tipodoc == null || tipodoc.isEmpty())) {
            this.tipodoc = tipoDoc;
        }
        if (tipodoc != null && (tipoDoc == null || tipoDoc.isEmpty())) {
            this.tipoDoc = tipodoc;
        }

        this.baseImp = detalles.stream().map(d -> d.getBaseImp()).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.igv = detalles.stream().map(d -> d.getIgv()).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorInaf = detalles.stream().map(d -> d.getValorInaf()).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorExo = detalles.stream().map(d -> d.getValorExo()).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.descuento = detalles.stream().map(d -> d.getDescuento()).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.igvDescuento = detalles.stream().map(d -> d.getIgvDescuento()).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        this.total = detalles.stream().map(d -> d.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);

        this.impVta = this.baseImp; // Para compatibilidad SIRE
        this.valorVenta = this.baseImp.add(this.valorInaf).add(this.valorExo);
    }

    public void agregarDetalle(VentaDetalle detalle) {
        detalles.add(detalle);
        detalle.setVentaRegistro(this);
    }

    public void removeDetalle(VentaDetalle detalle) {
        detalles.remove(detalle);
        detalle.setVentaRegistro(null);
    }

}
