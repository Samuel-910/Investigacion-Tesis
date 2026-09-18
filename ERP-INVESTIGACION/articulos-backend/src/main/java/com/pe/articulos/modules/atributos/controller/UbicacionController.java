package com.pe.articulos.modules.atributos.controller;

import com.pe.articulos.modules.atributos.entity.Ubicacion;
import com.pe.articulos.modules.atributos.service.UbicacionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionController extends BaseAtributoController<Ubicacion> {

    public UbicacionController(UbicacionService service) {
        super(service, "Ubicación");
    }
}
