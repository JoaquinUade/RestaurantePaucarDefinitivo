package paucar.stock;

import java.util.List;

import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.Stock;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.StockService;

public class FilaStockExpandible extends VBox {

    private boolean expandido = false;

    public FilaStockExpandible(
            Stock stock,
            StockService stockService) {

        setSpacing(5);

        Button btnExpandir = new Button("▶");

        Label lblProducto = new Label(
                stock.getNombreProducto()
        );

        Label lblCantidad = new Label(
                stock.getCantidad()
                        .stripTrailingZeros()
                        .toPlainString()
                + " "
                + stock.getUnidadCantidad()
        );

        Label lblMinimo = new Label(
                "Mínimo: "
                + stock.getStockMinimo()
                        .stripTrailingZeros()
                        .toPlainString()
        );

        HBox filaPrincipal = new HBox(
                15,
                btnExpandir,
                lblProducto,
                lblCantidad,
                lblMinimo
        );

        filaPrincipal.setPadding(
                new Insets(10)
        );

        VBox panelHistorial = new VBox();
        panelHistorial.setSpacing(3);

        panelHistorial.setVisible(false);
        panelHistorial.setManaged(false);

        btnExpandir.setOnAction(e -> {

            expandido = !expandido;

            if (expandido) {

                btnExpandir.setText("▼");

                panelHistorial.getChildren().clear();

                List<HistorialStock> historial
                        = stockService.obtenerHistorialPorStock(
                                stock.getIdStock()
                        );

                Label encabezado
                        = new Label(
                                "Fecha      Movimiento      Stock"
                        );

                panelHistorial
                        .getChildren()
                        .add(encabezado);

                for (HistorialStock h : historial) {

                    String movimiento
                            = h.getMovimiento().compareTo(
                                    java.math.BigDecimal.ZERO) > 0
                                    ? "+" + h.getMovimiento()
                                            .stripTrailingZeros()
                                            .toPlainString()
                                    : h.getMovimiento()
                                            .stripTrailingZeros()
                                            .toPlainString();
                    Label fila
                            = new Label(
                                    h.getFecha()
                                    + "      "
                                    + movimiento
                                    + "      "
                                    + h.getCantidad()
                                            .stripTrailingZeros()
                                            .toPlainString()
                            );

                    panelHistorial
                            .getChildren()
                            .add(fila);
                }

                panelHistorial.setVisible(true);
                panelHistorial.setManaged(true);

            } else {

                btnExpandir.setText("▶");

                panelHistorial.setVisible(false);
                panelHistorial.setManaged(false);
            }
        });

        getChildren().addAll(
                filaPrincipal,
                panelHistorial
        );
    }
}
