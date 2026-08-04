package paucar.stock;

import java.math.BigDecimal;
import java.util.List;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastosVariables;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DialogSumarStock {

    private static GastosVariables ultimoGastoSeleccionado;

    public static BigDecimal mostrar(
            List<CategoriaGastoVariable> categorias,
            List<GastosVariables> gastos) {

        Dialog<BigDecimal> dialog = new Dialog<>();

        dialog.setTitle("Seleccionar compra");

        ButtonType btnGuardar
                = new ButtonType(
                        "Aceptar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnGuardar,
                        ButtonType.CANCEL);
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

        TableView<GastosVariables> tabla
                = new TableView<>();
        final GastosVariables[] gastoSeleccionado
                = new GastosVariables[1];

        TextField txtCantidad = new TextField();
        txtCantidad.setText("0");
        txtCantidad.setPromptText(
                "Cantidad a ingresar al stock");

        TableColumn<GastosVariables, String> colProducto
                = new TableColumn<>("Producto");

        colProducto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getProducto()));

        TableColumn<GastosVariables, String> colCantidad
                = new TableColumn<>("Cantidad");

        colCantidad.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getCantComprada()
                                .stripTrailingZeros()
                                .toPlainString()));

        TableColumn<GastosVariables, String> colMedida
                = new TableColumn<>("Medida");

        colMedida.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getMedida()));

        tabla.getColumns().addAll(
                colProducto,
                colCantidad,
                colMedida);
        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    gastoSeleccionado[0] = newValue;
                });
        comboCategoria.setOnAction(e -> {

            CategoriaGastoVariable categoria
                    = comboCategoria.getValue();

            if (categoria == null) {
                return;
            }

            List<GastosVariables> filtrados = gastos.stream()
                    .filter(g
                            -> g.getCategoria() != null
                    && g.getCategoria()
                            .getIdCategoria()
                            .equals(
                                    categoria.getIdCategoria())
                    && !Boolean.TRUE.equals(
                            g.getCargadoEnStock()))
                    .toList();

            tabla.setItems(
                    FXCollections.observableArrayList(
                            filtrados));
        });
        dialog.getDialogPane().setContent(
                new VBox(
                        10,
                        new Label("Categoría"),
                        comboCategoria,
                        tabla,
                        new Label("Cantidad a ingresar"),
                        txtCantidad));

        dialog.setResultConverter(btn -> {

            if (btn == btnGuardar) {
                if (gastoSeleccionado[0] == null) {
                    return null;
                }

                ultimoGastoSeleccionado
                        = gastoSeleccionado[0];
                try {

                    return new BigDecimal(
                            txtCantidad.getText().trim()
                    );

                } catch (Exception e) {

                    return BigDecimal.ZERO;
                }
            }

            return null;
        });

        return dialog.showAndWait()
                .orElse(null);
    }

    public static GastosVariables getUltimoGastoSeleccionado() {
        return ultimoGastoSeleccionado;
    }
}
