package com.pe.articulos.modules.productos.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pe.articulos.modules.sucursal.entity.Sucursal;
import com.pe.articulos.modules.users.entity.DatosPersonales;
import com.pe.articulos.core.enums.EstadoGeneral;
import com.pe.articulos.core.enums.conversor.EstadoGeneralConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "movimiento_diverso", indexes = {
        @Index(name = "idx_movimiento_sucursal", columnList = "id_sucursal"),
        @Index(name = "idx_movimiento_fecha", columnList = "fecha"),
        @Index(name = "idx_movimiento_estado", columnList = "estado")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoDiverso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "motivo", nullable = false, length = 100)
    private String motivo;

    @Column(name = "estado", nullable = false)
    @Convert(converter = EstadoGeneralConverter.class)
    @Builder.Default
    private EstadoGeneral estado = EstadoGeneral.ACTIVO; // ACTIVO, ANULADO

    @Column(name = "num_documento", length = 50)
    private String numDocumento; // Opcional, generado o manual

    @Column(name = "serie", length = 20)
    private String serie;

    @Column(name = "numero")
    private Integer numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "direccion", "telefono", "estado", "fechaCreacion", "fechaActualizacion", "personal"})
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnoreProperties({
        "hibernateLazyInitializer", "handler", "verNombre", "verApepat", "verApemat", "sexo", "nacfec", "naclug", 
        "naclocl", "estciv", "rhc", "tipodoc", "numdoc", "direcc", "domref", "domloc", "fonLocal", "fono2", 
        "login", "passwd", "idInaru", "idOcupac", "email", "foto", "ruc", "gradoInaru", "tipoSangre", 
        "cemLabor", "direcLabor", "fonoLabor", "anexoLabor", "idProsed", "nombreProsed", "fechaIngRna", 
        "observacion", "fotoCaminoFicha", "fallecido", "fechaFallec", "idUltimoCue", "idPersonalUser", 
        "ultimoNaru", "ultimaRazon", "idJuridico", "nomOcupacion", "numFolioInteger", "archLista", 
        "ultimaDirecCue", "gruposSanguineo", "ubicacion", "maMiembr", "maApePar", "maApeMat", "paNombre", 
        "paApePat", "paApeMat", "religion", "gradoInaru1", "gradoInaruL", "profesion", "ocupacion", 
        "idReligion", "razaHumana", "lugarProcedencia", "gradoInstruccion", "dniRef", "rhcOld", 
        "fechaBaja", "active", "createdAt", "updatedAt", "lastLogin", "roles", "directPermissions", 
        "vinculosEmpresas", "compania", "segurosAsociados", "punto", "sucursalesAsignadas", "sucursalActual", 
        "useDirectPermissions", "authorities"
    })
    private DatosPersonales usuario;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("movimiento")
    @Builder.Default
    private List<MovimientoDiversoDetalle> detalles = new ArrayList<>();

    public void addDetalle(MovimientoDiversoDetalle detalle) {
        detalles.add(detalle);
        detalle.setMovimiento(this);
    }

    public void removeDetalle(MovimientoDiversoDetalle detalle) {
        detalles.remove(detalle);
        detalle.setMovimiento(null);
    }
}
