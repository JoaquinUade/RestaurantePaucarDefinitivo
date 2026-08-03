package paucar.ventas.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import paucar.service.ProductosService;

public class PanelProductos {

    private final VBox contLineas;
    private final Label lblTotal;
    private final Label lblRestante;
    private final Button btnAgregarLinea;

    public PanelProductos(
            ObservableList<ProductosService.ProductoItem> productos) {

        contLineas = new VBox(6);
        contLineas.setPadding(new Insets(6));

        lblTotal = new Label("Total: $0");
        lblRestante = new Label("Restan pagar: $0");

        btnAgregarLinea = new Button("+ Producto");
        btnAgregarLinea.getStyleClass().add("btn-primary");

        btnAgregarLinea.setOnAction(e ->
                contLineas.getChildren().add(
                        ProductoLinea.crear(
                                contLineas,
                                productos)));

        contLineas.getChildren().add(
                ProductoLinea.crear(
                        contLineas,
                        productos));
    }

    public VBox getContLineas() {
        return contLineas;
    }

    public Label getLblTotal() {
        return lblTotal;
    }

    public Label getLblRestante() {
        return lblRestante;
    }

    public Button getBtnAgregarLinea() {
        return btnAgregarLinea;
    }
}