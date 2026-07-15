package paucar.gastos.Fijos;

import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.GastosFijos;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import paucar.shared.MonedaUtils;

public class TablaMensualFijos extends VBox {

    public TablaMensualFijos(List<GastosFijos> gastos,
            Consumer<GastosFijos> onSelect,
            boolean esPersonal) {

        TableView<GastosFijos> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // ✅ DETALLE (antes nombre)
        TableColumn<GastosFijos, String> colDetalle;

        if (esPersonal) {
            colDetalle = new TableColumn<>("Empleado");
        } else {
            colDetalle = new TableColumn<>("Detalle");
        }

        colDetalle.setCellValueFactory(c
                -> new SimpleStringProperty(c.getValue().getDetalle()));
        colDetalle.setCellFactory(tc -> new TableCell<>() {

            private final javafx.scene.text.Text text
                    = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty()
                        .bind(tc.widthProperty().subtract(16));

                setGraphic(text);
            }

            @Override
            protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    text.setText(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
            }
        });
        TableColumn<GastosFijos, String> colMonto
                = new TableColumn<>("Monto");

        colMonto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        MonedaUtils.formatearMoneda(c.getValue().getMonto())
                )
        );
        // ✅ ESTADO (Boolean → texto)
        TableColumn<GastosFijos, Boolean> colEstado
                = new TableColumn<>("Estado");

        colEstado.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        c.getValue().getEstado()
                )
        );

        colEstado.setCellFactory(tc -> new TableCell<>() {

            private final ComboBox<String> combo = new ComboBox<>();

            {
                combo.getItems().addAll("Pendiente", "Pagado");

                combo.valueProperty().addListener((obs, anterior, nuevo) -> {

                    if (getIndex() >= 0
                            && getIndex() < getTableView().getItems().size()) {

                        GastosFijos gasto
                                = getTableView().getItems().get(getIndex());

                        gasto.setEstado("Pagado".equals(nuevo));

                        // Acá luego podrás llamar a tu service
                        // service.actualizarEstado(...)
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                combo.setValue(
                        Boolean.TRUE.equals(item)
                        ? "Pagado"
                        : "Pendiente"
                );

                setGraphic(combo);
            }
        });

        TableColumn<GastosFijos, String> colObs = null;

        if (!esPersonal) {
            colObs = new TableColumn<>("Observación");

            colObs.setCellValueFactory(c
                    -> new SimpleStringProperty(
                            c.getValue().getObservacion() == null
                            ? ""
                            : c.getValue().getObservacion()
                    )
            );

            colObs.setCellFactory(tc -> new TableCell<>() {

                private final javafx.scene.text.Text text
                        = new javafx.scene.text.Text();

                {
                    text.wrappingWidthProperty()
                            .bind(tc.widthProperty().subtract(16));

                    setGraphic(text);
                }

                @Override
                protected void updateItem(String item, boolean empty) {

                    super.updateItem(item, empty);

                    if (empty || item == null || item.isBlank()) {
                        text.setText(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                    }
                }
            });

            colObs.setOnEditCommit(e -> {
                GastosFijos g = e.getRowValue();
                g.setObservacion(e.getNewValue());
            });
        }
        tabla.setPrefHeight(460);
        // ✅ TABLA EDITABLE
        tabla.setEditable(true);
        // ✅ AGREGAR COLUMNAS
        tabla.getColumns().add(colDetalle);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colEstado);

        if (colObs != null) {
            tabla.getColumns().add(colObs);
        }

        // ✅ CARGAR DATOS
        tabla.setItems(FXCollections.observableArrayList(gastos));

        // ✅ SELECCIÓN
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                onSelect.accept(newSel);
            }
        });

        // ✅ AGREGAR TABLA AL PANEL
        getChildren().add(tabla);
    }
}
