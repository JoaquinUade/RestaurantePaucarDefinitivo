package paucar.ventas.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class PanelPagadores {

    private PanelPagadores() {
    }

    public static TextField crearCantidadPagadores(
        VBox contPagadores,
        Runnable actualizar) {

    TextField tfCantidadPagadores =
            new TextField("1");

    tfCantidadPagadores.setPrefWidth(80);
    tfCantidadPagadores.setPromptText("Cantidad");

    tfCantidadPagadores.textProperty().addListener(
            (obs, oldValue, newValue) -> {

                if (!newValue.matches("\\d*")) {
                    tfCantidadPagadores.setText(
                            newValue.replaceAll(
                                    "[^\\d]",
                                    ""));
                    return;
                }

                contPagadores.getChildren().clear();

                try {

                    int cantidad =
                            Integer.parseInt(
                                    newValue.isBlank()
                                    ? "1"
                                    : newValue);

                    if (cantidad < 1) {
                        cantidad = 1;
                    }

                    if (cantidad > 1) {

    for (int i = 1;
            i <= cantidad;
            i++) {

        TextField tfMonto =
                new TextField();

        tfMonto.textProperty().addListener(
                (obsMonto,
                        oldMonto,
                        newMonto) -> {

                    actualizar.run();
                });

        HBox fila =
                new HBox(
                        10,
                        new Label(
                                "Persona "
                                + i
                                + ":"),
                        tfMonto);

        contPagadores
                .getChildren()
                .add(fila);
    }
}

                    actualizar.run();

                } catch (NumberFormatException ex) {
                }
            });

    tfCantidadPagadores.setText("1");

    return tfCantidadPagadores;
}

    public static VBox crearContenedor() {
        return new VBox(5);
    }
}
