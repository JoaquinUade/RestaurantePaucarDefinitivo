package paucar.pagos.pagosView;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import paucar.pagos.TablaPagos;
import paucar.service.PagosService;

public class PagosSemanalesView extends BorderPane {

    private final PagosService service;

    private final TablaPagos tablaSemana1;
    private final TablaPagos tablaSemana2;
    private final TablaPagos tablaSemana3;
    private final TablaPagos tablaSemana4;

    private LocalDate fecha = LocalDate.now();

    public PagosSemanalesView(PagosService service) {

        this.service = service;

        tablaSemana1 = new TablaPagos(
                null,
                this::recargar,
                service);

        tablaSemana2 = new TablaPagos(
                null,
                this::recargar,
                service);

        tablaSemana3 = new TablaPagos(
                null,
                this::recargar,
                service);

        tablaSemana4 = new TablaPagos(
                null,
                this::recargar,
                service);
        Label lbl1 = new Label("Semana 1 (1-7)");
        Label lbl2 = new Label("Semana 2 (8-14)");
        Label lbl3 = new Label("Semana 3 (15-21)");
        Label lbl4 = new Label("Semana 4 (22-fin)");
        lbl1.setStyle("-fx-text-fill: white;");
        lbl2.setStyle("-fx-text-fill: white;");
        lbl3.setStyle("-fx-text-fill: white;");
        lbl4.setStyle("-fx-text-fill: white;");
        VBox contenido = new VBox(
                15,
                lbl1,
                tablaSemana1,
                lbl2,
                tablaSemana2,
                lbl3,
                tablaSemana3,
                lbl4,
                tablaSemana4
        );

        ScrollPane scroll = new ScrollPane(contenido);
        contenido.setStyle(
                "-fx-background-color: #94002C;"
        );
        scroll.setFitToWidth(true);

        setCenter(scroll);

        recargar();
    }

    public void recargar() {

        List<PagoEmpresa> pagos = service.obtenerTodos()
                .stream()
                .filter(p
                        -> p.getTipoPeriodicidad()
                == TipoPeriodicidad.SEMANAL)
                .toList();

        List<PagoEmpresa> semana1 = pagos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> p.getFecha().getDayOfMonth() >= 1)
                .filter(p -> p.getFecha().getDayOfMonth() <= 7)
                .toList();

        List<PagoEmpresa> semana2 = pagos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> p.getFecha().getDayOfMonth() >= 8)
                .filter(p -> p.getFecha().getDayOfMonth() <= 14)
                .toList();

        List<PagoEmpresa> semana3 = pagos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> p.getFecha().getDayOfMonth() >= 15)
                .filter(p -> p.getFecha().getDayOfMonth() <= 21)
                .toList();

        List<PagoEmpresa> semana4 = pagos.stream()
                .filter(p -> p.getFecha() != null)
                .filter(p -> p.getFecha().getDayOfMonth() >= 22)
                .toList();

        tablaSemana1.setPagos(semana1);
        tablaSemana2.setPagos(semana2);
        tablaSemana3.setPagos(semana3);
        tablaSemana4.setPagos(semana4);
    }

    public void actualizarFecha(LocalDate fecha) {

        this.fecha = fecha;

        recargar();
    }
    public PagoEmpresa getSeleccionado() {

    if (tablaSemana1.getSeleccionado() != null) {
        return tablaSemana1.getSeleccionado();
    }

    if (tablaSemana2.getSeleccionado() != null) {
        return tablaSemana2.getSeleccionado();
    }

    if (tablaSemana3.getSeleccionado() != null) {
        return tablaSemana3.getSeleccionado();
    }

    if (tablaSemana4.getSeleccionado() != null) {
        return tablaSemana4.getSeleccionado();
    }

    return null;
}
}
