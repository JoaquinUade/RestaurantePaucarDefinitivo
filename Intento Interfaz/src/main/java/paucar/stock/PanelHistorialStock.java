package paucar.stock;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.Stock;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;

public class PanelHistorialStock extends VBox {

    public PanelHistorialStock(
        String categoria,
        List<Stock> stocks,
        boolean modoDiario,
        StockService stockService,
        GastosVariablesService gastosVariablesService,
        CategoriasGastosService categoriasService,
        LocalDate fechaSeleccionada) {

       Label titulo = new Label(categoria);

titulo.getStyleClass().add(
        "titulo-xl-blanco"
);

getChildren().add(titulo);

getChildren().add(
        new TablaHistorialStock(
                stocks,
                modoDiario,
                stockService,
                gastosVariablesService,
                categoriasService,
                fechaSeleccionada
        )
);
    }
}