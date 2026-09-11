package paucar.pagos.pagosView;

import java.util.List;
import java.time.LocalDate;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import paucar.config.Responsive;
import paucar.pagos.DialogPagos;
import paucar.pagos.TablaPagos;
import paucar.service.ClientesService;
import paucar.service.PagosService;

public class PagosQuincenalesView extends BorderPane {

    private final PagosService service;
    private LocalDate fecha = LocalDate.now();

    private final TablaPagos tablaQuincena1;
    private final TablaPagos tablaQuincena2;
    private final ClientesService clientesService;

    public PagosQuincenalesView(PagosService service, ClientesService clientesService) {

        this.service = service;
        this.clientesService = clientesService;

        tablaQuincena1 = new TablaPagos(
                pago -> {

                    List<String> empresas
                    = clientesService.obtenerNombresPagables();
                    PagoEmpresa nuevo
                    = DialogPagos.mostrarEditar(
                            empresas,
                            clientesService,
                            null,
                            pago);
                    if (nuevo != null) {
                        service.modificar(pago.getId(), nuevo);
                        recargar();
                    }
                },
                this::recargar,
                service);

        tablaQuincena2 = new TablaPagos(
                pago -> {

                    List<String> empresas
                    = clientesService.obtenerNombresPagables();

                    PagoEmpresa nuevo
                    = DialogPagos.mostrarEditar(
                            empresas,
                            clientesService,
                            null,
                            pago);
                    if (nuevo != null) {

                        service.modificar(pago.getId(), nuevo);
                        recargar();
                    }
                },
                this::recargar,
                service);

        Label lbl1 = new Label("Primera Quincena (1-15)");
        Label lbl2 = new Label("Segunda Quincena (16-fin)");

        lbl1.setStyle("-fx-text-fill: white;");
        lbl2.setStyle("-fx-text-fill: white;");

        VBox contenido = new VBox(Responsive.pe(15),
                lbl1,
                tablaQuincena1,
                lbl2,
                tablaQuincena2
        );

        contenido.setStyle("-fx-background-color: #94002C;");

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);

        setCenter(scroll);

        recargar();
    }

    public void actualizarFecha(LocalDate fecha) {
        this.fecha = fecha;
        recargar();
    }

    public void recargar() {

        List<PagoEmpresa> pagos = service.obtenerTodos()
                .stream()
                .filter(p -> p.getFecha() != null
                        && p.getFecha().getYear() == fecha.getYear()
                        && p.getFecha().getMonth() == fecha.getMonth())
                .filter(p -> p.getTipoPeriodicidad() == TipoPeriodicidad.QUINCENAL)
                .toList();

        List<PagoEmpresa> quincena1 = pagos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> p.getFecha().getDayOfMonth() <= 15)
                .toList();

        List<PagoEmpresa> quincena2 = pagos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> p.getFecha().getDayOfMonth() >= 16)
                .toList();

        tablaQuincena1.setPagos(quincena1);
        tablaQuincena2.setPagos(quincena2);
    }

    public PagoEmpresa getSeleccionado() {

        if (tablaQuincena1.getSeleccionado() != null) {
            return tablaQuincena1.getSeleccionado();
        }

        if (tablaQuincena2.getSeleccionado() != null) {
            return tablaQuincena2.getSeleccionado();
        }

        return null;
    }
}
