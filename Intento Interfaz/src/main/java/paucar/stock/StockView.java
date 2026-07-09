package paucar.stock;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;        
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;

public class StockView extends BorderPane {

    private final TablaStock tablaStock;

    private final StockService service;
    private final CategoriasGastosService categoriasService;
    private Stock stockSeleccionado;
    private final GastosVariablesService gastosVariablesService;

    private final HBox contenedorCategorias
            = new HBox(20);

    public StockView(StockService service, CategoriasGastosService categoriasService,
            GastosVariablesService gastosVariablesService) {

        this.service = service;
        this.categoriasService = categoriasService;
        this.gastosVariablesService = gastosVariablesService;

        tablaStock = new TablaStock();
        Button btnAgregar = new Button("Agregar Producto");
        btnAgregar.getStyleClass().add("btn-agregar");
        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        btnAgregar.setOnAction(e -> {

            List<CategoriaGastoVariable> categorias
                    = categoriasService.obtenerCategorias();

            List<GastosVariables> gastos
                    = gastosVariablesService.obtenerTodos();

            StockRequest request
                    = DialogStock.mostrar(
                            categorias,
                            gastos
                    );

            if (request != null) {

                service.crear(request);

                recargar();
            }
        });
        btnEditar.setOnAction(e -> {

            if (stockSeleccionado == null) {
                return;
            }

            if (stockSeleccionado == null) {
                return;
            }

            List<CategoriaGastoVariable> categorias
                    = categoriasService.obtenerCategorias();

            StockRequest editado
                    = DialogStock.mostrarEditar(
                            categorias,
                            stockSeleccionado
                    );

            if (editado != null) {

                stockSeleccionado.setNombreProducto(
                        editado.getNombreProducto());

                stockSeleccionado.setStockMinimo(
                        editado.getStockMinimo());

                stockSeleccionado.setUnidadStockMinimo(
                        editado.getUnidadStockMinimo());

                service.editar(
                        stockSeleccionado.getIdStock(),
                        stockSeleccionado
                );

                recargar();
            }
        });
        btnEliminar.setOnAction(e -> {

            if (stockSeleccionado == null) {
                return;
            }

            boolean confirmado
                    = DialogStock.confirmarEliminacion();

            if (confirmado) {

                service.eliminar(
                        stockSeleccionado.getIdStock()
                );

                recargar();
            }
        });
        HBox barraBotones = new HBox(10, btnEditar, btnEliminar);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
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
 HBox topBar = new HBox(10, spacer, btnAgregar);

        topBar.setPadding(new Insets(10));
        fondo.getChildren().addAll(topBar, scroll, barraBotones);

        setCenter(fondo);

        recargar();
    }

    private void recargar() {

        contenedorCategorias.getChildren().clear();

        List<Stock> stocks = service.obtenerTodos();

        for (Stock s : stocks) {
            System.out.println(
                    s.getNombreProducto() + " - "
                    + s.getCategoriaGastoVariable().getNombre());
        }

        Map<String, List<Stock>> porCategoria
                = stocks.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getCategoriaGastoVariable()
                                        .getNombre()));

        System.out.println("Categorias: " + porCategoria.size());

        porCategoria.forEach((categoria, lista) -> {

            System.out.println("Categoria: " + categoria);

            contenedorCategorias.getChildren().add(
                    new PanelCategoriaStock(
                            categoria,
                            lista,
                            stock -> stockSeleccionado = stock));
        });
    }
}
