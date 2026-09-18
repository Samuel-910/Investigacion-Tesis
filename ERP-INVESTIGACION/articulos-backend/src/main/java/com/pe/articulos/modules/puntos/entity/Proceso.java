package com.pe.articulos.modules.puntos.entity;

import com.pe.articulos.modules.atributos.entity.BaseAtributo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "puntos_proceso")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Proceso extends BaseAtributo {
}
