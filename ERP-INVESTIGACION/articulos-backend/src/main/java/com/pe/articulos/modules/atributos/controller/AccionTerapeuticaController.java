package com.pe.articulos.modules.atributos.controller;

import com.pe.articulos.modules.atributos.entity.AccionTerapeutica;
import com.pe.articulos.modules.atributos.service.AccionTerapeuticaService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/acciones-terapeuticas")
public class AccionTerapeuticaController extends BaseAtributoController<AccionTerapeutica> {

    public AccionTerapeuticaController(AccionTerapeuticaService service) {
        super(service, "Acción Terapéutica");
    }
}
