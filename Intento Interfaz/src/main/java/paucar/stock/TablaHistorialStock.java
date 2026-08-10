package paucar.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.Stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

        TableView<Stock> tabla = new TableView<>();

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // PRODUCTO
        TableColumn<Stock, String> colProducto =
                new TableColumn<>("Producto");

        colProducto.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getNombreProducto()));

        // STOCK ACTUAL
        TableColumn<Stock, String> colCantidad =
                new TableColumn<>("Stock actual");

        colCantidad.setCellValueFactory(c -> {

            Stock stock = c.getValue();

            String texto =
                    stock.getCantidad()
                            .stripTrailingZeros()
                            .toPlainString()
                    + " "
                    + stock.getUnidadCantidad();

            return new SimpleStringProperty(texto);
        });

        // STOCK MINIMO
        TableColumn<Stock, String> colMinimo =
                new TableColumn<>("Stock mínimo");

        colMinimo.setCellValueFactory(c ->

                new SimpleStringProperty(
                        c.getValue()
                                .getStockMinimo()
                                .stripTrailingZeros()
                                .toPlainString()
                )
        );
// SUBIR STOCK
TableColumn<Stock, Void> colSubirStock =
        new TableColumn<>("Subir stock");

colSubirStock.setCellFactory(param ->
        new TableCell<>() {

            private final Button btn =
                    new Button("+");

            {
                btn.setOnAction(event -> {

    Stock stock =
            getTableView()
                    .getItems()
                    .get(getIndex());

    BigDecimal cantidad =
            DialogSumarStock.mostrar(
                    stock.getCategoriaGastoVariable()
                         .getIdCategoria(),
                    gastosVariablesService.obtenerTodos()
            );

    if (cantidad != null) {

        stockService.sumarStock(
                stock.getIdStock(),
                cantidad,
                DialogSumarStock.getUltimaFechaSeleccionada(),
                DialogSumarStock
                        .getUltimoGastoSeleccionado()
                        .getIdGastoVariable()
        );
    }
});
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        TableColumn<Stock, Void> colRestarStock =
        new TableColumn<>("Consumir");
        colRestarStock.setCellFactory(param ->
        new TableCell<>() {

            private final Button btn =
                    new Button("-");

            {
                btn.setOnAction(event -> {

                    Stock stock =
                            getTableView()
                                    .getItems()
                                    .get(getIndex());

                    BigDecimal cantidad =
                            DialogRestarStock.mostrar(
                                    stock.getNombreProducto(),
                                    stock.getCantidad()
                            );

                    if (cantidad != null) {

    stockService.restarStock(
            stock.getIdStock(),
            cantidad
    );
}
                });
            }

            @Override
            protected void updateItem(
                    Void item,
                    boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        // HISTORIAL
        TableColumn<Stock, Void> colHistorial =
                new TableColumn<>("Historial");

        colHistorial.setCellFactory(param ->
                new TableCell<>() {

                    private final Button btn =
                            new Button("Ver");

                    {
                        btn.setOnAction(event -> {

                            Stock stock =
                                    getTableView()
                                            .getItems()
                                            .get(getIndex());

                            List<HistorialStock> historial =
                                    stockService
                                            .obtenerHistorialPorStock(
                                                    stock.getIdStock());

                            DialogHistorialStock.mostrar(
                                    stock,
                                    historial
                            );
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                });

       tabla.getColumns().add(colProducto);
tabla.getColumns().add(colCantidad);
tabla.getColumns().add(colMinimo);
tabla.getColumns().add(colSubirStock);
tabla.getColumns().add(colRestarStock);
tabla.getColumns().add(colHistorial);

        tabla.setItems(
                FXCollections.observableArrayList(
                        stocks));

        tabla.setPrefHeight(
                (stocks.size() * 30) + 60);

        long faltantes =
                stocks.stream()
                        .filter(s ->
                                s.getCantidad()
                                        .compareTo(
                                                s.getStockMinimo())
                                        <= 0)
                        .count();

        Label lblInfo =
                new Label(
                        "Productos con bajo stock: "
                        + faltantes);

        getChildren().addAll(
                tabla,
                lblInfo);
    }
}