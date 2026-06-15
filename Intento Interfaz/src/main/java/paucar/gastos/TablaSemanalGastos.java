package paucar.gastos;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.uade.tpo.demo.entity.GastosVariables;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class TablaSemanalGastos extends VBox {

    public TablaSemanalGastos(List<GastosVariables> gastos) {

        TableView<GastosVariables> tabla = new TableView<>();
        Locale localeAR = Locale.forLanguageTag("es-AR");
        // ✅ PRODUCTO
        TableColumn<GastosVariables, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getProducto()
                )
        );

        // ✅ CANTIDAD + MEDIDA
        TableColumn<GastosVariables, String> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCantidad() + " " + c.getValue().getMedida()
                )
        );

        // ✅ MONTO (precio)
        TableColumn<GastosVariables, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(c -> {

           NumberFormat formato = NumberFormat.getCurrencyInstance(localeAR);

            return new javafx.beans.property.SimpleStringProperty(
                    formato.format(c.getValue().getMonto())
            );
        });

        tabla.getColumns().add(colNombre);
        tabla.getColumns().add(colCantidad);
        tabla.getColumns().add(colPrecio);
        tabla.setItems(FXCollections.observableArrayList(gastos));

        // ✅ TOTAL
        double total = gastos.stream()
                .mapToDouble(g -> g.getMonto().doubleValue())
                .sum();

       NumberFormat formato = NumberFormat.getCurrencyInstance(localeAR);
        Label lblTotal = new Label("Total: " + formato.format(total));

        getChildren().addAll(tabla, lblTotal);
    }
}