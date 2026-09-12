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
                System.out.println(
                        "Cantidad de hijos de la fila: "
                        + fila.getChildren().size()
                );
                for (Node hijo : fila.getChildren()) {
                    System.out.println(
                            hijo.getClass().getName()
                    );
                }
                @SuppressWarnings("unchecked")
                ComboBox<ProductosService.ProductoItem> cbProd
                        = (ComboBox<ProductosService.ProductoItem>) fila.getChildren().get(0);

                TextField tfCant
                        = (TextField) fila.getChildren().get(1);

                if (cbProd.getValue() != null
                        && !tfCant.getText().isBlank()) {

                    try {

                        int cantidad
                                = Integer.parseInt(
                                        tfCant.getText());

                        double subtotal
                                = cbProd.getValue().precio()
                                * cantidad;

                        System.out.println("-----------");
                        System.out.println("Producto: " + cbProd.getValue());
                        System.out.println("Cantidad: " + cantidad);
                        System.out.println("Subtotal: " + subtotal);

                        total += subtotal;

                    } catch (NumberFormatException ex) {
                        System.err.println("Error al parsear la cantidad: " + ex.getMessage());
                    }
                }
            }
        }
        System.out.println(
                "Total calculado = " + total);
        return total;
    }
}
