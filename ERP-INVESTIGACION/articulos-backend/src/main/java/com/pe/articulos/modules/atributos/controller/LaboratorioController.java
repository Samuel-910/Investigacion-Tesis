package com.pe.articulos.modules.atributos.controller;

import com.pe.articulos.modules.atributos.entity.Laboratorio;
import com.pe.articulos.modules.atributos.service.LaboratorioService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController extends BaseAtributoController<Laboratorio> {

    public LaboratorioController(LaboratorioService service) {
        super(service, "Laboratorio");
    }
}
