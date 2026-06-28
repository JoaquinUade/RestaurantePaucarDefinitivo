package paucar.gastos.Variables;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.GastosVariables;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PanelCategoriaGastosV extends VBox {

    public PanelCategoriaGastosV(String nombreCategoria, List<GastosVariables> gastos, Consumer<GastosVariables> onSelect) {

        Label titulo = new Label(nombreCategoria);
        titulo.getStyleClass().add("card-header");
        titulo.setMaxWidth(Double.MAX_VALUE);

        Map<Integer, List<GastosVariables>> porSemana = gastos.stream()
                .collect(Collectors.groupingBy(g -> {
                    LocalDate f = g.getFecha();
                    WeekFields wf = WeekFields.of(Locale.getDefault());
                    return f.get(wf.weekOfWeekBasedYear());
                }));

        getChildren().add(titulo);

        porSemana.values().forEach(lista -> {
            getChildren().add(new TablaSemanalGastosV(lista, onSelect));
        });

    }
}
