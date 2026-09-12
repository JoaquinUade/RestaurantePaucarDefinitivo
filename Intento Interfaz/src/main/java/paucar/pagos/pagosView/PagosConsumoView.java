package paucar.pagos.pagosView;
import java.util.List;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

import javafx.scene.layout.BorderPane;
import paucar.pagos.DialogPagos;
import paucar.pagos.TablaPagos;
import paucar.service.ClientesService;
import paucar.service.PagosService;

public final class PagosConsumoView extends BorderPane {

    private final PagosService service;
    private final TablaPagos tabla;
private final ClientesService clientesService;
    public PagosConsumoView(PagosService service, ClientesService clientesService) {

        this.service = service;
        this.clientesService = clientesService;

        tabla = new TablaPagos(
    pago -> {

        List<String> empresas =
                this.clientesService.obtenerNombresPagables();

        PagoEmpresa nuevo =
                DialogPagos.mostrarEditar(
                        empresas,
                        this.clientesService,
                        null,
                        pago);

        if (nuevo != null) {
            service.modificar(pago.getId(), nuevo);
            recargar();
        }
    },
    this::recargar,
    service
);


        setCenter(tabla);

        recargar();
    }

    public final void recargar() {

        List<PagoEmpresa> pagos = service.obtenerTodos()
                .stream()
                .filter(p ->
                        p.getTipoPeriodicidad()
                                == TipoPeriodicidad.CONSUMOVARIOSDIAS)
                .toList();

        tabla.setPagos(pagos);
    }
}