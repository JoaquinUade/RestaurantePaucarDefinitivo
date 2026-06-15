package paucar.gastos;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastoVariableRequest;
import com.uade.tpo.demo.entity.GastosVariables;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;

public class GastosView extends VBox {

    private final GastosVariablesService service;
    private final CategoriasGastosService categoriasService;

    private final HBox contenedorCategorias = new HBox(20);

    public GastosView(GastosVariablesService service, CategoriasGastosService catService) {
        this.service = service;
        this.categoriasService = catService;

        Button btnAgregar = new Button("Agregar Gasto");

        btnAgregar.setOnAction(e -> {
            List<CategoriaGastoVariable> categorias = categoriasService.obtenerCategorias();
            GastoVariableRequest req = DialogGastos.mostrar(categorias);

            if (req != null) {
                service.crear(req);
                recargar();
            }
        });

        ScrollPane scroll = new ScrollPane(contenedorCategorias);
        scroll.setFitToHeight(true);

        setPadding(new Insets(20));
        setSpacing(20);

        getChildren().addAll(btnAgregar, scroll);

        recargar();
    }

    private void recargar() {

        contenedorCategorias.getChildren().clear();

        List<GastosVariables> gastos = service.obtenerTodos();

        Map<String, List<GastosVariables>> porCategoria =
                gastos.stream().collect(Collectors.groupingBy(g -> g.getCategoria().getNombre()));

        porCategoria.forEach((categoria, lista) -> {
            contenedorCategorias.getChildren().add(
                    new PanelCategoriaGastos(categoria, lista)
            );
        });
    }
}