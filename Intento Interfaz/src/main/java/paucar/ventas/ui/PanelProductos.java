package paucar.ventas.ui;


import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import paucar.config.Responsive;
import paucar.service.ProductosService;

public class PanelProductos {

    private final VBox contLineas;
    private final Label lblTotal;
    private final Label lblRestante;
    private final Button btnAgregarLinea;
    private Runnable recalcular;

    public PanelProductos(
            ObservableList<ProductosService.ProductoItem> productos,
            Runnable recalcular) {

    this.recalcular = recalcular;
    
    contLineas  = new VBox(Responsive.pe(6));

    contLineas.setPadding (
    Responsive.insets(6));

        lblTotal  = new Label("Total: $0");
    lblRestante  = new Label("Restan pagar: $0");

    btnAgregarLinea  = new Button("+ Producto");

    btnAgregarLinea.getStyleClass ()
         .add("btn-primary");

        btnAgregarLinea.setOnAction(e
                -> contLineas.getChildren().add(
                        ProductoLinea.crear(
                                contLineas,
                                productos,
                                this.recalcular)));

        contLineas.getChildren().add(
                ProductoLinea.crear(
                        contLineas,
                        productos,
                        this.recalcular));
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

    public void setRecalcular(Runnable recalcular) {
        this.recalcular = recalcular;
    }
}
