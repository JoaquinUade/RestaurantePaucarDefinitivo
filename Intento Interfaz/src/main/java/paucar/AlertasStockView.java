package paucar;


import paucar.config.Responsive;
import java.math.BigDecimal;
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
        contenedor = new VBox(Responsive.pe(15));
        contenedor.setPadding(Responsive.insets(20));
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

        if (alertas.isEmpty()) {
            Label mensajeSinAlertas = new Label(
                    "Aún no hay alertas: todos los productos tienen stock suficiente.");
            mensajeSinAlertas.setStyle("-fx-font-size: 16px; -fx-text-fill: #cbd5e1;");
            contenedor.getChildren().add(mensajeSinAlertas);
            return;
        }

        for (Stock stock : alertas) {

            AlertaStockCard card = new AlertaStockCard(
                    stock.getNombreProducto(),
                    formatearCantidad(stock.getCantidad())
                    + " "
                    + stock.getUnidadCantidad(),
                    formatearCantidad(stock.getStockMinimo())
                    + " "
                    + stock.getUnidadCantidad()
            );

            contenedor.getChildren().add(card);
        }
    }

    private String formatearCantidad(BigDecimal valor) {
    return valor.stripTrailingZeros().toPlainString();
}
}
