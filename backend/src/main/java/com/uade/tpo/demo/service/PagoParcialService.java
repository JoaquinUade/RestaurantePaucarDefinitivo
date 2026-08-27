package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.PagoParcial;
import com.uade.tpo.demo.entity.dto.PagoParcialRequest;
import java.util.List;

public interface PagoParcialService {

    PagoParcial registrarPagoParcial(PagoParcialRequest request);

    List<PagoParcial> obtenerTodos();
}