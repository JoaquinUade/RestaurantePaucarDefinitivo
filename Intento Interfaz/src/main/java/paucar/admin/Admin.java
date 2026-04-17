package paucar.admin;

import com.uade.tpo.demo.entity.Producto;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.AdminService;

public class Admin extends BorderPane {

    private final TableView<Producto> tabla;
    private final AdminService adminService;

    // 🔹 Recibe el service desde Aplicacion
    public Admin(AdminService adminService) {
        this.adminService = adminService;

        Label titulo = new Label("Administración de Productos");

        tabla = new TableView<>();
        configurarTabla();
        cargarProductos();

        Button btnCrear = new Button("Crear");
        btnCrear.setOnAction(e -> abrirDialogCrear());

        Button btnEditar = new Button("Editar");
        btnEditar.setOnAction(e -> editarProductoSeleccionado());

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarProductoSeleccionado());

        HBox botones = new HBox(10, btnCrear, btnEditar, btnEliminar);
        botones.setPadding(new Insets(10));

        VBox contenedor = new VBox(20, titulo, tabla, botones);
        contenedor.setPadding(new Insets(20));

        setCenter(contenedor);
    }

    // ===== columnas =====
    private void configurarTabla() {

        TableColumn<Producto, String> colNombre
                = new TableColumn<>("Nombre");

        colNombre.setCellValueFactory(
                p -> new ReadOnlyStringWrapper(p.getValue().getNombre())
        );

        TableColumn<Producto, String> colPrecio
                = new TableColumn<>("Precio");

        colPrecio.setCellValueFactory(
                p -> new ReadOnlyStringWrapper(
                        String.format("$ %s", p.getValue().getPrecio())
                )
        );

        TableColumn<Producto, String> colCategoria
                = new TableColumn<>("Categoría");

        colCategoria.setCellValueFactory(
                p -> new ReadOnlyStringWrapper(
                        p.getValue().getCategoria().name()
                )
        );

        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colPrecio);
        tabla.getColumns().add(colCategoria);

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    }

    // ===== datos =====
    private void cargarProductos() {
        tabla.getItems().setAll(
                adminService.obtenerProductosAdmin()
        );
    }

    private void abrirDialogCrear() {

        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Crear producto");

        // Botones del dialog
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // Campos del formulario
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");

        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio");

        ComboBox<String> cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("OTROS","ENTRADA","BEBIDA", "MILANESAS", "WOKS",
                                      "SANDWICHES", "ENSALADAS", "FAJITAS", "PASTAS", "VINOS",
                                      "DESAYUNO", "GUARNICIONES");
                                      
        VBox form = new VBox(10,
                new Label("Nombre"), txtNombre,
                new Label("Precio"), txtPrecio,
                new Label("Categoría"), cmbCategoria
        );
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);

        // Convertir el resultado del dialog en Producto
        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                Producto p = new Producto();
                p.setNombre(txtNombre.getText());
                p.setPrecio(Double.valueOf(txtPrecio.getText()));
                p.setCategoria(
                        com.uade.tpo.demo.entity.Categoria.valueOf(
                                cmbCategoria.getValue()
                        )
                );
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(producto -> {
            adminService.crearProducto(producto);
            cargarProductos();
        });
    }

    private void editarProductoSeleccionado() {

        Producto seleccionado = tabla.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Seleccioná un producto para editar").showAndWait();
            return;
        }

        abrirDialogEditar(seleccionado);
    }

    private void abrirDialogEditar(Producto producto) {

        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Editar producto");

        ButtonType btnGuardar = new ButtonType("Guardar",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(btnGuardar, ButtonType.CANCEL);

        TextField txtNombre = new TextField(producto.getNombre());

        TextField txtPrecio = new TextField(
                String.valueOf(producto.getPrecio())
        );

        ComboBox<String> cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("COMIDA", "BEBIDA", "OTROS");
        cmbCategoria.setValue(producto.getCategoria().name());

        VBox form = new VBox(10,
                new Label("Nombre"), txtNombre,
                new Label("Precio"), txtPrecio,
                new Label("Categoría"), cmbCategoria
        );
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                producto.setNombre(txtNombre.getText());
                producto.setPrecio(Double.valueOf(txtPrecio.getText()));
                producto.setCategoria(
                        com.uade.tpo.demo.entity.Categoria.valueOf(
                                cmbCategoria.getValue()
                        )
                );
                return producto;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            adminService.editarProducto(p);
            cargarProductos();
        });
    }

    private void eliminarProductoSeleccionado() {

        Producto seleccionado = tabla.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Seleccioná un producto para eliminar").showAndWait();
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Seguro que querés eliminar el producto \""
                + seleccionado.getNombre() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                adminService.eliminarProducto(seleccionado.getIdProducto());
                cargarProductos();
            }
        });
    }
}
