package paucar.gastos.Fijos;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.Empleado;
import com.uade.tpo.demo.entity.GastosFijos;
import com.uade.tpo.demo.entity.dto.GastoFijoRequest;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.service.EmpleadoService;
import paucar.service.GastosFijosService;

public class GastosFijosView extends VBox {

    private final GastosFijosService service;
    private final EmpleadoService empleadoService;
    private final VBox contenedor = new VBox(15);
    private final DatePicker filtroFecha;
    private GastosFijos gastoSeleccionado;

    public GastosFijosView(GastosFijosService service, EmpleadoService empleadoService) {

        this.service = service;
        this.empleadoService = empleadoService;

        // ✅ FILTRO POR MES
        filtroFecha = new DatePicker(LocalDate.now());
        filtroFecha.setPromptText("Filtrar por mes");
        filtroFecha.getStyleClass().add("date-agregar");

        // ✅ BOTÓN FILTRAR
        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.getStyleClass().add("btn-filtrar");

        btnFiltrar.setOnAction(e -> {
            recargar(filtroFecha.getValue());
        });

        // ✅ BOTÓN AGREGAR (luego lo conectamos)
        Button btnAgregar = new Button("+ Agregar");
        btnAgregar.getStyleClass().add("btn-agregar");

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");

        btnAgregar.setOnAction(e -> {

            List<Empleado> empleados = this.empleadoService.obtenerTodosLosEmpleados();

            System.out.println("Empleados: " + empleados.size());

            GastoFijoRequest req = DialogGastosFijos.mostrar(empleados);

            if (req != null) {
                service.crear(req);
                recargar(filtroFecha.getValue());
            }
        });
        btnEditar.setOnAction(e -> {

            if (gastoSeleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un gasto").showAndWait();
                return;
            }

            GastoFijoRequest original = new GastoFijoRequest();
            original.setFecha(gastoSeleccionado.getFecha());
            original.setDetalle(gastoSeleccionado.getDetalle());
            original.setMonto(gastoSeleccionado.getMonto());
            original.setObservacion(gastoSeleccionado.getObservacion());
            original.setEstado(gastoSeleccionado.getEstado());
            original.setEsPersonal(gastoSeleccionado.getEsPersonal());

            GastoFijoRequest editado
                    = DialogGastosFijos.mostrarEditar(original);

            if (editado != null) {
                service.editar(
                        gastoSeleccionado.getIdGastoFijo(), // ✅ ESTE ES EL CORRECTO
                        editado
                );

                recargar(filtroFecha.getValue());
            }

            if (editado != null) {
                service.editar(
                        gastoSeleccionado.getIdGastoFijo(),
                        editado
                );
                recargar(filtroFecha.getValue());
            }

        });
        // ✅ ESPACIADOR
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label titulo = new Label("Gastos Fijos");
        titulo.getStyleClass().add("subtitulo-mid-blanco");
        // ✅ FILA SUPERIOR
        HBox filaSuperior = new HBox(10, filtroFecha, btnFiltrar, titulo, spacer, btnAgregar);
        HBox barraBotones = crearBarraBotones();
        barraBotones.setPadding(new Insets(0));

        // ✅ CONTENEDOR
        contenedor.setPadding(new Insets(15));
        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.setFitToWidth(true);
        scroll.setMinHeight(537);

        // ✅ FONDO (igual que tu otra vista)
        VBox fondo = new VBox(15);
        fondo.getStyleClass().add("fondo-rojo");
        fondo.setPadding(new Insets(15));

        fondo.getChildren().addAll(filaSuperior, scroll, barraBotones);

        getChildren().add(fondo);

        // ✅ CARGA INICIAL
        recargar(filtroFecha.getValue());
    }

    private void recargar(LocalDate fechaFiltro) {

        contenedor.getChildren().clear();

        List<GastosFijos> gastos = service.obtenerTodos();

        // ✅ FILTRAR POR MES
        if (fechaFiltro != null) {
            gastos = gastos.stream()
                    .filter(g
                            -> g.getFecha().getMonth() == fechaFiltro.getMonth()
                    && g.getFecha().getYear() == fechaFiltro.getYear()
                    )
                    .toList();
        }
// ✅ separar listas
        List<GastosFijos> personales = gastos.stream()
                .filter(g -> Boolean.TRUE.equals(g.getEsPersonal()))
                .toList();

        List<GastosFijos> generales = gastos.stream()
                .filter(g -> !Boolean.TRUE.equals(g.getEsPersonal()))
                .toList();

        TablaMensualFijos tablaGenerales
                = new TablaMensualFijos(generales, g -> {
                    gastoSeleccionado = g;
                }, false);

        TablaMensualFijos tablaPersonal
                = new TablaMensualFijos(personales, g -> {
                    gastoSeleccionado = g;
                }, true);
Label labelGastosFijos = new Label();
labelGastosFijos.getStyleClass().add("card-header");
labelGastosFijos.setText("Gastos fijos");
labelGastosFijos.setMaxWidth(Double.MAX_VALUE);

Label labelPagosPersonal = new Label();
labelPagosPersonal.getStyleClass().add("card-header");
labelPagosPersonal.setText("Pagos al personal");
labelPagosPersonal.setMaxWidth(Double.MAX_VALUE);

VBox bloqueGenerales = new VBox(0, labelGastosFijos, tablaGenerales);
VBox bloquePersonal = new VBox(0, labelPagosPersonal, tablaPersonal);
// ✅ contenedor horizontal
        HBox fila = new HBox(20, bloquePersonal, bloqueGenerales);

// ✅ ESTO ES LA CLAVE
        HBox.setHgrow(bloquePersonal, Priority.ALWAYS);
        HBox.setHgrow(bloqueGenerales, Priority.ALWAYS);

// ✅ que las tablas también se expandan
        tablaPersonal.setMaxWidth(Double.MAX_VALUE);
        tablaGenerales.setMaxWidth(Double.MAX_VALUE);

        contenedor.getChildren().add(fila);
    }

    private HBox crearBarraBotones() {

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        // ✅ EDITAR
        btnEditar.setOnAction(e -> {

            if (gastoSeleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un gasto").showAndWait();
                return;
            }

            GastoFijoRequest original = new GastoFijoRequest();
            original.setFecha(gastoSeleccionado.getFecha());
            original.setDetalle(gastoSeleccionado.getDetalle());
            original.setMonto(gastoSeleccionado.getMonto());
            original.setObservacion(gastoSeleccionado.getObservacion());
            original.setEstado(gastoSeleccionado.getEstado());
            original.setEsPersonal(gastoSeleccionado.getEsPersonal());

            GastoFijoRequest editado
                    = DialogGastosFijos.mostrarEditar(original);

            if (editado != null) {
                service.editar(
                        gastoSeleccionado.getIdGastoFijo(),
                        editado
                );
                recargar(filtroFecha.getValue());
            }
        });

        // ✅ ELIMINAR
        btnEliminar.setOnAction(e -> {

            if (gastoSeleccionado == null) {
                return;
            }

            boolean confirmado
                    = DialogGastosFijos.confirmarEliminacion();

            if (confirmado) {
                service.eliminar(
                        gastoSeleccionado.getIdGastoFijo()
                );
                recargar(filtroFecha.getValue());
            }
        });

        return new HBox(10, btnEditar, btnEliminar);
    }
}
