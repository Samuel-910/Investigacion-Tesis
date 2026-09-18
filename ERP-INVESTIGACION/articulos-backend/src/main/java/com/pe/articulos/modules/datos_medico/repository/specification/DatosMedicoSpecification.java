package com.pe.articulos.modules.datos_medico.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class DatosMedicoSpecification {

    public static Specification<com.pe.articulos.modules.datos_medico.entity.DatosMedico> conFiltros(
            String nombreMed,
            String nroCmp,
            String estado,
            String tipo,
            Long idArea,
            Long idSucursal,
            String vacaciones,
            String tipoMedico) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nombreMed != null && !nombreMed.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombreMed")),
                        "%" + nombreMed.toLowerCase() + "%"));
            }

            if (nroCmp != null && !nroCmp.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("nroCmp"), nroCmp));
            }

            if (estado != null && !estado.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            if (tipo != null && !tipo.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("tipo"), tipo));
            }

            if (idArea != null) {
                predicates.add(criteriaBuilder.equal(root.get("idArea"), idArea));
            }

            if (idSucursal != null) {
                predicates.add(criteriaBuilder.equal(root.get("idSucursal"), idSucursal));
            }

            if (vacaciones != null && !vacaciones.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("vacaciones"), vacaciones));
            }

            if (tipoMedico != null && !tipoMedico.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("tipoMedico"), tipoMedico));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}