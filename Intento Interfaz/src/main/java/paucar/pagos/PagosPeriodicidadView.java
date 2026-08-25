package paucar.pagos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import paucar.service.ClientesService;
import paucar.service.PagosService;
import paucar.shared.FechaUtils;
import paucar.shared.MonedaUtils;

public class PagosPeriodicidadView extends BorderPane {

    private final PagosService service;
    private final ClientesService clientesService;
    private final TipoPeriodicidad periodicidad;

    private final TablaPagos tabla;
    private final Label lblTotal = new Label();

    private LocalDate fecha = LocalDate.now();

    public PagosPeriodicidadView(
            PagosService service,
            ClientesService clientesService,
            TipoPeriodicidad periodicidad) {

        this.service = service;
        this.clientesService = clientesService;
        this.periodicidad = periodicidad;

        tabla = new TablaPagos(
        null,
        this::recargar,
        service);

        VBox contenido = new VBox(10, tabla, lblTotal);
        contenido.setPadding(new Insets(10));

        VBox.setVgrow(tabla, Priority.ALWAYS);

        setCenter(contenido);

        recargar();
    }

    public void recargar() {

    List<PagoEmpresa> pagos = service.obtenerTodos()
            .stream()
            .filter(p -> p.getFecha() != null)
            .filter(p -> p.getFecha().getMonth() == fecha.getMonth())
            .filter(p -> p.getFecha().getYear() == fecha.getYear())
            .filter(p -> p.getTipoPeriodicidad() == periodicidad)
            .toList();

    tabla.setPagos(pagos);

    BigDecimal totalPagado = pagos.stream()
            .filter(p -> p.getEstado() != null
                    && p.getEstado().name().equals("PAGADO"))
            .map(p -> p.getMonto() == null
                    ? BigDecimal.ZERO
                    : p.getMonto())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalDebe = pagos.stream()
            .filter(p -> p.getEstado() != null
                    && p.getEstado().name().equals("DEBE"))
            .map(p -> p.getMonto() == null
                    ? BigDecimal.ZERO
                    : p.getMonto())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    lblTotal.setStyle(
            "-fx-text-fill: white;"
            + "-fx-font-size: 18px;"
            + "-fx-font-weight: bold;"
    );

    lblTotal.setText(
            "Mes de " + FechaUtils.mes(fecha)
            + "   |   Pagado: " + MonedaUtils.formatearMoneda(totalPagado)
            + "   |   Nos deben: " + MonedaUtils.formatearMoneda(totalDebe)
    );
}

    public void actualizarFecha(LocalDate fecha) {

        this.fecha = fecha;

        recargar();
    }

}
