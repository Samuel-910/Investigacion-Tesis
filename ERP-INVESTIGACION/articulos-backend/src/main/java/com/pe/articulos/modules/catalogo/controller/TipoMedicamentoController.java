package com.pe.articulos.modules.catalogo.controller;

import com.pe.articulos.modules.atributos.controller.BaseAtributoController;
import com.pe.articulos.modules.catalogo.entity.TipoMedicamento;
import com.pe.articulos.modules.catalogo.service.TipoMedicamentoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo/tipos-medicamentos")
public class TipoMedicamentoController extends BaseAtributoController<TipoMedicamento> {

    public TipoMedicamentoController(TipoMedicamentoService service) {
        super(service, "Tipo Medicamento");
    }
}
