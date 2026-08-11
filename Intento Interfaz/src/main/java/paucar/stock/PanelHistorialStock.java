package paucar.stock;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.Stock;

import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;

public class PanelHistorialStock extends VBox {

    public PanelHistorialStock(
        String categoria,
        List<Stock> stocks,
        StockService stockService,
        GastosVariablesService gastosVariablesService,
        CategoriasGastosService categoriasService,
        LocalDate fechaSeleccionada, Consumer<Stock> onSelect) {

    TituloPanel titulo =
            new TituloPanel(categoria);

    getChildren().add(titulo);

    getChildren().add(
            new TablaHistorialStock(
                    stocks,
                    stockService,
                    gastosVariablesService,
                    categoriasService,
                    fechaSeleccionada,
                    onSelect
            )
    );
}
}