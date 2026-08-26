package paucar.pagos;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.pagos.pagosView.PagosSemanalesView;
import paucar.service.ClientesService;
import paucar.service.PagosService;
import paucar.service.VentasBackend;

public class PagosView extends BorderPane {

    private final PagosService service;
    private final ClientesService clientesService;
    private final VentasBackend ventasBackend;
    private final DatePicker filtroFecha;
    private final Label lblTotal = new Label();
    private PagoEmpresa seleccionado;

    private PagosPeriodicidadView vistaMensual;
    private PagosSemanalesView vistaSemanal;
    private PagosPeriodicidadView vistaQuincenal;
    private PagosPeriodicidadView vistaConsumo;

    private final ComboBox<String> comboPeriodicidad
            = new ComboBox<>();

    private final BorderPane contenedorResultado
            = new BorderPane();

    public PagosView(PagosService service, ClientesService clientesService, VentasBackend ventasBackend) {

        this.service = service;
        this.clientesService = clientesService;
        this.ventasBackend = ventasBackend;

        Label titulo = new Label("Pagos");
        titulo.getStyleClass().add("titulo-xl-blanco");

        filtroFecha = new DatePicker(LocalDate.now());
        filtroFecha.getStyleClass().add("date-agregar");
        filtroFecha.setOnAction(e -> recargar());

        lblTotal.getStyleClass().add("titulo-xl-blanco");

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");
        btnEditar.setOnAction(e -> editar());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");
        btnEliminar.setOnAction(e -> eliminar());

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.setOnAction(e -> aplicarFiltro());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        comboPeriodicidad.getItems().addAll(
                "Mensual",
                "Semanal",
                "Quincenal",
                "Consumo varios días");

        comboPeriodicidad.setValue("Mensual");
        btnFiltrar.setOnAction(e -> aplicarFiltro());

        HBox topBar = new HBox(
                10,
                filtroFecha,
                comboPeriodicidad,
                btnFiltrar,
                titulo,
                spacer
        );
        topBar.setPadding(new Insets(10));
        HBox bottomBar = new HBox(10, btnEditar, btnEliminar, spacer);
        bottomBar.setPadding(new Insets(10));

        VBox fondo = new VBox(
                15,
                topBar,
                contenedorResultado,
                bottomBar);

        fondo.getStyleClass().add("fondo-rojo");
        fondo.setPadding(new Insets(5));
        setCenter(fondo);

        aplicarFiltro();
    }

    private void editar() {

        if (seleccionado == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione un pago")
                    .showAndWait();
            return;
        }

        List<String> empresas = clientesService
                .obtenerNombresPorTipo(TipoCliente.EMPRESA);

        PagoEmpresa nuevo = DialogPagos.mostrarEditar(
                empresas,
                clientesService,
                ventasBackend,
                seleccionado);

        if (nuevo != null) {
            service.modificar(seleccionado.getId(), nuevo);
            recargar();
        }
    }

    private void eliminar() {

        switch (comboPeriodicidad.getValue()) {

            case "Mensual" ->
                seleccionado = vistaMensual.getSeleccionado();

            case "Quincenal" ->
                seleccionado = vistaQuincenal.getSeleccionado();

            case "Consumo varios días" ->
                seleccionado = vistaConsumo.getSeleccionado();

            case "Semanal" ->
                seleccionado = vistaSemanal.getSeleccionado();
        }

        if (seleccionado == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Seleccione un pago")
                    .showAndWait();
            return;
        }

        System.out.println("Pago seleccionado: " + seleccionado.getId());

        if (DialogPagos.confirmarLimpiarDatos()) {

            seleccionado.setCuit(null);
            seleccionado.setFactura(null);
            seleccionado.setObservacion(null);
            seleccionado.setNumeroPago(null);
            
            System.out.println("CUIT = " + seleccionado.getCuit());
            System.out.println("FACTURA = " + seleccionado.getFactura());
            System.out.println("OBS = " + seleccionado.getObservacion());
            System.out.println("NUMERO = " + seleccionado.getNumeroPago());

            service.modificar(seleccionado.getId(), seleccionado);

            recargar();
        }
    }

    private void aplicarFiltro() {

        switch (comboPeriodicidad.getValue()) {

            case "Mensual" -> {

                if (vistaMensual == null) {
                    vistaMensual = new PagosPeriodicidadView(
                            service,
                            clientesService,
                            TipoPeriodicidad.MENSUAL);
                }

                vistaMensual.actualizarFecha(filtroFecha.getValue());

                contenedorResultado.setCenter(vistaMensual);
            }

            case "Semanal" -> {

                if (vistaSemanal == null) {

                    vistaSemanal = new PagosSemanalesView(service);

                }

                vistaSemanal.actualizarFecha(filtroFecha.getValue());

                contenedorResultado.setCenter(vistaSemanal);
            }

            case "Quincenal" -> {

                if (vistaQuincenal == null) {
                    vistaQuincenal = new PagosPeriodicidadView(
                            service,
                            clientesService,
                            TipoPeriodicidad.QUINCENAL);
                }

                vistaQuincenal.actualizarFecha(filtroFecha.getValue());

                contenedorResultado.setCenter(vistaQuincenal);
            }

            case "Consumo varios días" -> {

                if (vistaConsumo == null) {
                    vistaConsumo = new PagosPeriodicidadView(
                            service,
                            clientesService,
                            TipoPeriodicidad.CONSUMOVARIOSDIAS);
                }

                vistaConsumo.actualizarFecha(filtroFecha.getValue());

                contenedorResultado.setCenter(vistaConsumo);
            }
        }
    }

    private void recargar() {
        aplicarFiltro();
    }

}
