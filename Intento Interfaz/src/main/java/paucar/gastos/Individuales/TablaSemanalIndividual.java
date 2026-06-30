package paucar.gastos.Individuales;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.GastosIndividuales;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class TablaSemanalIndividual extends VBox {
    public TablaSemanalIndividual(List<GastosIndividuales> gastos, Consumer<GastosIndividuales> onSelect) {

                TableView<GastosIndividuales> tabla = new TableView<>();
                tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
                Locale localeAR = Locale.forLanguageTag("es-AR");
                // ✅ FECHA (día/mes)
                TableColumn<GastosIndividuales, String> colFecha = new TableColumn<>("Fecha");
                colFecha.setCellValueFactory(c -> {

                        java.time.LocalDate fecha = c.getValue().getFecha();

                        String mes = fecha.getMonth()
                                        .getDisplayName(java.time.format.TextStyle.FULL, localeAR);

                        String textoFecha = fecha.getDayOfMonth() + "-" + mes;

                        return new javafx.beans.property.SimpleStringProperty(textoFecha);
                });
                // ✅ PRODUCTO
                TableColumn<GastosIndividuales, String> colNombre = new TableColumn<>("Producto");
                colNombre.setPrefWidth(200);
                colNombre.setCellValueFactory(
                                c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDetalle()));

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
                // ✅ MONTO (precio)
                TableColumn<GastosIndividuales, String> colPrecio = new TableColumn<>("Precio");
                colPrecio.setCellValueFactory(c -> {

                        NumberFormat formato = NumberFormat.getCurrencyInstance(localeAR);

                        return new javafx.beans.property.SimpleStringProperty(
                                        formato.format(c.getValue().getMonto()));
                });
                colNombre.setSortable(false);
                colPrecio.setSortable(false);

                tabla.getColumns().add(colFecha);
                tabla.getColumns().add(colNombre);
                tabla.getColumns().add(colPrecio);
                tabla.setItems(FXCollections.observableArrayList(gastos));
                tabla.setPrefHeight((gastos.size() * 30) + 35);
                // ✅ DETECTAR SELECCIÓN EN LA TABLA
                tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                        if (newSel != null) {
                                onSelect.accept(newSel);
                                System.out.println("Seleccionado: " + newSel.getDetalle());
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
