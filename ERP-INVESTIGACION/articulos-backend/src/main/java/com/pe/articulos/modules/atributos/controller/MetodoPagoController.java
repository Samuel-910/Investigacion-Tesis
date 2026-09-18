package com.pe.articulos.modules.atributos.controller;

import com.pe.articulos.modules.atributos.entity.MetodoPago;
import com.pe.articulos.modules.atributos.service.MetodoPagoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController extends BaseAtributoController<MetodoPago> {
    public MetodoPagoController(MetodoPagoService service) {
        super(service, "Método de Pago");
    }
}
