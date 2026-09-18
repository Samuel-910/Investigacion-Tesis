package com.pe.articulos.modules.puntos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.pe.articulos.modules.almacen.entity.Almacen;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "puntos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE puntos SET valido = 'N' WHERE punto = ?")
@SQLRestriction("valido = 'S'")
public class Punto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long punto;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Sucursal sucursal;

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Tipo tipoRelacion;

    @Column(name = "id_tipo")
    private Long idTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Proceso procesoRelacion;

    @Column(name = "id_proceso")
    private Long idProceso;

    @Column(name = "tippro", length = 50)
    private String tippro;

    @Column(name = "id_nivel")
    private Integer idNivel;

    @Column(name = "excep", length = 10)
    private String excep;

    @Column(name = "impresion", length = 255)
    private String impresion;

    @Column(name = "imprime_comp", length = 10)
    private String imprimeComp;

    @Column(name = "agrupa_serv", length = 10)
    private String agrupaServ;

    @Column(name = "emite_honor_med", length = 10)
    private String emiteHonorMed;

    @Column(name = "tipo2", length = 50)
    private String tipo2;

    @Column(name = "abreviacion", length = 50)
    private String abreviacion;

    @Column(name = "fecini")
    private LocalDateTime fecini;

    @Column(name = "fecfin")
    private LocalDateTime fecfin;

    @Column(name = "cencos", length = 50)
    private String cencos;

    @Column(name = "valido", length = 10)
    private String valido;

    @Column(name = "crea_cuenta")
    private LocalDateTime creaCuenta;

    @Column(name = "carga_presup", precision = 10, scale = 2)
    private BigDecimal cargaPresup;

    @Column(name = "ult_consultas")
    private Integer ultConsultas;

    @Column(name = "acumula_pedidos", precision = 10, scale = 2)
    private BigDecimal acumulaPedidos;

    @Column(name = "carga_paquete")
    private Integer cargaPaquete;

    @Column(name = "ver_nro_ctas")
    private Integer verNroCtas;

    @Column(name = "carga_cuenta")
    private Integer cargaCuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_almacen", insertable = false, updatable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Almacen almacen;

    @Column(name = "id_almacen")
    private Long idAlmacen;

    @Column(name = "cobra", length = 10)
    private String cobra;

    @Column(name = "busca_x_iniciales", length = 10)
    private String buscaXIniciales;

    @Column(name = "carga_medico", length = 10)
    private String cargaMedico;

    @Column(name = "ip_acceso_modulo", length = 100)
    private String ipAccesoModulo;

    @Column(name = "imprime_preventa", length = 10)
    private String imprimePreventa;

    @Column(name = "modulo")
    private Integer modulo;

    @Column(name = "orden", length = 50)
    private String orden;

    @Column(name = "receta", length = 10)
    private String receta;

    @Column(name = "terminal", length = 50)
    private String terminal;

    @Column(name = "tipo_ahe", length = 50)
    private String tipoAhe;

    @Column(name = "rep_recep", length = 10)
    private String repRecep;

    @Column(name = "autorizado", length = 10)
    private String autorizado;

    @Column(name = "senc", length = 10)
    private String senc;

    @Column(name = "abrev_senc", length = 50)
    private String abrevSenc;

    @Column(name = "cencos_old", length = 50)
    private String cencosOld;

    @Column(name = "arqueo", length = 10)
    private String arqueo;

    @Column(name = "fe", length = 10)
    private String fe;

    // ==========================================
    // RELACIONES INVERSAS (De Finanzas)
    // ==========================================

    @OneToMany(mappedBy = "punto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<PuntoDocumento> documentos = new HashSet<>();

    // ==========================================
    // AUDITORÍA (De Finanzas)
    // ==========================================

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // MÉTODOS DE UTILIDAD
    // ==========================================

    public void addDocumento(PuntoDocumento documento) {
        documentos.add(documento);
        documento.setPunto(this);
    }

    public void removeDocumento(PuntoDocumento documento) {
        documentos.remove(documento);
        documento.setPunto(null);
    }
}
