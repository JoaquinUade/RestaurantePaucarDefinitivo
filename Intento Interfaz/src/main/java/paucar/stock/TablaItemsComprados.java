package paucar.stock;

import com.uade.tpo.demo.entity.GastosVariables;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import paucar.shared.MonedaUtils;

public class TablaItemsComprados {

    public static TableView<GastosVariables> crear() {

        TableView<GastosVariables> tabla
                = new TableView<>();

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<GastosVariables, String> colFecha
                = new TableColumn<>("Fecha");

        colFecha.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getFecha()
                                .toString()
                ));

        TableColumn<GastosVariables, String> colProducto
                = new TableColumn<>("Producto");

        colProducto.setPrefWidth(200);

        colProducto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getProducto()));

        colProducto.setCellFactory(tc -> new TableCell<>() {

            private final javafx.scene.text.Text text
                    = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty()
                        .bind(tc.widthProperty().subtract(10));

                setPrefHeight(
                        javafx.scene.layout.Region.USE_COMPUTED_SIZE);

                setGraphic(text);
            }

            @Override
            protected void updateItem(
                    String item,
                    boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                    text.setText(null);
                    setGraphic(null);

                } else {

                    text.setText(item);
                    setGraphic(text);
                }
            }
        });

        TableColumn<GastosVariables, String> colCantidad
                = new TableColumn<>("Cantidad");

        colCantidad.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getCantidad()
                                .stripTrailingZeros()
                                .toPlainString()
                        + " "
                        + c.getValue().getMedida()
                ));

        TableColumn<GastosVariables, String> colPrecio
                = new TableColumn<>("Precio");

        colPrecio.setCellValueFactory(c
        -> new SimpleStringProperty(
                MonedaUtils.formatearMoneda(
                        c.getValue().getMonto())
        ));
        colProducto.setSortable(false);
        colCantidad.setSortable(false);
        colPrecio.setSortable(false);
        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colProducto);
        tabla.getColumns().add(colCantidad);
        tabla.getColumns().add(colPrecio);

        tabla.setPrefHeight(250);

        return tabla;
    }
}
