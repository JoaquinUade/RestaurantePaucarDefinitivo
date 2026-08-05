package paucar.stock;

import java.math.BigDecimal;
import java.util.List;

import com.uade.tpo.demo.entity.GastosVariables;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DialogSumarStock {

    private static GastosVariables ultimoGastoSeleccionado;

    public static BigDecimal mostrar(
            Long idCategoria,
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

        TableView<GastosVariables> tabla
                = new TableView<>();
        List<GastosVariables> filtrados = gastos.stream()
                .filter(g
                        -> g.getCategoria() != null
                && g.getCategoria()
                        .getIdCategoria()
                        .equals(idCategoria)
                && !Boolean.TRUE.equals(
                        g.getCargadoEnStock()))
                .toList();

        tabla.setItems(
                FXCollections.observableArrayList(
                        filtrados));
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        final GastosVariables[] gastoSeleccionado
                = new GastosVariables[1];

        TextField txtCantidad = new TextField();
        txtCantidad.setText("0");
        txtCantidad.setPromptText(
                "Cantidad a ingresar al stock");

        TableColumn<GastosVariables, String> colProducto
                = new TableColumn<>("Producto");
        colProducto.setPrefWidth(120);
        colProducto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getProducto()));
        colProducto.setCellFactory(tc -> new TableCell<>() {

            private final javafx.scene.text.Text text
                    = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty().bind(
                        tc.widthProperty().subtract(10));

                setGraphic(text);
            }

            @Override
            protected void updateItem(
                    String item,
                    boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    text.setText(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
        TableColumn<GastosVariables, String> colCantidad
                = new TableColumn<>("Cantidad");
        colCantidad.setPrefWidth(120);
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
        colMedida.setPrefWidth(120);
        colProducto.setSortable(false);
        colCantidad.setSortable(false);
        colMedida.setSortable(false); 
        tabla.getColumns().add(colProducto);
        tabla.getColumns().add(colCantidad);
        tabla.getColumns().add(colMedida);
        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    gastoSeleccionado[0] = newValue;
                });

        dialog.getDialogPane().setContent(
                new VBox(
                        10,
                        new Label("Compras disponibles"),
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
