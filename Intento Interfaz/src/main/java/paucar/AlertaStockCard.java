package paucar;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AlertaStockCard extends VBox {

    public AlertaStockCard(
            String producto,
            String cantidad,
            String stockMinimo) {

        setSpacing(8);
        setPadding(new Insets(15));

        setStyle("""
            -fx-background-color: #FFF3CD;
            -fx-border-color: #FFC107;
            -fx-border-width: 2;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
        """);

        Label nombre =
                new Label("⚠ " + producto);

        Label actual =
                new Label("Stock actual: " + cantidad);

        Label minimo =
                new Label("Stock mínimo: " + stockMinimo);

        Label mensaje =
                new Label("Debe reponerse");

        mensaje.setStyle("-fx-text-fill: red;");

        getChildren().addAll(
                nombre,
                actual,
                minimo,
                mensaje
        );
    }
}