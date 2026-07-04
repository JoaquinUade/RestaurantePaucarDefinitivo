package paucar.stock;

import java.math.BigDecimal;
import java.util.List;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DialogStock {

    public static StockRequest mostrar(List<CategoriaGastoVariable> categorias,
                                       List<GastosVariables> gastos) {
        TableView<GastosVariables> tabla
                = new TableView<>();
        TableColumn<GastosVariables, String> colProducto
                = new TableColumn<>("Producto");

        colProducto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getProducto()));
        TableColumn<GastosVariables, String> colCantidad
                = new TableColumn<>("Cantidad");

        colCantidad.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getCantidad()
                                .stripTrailingZeros()
                                .toPlainString()
                        + " "
                        + c.getValue().getMedida()
                ));
        tabla.getColumns().add(colProducto);
        tabla.getColumns().add(colCantidad);

        tabla.setPrefHeight(250);
        Dialog<StockRequest> dialog = new Dialog<>();

        dialog.setTitle("Agregar Stock");

        ButtonType btnGuardar
                = new ButtonType(
                        "Guardar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(btnGuardar, ButtonType.CANCEL);

        ComboBox<CategoriaGastoVariable> comboCategoria
                = new ComboBox<>();

        comboCategoria.getItems().addAll(categorias);

        comboCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });

        comboCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });
comboCategoria.setOnAction(e -> {

    CategoriaGastoVariable categoria =
            comboCategoria.getValue();

    if (categoria == null) {
        return;
    }

    List<GastosVariables> filtrados =
            gastos.stream()
                    .filter(g ->
                            g.getCategoria()
                                    .getIdCategoria()
                                    .equals(
                                            categoria.getIdCategoria()))
                    .toList();

    tabla.getItems().setAll(filtrados);

    System.out.println(
            "Productos encontrados: "
                    + filtrados.size());
});
        TextField txtCantidad = new TextField();
        TextField txtStockMinimo = new TextField();

        VBox form = new VBox(
                10,
                new Label("Categoría"),
                comboCategoria,
                new Label("Producto"),
                tabla,
                new Label("Cantidad"),
                txtCantidad,
                new Label("Stock mínimo"),
                txtStockMinimo
        );

        form.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {

            if (btn == btnGuardar) {

                if (comboCategoria.getValue() == null) {

                    new Alert(
                            Alert.AlertType.WARNING,
                            "Seleccione una categoría"
                    ).showAndWait();

                    return null;
                }

                GastosVariables seleccionado
                        = tabla.getSelectionModel()
                                .getSelectedItem();

                if (seleccionado == null) {
                    return null;
                }

                StockRequest request
                        = new StockRequest();

                request.setCategoriaId(
                        comboCategoria
                                .getValue()
                                .getIdCategoria()
                );

                request.setNombreProducto(
                        seleccionado.getProducto()
                );

                try {

                    String texto
                            = txtCantidad.getText().trim();

                    java.util.regex.Pattern pattern
                            = java.util.regex.Pattern.compile(
                                    "(\\d+(?:\\.\\d+)?)(.*)"
                            );

                    java.util.regex.Matcher matcher
                            = pattern.matcher(texto);

                    if (matcher.matches()) {

                        request.setCantidad(
                                new BigDecimal(
                                        matcher.group(1)
                                )
                        );

                        request.setUnidadMedida(
                                matcher.group(2).trim()
                        );

                    }

                } catch (Exception e) {

                    request.setCantidad(BigDecimal.ZERO);
                }

                try {

                    request.setStockMinimo(
                            new BigDecimal(
                                    txtStockMinimo.getText()
                            )
                    );

                } catch (Exception e) {

                    request.setStockMinimo(
                            BigDecimal.ZERO
                    );
                }

                return request;
            }

            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public static StockRequest mostrarEditar(
            List<CategoriaGastoVariable> categorias,
            Stock original) {

        Dialog<StockRequest> dialog = new Dialog<>();

        dialog.setTitle("Editar Stock");

        ButtonType btnGuardar
                = new ButtonType(
                        "Guardar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(btnGuardar, ButtonType.CANCEL);

        ComboBox<CategoriaGastoVariable> comboCategoria
                = new ComboBox<>();

        comboCategoria.getItems().addAll(categorias);

        comboCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });
        comboCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });

        categorias.stream()
                .filter(c
                        -> c.getIdCategoria().equals(
                        original.getCategoriaGastoVariable()
                                .getIdCategoria()))
                .findFirst()
                .ifPresent(comboCategoria::setValue);

        TextField txtProducto
                = new TextField(
                        original.getNombreProducto());

        TextField txtCantidad
                = new TextField(
                        original.getCantidad()
                                .stripTrailingZeros()
                                .toPlainString()
                        + " "
                        + original.getUnidadMedida());

        TextField txtStockMinimo
                = new TextField(
                        original.getStockMinimo()
                                .stripTrailingZeros()
                                .toPlainString());

        VBox form = new VBox(
                10,
                new Label("Categoría"),
                comboCategoria,
                new Label("Producto"),
                txtProducto,
                new Label("Cantidad"),
                txtCantidad,
                new Label("Stock mínimo"),
                txtStockMinimo
        );

        form.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {

            if (btn == btnGuardar) {

                StockRequest req
                        = new StockRequest();

                req.setCategoriaId(
                        comboCategoria.getValue()
                                .getIdCategoria());

                req.setNombreProducto(
                        txtProducto.getText());

                try {

                    String texto
                            = txtCantidad.getText().trim();

                    java.util.regex.Pattern pattern
                            = java.util.regex.Pattern.compile(
                                    "(\\d+(?:\\.\\d+)?)(.*)");

                    java.util.regex.Matcher matcher
                            = pattern.matcher(texto);

                    if (matcher.matches()) {

                        req.setCantidad(
                                new BigDecimal(
                                        matcher.group(1)));

                        req.setUnidadMedida(
                                matcher.group(2).trim());
                    }

                } catch (Exception e) {

                    req.setCantidad(BigDecimal.ZERO);
                }

                try {

                    req.setStockMinimo(
                            new BigDecimal(
                                    txtStockMinimo.getText()));

                } catch (Exception e) {

                    req.setStockMinimo(
                            BigDecimal.ZERO);
                }

                return req;
            }

            return null;
        });

        return dialog.showAndWait().orElse(null);
    }
    private static final String PASSWORD = "1234";

    public static boolean confirmarEliminacion() {

        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Eliminar Producto");

        ButtonType btnEliminar
                = new ButtonType(
                        "Eliminar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnEliminar,
                        ButtonType.CANCEL);

        PasswordField txtPass
                = new PasswordField();

        VBox form = new VBox(
                10,
                new Label("Contraseña"),
                txtPass
        );

        form.setPadding(new Insets(10));

        dialog.getDialogPane()
                .setContent(form);

        final boolean[] confirmado
                = {false};

        dialog.setResultConverter(btn -> {

            if (btn == btnEliminar
                    && txtPass.getText()
                            .equals(PASSWORD)) {

                confirmado[0] = true;

            } else if (btn == btnEliminar) {

                new Alert(
                        Alert.AlertType.ERROR,
                        "Contraseña incorrecta")
                        .showAndWait();
            }

            return null;
        });

        dialog.showAndWait();

        return confirmado[0];
    }
}
