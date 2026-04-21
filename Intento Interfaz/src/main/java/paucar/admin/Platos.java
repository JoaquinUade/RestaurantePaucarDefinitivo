package paucar.admin;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.uade.tpo.demo.entity.Categoria;
import com.uade.tpo.demo.entity.Producto;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import paucar.service.AdminService;

public class Platos extends BorderPane {

    private final AdminService adminService;
    private final List<TableView<Producto>> tablas = new ArrayList<>();
    private GridPane filaSeleccionada;
    Locale localeAR = Locale.forLanguageTag("es-AR");
    private final NumberFormat formatoAR = NumberFormat.getCurrencyInstance(localeAR);

    private final ObjectProperty<Producto> productoSeleccionado = new SimpleObjectProperty<>();

    public Platos(AdminService adminService) {
        this.adminService = adminService;
        getStyleClass().add("platos-root");
        Label titulo = new Label("Administración de Productos");
        titulo.getStyleClass().add("administracion-de-productos");
        GridPane grid = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.33);
        col3.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2, col3);

        grid.setHgap(20); // espacio entre columnas
        grid.setVgap(20); // espacio entre filas
        grid.setPadding(new Insets(10));

        Categoria[] categorias = {
                Categoria.DESAYUNO,
                Categoria.ENTRADA,
                Categoria.BEBIDA,
                Categoria.POSTRES,
                Categoria.VINOS,
                Categoria.MILANESAS,
                Categoria.ENSALADAS,
                Categoria.PASTAS,
                Categoria.WOKS,
                Categoria.CARNE,
                Categoria.FAJITAS,
                Categoria.SANDWICHES,
                Categoria.GUARNICIONES,
                Categoria.OTROS
        };

        String[] nombres = {
                "Desayunos", "Entradas", "Bebidas", "Postres", "Vinos",
                "Milanesas", "Ensaladas", "Pastas", "Woks", "Carnes",
                "Fajitas", "Sandwiches", "Guarniciones", "Otros"
        };

        int columnas = 3;

        for (int i = 0; i < categorias.length; i++) {

            int columna = i % columnas; // 0, 1, 2
            int fila = i / columnas; // 0, 0, 0, 1, 1, 1...

            grid.add(
                    crearTablaCategoria(nombres[i], categorias[i]),
                    columna,
                    fila);
        }
        ScrollPane scroll = new ScrollPane(grid);
        scroll.getStyleClass().add("platos-scroll");
        scroll.setFitToWidth(true);
        scroll.setPannable(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Button btnCrear = new Button("Crear");
        btnCrear.setOnAction(e -> abrirDialogCrear());

        Button btnEditar = new Button("Editar");
        btnEditar.setOnAction(e -> {

            Producto seleccionado = productoSeleccionado.get();

            if (seleccionado == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Seleccioná un producto para editar").showAndWait();
                return;
            }

            abrirDialogEditar(seleccionado);
             refrescarTodasLasTablas();
        });

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> {

            Producto seleccionado = productoSeleccionado.get();

            if (seleccionado == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Seleccioná un producto para eliminar").showAndWait();
                return;
            }
            Long idProducto = seleccionado.getIdProducto();
            adminService.eliminarProducto(idProducto);
            refrescarTodasLasTablas();
        });

        HBox botones = new HBox(10, btnCrear, btnEditar, btnEliminar);
        botones.setPadding(new Insets(10));

        VBox contenedor = new VBox(20, titulo, scroll, botones);
        contenedor.setPadding(new Insets(20));

        setCenter(contenedor);
    }

    private VBox crearTablaCategoria(String titulo, Categoria categoria) {

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lblTitulo.setMaxWidth(Double.MAX_VALUE);
        lblTitulo.setAlignment(Pos.CENTER);

        VBox listaProductos = new VBox(4);
        listaProductos.setPadding(new Insets(5));

        adminService.obtenerProductosAdmin()
                .stream()
                .filter(p -> p.getCategoria() == categoria)
                .forEach(p -> {

                    GridPane fila = crearFilaProducto(p);

                    fila.setOnMouseClicked(e -> {

                        if (filaSeleccionada != null) {
                            filaSeleccionada.setStyle("""
                                        -fx-border-color: #dddddd;
                                        -fx-border-width: 0 0 1 0;
                                    """);
                        }

                        fila.setStyle("""
                                    -fx-background-color: #cce5ff;
                                    -fx-border-color: #88bfff;
                                    -fx-border-width: 0 0 1 0;
                                """);

                        filaSeleccionada = fila;
                        productoSeleccionado.set(p);
                    });
                    listaProductos.getChildren().add(fila);
                });

        VBox contenedor = new VBox(8, lblTitulo, listaProductos);
        contenedor.setPadding(new Insets(10));
        contenedor.setMaxWidth(Double.MAX_VALUE);
        contenedor.setFocusTraversable(false);
        contenedor.setStyle("""
                    -fx-background-color: white;
                    -fx-border-color: #cccccc;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """);
        return contenedor;
    }

    private GridPane crearFilaProducto(Producto p) {

        Label lblNombre = new Label(p.getNombre());
        lblNombre.setWrapText(true);
        lblNombre.setMaxWidth(Double.MAX_VALUE);

        Label lblPrecio = new Label(formatoAR.format(p.getPrecio()));
        lblPrecio.setAlignment(Pos.CENTER_RIGHT);

        GridPane fila = new GridPane();
        fila.setHgap(10);
        fila.setPadding(new Insets(4, 2, 4, 2));

        ColumnConstraints colNombre = new ColumnConstraints();
        colNombre.setHgrow(Priority.ALWAYS);

        ColumnConstraints colPrecio = new ColumnConstraints();
        colPrecio.setMinWidth(90);
        colPrecio.setHgrow(Priority.NEVER);

        fila.getColumnConstraints().addAll(colNombre, colPrecio);

        fila.add(lblNombre, 0, 0);
        fila.add(lblPrecio, 1, 0);

        // ✅ ESTA ES LA CLAVE
        GridPane.setHalignment(lblPrecio, javafx.geometry.HPos.RIGHT);

        fila.setStyle("""
                    -fx-border-color: #dddddd;
                    -fx-border-width: 0 0 1 0;
                """);

        fila.setOnMouseClicked(e -> {
            productoSeleccionado.set(p);
            fila.setStyle("""
                        -fx-background-color: #cce5ff;
                        -fx-border-color: #88bfff;
                        -fx-border-width: 0 0 1 0;
                    """);
        });

        return fila;
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

        txtPrecio.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[0-9.,]*")) {
                return change;
            }
            return null;
        }));

        ComboBox<String> cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("OTROS", "ENTRADA", "BEBIDA", "MILANESAS", "WOKS",
                "SANDWICHES", "ENSALADAS", "FAJITAS", "PASTAS", "VINOS",
                "DESAYUNO", "GUARNICIONES", "CARNE", "POSTRES");

        VBox form = new VBox(10,
                new Label("Nombre"), txtNombre,
                new Label("Precio"), txtPrecio,
                new Label("Categoría"), cmbCategoria);
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                Producto p = new Producto();
                p.setNombre(txtNombre.getText());

                String textoPrecio = txtPrecio.getText()
                        .replace(".", "") // elimina separadores de miles
                        .replace(",", "."); // coma decimal → punto

                double precio;
                try {
                    precio = Double.parseDouble(textoPrecio);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Precio inválido").showAndWait();
                    return null;
                }

                p.setPrecio(precio);

                p.setCategoria(Categoria.valueOf(cmbCategoria.getValue()));
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            adminService.crearProducto(p);
            refrescarTodasLasTablas();
        });

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
    String.valueOf(producto.getPrecio()).replace(".", ",")
);
        ComboBox<String> cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("OTROS", "ENTRADA", "BEBIDA", "MILANESAS", "WOKS",
                "SANDWICHES", "ENSALADAS", "FAJITAS", "PASTAS",
                "VINOS", "DESAYUNO", "GUARNICIONES", "CARNE");
        cmbCategoria.setValue(producto.getCategoria().name());

        VBox form = new VBox(10,
                new Label("Nombre"), txtNombre,
                new Label("Precio"), txtPrecio,
                new Label("Categoría"), cmbCategoria);
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                producto.setNombre(txtNombre.getText());

                String textoPrecio = txtPrecio.getText()
                        .replace(".", "") // elimina separadores de miles
                        .replace(",", "."); // coma decimal → punto
                double precio;
                try {
                    precio = Double.parseDouble(textoPrecio);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Precio inválido").showAndWait();
                    return null;
                }
                producto.setPrecio(precio);

                producto.setCategoria(
                        com.uade.tpo.demo.entity.Categoria.valueOf(
                                cmbCategoria.getValue()));
                return producto;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(p -> {
            adminService.editarProducto(p);
            refrescarTodasLasTablas();
        });
    }

    private void refrescarTodasLasTablas() {
        for (TableView<Producto> tabla : tablas) {/* recorre todas las tablas */
            tabla.refresh();/* actualiza la tabla */
            tabla.getItems().clear();/* limpia los elementos de la tabla */
        }
        tablas.clear();/* limpia la lista de tablas */
    }

}