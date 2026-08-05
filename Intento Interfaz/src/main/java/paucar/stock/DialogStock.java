package paucar.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.GastosVariablesService;

public class DialogStock {

    public static StockRequest mostrar(List<CategoriaGastoVariable> categorias,
            List<GastosVariables> gastos, List<Stock> stocks) {
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

        TextField txtProducto = new TextField();
        TextField txtStockMinimo = new TextField();
        TextField txtUnidad = new TextField();

        txtUnidad.setPromptText(
                "kg, unidad, caja, cajón..."
        );
        DatePicker dateFecha
                = new DatePicker(LocalDate.now());
        VBox form = new VBox(
                10,
                new Label("Fecha"),
                dateFecha,
                new Label("Categoría"),
                comboCategoria,
                new Label("Producto"),
                txtProducto,
                new Label("Stock mínimo"),
                txtStockMinimo,
                new Label("Unidad"),
                txtUnidad);

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
                if (txtProducto.getText().isBlank()) {

                    new Alert(
                            Alert.AlertType.WARNING,
                            "Ingrese un producto"
                    ).showAndWait();

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
                        txtProducto.getText().trim()
                );
                request.setGastoVariableId(null);

                request.setCantidad(BigDecimal.ZERO);

                request.setUnidadCantidad(txtUnidad.getText().trim());
                request.setFecha(
                        dateFecha.getValue());
                try {

                    request.setStockMinimo(
                            new BigDecimal(
                                    txtStockMinimo.getText().trim()
                            )
                    );

                } catch (Exception e) {

                    request.setStockMinimo(
                            BigDecimal.ZERO
                    );
                }
                System.out.println(
                        "FECHA: "
                        + request.getFecha());
                System.out.println("SE CREO EL REQUEST");
                System.out.println(request.getNombreProducto());
                System.out.println(request.getCantidad());
                return request;
            }

            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public static StockRequest mostrarEditar(List<CategoriaGastoVariable> categorias,
            Stock original, GastosVariablesService gastosService) {

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

        TextField txtStockMinimo
                = new TextField(
                        original.getStockMinimo()
                                .stripTrailingZeros()
                                .toPlainString());
        TextField txtUnidad
                = new TextField(
                        original.getUnidadCantidad());
        List<GastosVariables> historial
                = gastosService.obtenerPorStock(
                        original.getIdStock());
        TableView<GastosVariables> tablaHistorial
                = new TableView<>();
tablaHistorial.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaHistorial.setPrefHeight(200);
        TableColumn<GastosVariables, Void> colQuitar =
        new TableColumn<>("Quitar");

colQuitar.setPrefWidth(120);

colQuitar.setCellFactory(param ->
        new TableCell<>() {

            private final javafx.scene.control.Button btn =
                    new javafx.scene.control.Button("Quitar");

            {
                btn.setOnAction(event -> {

                    GastosVariables gasto =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    Alert alert = new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "¿Desea quitar ese gasto de el stock?"
                    );

                    alert.showAndWait().ifPresent(r -> {

    if (r == ButtonType.OK) {

        gastosService.desvincularStock(
                gasto.getIdGastoVariable()
        );

        getTableView()
                .getItems()
                .remove(gasto);
    }
});
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : btn);
            }
        });
        TableColumn<GastosVariables, String> colFecha
                = new TableColumn<>("Fecha");
colFecha.setPrefWidth(100);
        colFecha.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getFecha() != null
                        ? c.getValue().getFecha().toString()
                        : ""));
                        TableColumn<GastosVariables, String> colCategoria
        = new TableColumn<>("Categoría");

colCategoria.setPrefWidth(120);

colCategoria.setCellValueFactory(c ->
        new SimpleStringProperty(
                c.getValue().getCategoria() != null
                ? c.getValue()
                        .getCategoria()
                        .getNombre()
                : ""
        ));
        TableColumn<GastosVariables, String> colProductoHist
                = new TableColumn<>("Producto");
colProductoHist.setPrefWidth(100);
        colProductoHist.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getProducto()));
        TableColumn<GastosVariables, String> colCantidadHist
                = new TableColumn<>("Cantidad");
colCantidadHist.setPrefWidth(100);
        colCantidadHist.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getCantComprada()
                                .stripTrailingZeros()
                                .toPlainString()
                        + " "
                        + c.getValue().getMedida()));
        tablaHistorial.getItems().addAll(historial);
        tablaHistorial.getColumns().add(colQuitar);
        tablaHistorial.getColumns().add(colFecha);
        tablaHistorial.getColumns().add(colCategoria);
        tablaHistorial.getColumns().add(colProductoHist);
        tablaHistorial.getColumns().add(colCantidadHist);
       VBox datosBox = new VBox(
        10,
        new Label("Categoría"),
        comboCategoria,
        new Label("Producto"),
        txtProducto,
        new Label("Stock mínimo"),
        txtStockMinimo,
        new Label("Unidad"),
        txtUnidad
);

VBox historialBox = new VBox(
        10,
        new Label("Historial de cargas"),
        tablaHistorial
);

tablaHistorial.setPrefWidth(450);

HBox form = new HBox(
        20,
        datosBox,
        historialBox
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

                req.setCantidad(original.getCantidad());

                req.setUnidadCantidad(
                        txtUnidad.getText().trim()
                );

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
