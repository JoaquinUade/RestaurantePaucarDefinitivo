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

public class PagosView extends BorderPane {

    private final PagosService service;
    private final ClientesService clientesService;
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

    public PagosView(PagosService service, ClientesService clientesService) {

        this.service = service;
        this.clientesService = clientesService;

        Label titulo = new Label("Pagos");
        titulo.getStyleClass().add("titulo-xl-blanco");

        filtroFecha = new DatePicker(LocalDate.now());
        filtroFecha.getStyleClass().add("date-agregar");
        filtroFecha.setOnAction(e -> recargar());

        lblTotal.getStyleClass().add("titulo-xl-blanco");

        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.getStyleClass().add("btn-agregar");
        btnAgregar.setOnAction(e -> agregar());

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
                spacer,
                btnAgregar);
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

    private void agregar() {

        List<String> empresas = clientesService
                .obtenerNombresPorTipo(TipoCliente.EMPRESA);

        PagoEmpresa pago = DialogPagos.mostrar(empresas, clientesService);

        if (pago != null) {
            service.crear(pago);
            recargar();
        }
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
                empresas, clientesService, seleccionado);

        if (nuevo != null) {
            service.modificar(seleccionado.getId(), nuevo);
            recargar();
        }
    }

    private void eliminar() {

        if (seleccionado == null) {
            return;
        }

        if (DialogPagos.confirmarEliminacion()) {
            service.eliminar(seleccionado.getId());
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
