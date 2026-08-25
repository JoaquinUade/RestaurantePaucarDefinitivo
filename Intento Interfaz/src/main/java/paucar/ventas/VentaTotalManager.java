package paucar.ventas;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.ventas.util.CalculadoraVenta;

public class VentaTotalManager {

    private VentaTotalManager() {
    }

    public static void actualizarTotales(
            VBox contLineas,
            VBox contPagadores,
            Label lblTotal,
            Label lblRestante) {

        double total =
                CalculadoraVenta.calcularTotal(
                        contLineas);

        int cantidadPagadores =
                contPagadores.getChildren().size();

        if (cantidadPagadores > 0) {

            double base =
                    Math.floor(
                            (total / cantidadPagadores)
                            * 100)
                    / 100;

            double restanteDivision = total;

            for (int i = 0;
                 i < cantidadPagadores;
                 i++) {

                HBox fila =
                        (HBox) contPagadores
                                .getChildren()
                                .get(i);

                TextField tfMonto =
                        (TextField) fila
                                .getChildren()
                                .get(1);

                double sugerencia;

                if (i == cantidadPagadores - 1) {

                    sugerencia =
                            restanteDivision;

                } else {

                    sugerencia = base;
                    restanteDivision -= base;
                }

                tfMonto.setPromptText(
                        String.format(
                                "$ %.2f",
                                sugerencia));
            }
        }

        lblTotal.setText(
                "Total: $" + total);

        double pagado = 0;

        for (Node n : contPagadores.getChildren()) {

            HBox fila = (HBox) n;

            TextField tfMonto =
                    (TextField) fila
                            .getChildren()
                            .get(1);

            if (!tfMonto.getText().isBlank()) {

                try {

                    pagado += Double.parseDouble(
                            tfMonto.getText());

                } catch (NumberFormatException ex) {
                }
            }
        }

        if (contPagadores.getChildren().isEmpty()) {

            lblRestante.setVisible(false);
            lblRestante.setManaged(false);

            return;
        }

        lblRestante.setVisible(true);
        lblRestante.setManaged(true);

        double restante = total - pagado;

        lblRestante.getStyleClass().removeAll(
                "restante-pendiente",
                "restante-completo",
                "restante-excedido");

        if (restante > 0) {

            lblRestante.setText(
                    String.format(
                            "Restan pagar: $%.2f",
                            restante));

            lblRestante.getStyleClass().add(
                    "restante-pendiente");

        } else if (restante < 0) {

            lblRestante.setText(
                    String.format(
                            "El cliente está pagando de más: $%.2f",
                            Math.abs(restante)));

            lblRestante.getStyleClass().add(
                    "restante-excedido");

        } else {

            lblRestante.setText(
                    "Pago completo");

            lblRestante.getStyleClass().add(
                    "restante-completo");
        }
    }
}