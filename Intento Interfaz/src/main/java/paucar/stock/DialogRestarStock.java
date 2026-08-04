package paucar.stock;

import java.math.BigDecimal;

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

        ButtonType btnAceptar =
                new ButtonType(
                        "Aceptar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnAceptar,
                        ButtonType.CANCEL);

        Label lblProducto =
                new Label("Producto: " + producto);

        Label lblStock =
                new Label(
                        "Stock actual: "
                        + stockActual.stripTrailingZeros()
                                .toPlainString());

        TextField txtCantidad =
                new TextField();

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
                    return new BigDecimal(
                            txtCantidad.getText().trim());
                } catch (Exception e) {
                    return null;
                }
            }

            return null;
        });

        return dialog.showAndWait()
                .orElse(null);
    }
}