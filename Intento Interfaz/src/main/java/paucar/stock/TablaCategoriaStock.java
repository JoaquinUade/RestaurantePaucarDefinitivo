package paucar.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.Stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import paucar.service.StockService;

public class TablaCategoriaStock extends VBox {

    private final StockService stockService;

    public TablaCategoriaStock(List<Stock> stocks, Consumer<Stock> onSelect,
                               boolean modoDiario, StockService stockService,
                               LocalDate fechaSeleccionada) {
        this.stockService = stockService;
        TableView<Stock> tabla = new TableView<>();
        tabla.setEditable(modoDiario);
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // PRODUCTO
        TableColumn<Stock, String> colProducto
                = new TableColumn<>("Producto");

        colProducto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getNombreProducto()));

        // CANTIDAD
        TableColumn<Stock, String> colCantComprada
                = new TableColumn<>("Cantidad comprada");

        colCantComprada.setCellValueFactory(c -> {

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

            if (!modoDiario) {
                texto += " " + stock.getUnidadStockMinimo();
            }

            return new SimpleStringProperty(texto);
        });

        // UNIDAD
        TableColumn<Stock, String> colUnidad
                = new TableColumn<>("Unidad");

        colUnidad.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getUnidadStockMinimo()));
        
        tabla.getColumns().add(colProducto);

        if (modoDiario) {
                 colMinimo.setCellFactory(
                TextFieldTableCell.forTableColumn());
                colMinimo.setOnEditCommit(event -> {

            Stock stock = event.getRowValue();

            try {

                BigDecimal nuevoMinimo
                        = new BigDecimal(
                                event.getNewValue());

                stock.setStockMinimo(nuevoMinimo);

                stock.setFecha(fechaSeleccionada);

                stockService.ajustarStockDisponible(
                        stock.getIdStock(),
                        nuevoMinimo,
                        fechaSeleccionada);

                tabla.refresh();

            } catch (Exception e) {

                System.err.println(
                        "Error al editar stock mínimo: "
                        + e.getMessage());
            }
        });
            tabla.getColumns().add(colMinimo);
            tabla.getColumns().add(colUnidad);
        } else {

            tabla.getColumns().add(colCantComprada);
            tabla.getColumns().add(colMinimo);
        }

        tabla.setItems(
                FXCollections.observableArrayList(stocks));

        tabla.setPrefHeight(
                (stocks.size() * 30) + 35);

        tabla.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {

                    if (newSel != null) {

                        onSelect.accept(newSel);

                        System.out.println(
                                "Seleccionado: "
                                + newSel.getNombreProducto());
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
