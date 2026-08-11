package paucar;

import java.util.List;

import com.uade.tpo.demo.entity.Stock;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import paucar.service.StockService;

public class AlertasStockView extends ScrollPane {

    private final VBox contenedor;

    public AlertasStockView(StockService stockService) {
System.out.println("ENTRE A ALERTAS");
        contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));

        Label titulo = new Label("⚠ Alertas de Stock");
        titulo.getStyleClass().add("titulo-alertas");

        contenedor.getChildren().add(titulo);

        cargarAlertas(stockService);

        setContent(contenedor);
        setFitToWidth(true);
    }
private void cargarAlertas(StockService stockService) {

    List<Stock> alertas =
            stockService.obtenerFaltantes();

    System.out.println(
            "Cantidad de alertas: "
            + alertas.size()
    );

    for (Stock stock : alertas) {

        System.out.println(
                stock.getNombreProducto()
                + " | "
                + stock.getCantidad()
                + " | "
                + stock.getStockMinimo()
        );
    }
}
}