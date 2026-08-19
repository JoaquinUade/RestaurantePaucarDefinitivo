package paucar;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AlertaStockCard extends VBox {

    public AlertaStockCard(
            String producto,
            String cantidad,
            String stockMinimo) {

        setSpacing(10);
        setPadding(new Insets(18));

        setStyle("""
            -fx-background-color: linear-gradient(to bottom, #FFF8E1, #FFE082);
            -fx-background-radius: 15;
            -fx-border-radius: 15;
            -fx-border-color: #FFB300;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0.2, 0, 4);
        """);

        Label nombre = new Label("⚠ " + producto);
        nombre.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #5D4037;
        """);

        Label actual = new Label("📦 Stock actual: " + cantidad);
        actual.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #424242;
        """);

        Label minimo = new Label("📉 Stock mínimo: " + stockMinimo);
        minimo.setStyle("""
            -fx-font-size: 14px;
            -fx-text-fill: #424242;
        """);

        Label mensaje = new Label("❌ Debe reponerse urgentemente");
        mensaje.setStyle("""
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-text-fill: #D32F2F;
        """);

        getChildren().addAll(
                nombre,
                actual,
                minimo,
                mensaje
        );
    }
}