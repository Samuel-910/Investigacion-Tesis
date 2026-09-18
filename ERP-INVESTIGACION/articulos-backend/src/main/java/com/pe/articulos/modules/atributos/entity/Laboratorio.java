package com.pe.articulos.modules.atributos.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "laboratorios", indexes = {
        @Index(name = "idx_laboratorios_estado", columnList = "estado")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Laboratorio extends BaseAtributo {
}
