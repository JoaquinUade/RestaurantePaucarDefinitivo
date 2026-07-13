package paucar.stock;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.Stock;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import paucar.service.StockService;

public class PanelCategoriaStock extends VBox {

    public PanelCategoriaStock(
            String nombreCategoria,
            List<Stock> stocks, Consumer<Stock> onSelect, boolean modoDiario,
            StockService stockService, LocalDate fechaSeleccionada) {

        Label titulo = new Label(nombreCategoria);
        titulo.getStyleClass().add("card-header");
        titulo.setMaxWidth(Double.MAX_VALUE);

        getChildren().add(titulo);

        getChildren().add(
                new TablaCategoriaStock(
                        stocks,
                        onSelect,
                        modoDiario, stockService, fechaSeleccionada));
    }
}