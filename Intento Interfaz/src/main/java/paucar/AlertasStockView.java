package paucar;

import java.util.List;

import com.uade.tpo.demo.entity.Stock;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import paucar.service.StockService;

public class AlertasStockView extends ScrollPane {

    private final VBox contenedor;

    public AlertasStockView(StockService stockService) {
        System.out.println("ENTRE A ALERTAS");
        contenedor = new VBox(15);
        contenedor.setPadding(new Insets(20));
        contenedor.setStyle("""
            -fx-background-color: transparent;
        """);

        setStyle("""
            -fx-background-color: transparent;
        """);
        cargarAlertas(stockService);
        setContent(contenedor);
        setFitToWidth(true);
        contenedor.setStyle("""
    -fx-background-color: #0f172a; 
""");
    }

    private void cargarAlertas(StockService stockService) {

        List<Stock> alertas
                = stockService.obtenerFaltantes();

        for (Stock stock : alertas) {

            AlertaStockCard card
                    = new AlertaStockCard(
                            stock.getNombreProducto(),
                            stock.getCantidad() + " " + stock.getUnidadCantidad(),
                            stock.getStockMinimo() + " " + stock.getUnidadCantidad()
                    );

            contenedor.getChildren().add(card);
        }
    }
}
