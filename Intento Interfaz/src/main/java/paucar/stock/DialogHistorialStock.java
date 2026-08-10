package paucar.stock;

import java.util.List;

import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.Stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogHistorialStock {

    public static void mostrar(
            Stock stock,
            List<HistorialStock> historial) {

        Stage ventana = new Stage();

        ventana.initModality(
                Modality.APPLICATION_MODAL);

        ventana.setTitle(
                "Historial - "
                + stock.getNombreProducto());

        TableView<HistorialStock> tabla =
                new TableView<>();

        TableColumn<HistorialStock, String> colFecha =
                new TableColumn<>("Fecha");

        colFecha.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue()
                                .getFecha()
                                .toString()));

        TableColumn<HistorialStock, String> colMovimiento =
                new TableColumn<>("Movimiento");

        colMovimiento.setCellValueFactory(c -> {

            if (c.getValue().getMovimiento() == null) {
                return new SimpleStringProperty("-");
            }

            return new SimpleStringProperty(
                    c.getValue()
                            .getMovimiento()
                            .stripTrailingZeros()
                            .toPlainString());
        });

        TableColumn<HistorialStock, String> colStock =
                new TableColumn<>("Stock");

        colStock.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue()
                                .getCantidad()
                                .stripTrailingZeros()
                                .toPlainString()));

        TableColumn<HistorialStock, String> colGasto =
                new TableColumn<>("Gasto");

        colGasto.setCellValueFactory(c -> {

            if (c.getValue().getGastoVariable() == null) {
                return new SimpleStringProperty("-");
            }

            return new SimpleStringProperty(
                    c.getValue()
                            .getGastoVariable()
                            .getProducto());
        });

        tabla.getColumns().addAll(
                colFecha,
                colMovimiento,
                colStock,
                colGasto);

        tabla.setItems(
                FXCollections.observableArrayList(
                        historial));

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        VBox root = new VBox(tabla);

        Scene scene =
                new Scene(root, 700, 400);

        ventana.setScene(scene);
        ventana.showAndWait();
    }
}