package paucar.gastos.Individuales;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.Empleado;
import com.uade.tpo.demo.entity.GastosIndividuales;
import com.uade.tpo.demo.entity.dto.GastoIndividualRequest;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.config.Responsive;
import paucar.service.EmpleadoService;
import paucar.service.GastosIndividualesService;

public final class GastosIndividualesView extends VBox {

    private final GastosIndividualesService service;
    private GastosIndividuales gastoSeleccionado;
    private final ComboBox<String> comboMes;
    private final EmpleadoService empleadoService;

    private final HBox contenedor = new HBox(Responsive.pe(20));
    private final Label mensajeSinDatos = new Label(
            "No hay gastos individuales ingresados para mostrar en este período.");

    public GastosIndividualesView(GastosIndividualesService service,
            EmpleadoService empleadoService) {
        this.service = service;
        this.empleadoService = empleadoService;

        comboMes = new ComboBox<>();

        comboMes.getItems().addAll(
                "Enero",
                "Febrero",
                "Marzo",
                "Abril",
                "Mayo",
                "Junio",
                "Julio",
                "Agosto",
                "Septiembre",
                "Octubre",
                "Noviembre",
                "Diciembre"
        );

        comboMes.getSelectionModel().select(
                LocalDate.now().getMonthValue() - 1
        );

        comboMes.getStyleClass().add("combo-agregar");

        Button btnAgregar = new Button("+ Agregar Gasto");
        btnAgregar.getStyleClass().add("btn-agregar");
        btnAgregar.setOnAction(e -> {

            // ⚠️ esto depende de dónde saques empleados
            List<Empleado> empleados = empleadoService.obtenerTodosLosEmpleados();

            GastoIndividualRequest req
                    = DialogGastosIndividuales.mostrar(empleados);

            if (req != null) {
                service.crear(req);
                recargar();
            }
        });

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.getStyleClass().add("btn-filtrar");
        btnFiltrar.setOnAction(e -> recargar());

        HBox barraBotones = crearBarraBotones();
        barraBotones.setPadding(Responsive.insets(0));
        contenedor.setPadding(Responsive.insets(15));
        mensajeSinDatos.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");

        ScrollPane scroll = new ScrollPane(contenedor);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 🔥 claves
        scroll.setFitToWidth(false); // permite scroll horizontal
        scroll.setFitToHeight(false);

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // horizontal

        VBox fondo = new VBox();
        fondo.getStyleClass().add("fondo-rojo");
        fondo.setPadding(Responsive.insets(15));
        fondo.setSpacing(Responsive.pe(15));
        fondo.setFillWidth(true);
        VBox.setVgrow(fondo, Priority.ALWAYS);

        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label titulo = new Label("Gastos Individuales");
        titulo.getStyleClass().add("subtitulo-mid-blanco");
        HBox top = new HBox(Responsive.pe(10), comboMes,btnFiltrar, titulo, spacer, btnAgregar);

        fondo.getChildren().addAll(top, scroll, barraBotones);

        getChildren().add(fondo);

        recargar();
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
                recargar();
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
                recargar();
            }
        });

        return new HBox(Responsive.pe(10), btnEditar, btnEliminar);
    }

    private void recargar() {

        contenedor.getChildren().clear();

        List<GastosIndividuales> gastos = service.obtenerTodos();

        int mesSeleccionado
                = comboMes.getSelectionModel().getSelectedIndex() + 1;

        gastos = gastos.stream()
                .filter(g -> 
                    g.getFecha().getMonthValue() == mesSeleccionado).toList();
        if (gastos.isEmpty()) {
            contenedor.getChildren().add(mensajeSinDatos);
            return;
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
