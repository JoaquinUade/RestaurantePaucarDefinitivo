package paucar.gastos.Variables;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.GastosVariables;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class TablaSemanalGastosV extends VBox {

        public TablaSemanalGastosV(List<GastosVariables> gastos, Consumer<GastosVariables> onSelect) {

                TableView<GastosVariables> tabla = new TableView<>();
                tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
                Locale localeAR = Locale.forLanguageTag("es-AR");
                // ✅ FECHA (día/mes)
                TableColumn<GastosVariables, String> colFecha = new TableColumn<>("Fecha");
                colFecha.setCellValueFactory(c -> {

                        java.time.LocalDate fecha = c.getValue().getFecha();

                        String mes = fecha.getMonth()
                                        .getDisplayName(java.time.format.TextStyle.FULL, localeAR);

                        String textoFecha = fecha.getDayOfMonth() + "-" + mes;

                        return new javafx.beans.property.SimpleStringProperty(textoFecha);
                });
                // ✅ PRODUCTO
                TableColumn<GastosVariables, String> colNombre = new TableColumn<>("Producto");
                colNombre.setPrefWidth(200);
                colNombre.setCellValueFactory(
                                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getProducto()));

                colNombre.setCellFactory(tc -> new TableCell<>() {

                        private final javafx.scene.text.Text text = new javafx.scene.text.Text();

                        {
                                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(10));
                                setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
                                setGraphic(text);
                        }

                        @Override
                        protected void updateItem(String item, boolean empty) {
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
                // ✅ CANTIDAD + MEDIDA
                TableColumn<GastosVariables, String> colCantidad = new TableColumn<>("Cantidad");
                colCantidad.setCellValueFactory(c -> {

                        String cantidad = c.getValue()
                                        .getCantidad()
                                        .stripTrailingZeros()
                                        .toPlainString();

                        String medida = c.getValue().getMedida();

                        return new javafx.beans.property.SimpleStringProperty(
                                        medida.isEmpty() ? cantidad : cantidad + " " + medida);
                });

                // ✅ MONTO (precio)
                TableColumn<GastosVariables, String> colPrecio = new TableColumn<>("Precio");
                colPrecio.setCellValueFactory(c -> {

                        NumberFormat formato = NumberFormat.getCurrencyInstance(localeAR);

                        return new javafx.beans.property.SimpleStringProperty(
                                        formato.format(c.getValue().getMonto()));
                });
                colNombre.setSortable(false);
                colCantidad.setSortable(false);
                colPrecio.setSortable(false);

                tabla.getColumns().add(colFecha);
                tabla.getColumns().add(colNombre);
                tabla.getColumns().add(colCantidad);
                tabla.getColumns().add(colPrecio);
                tabla.setItems(FXCollections.observableArrayList(gastos));
                tabla.setPrefHeight((gastos.size() * 30) + 35);
                // ✅ DETECTAR SELECCIÓN EN LA TABLA
                tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                        if (newSel != null) {
                                onSelect.accept(newSel);
                                System.out.println("Seleccionado: " + newSel.getProducto());
                        }
                });
                // ✅ TOTAL
                double total = gastos.stream()
                                .mapToDouble(g -> g.getMonto().doubleValue())
                                .sum();

                NumberFormat formato = NumberFormat.getCurrencyInstance(localeAR);
                Label lblTotal = new Label("Total: " + formato.format(total));

                getChildren().addAll(tabla, lblTotal);
        }
}