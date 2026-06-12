package paucar.admin.categoriasgastos;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;

public class CategoriasGastosView extends BorderPane {

    private final CategoriasGastosService service;

    private final TablaCategoriasGastos panel;
    private final ListView<CategoriaGastoVariable> lista;

    public CategoriasGastosView(CategoriasGastosService service) {

        this.service = service;

        Label titulo = new Label("Categorías de Gastos Variables");
        titulo.getStyleClass().add("administracion-de-empresasclientes");

        panel = new TablaCategoriasGastos("Categorías");
        lista = panel.getLista();

        cargarDatos();

        HBox botones = crearBotones();

        VBox layout = new VBox(20, titulo, panel, botones);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("fondo-empresasclientes");

        setCenter(layout);
    }

    // ✅ BOTONES
    private HBox crearBotones() {

        Button btnCrear = new Button("Crear");
        Button btnEditar = new Button("Editar");
        Button btnEliminar = new Button("Eliminar");

        btnCrear.getStyleClass().add("btn-crear");
        btnEditar.getStyleClass().add("btn-editar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        // CREAR
        btnCrear.setOnAction(e -> {
            String nombre = DialogCategoriasGastos.abrirDialogCrear();

            if (nombre != null) {
                service.crearCategoria(new CategoriaGastoVariable(nombre));
                cargarDatos();
            }
        });

        // EDITAR
        btnEditar.setOnAction(e -> {
            var seleccionado = lista.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione una categoría").showAndWait();
                return;
            }

            String nuevo = DialogCategoriasGastos.abrirDialogEditar(seleccionado.getNombre());

            if (nuevo != null) {
                seleccionado.setNombre(nuevo);
                service.editarCategoria(seleccionado);
                cargarDatos();
            }
        });

        // ELIMINAR
        btnEliminar.setOnAction(e -> {
            var seleccionado = lista.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione una categoría").showAndWait();
                return;
            }

            if (DialogCategoriasGastos.confirmarEliminacion()) {
                service.eliminarCategoria(seleccionado.getIdCategoria());
                cargarDatos();
            }
        });

        return new HBox(10, btnCrear, btnEditar, btnEliminar);
    }

    // ✅ CARGAR DATOS
    private void cargarDatos() {
        lista.getItems().setAll(service.obtenerCategorias());
    }
}