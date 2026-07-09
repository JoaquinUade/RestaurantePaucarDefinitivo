package paucar.stock;

import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.Stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class TablaCategoriaStock extends VBox {

    public TablaCategoriaStock(
            List<Stock> stocks,
            Consumer<Stock> onSelect) {

        TableView<Stock> tabla =
                new TableView<>();

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // PRODUCTO

        TableColumn<Stock, String> colProducto =
                new TableColumn<>("Producto");

        colProducto.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue()
                                .getNombreProducto()));

        // CANTIDAD

        TableColumn<Stock, String> colCantidad =
                new TableColumn<>("Cantidad");

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

        colMinimo.setCellValueFactory(c -> {

            Stock stock = c.getValue();

            String texto =
        stock.getStockMinimo()
                .stripTrailingZeros()
                .toPlainString()
                + " "
                + stock.getUnidadStockMinimo();


            return new SimpleStringProperty(texto);
        });

        // ESTADO

        TableColumn<Stock, String> colEstado =
                new TableColumn<>("Estado");

        colEstado.setCellValueFactory(c -> {

            Stock stock = c.getValue();

            boolean bajoStock =
                    stock.getCantidad()
                            .compareTo(
                                    stock.getStockMinimo())
                            <= 0;

            return new SimpleStringProperty(
                    bajoStock
                            ? "Bajo Stock"
                            : "OK");
        });

        tabla.getColumns().add(colProducto);
        tabla.getColumns().add(colCantidad);
        tabla.getColumns().add(colMinimo);
        tabla.getColumns().add(colEstado);

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