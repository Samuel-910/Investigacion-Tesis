package com.pe.articulos.modules.puntos.controller;

import com.pe.articulos.modules.atributos.controller.BaseAtributoController;
import com.pe.articulos.modules.puntos.entity.Tipo;
import com.pe.articulos.modules.puntos.service.TipoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/puntos/tipos")
public class TipoController extends BaseAtributoController<Tipo> {

    public TipoController(TipoService service) {
        super(service, "Tipo");
    }
}
