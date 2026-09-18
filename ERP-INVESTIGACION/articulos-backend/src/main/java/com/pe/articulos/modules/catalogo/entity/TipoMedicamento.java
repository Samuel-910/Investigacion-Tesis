package com.pe.articulos.modules.catalogo.entity;

import com.pe.articulos.modules.atributos.entity.BaseAtributo;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "catalogo_tipo_medicamento", indexes = {
        @Index(name = "idx_tipo_med_estado", columnList = "estado")
})
public class TipoMedicamento extends BaseAtributo {
}
