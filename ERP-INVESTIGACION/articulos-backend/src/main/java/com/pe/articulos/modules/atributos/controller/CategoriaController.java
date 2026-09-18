package com.pe.articulos.modules.atributos.controller;

import com.pe.articulos.modules.atributos.entity.Categoria;
import com.pe.articulos.modules.atributos.service.CategoriaService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController extends BaseAtributoController<Categoria> {

    public CategoriaController(CategoriaService service) {
        super(service, "Categoría");
    }
}
