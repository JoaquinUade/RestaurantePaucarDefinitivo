package paucar.ventas.util;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.ProductosService;

public final class CalculadoraVenta {

    private CalculadoraVenta() {
    }

    public static double calcularTotal(
            VBox contLineas) {

        double total = 0;

        for (Node n : contLineas.getChildren()) {

            if (n instanceof HBox fila) {

                @SuppressWarnings("unchecked")
                ComboBox<ProductosService.ProductoItem> cbProd
                        = (ComboBox<ProductosService.ProductoItem>)
                        fila.getChildren().get(0);

                TextField tfCant
                        = (TextField)
                        fila.getChildren().get(1);

                if (cbProd.getValue() != null
                        && !tfCant.getText().isBlank()) {

                    try {

                        int cantidad =
                                Integer.parseInt(
                                        tfCant.getText());

                        total +=
                                cbProd.getValue().precio()
                                * cantidad;

                    } catch (Exception ex) {
                    }
                }
            }
        }

        return total;
    }
}