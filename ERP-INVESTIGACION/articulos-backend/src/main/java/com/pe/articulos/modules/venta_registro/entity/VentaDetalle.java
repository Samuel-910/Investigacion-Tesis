package com.pe.articulos.modules.venta_registro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "venta_detalle")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movart")
    private Long idMovart;

    @Column(name = "id_articulo")
    private Long idArticulo;

    @Column(name = "glosa", length = 500)
    private String glosa;

    @Column(name = "id_examen")
    private Long idExamen;

    @Column(name = "cantidad", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal cantidad = BigDecimal.ONE;
    @Column(name = "id_convenio_detalle", length = 20)
    private String idConvenioDetalle;

    @Column(name = "valor_af", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal valorAf = BigDecimal.ZERO;

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

    @Column(name = "copago_fact", precision = 12, scale = 4)
    private BigDecimal copagoFact;

    @Column(name = "tipo_copago", length = 10)
    private String tipoCopago;

    @Column(name = "copago_orig", precision = 12, scale = 4)
    private BigDecimal copagoOrig;

    @Column(name = "copago_con_igv", precision = 12, scale = 4)
    private BigDecimal copagoConIgv;

    @Column(name = "copago_corregido", precision = 12, scale = 4)
    private BigDecimal copagoCorregido;

    @Column(name = "copago_corregido1", precision = 12, scale = 4)
    private BigDecimal copagoCorregido1;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "id_medico_ser")
    private Integer idMedicoSer;

    @Column(name = "id_cita")
    private Long idCita;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @Column(name = "estado_atencion", length = 10)
    private String estadoAtencion;

    @Column(name = "id_sol_det")
    private Long idSolDet;

    @Column(name = "pago_hono", precision = 12, scale = 4)
    private BigDecimal pagoHono;

    @Column(name = "coberturado", length = 1)
    private String coberturado;

    @Column(name = "id_personal_user")
    private Integer idPersonalUser;

    @Column(name = "user_upd", length = 50)
    private String userUpd;

    @Column(name = "id_cuenta_alm", length = 20)
    private String idCuentaAlm;

    @Column(name = "id_cuenta_cv", length = 20)
    private String idCuentaCv;

    @Column(name = "impo_liq", precision = 12, scale = 4)
    private BigDecimal impoLiq;

    @Column(name = "centro_costo", length = 50)
    private String centroCosto;

    @Column(name = "id_mov_hon")
    private Long idMovHon;

    @Column(name = "agregar_igv", length = 1)
    private String agregarIgv;

    @Column(name = "modo_det", length = 10)
    private String modoDet;

    @Column(name = "id_presupuesto")
    private Long idPresupuesto;

    @Column(name = "cambia_precio", length = 1)
    private String cambiaPrecio;

    @Column(name = "id_autoriza_desc")
    private Integer idAutorizaDesc;

    @Column(name = "id_paquete")
    private Long idPaquete;

    @Column(name = "id_medico_rec")
    private Integer idMedicoRec;

    @Column(name = "consultorio", length = 50)
    private String consultorio;

    @Column(name = "ret_hono", precision = 12, scale = 4)
    private BigDecimal retHono;

    @Column(name = "id_user_anul")
    private Integer idUserAnul;

    @Column(name = "fecanu")
    private LocalDateTime fecanu;

    @Column(name = "prioridad", length = 10)
    private String prioridad;

    @Column(name = "muestra", length = 100)
    private String muestra;

    @Column(name = "equipo", length = 100)
    private String equipo;

    @Column(name = "dscto_pac", precision = 12, scale = 4)
    private BigDecimal dsctoPac;

    @Column(name = "tipo_serv", length = 10)
    private String tipoServ;

    @Column(name = "motivo_modif", columnDefinition = "TEXT")
    private String motivoModif;

    @Column(name = "respuesta", columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "id_personal_temp")
    private Integer idPersonalTemp;

    @Column(name = "id_tipo_ate_temp")
    private Integer idTipoAteTemp;

    @Column(name = "id_serv_fij_temp")
    private Long idServFijTemp;

    @Column(name = "id_plan_temp")
    private Long idPlanTemp;

    @Column(name = "motivo_anul", columnDefinition = "TEXT")
    private String motivoAnul;

    @Column(name = "id_personal_dig")
    private Integer idPersonalDig;

    @Column(name = "fecha_desc")
    private LocalDateTime fechaDesc;

    @Column(name = "det_transf", length = 1)
    private String detTransf;

    @Column(name = "id_liq")
    private Long idLiq;

    @Column(name = "id_pendiente")
    private Long idPendiente;

    @Column(name = "id_vnt_pendiente")
    private Long idVntPendiente;

    @Column(name = "nivel2", length = 50)
    private String nivel2;

    @Column(name = "nivel1", length = 50)
    private String nivel1;

    @Column(name = "nivel_impresion", length = 50)
    private String nivelImpresion;

    @Column(name = "precio_unitario", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Column(name = "porc_igv", precision = 5, scale = 2)
    private BigDecimal porcIgv;

    @Column(name = "id_pol_copago")
    private Long idPolCopago;

    @Column(name = "id_pol_cob")
    private Long idPolCob;

    @Column(name = "id_almart")
    private Long idAlmart;

    @Column(name = "porc_dsc", precision = 5, scale = 2)
    private BigDecimal porcDsc;

    @Column(name = "porc_cob", precision = 5, scale = 2)
    private BigDecimal porcCob;

    @Column(name = "tipo_precio", length = 10)
    private String tipoPrecio;

    @Column(name = "porc_utilidad", precision = 5, scale = 2)
    private BigDecimal porcUtilidad;

    @Column(name = "rentab", precision = 12, scale = 4)
    private BigDecimal rentab;

    @Column(name = "cu", precision = 12, scale = 4)
    private BigDecimal cu;

    @Column(name = "cantidad_devuelta", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal cantidadDevuelta = BigDecimal.ZERO;

    @Column(name = "id_lote")
    private Long idLote;

    @Column(name = "id_personal_cambio")
    private Integer idPersonalCambio;

    @Column(name = "motivo_cambio", columnDefinition = "TEXT")
    private String motivoCambio;

    @Column(name = "id_medico_ant")
    private Integer idMedicoAnt;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "traspaso", length = 1)
    private String traspaso;

    @Column(name = "user_traspaso", length = 50)
    private String userTraspaso;

    @Column(name = "fecha_traspaso")
    private LocalDateTime fechaTraspaso;

    @Column(name = "cortesia", length = 1)
    private String cortesia;

    @Column(name = "devuelto_hosp", length = 1)
    private String devueltoHosp;

    @Column(name = "sf", length = 1)
    private String sf;

    @Column(name = "p_cirugia", precision = 12, scale = 4)
    private BigDecimal pCirugia;

    @Column(name = "importe_asistencia", precision = 12, scale = 4)
    private BigDecimal importeAsistencia;

    @Column(name = "fecha_upd")
    private LocalDateTime fechaUpd;

    @Column(name = "fecha_activ")
    private LocalDateTime fechaActiv;

    @Column(name = "user_activ", length = 50)
    private String userActiv;

    @Column(name = "motivo_activ", columnDefinition = "TEXT")
    private String motivoActiv;

    @Column(name = "pg_dsc", precision = 12, scale = 4)
    private BigDecimal pgDsc;

    @Column(name = "tipo_atencion", length = 50)
    private String tipoAtencion;

    @Column(name = "id_rel_hono")
    private Long idRelHono;

    @Column(name = "tipo_desc", length = 10)
    private String tipoDesc;

    @Column(name = "f_igv", precision = 12, scale = 4)
    private BigDecimal fIgv;

    @Column(name = "f_valor_venta", precision = 12, scale = 4)
    private BigDecimal fValorVenta;

    @Column(name = "f_bi", precision = 12, scale = 4)
    private BigDecimal fBi;

    @Column(name = "f_total", precision = 12, scale = 4)
    private BigDecimal fTotal;

    @Column(name = "f_parte_hono", precision = 12, scale = 4)
    private BigDecimal fParteHono;

    @Column(name = "id_recargo")
    private Long idRecargo;

    @Column(name = "inafecto", length = 1)
    private String inafecto;

    @Column(name = "id_param")
    private Long idParam;

    @Column(name = "porc_param", precision = 5, scale = 2)
    private BigDecimal porcParam;

    @Column(name = "porc_polcob", precision = 5, scale = 2)
    private BigDecimal porcPolcob;

    @Column(name = "utilidad", precision = 12, scale = 4)
    private BigDecimal utilidad;

    @Column(name = "sist_recep", length = 50)
    private String sistRecep;

    @Column(name = "det_transf2", length = 1)
    private String detTransf2;

    @Column(name = "activa_serv_user", length = 50)
    private String activaServUser;

    @Column(name = "activa_serv_fecha")
    private LocalDateTime activaServFecha;

    @Column(name = "id_movart_relacion")
    private Long idMovartRelacion;

    @Column(name = "id_mov_det")
    private Long idMovDet;

    @Column(name = "gen_labo", length = 1)
    private String genLabo;

    @Column(name = "hora_examen")
    private LocalDateTime horaExamen;

    @Column(name = "examen_pend", length = 1)
    private String examenPend;

    @Column(name = "receta", columnDefinition = "TEXT")
    private String receta;

    @Column(name = "oculto", length = 1)
    private String oculto;

    @Column(name = "ip_anul", length = 50)
    private String ipAnul;

    @Column(name = "med_emer", length = 1)
    private String medEmer;

    @Column(name = "validado", length = 1)
    private String validado;

    @Column(name = "imp", length = 1)
    private String imp;

    @Column(name = "nro_lote", length = 50)
    private String nroLote;

    @Column(name = "fecha_venc")
    private LocalDate fechaVenc;

    @Column(name = "cod_digemid", length = 50)
    private String codDigemid;

    @Column(name = "pre_kairos", precision = 12, scale = 4)
    private BigDecimal preKairos;

    @Column(name = "no_factu", length = 1)
    private String noFactu;

    @Column(name = "obs_liq", columnDefinition = "TEXT")
    private String obsLiq;

    @Column(name = "habit", length = 50)
    private String habit;

    @Column(name = "cama", length = 50)
    private String cama;

    @Column(name = "z_total", precision = 12, scale = 4)
    private BigDecimal zTotal;

    @Column(name = "z_igv", precision = 12, scale = 4)
    private BigDecimal zIgv;

    @Column(name = "z_base_imp", precision = 12, scale = 4)
    private BigDecimal zBaseImp;

    @Column(name = "z_descuento", precision = 12, scale = 4)
    private BigDecimal zDescuento;

    @Column(name = "z_valor_af", precision = 12, scale = 4)
    private BigDecimal zValorAf;

    @Column(name = "total_mod", precision = 12, scale = 4)
    private BigDecimal totalMod;

    @Column(name = "favor_cliente", precision = 12, scale = 4)
    private BigDecimal favorCliente;

    @Column(name = "z_valor_venta", precision = 12, scale = 4)
    private BigDecimal zValorVenta;

    @Column(name = "id_detalle")
    private Long idDetalle;

    @Column(name = "id_catalogo")
    private Long idCatalogo;

    @Column(name = "item")
    private Integer item;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio_venta", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @Column(name = "valor_unitario", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @Column(name = "valor_total", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(name = "monto_descuento", precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "id_orden")
    private String idOrden;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", foreignKey = @ForeignKey(name = "fk_detalle_venta"))
    private VentaRegistro ventaRegistro;

    public void calcularTotales() {
        BigDecimal multiplicadorIGV = new BigDecimal("1.18");

        BigDecimal cantidadEfectiva = cantidad.subtract(cantidadDevuelta != null ? cantidadDevuelta : BigDecimal.ZERO);
        BigDecimal baseBruta = precioVenta.multiply(cantidadEfectiva);

        boolean isExonerado = "E".equalsIgnoreCase(this.inafecto);
        boolean isInafecto = "I".equalsIgnoreCase(this.inafecto) || "S".equalsIgnoreCase(this.inafecto); // 'S' por

        boolean isNoGravado = isExonerado || isInafecto;

        if (porcentajeDescuento != null && porcentajeDescuento.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal descBase = baseBruta.multiply(porcentajeDescuento).divide(new BigDecimal("100"), 4,
                    java.math.RoundingMode.HALF_UP);

            if (isNoGravado) {
                this.montoDescuento = descBase;
            } else {
                this.montoDescuento = descBase.multiply(multiplicadorIGV).setScale(4, java.math.RoundingMode.HALF_UP);
            }
        } else if (this.montoDescuento == null) {
            this.montoDescuento = BigDecimal.ZERO;
        }

        if (isNoGravado) {

            this.baseImp = BigDecimal.ZERO;
            this.igv = BigDecimal.ZERO;
            BigDecimal montoFinal = baseBruta.subtract(this.montoDescuento);

            if (isExonerado) {
                this.valorExo = montoFinal;
                this.valorInaf = BigDecimal.ZERO;
            } else {
                this.valorExo = BigDecimal.ZERO;
                this.valorInaf = montoFinal;
            }

            this.total = montoFinal;
            this.descuento = this.montoDescuento;
            this.igvDescuento = BigDecimal.ZERO;

            this.valorUnitario = precioVenta;
            this.valorTotal = this.total;
        } else {

            BigDecimal totalBruto = baseBruta.multiply(multiplicadorIGV).setScale(4, java.math.RoundingMode.HALF_UP);
            this.total = totalBruto.subtract(this.montoDescuento);

            this.baseImp = this.total.divide(multiplicadorIGV, 4, java.math.RoundingMode.HALF_UP);
            this.igv = this.total.subtract(this.baseImp);
            this.valorInaf = BigDecimal.ZERO;
            this.valorExo = BigDecimal.ZERO;

            this.descuento = this.montoDescuento.divide(multiplicadorIGV, 4, java.math.RoundingMode.HALF_UP);
            this.igvDescuento = this.montoDescuento.subtract(this.descuento);

            this.valorUnitario = precioVenta;
            this.valorTotal = this.baseImp;
        }

    }
}
