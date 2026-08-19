package paucar.stock.aumentoydisminucion;

import java.math.BigDecimal;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DialogRestarStock {

    public static BigDecimal mostrar(String producto,
            BigDecimal stockActual) {

        Dialog<BigDecimal> dialog = new Dialog<>();

        dialog.setTitle("Consumir stock");

        ButtonType btnAceptar
                = new ButtonType(
                        "Aceptar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnAceptar,
                        ButtonType.CANCEL);

        Label lblProducto
                = new Label("Producto: " + producto);

        Label lblStock
                = new Label(
                        "Stock actual: "
                        + stockActual.stripTrailingZeros()
                                .toPlainString());

        TextField txtCantidad
                = new TextField();

        txtCantidad.setPromptText(
                "Cantidad consumida");

        dialog.getDialogPane().setContent(
                new VBox(
                        10,
                        lblProducto,
                        lblStock,
                        new Label("Cantidad a descontar"),
                        txtCantidad));

        dialog.setResultConverter(btn -> {

            if (btn == btnAceptar) {

                try {

                    BigDecimal cantidad
                            = new BigDecimal(
                                    txtCantidad.getText().trim());

                    if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {

                        Alert alert = new Alert(
                                Alert.AlertType.ERROR);

                        alert.setHeaderText(null);
                        alert.setContentText(
                                "La cantidad debe ser mayor a cero");

                        alert.showAndWait();

                        return null;
                    }

                    if (cantidad.compareTo(stockActual) > 0) {

                        Alert alert = new Alert(
                                Alert.AlertType.ERROR);

                        alert.setHeaderText(null);
                        alert.setContentText(
                                "No hay stock suficiente");

                        alert.showAndWait();

                        return null;
                    }

                    return cantidad;

                } catch (Exception e) {

                    Alert alert = new Alert(
                            Alert.AlertType.ERROR);

                    alert.setHeaderText(null);
                    alert.setContentText(
                            "Ingrese un número válido");

                    alert.showAndWait();

                    return null;
                }
            }

            return null;
        });

        return dialog.showAndWait()
                .orElse(null);
    }
}
