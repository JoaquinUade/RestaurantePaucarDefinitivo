package paucar.ventas.ui;


import paucar.config.Responsive;
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
            ObservableList<ProductosService.ProductoItem> productos,
            Runnable recalcular) {
        ComboBox<ProductosService.ProductoItem> cbProd = new ComboBox<>();

        cbProd.getStyleClass().add("combo-agregar");

        cbProd.setPrefWidth(Responsive.px(280));
        cbProd.setPromptText("Producto");
        cbProd.setEditable(true);
        cbProd.valueProperty().addListener((obs, oldV, newV)
                -> recalcular.run());

        ProductoAutoCompletar.configurar(
                cbProd,
                productos);
        TextField tfCant = new TextField();

        tfCant.setPromptText("Cant.");
        tfCant.setPrefWidth(Responsive.px(70));

        tfCant.textProperty().addListener((o, a, b) -> {

            if (b != null && !b.matches("\\d*")) {
                tfCant.setText(
                        b.replaceAll("[^\\d]", ""));
            }

            recalcular.run();
        });

        Button btnDelete = new Button("✕");

        btnDelete.getStyleClass().add("btn-danger");

        HBox fila = new HBox(Responsive.pe(6),
                cbProd,
                tfCant,
                btnDelete);

        fila.setAlignment(Pos.CENTER_LEFT);

        btnDelete.setOnAction(
                e -> contLineas.getChildren().remove(fila));

        return fila;
    }
}
