package paucar.gastos.Variables;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastoVariableRequest;
import com.uade.tpo.demo.entity.GastosVariables;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;

public class GastosVariablesView extends VBox {

    private final GastosVariablesService service;
    private final CategoriasGastosService categoriasService;
    private GastosVariables gastoSeleccionado;
    private DatePicker filtroFecha;

    private final HBox contenedorCategorias = new HBox(20);

    public GastosVariablesView(GastosVariablesService service, CategoriasGastosService catService) {
        this.service = service;
        this.categoriasService = catService;

        DatePicker filtroFecha = new DatePicker(LocalDate.now());
        filtroFecha.getStyleClass().add("date-agregar");
        filtroFecha.setPromptText("Filtrar por mes");

        Button btnAgregar = new Button("+ Agregar Gasto");
        btnAgregar.getStyleClass().add("btn-agregar");
        btnAgregar.setOnAction(e -> {
            List<CategoriaGastoVariable> categorias = categoriasService.obtenerCategorias();

            System.out.println("Categorias cargadas:");
            categorias.forEach(c -> System.out.println(c.getNombre()));

            GastoVariableRequest req = DialogGastosV.mostrar(categorias);

            if (req != null) {
                service.crear(req);
                recargar(filtroFecha.getValue());
            }
        });

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.getStyleClass().add("btn-filtrar");
        btnFiltrar.setOnAction(e -> {
            recargar(filtroFecha.getValue());
        });

        HBox barraBotones = crearBarraBotones();
        barraBotones.setPadding(new Insets(0));
        contenedorCategorias.setPadding(new Insets(15));

        ScrollPane scroll = new ScrollPane(contenedorCategorias);
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

        HBox filaSuperior = new HBox(10, filtroFecha, btnFiltrar, spacer, btnAgregar);

        fondo.getChildren().addAll(filaSuperior, scroll, barraBotones);

        getChildren().add(fondo);

        recargar(filtroFecha.getValue());
    }

    private HBox crearBarraBotones() {

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        // LOGICA EDITAR
        btnEditar.setOnAction(e -> {
            if (gastoSeleccionado == null) {
                System.out.println("Seleccione un gasto");
                return;
            }

            List<CategoriaGastoVariable> categorias = categoriasService.obtenerCategorias();

            GastoVariableRequest original = new GastoVariableRequest();
            original.setFecha(gastoSeleccionado.getFecha());
            original.setCategoriaId(gastoSeleccionado.getCategoria().getIdCategoria());
            original.setProducto(gastoSeleccionado.getProducto());
            original.setCantidad(gastoSeleccionado.getCantidad());
            original.setMedida(gastoSeleccionado.getMedida());
            original.setMonto(gastoSeleccionado.getMonto());

            GastoVariableRequest editado = DialogGastosV.mostrarEditar(categorias, original);

            if (editado != null) {
                service.editar(gastoSeleccionado.getIdGastoVariable(), editado);
                recargar(filtroFecha.getValue());
            }
        });

        // LOGICA ELIMINAR
        btnEliminar.setOnAction(e -> {
            if (gastoSeleccionado == null) {
                System.out.println("Seleccione un gasto");
                return;
            }

            boolean confirmado = DialogGastosV.confirmarEliminacion();

            if (confirmado) {
                service.eliminar(gastoSeleccionado.getIdGastoVariable());
                recargar(filtroFecha.getValue());
            }
        });

        return new HBox(10, btnEditar, btnEliminar);
    }

    private void recargar(LocalDate fechaFiltro) {

        contenedorCategorias.getChildren().clear();

        List<GastosVariables> gastos = service.obtenerTodos();

        if (fechaFiltro != null) {
            gastos = gastos.stream()
                    .filter(g -> g.getFecha().getMonth() == fechaFiltro.getMonth()
                            && g.getFecha().getYear() == fechaFiltro.getYear())
                    .toList();
        }

        Map<String, List<GastosVariables>> porCategoria = gastos.stream()
                .collect(Collectors.groupingBy(g -> g.getCategoria().getNombre()));

        porCategoria.forEach((categoria, lista) -> {
            contenedorCategorias.getChildren().add(

                    new PanelCategoriaGastosV(categoria, lista, gasto -> {
                        gastoSeleccionado = gasto;
                    }));
        });
    }
}