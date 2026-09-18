package com.pe.articulos.modules.puntos.controller;

import com.pe.articulos.modules.atributos.controller.BaseAtributoController;
import com.pe.articulos.modules.puntos.entity.Proceso;
import com.pe.articulos.modules.puntos.service.ProcesoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/puntos/procesos")
public class ProcesoController extends BaseAtributoController<Proceso> {

    public ProcesoController(ProcesoService service) {
        super(service, "Proceso");
    }
}
