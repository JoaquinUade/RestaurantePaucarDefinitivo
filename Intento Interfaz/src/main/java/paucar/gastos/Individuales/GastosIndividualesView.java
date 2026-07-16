package paucar.gastos.Individuales;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.Empleado;
import com.uade.tpo.demo.entity.GastosIndividuales;
import com.uade.tpo.demo.entity.dto.GastoIndividualRequest;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.service.EmpleadoService;
import paucar.service.GastosIndividualesService;

public class GastosIndividualesView extends VBox {

    private final GastosIndividualesService service;
    private GastosIndividuales gastoSeleccionado;
    private final DatePicker filtroFecha;
    private final EmpleadoService empleadoService;

    private final HBox contenedor = new HBox(20);

    public GastosIndividualesView(GastosIndividualesService service,
            EmpleadoService empleadoService) {
        this.service = service;
        this.empleadoService = empleadoService;

        filtroFecha = new DatePicker(LocalDate.now());
        filtroFecha.getStyleClass().add("date-agregar");
        filtroFecha.setPromptText("Filtrar por mes");

        Button btnAgregar = new Button("+ Agregar Gasto");
        btnAgregar.getStyleClass().add("btn-agregar");
        btnAgregar.setOnAction(e -> {

            // ⚠️ esto depende de dónde saques empleados
            List<Empleado> empleados = empleadoService.obtenerTodosLosEmpleados();

            GastoIndividualRequest req
                    = DialogGastosIndividuales.mostrar(empleados);

            if (req != null) {
                service.crear(req);
                recargar(filtroFecha.getValue());
            }
        });

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.getStyleClass().add("btn-filtrar");
        btnFiltrar.setOnAction(e -> recargar(filtroFecha.getValue()));

        HBox barraBotones = crearBarraBotones();
        barraBotones.setPadding(new Insets(0));
        contenedor.setPadding(new Insets(15));

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMinHeight(537);
        // 🔥 claves
        scroll.setFitToWidth(false); // permite scroll horizontal
        scroll.setFitToHeight(false);

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // horizontal

        VBox fondo = new VBox();
        fondo.getStyleClass().add("fondo-rojo");
        fondo.setPadding(new Insets(15));
        fondo.setSpacing(15);

        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label titulo = new Label("Gastos Individuales");
        titulo.getStyleClass().add("subtitulo-mid-blanco");
        HBox top = new HBox(10, filtroFecha, btnFiltrar,titulo, spacer, btnAgregar);

        fondo.getChildren().addAll(top, scroll, barraBotones);

        getChildren().add(fondo);

        recargar(filtroFecha.getValue());
    }

    private HBox crearBarraBotones() {

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        // ✅ EDITAR
        btnEditar.setOnAction(e -> {
            if (gastoSeleccionado == null) {
                return;
            }

            List<Empleado> empleados = empleadoService.obtenerTodosLosEmpleados();

            GastoIndividualRequest original = new GastoIndividualRequest();
            original.setFecha(gastoSeleccionado.getFecha());
            original.setDetalle(gastoSeleccionado.getDetalle());
            original.setMonto(gastoSeleccionado.getMonto());
            original.setEmpleadoId(
                    gastoSeleccionado.getEmpleado().getIdEmpleado()
            );

            GastoIndividualRequest editado
                    = DialogGastosIndividuales.mostrarEditar(empleados, original);

            if (editado != null) {
                service.editar(
                        gastoSeleccionado.getIdGastoIndividual(),
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
                    = DialogGastosIndividuales.confirmarEliminacion();

            if (confirmado) {
                service.eliminar(
                        gastoSeleccionado.getIdGastoIndividual()
                );
                recargar(filtroFecha.getValue());
            }
        });

        return new HBox(10, btnEditar, btnEliminar);
    }

    private void recargar(LocalDate fechaFiltro) {

        contenedor.getChildren().clear();

        List<GastosIndividuales> gastos = service.obtenerTodos();

        if (fechaFiltro != null) {
            gastos = gastos.stream()
                    .filter(g
                            -> g.getFecha().getMonth() == fechaFiltro.getMonth()
                    && g.getFecha().getYear() == fechaFiltro.getYear()
                    )
                    .toList();
        }
        Map<String, List<GastosIndividuales>> porEmpleado = gastos.stream()
                .collect(Collectors.groupingBy(g -> g.getEmpleado().getNombre()));
        // ✅ acá ya NO usamos categorías
        porEmpleado.forEach((empleado, lista) -> {
            contenedor.getChildren().add(
                    new PanelGastosIndividuales(empleado, lista, gasto -> {
                        gastoSeleccionado = gasto;
                    }));
        });
    }
}
