package com.uade.tpo.demo.service;

import com.uade.tpo.demo.entity.PagoParcial;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.Venta;
import com.uade.tpo.demo.entity.dto.PagoParcialRequest;
import com.uade.tpo.demo.repository.PagoParcialRepository;
import com.uade.tpo.demo.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoParcialServiceImpl implements PagoParcialService {

    @Autowired
    private PagoParcialRepository pagoParcialRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Override
    @Transactional
    public PagoParcial registrarPagoParcial(PagoParcialRequest request) {

        if (request.getIdVentas() == null || request.getIdVentas().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos una venta a pagar");
        }

        List<Venta> ventas = ventaRepository.findAllById(request.getIdVentas());
        if (ventas.size() != request.getIdVentas().size()) {
    throw new RuntimeException(
            "Una o más ventas no existen");
}
        if (ventas.isEmpty()) {
            throw new RuntimeException("No se encontraron las ventas indicadas");
        }
Long empresaId =
        ventas.get(0)
              .getCliente()
              .getIdCliente();

for (Venta v : ventas) {

    if (!v.getCliente()
            .getIdCliente()
            .equals(empresaId)) {

        throw new RuntimeException(
                "Todas las ventas deben pertenecer a la misma empresa");
    }
}
        

        BigDecimal montoTotal = BigDecimal.ZERO;

        // Marcar las ventas seleccionadas como pagadas y acumular el total
        LocalDateTime ahora = request.getFechaPago() != null ? request.getFechaPago() : LocalDateTime.now();
        for (Venta v : ventas) {

            if (v.getEstado() != TipoDePago.DEBE) {
                throw new RuntimeException(
                        "La venta "
                        + v.getIdVenta()
                        + " no pertenece a una cuenta corriente");
            }

            v.setEstado(TipoDePago.DEUDA_PAGADA);

            v.setFechaPago(ahora);

            if (v.getMonto() != null) {
                montoTotal = montoTotal.add(v.getMonto());
            }
        }
        ventaRepository.saveAll(ventas);

        PagoParcial pago = new PagoParcial();
        pago.setFechaPago(ahora);
        pago.setPayerName(request.getPayerName());
        pago.setCuit(request.getCuit());
        pago.setFactura(request.getFactura());
        pago.setObservaciones(request.getObservaciones());
        pago.setMontoTotal(montoTotal);
        pago.setVentas(ventas);

        return pagoParcialRepository.save(pago);
    }

    @Override
    public List<PagoParcial> obtenerTodos() {
        return pagoParcialRepository.findAll();
    }
}
