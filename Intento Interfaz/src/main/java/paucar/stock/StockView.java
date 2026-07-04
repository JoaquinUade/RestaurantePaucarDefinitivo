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

    public StockView(StockService service,CategoriasGastosService categoriasService,
                     GastosVariablesService gastosVariablesService) {

        this.service = service;
        this.categoriasService = categoriasService;
        this.gastosVariablesService = gastosVariablesService;

        tablaStock = new TablaStock();
        Button btnAgregar = new Button("Agregar Producto");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");

        btnAgregar.setOnAction(e -> {

    List<CategoriaGastoVariable> categorias =
            categoriasService.obtenerCategorias();

    List<GastosVariables> gastos =
            gastosVariablesService.obtenerTodos();

    DialogStock.mostrar(
            categorias,
            gastos
    );

    recargar();
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

                service.editar(
                        stockSeleccionado.getIdStock(),
                        stockSeleccionado
                );

                recargar();
            }
        });
        btnEliminar.setOnAction(e -> {

            Stock stockSeleccionado
                    = tablaStock.getSeleccionado();

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

        VBox topBar = new VBox(10);

        topBar.setPadding(new Insets(10));
        ScrollPane scroll
                = new ScrollPane(contenedorCategorias);

        scroll.setFitToHeight(false);
        scroll.setFitToWidth(false);

        VBox contenido = new VBox(
                15,
                scroll,
                barraBotones
        );

        topBar.getChildren().add(btnAgregar);

        setTop(topBar);
        setCenter(contenido);

        recargar();
    }

    private void recargar() {

        contenedorCategorias.getChildren().clear();

        List<Stock> stocks = service.obtenerTodos();

        System.out.println("Stocks encontrados: " + stocks.size());

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
