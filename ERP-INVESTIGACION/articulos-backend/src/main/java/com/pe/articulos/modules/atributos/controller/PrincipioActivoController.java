package com.pe.articulos.modules.atributos.controller;

import com.pe.articulos.modules.atributos.entity.PrincipioActivo;
import com.pe.articulos.modules.atributos.service.PrincipioActivoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/principios-activos")
public class PrincipioActivoController extends BaseAtributoController<PrincipioActivo> {

    public PrincipioActivoController(PrincipioActivoService service) {
        super(service, "Principio Activo");
    }
}
