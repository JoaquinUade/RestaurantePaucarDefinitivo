package paucar.ventas;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.uade.tpo.demo.entity.TipoDePago;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import com.uade.tpo.demo.entity.Venta;

@SuppressWarnings("unused")
public class Tabla extends VBox {

    private final TableView<Venta> tabla = new TableView<>();

    private final Button btnEliminar = new Button("Eliminar Venta");/*crea un botón para eliminar una venta seleccionada en la tabla*/

    private final Consumer<Venta> OnEliminar;/*declara un campo OnEliminar que es una función que acepta una fila de venta y
                                                   no devuelve nada, se usará para manejar la eliminación de ventas*/

    private final BiConsumer<Venta, TipoDePago> onCambiarEstado;
    private final NumberFormat moneda;

    private boolean soloLectura = false;

    public Tabla(ObservableList<Venta> items, Locale locale, Consumer<Venta> onEliminar,
            BiConsumer<Venta, TipoDePago> onCambiarEstado) {
        this.moneda = NumberFormat.getCurrencyInstance(locale);
        this.OnEliminar = onEliminar;
        this.onCambiarEstado = onCambiarEstado;

        setSpacing(8);
        setPadding(new Insets(0));

        tabla.setEditable(true);
        tabla.setItems(items);
        tabla.getColumns().setAll(crearColumnas());
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        btnEliminar.disableProperty()
                .bind(Bindings.isNull(tabla.getSelectionModel().selectedItemProperty()));

        btnEliminar.setOnAction(e -> {
            var sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null && onEliminar != null) {
                onEliminar.accept(sel); // ✅ avisa a Ventas
            }
        });

        VBox.setVgrow(tabla, Priority.ALWAYS);
        getChildren().addAll(tabla, btnEliminar);
    }

    private List<TableColumn<Venta, ?>> crearColumnas() {
        return List.of(
                colNombre(),
                colDescripcion(),
                colMonto(),
                colEstado(),
                colObservaciones()
        );
    }

    private TableColumn<Venta, String> colNombre() {

        var col = new TableColumn<Venta, String>("Nombre");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCliente().getNombre()
                ));

        col.setPrefWidth(200);

        return col;
    }

    private TableColumn<Venta, String> colDescripcion() {
        var col = new TableColumn<Venta, String>("Descripción");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDescripcion()
                ));

        col.setCellFactory(tc -> new TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(16));
                setGraphic(text);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText(null);
                    setTooltip(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });
        col.setEditable(false);
        col.setPrefWidth(420);
        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> colMonto() {
        var col = new TableColumn<Venta, String>("Monto");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(
                        formatMoneda(c.getValue().getMonto())
                ));

        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setEditable(false);
        col.setPrefWidth(140);
        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, TipoDePago> colEstado() {
        var col = new TableColumn<Venta, TipoDePago>("Estado");
        col.setCellValueFactory(c ->
        new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getEstado()
        ));

        col.setCellFactory(tc -> new TableCell<>() {

            private final ComboBox<TipoDePago> combo = new ComboBox<>();
            private final Label label = new Label();

            {
                combo.getItems().setAll(
        java.util.Arrays.stream(TipoDePago.values())
                .filter(tipo -> tipo != TipoDePago.DEUDA_PAGADA)
                .toList());
                combo.getStyleClass().add("combo-agregar");

                combo.valueProperty().addListener((obs, anterior, nuevo) -> {
                    if (Tabla.this.soloLectura) {
                        return;
                    }

                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Venta fila = getTableView().getItems().get(getIndex());
                        fila.setEstado(nuevo);

                        if (onCambiarEstado != null && fila.getIdVenta() != null) {
                            onCambiarEstado.accept(fila, nuevo);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(TipoDePago item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                if (Tabla.this.soloLectura) {
                    label.setText(item.name());
                    setGraphic(label);
                } else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });

        col.setPrefWidth(180);
        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> colObservaciones() {
        var col = new TableColumn<Venta, String>("Observaciones");
        col.setCellValueFactory(c ->
        new javafx.beans.property.SimpleStringProperty(
                c.getValue().getObservaciones()
        ));
        col.setCellFactory(tc -> new TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(16));
                setGraphic(text);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    text.setText(null);
                    setTooltip(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });
        col.setPrefWidth(179);
        col.setSortable(false);

        return col;
    }

    private String formatMoneda(BigDecimal v) {
        if (v == null) {
            return "$ 0,00";
        }
        return moneda.format(v);
    }

    public TableView<Venta> getTable() {
        return tabla;
    }

    public Node asNode() {
        return this;
    }

    public void setSoloLectura(boolean soloLectura) {
        this.soloLectura = soloLectura;

        // desactiva botón eliminar también
        btnEliminar.setVisible(!soloLectura);
        btnEliminar.setManaged(!soloLectura);
    }
}
