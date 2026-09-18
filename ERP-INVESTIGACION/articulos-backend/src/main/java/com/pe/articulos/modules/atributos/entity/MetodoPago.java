package com.pe.articulos.modules.atributos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "metodos_pago", indexes = {
        @Index(name = "idx_metodos_pago_estado", columnList = "estado")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class MetodoPago extends BaseAtributo {
}
