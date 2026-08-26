package paucar.pagos.pagosView;
import java.util.List;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

import javafx.scene.layout.BorderPane;
import paucar.pagos.TablaPagos;
import paucar.service.PagosService;

public class PagosConsumoView extends BorderPane {

    private final PagosService service;
    private final TablaPagos tabla;

    public PagosConsumoView(PagosService service) {

        this.service = service;

        tabla = new TablaPagos(
    null,
    this::recargar,
    service
);


        setCenter(tabla);

        recargar();
    }

    public void recargar() {

        List<PagoEmpresa> pagos = service.obtenerTodos()
                .stream()
                .filter(p ->
                        p.getTipoPeriodicidad()
                                == TipoPeriodicidad.CONSUMOVARIOSDIAS)
                .toList();

        tabla.setPagos(pagos);
    }
}