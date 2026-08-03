package paucar.ventas.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.ProductosService;

public final class ProductoLinea {

    private ProductoLinea() {
    }

    public static HBox crear(
        VBox contLineas,
        ObservableList<ProductosService.ProductoItem> productos) {

        ComboBox<ProductosService.ProductoItem> cbProd = new ComboBox<>();

        cbProd.getStyleClass().add("combo-agregar");

        cbProd.setPrefWidth(280);
        cbProd.setPromptText("Producto");
        cbProd.setEditable(true);

        ProductoAutoCompletar.configurar(
                cbProd,
                productos);
        TextField tfCant = new TextField();

        tfCant.setPromptText("Cant.");
        tfCant.setPrefWidth(70);

        tfCant.textProperty().addListener((o, a, b) -> {

    if (b != null && !b.matches("\\d*")) {
        tfCant.setText(
                b.replaceAll("[^\\d]", ""));
    }

});

        Button btnDelete = new Button("✕");

        btnDelete.getStyleClass().add("btn-danger");

        HBox fila = new HBox(
                6,
                cbProd,
                tfCant,
                btnDelete);

        fila.setAlignment(Pos.CENTER_LEFT);

        btnDelete.setOnAction(
                e -> contLineas.getChildren().remove(fila));

        return fila;
    }
}