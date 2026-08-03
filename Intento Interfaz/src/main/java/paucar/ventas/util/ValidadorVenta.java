package paucar.ventas.util;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.ProductosService;

public final class ValidadorVenta {

    private ValidadorVenta() {
    }

    public static boolean lineaValida(
            HBox fila) {

        @SuppressWarnings("unchecked")
        ComboBox<ProductosService.ProductoItem> comboProductos
                = (ComboBox<ProductosService.ProductoItem>)
                fila.getChildren().get(0);

        TextField cant
                = (TextField)
                fila.getChildren().get(1);

        if (comboProductos.getValue() == null) {
            return false;
        }

        if (cant.getText() == null
                || cant.getText().isBlank()) {
            return false;
        }

        try {

            return Integer.parseInt(
                    cant.getText()) >= 1;

        } catch (NumberFormatException ex) {

            return false;
        }
    }

    public static boolean botonAgregarInhabilitado(
            ComboBox<String> cbCliente,
            ObservableList<String> clientes,
            VBox contLineas) {

        boolean clienteValido =
                cbCliente.getValue() != null
                && clientes.contains(
                        cbCliente.getValue());

        if (!clienteValido) {
            return true;
        }

        if (contLineas.getChildren().isEmpty()) {
            return true;
        }

        for (var n : contLineas.getChildren()) {

            if (n instanceof HBox fila
                    && lineaValida(fila)) {

                return false;
            }
        }

        return true;
    }
}