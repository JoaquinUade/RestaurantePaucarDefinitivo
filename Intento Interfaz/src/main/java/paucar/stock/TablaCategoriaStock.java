package paucar.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.GastoVariableRequest;
import com.uade.tpo.demo.entity.GastosVariables;
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

public class TablaCategoriaStock extends VBox {

    public TablaCategoriaStock(
            List<Stock> stocks,
            Consumer<Stock> onSelect,
            boolean modoDiario,
            StockService stockService,
            GastosVariablesService gastosVariablesService,
            CategoriasGastosService categoriasService,
            LocalDate fechaSeleccionada) {

        TableView<Stock> tabla = new TableView<>();
        tabla.setEditable(modoDiario);
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPrefWidth(400);
        // PRODUCTO
        TableColumn<Stock, String> colProducto
                = new TableColumn<>("Producto");

        colProducto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getNombreProducto()));

        TableColumn<Stock, String> colCantidadStock
                = new TableColumn<>("Stock actual");

        colCantidadStock.setCellValueFactory(c -> {

            Stock stock = c.getValue();

            String texto
                    = stock.getCantidad()
                            .stripTrailingZeros()
                            .toPlainString()
                    + " "
                    + stock.getUnidadCantidad();

            return new SimpleStringProperty(texto);
        });

        // STOCK MINIMO
        TableColumn<Stock, String> colMinimo
                = new TableColumn<>("Stock mínimo");

        colMinimo.setCellValueFactory(c -> {

            Stock stock = c.getValue();

            String texto = stock.getStockMinimo()
                    .stripTrailingZeros()
                    .toPlainString();

            return new SimpleStringProperty(texto);
        });

        TableColumn<Stock, Void> colSubir
                = new TableColumn<>("+");

        colSubir.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("+");

            {
                btn.setOnAction(event -> {

                    Stock stock = getTableView()
                            .getItems()
                            .get(getIndex());

                    List<GastosVariables> gastos
                            = gastosVariablesService.obtenerTodos();

                    BigDecimal cantidadIngresada
                            = DialogSumarStock.mostrar(
                                    stock.getCategoriaGastoVariable()
                                            .getIdCategoria(),
                                    gastos);
                    GastosVariables gastoUtilizado
                            = DialogSumarStock.getUltimoGastoSeleccionado();
                    if (cantidadIngresada != null) {

                        stockService.sumarStock(
                                stock.getIdStock(),
                                cantidadIngresada,
                                fechaSeleccionada,
                                gastoUtilizado != null
                                        ? gastoUtilizado.getIdGastoVariable()
                                        : null);
                        if (gastoUtilizado != null) {

                            GastoVariableRequest request
                                    = new GastoVariableRequest();

                            request.setFecha(
                                    gastoUtilizado.getFecha());

                            request.setProducto(
                                    gastoUtilizado.getProducto());

                            request.setCantComprada(
                                    gastoUtilizado.getCantComprada());

                            request.setMedida(
                                    gastoUtilizado.getMedida());

                            request.setMonto(
                                    gastoUtilizado.getMonto());

                            request.setCategoriaId(
                                    gastoUtilizado.getCategoria() != null
                                    ? gastoUtilizado.getCategoria().getIdCategoria()
                                    : null);

                            request.setCargadoEnStock(true);
                            request.setStockId(stock.getIdStock());
                            gastosVariablesService.editar(
                                    gastoUtilizado.getIdGastoVariable(),
                                    request);
                        }
                        tabla.refresh();
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
        TableColumn<Stock, Void> colBajar
                = new TableColumn<>("-");

        colBajar.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("-");

            {
                btn.setOnAction(event -> {

                    Stock stock = getTableView()
                            .getItems()
                            .get(getIndex());

                    BigDecimal cantidadARestar
                            = DialogRestarStock.mostrar(
                                    stock.getNombreProducto(),
                                    stock.getCantidad());

                    if (cantidadARestar != null) {

                        BigDecimal nuevaCantidad
                                = stock.getCantidad()
                                        .subtract(cantidadARestar);

                        if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
                            nuevaCantidad = BigDecimal.ZERO;
                        }

                        stockService.ajustarStockDisponible(
                                stock.getIdStock(),
                                nuevaCantidad,
                                fechaSeleccionada);

                        stock.setCantidad(nuevaCantidad);

                        tabla.refresh();
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
        tabla.getColumns().add(colProducto);

        tabla.getColumns().add(colCantidadStock);
        tabla.getColumns().add(colMinimo);
        tabla.getColumns().add(colSubir);
        tabla.getColumns().add(colBajar);

        tabla.setItems(
                FXCollections.observableArrayList(stocks));

        tabla.setPrefHeight(
                (stocks.size() * 30) + 35);

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {

                    if (newSel != null) {

                        onSelect.accept(newSel);
                    }
                });

        long faltantes
                = stocks.stream()
                        .filter(s
                                -> s.getCantidad()
                                .compareTo(
                                        s.getStockMinimo())
                        <= 0)
                        .count();

        Label lblInfo
                = new Label(
                        "Productos con bajo stock: "
                        + faltantes);

        getChildren().addAll(
                tabla,
                lblInfo);
    }
}
