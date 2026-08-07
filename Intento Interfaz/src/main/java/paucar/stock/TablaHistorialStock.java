package paucar.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.HistorialStock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;
import paucar.shared.FechaUtils;

public class TablaHistorialStock extends VBox {

    public TablaHistorialStock(
            List<HistorialStock> historial,
            boolean modoDiario,
            StockService stockService,
            GastosVariablesService gastosVariablesService,
            CategoriasGastosService categoriasService,
            LocalDate fechaSeleccionada) {

        TableView<HistorialStock> tabla = new TableView<>();

        tabla.setEditable(modoDiario);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<HistorialStock, String> colFecha
                = new TableColumn<>("Fecha");

        colFecha.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getFecha() != null
                        ? FechaUtils.fechaMes(c.getValue().getFecha())
                        : ""));

        TableColumn<HistorialStock, String> colProducto
                = new TableColumn<>("Producto");

        colProducto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getStock()
                                .getNombreProducto()));

        TableColumn<HistorialStock, String> colCantidadStock
                = new TableColumn<>("Stock actual");

        colCantidadStock.setCellValueFactory(c -> {

            HistorialStock h = c.getValue();

            String texto
                    = h.getCantidad()
                            .stripTrailingZeros()
                            .toPlainString()
                    + " "
                    + h.getStock()
                            .getUnidadCantidad();

            return new SimpleStringProperty(texto);
        });

        TableColumn<HistorialStock, String> colMinimo
                = new TableColumn<>("Stock mínimo");

        colMinimo.setCellValueFactory(c -> {

            String texto
                    = c.getValue()
                            .getStock()
                            .getStockMinimo()
                            .stripTrailingZeros()
                            .toPlainString();

            return new SimpleStringProperty(texto);
        });

        TableColumn<HistorialStock, Void> colSubir
                = new TableColumn<>("+");

        colSubir.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("+");

            {
                btn.setOnAction(event -> {

                    HistorialStock historialActual
                            = getTableView()
                                    .getItems()
                                    .get(getIndex());

                    List<GastosVariables> gastos
                            = gastosVariablesService.obtenerTodos();

                    BigDecimal cantidadIngresada
                            = DialogSumarStock.mostrar(
                                    historialActual.getStock()
                                            .getCategoriaGastoVariable()
                                            .getIdCategoria(),
                                    gastos);

                    if (cantidadIngresada != null) {

                        BigDecimal nuevaCantidad
                                = historialActual.getCantidad()
                                        .add(cantidadIngresada);

                        stockService.ajustarStockDisponible(
                                historialActual.getStock()
                                        .getIdStock(),
                                nuevaCantidad,
                                historialActual.getFecha());

                        historialActual.setCantidad(nuevaCantidad);

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

        TableColumn<HistorialStock, Void> colBajar
                = new TableColumn<>("-");

        colBajar.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("-");

            {
                btn.setOnAction(event -> {

                    HistorialStock historialActual
                            = getTableView()
                                    .getItems()
                                    .get(getIndex());

                    BigDecimal cantidadARestar
                            = DialogRestarStock.mostrar(
                                    historialActual.getStock()
                                            .getNombreProducto(),
                                    historialActual.getCantidad());

                    if (cantidadARestar != null) {

                        BigDecimal nuevaCantidad
                                = historialActual.getCantidad()
                                        .subtract(cantidadARestar);

                        if (nuevaCantidad.compareTo(
                                BigDecimal.ZERO) < 0) {

                            nuevaCantidad = BigDecimal.ZERO;
                        }

                        stockService.ajustarStockDisponible(
                                historialActual.getStock()
                                        .getIdStock(),
                                nuevaCantidad,
                                historialActual.getFecha());

                        historialActual.setCantidad(nuevaCantidad);

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

        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colProducto);

        colMinimo.setCellFactory(
                TextFieldTableCell.forTableColumn());

        tabla.setPrefWidth(450);

        tabla.getColumns().add(colCantidadStock);
        tabla.getColumns().add(colMinimo);
        tabla.getColumns().add(colSubir);
        tabla.getColumns().add(colBajar);

        tabla.setItems(
                FXCollections.observableArrayList(historial));

        tabla.setPrefHeight(
                (historial.size() * 30) + 35);

        long faltantes
                = historial.stream()
                        .filter(h
                                -> h.getCantidad()
                                .compareTo(
                                        h.getStock()
                                                .getStockMinimo())
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
