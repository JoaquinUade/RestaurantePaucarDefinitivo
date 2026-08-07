package paucar.stock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.HistorialStock;

import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;

public class PanelHistorialStock extends VBox {

    public PanelHistorialStock(
            List<HistorialStock> historial,
            boolean modoDiario,
            StockService stockService,
            GastosVariablesService gastosVariablesService,
            CategoriasGastosService categoriasService,
            LocalDate fechaSeleccionada) {

        Map<Integer, List<HistorialStock>> porSemana =
                historial.stream()
                        .collect(Collectors.groupingBy(
                                h -> ((h.getFecha()
                                        .getDayOfMonth() - 1) / 7) + 1
                        ));

        porSemana = new TreeMap<>(porSemana);

        porSemana.forEach((semana, listaSemana) -> {

    getChildren().add(
            new TablaHistorialStock(
                    listaSemana,
                    modoDiario,
                    stockService,
                    gastosVariablesService,
                    categoriasService,
                    fechaSeleccionada
            )
    );
});
    }
}