package paucar.stock;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.Stock;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;

public class TablaHistorialStock extends VBox {

    public TablaHistorialStock(
        List<Stock> stocks,
        boolean modoDiario,
        StockService stockService,
        GastosVariablesService gastosVariablesService,
        CategoriasGastosService categoriasService,
        LocalDate fechaSeleccionada) {

    VBox contenedorStocks = new VBox(5);

    for (Stock stock : stocks) {

        contenedorStocks.getChildren().add(
                new FilaStockExpandible(
                        stock,
                        stockService
                )
        );
    }

    long faltantes =
            stocks.stream()
                    .filter(s ->
                            s.getCantidad()
                                    .compareTo(
                                            s.getStockMinimo()
                                    ) <= 0
                    )
                    .count();

    Label lblInfo =
            new Label(
                    "Productos con bajo stock: "
                    + faltantes
            );

    getChildren().addAll(
            contenedorStocks,
            lblInfo
    );
}
}
