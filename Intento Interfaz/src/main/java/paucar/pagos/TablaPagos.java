package paucar.pagos;

import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.EstadoPago;
import com.uade.tpo.demo.entity.PagoEmpresa;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import paucar.service.PagosService;
import paucar.shared.MonedaUtils;

public class TablaPagos extends VBox {

    private final TableView<PagoEmpresa> tabla;
    private final Runnable onEstadoCambiado;

    public TablaPagos(
            Consumer<PagoEmpresa> onSelect,
            Runnable onEstadoCambiado,
            PagosService service) {

        this.onEstadoCambiado = onEstadoCambiado;
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        

        // Empresa
        TableColumn<PagoEmpresa, String> colEmpresa
                = new TableColumn<>("Empresa");

        colEmpresa.setCellValueFactory(c
                -> new SimpleStringProperty(c.getValue().getNombre()));
        colEmpresa.setCellFactory(tc -> new TableCell<>() {

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

        // CUIT
        TableColumn<PagoEmpresa, String> colCuit
                = new TableColumn<>("CUIT");
        colCuit.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getCuit() == null
                        ? "" : c.getValue().getCuit()));
        colCuit.setCellFactory(tc -> new TableCell<>() {

            private final javafx.scene.control.Button btn
                    = new javafx.scene.control.Button("Completar");

            {
                btn.setOnAction(e -> {

                    PagoEmpresa pago = getTableRow().getItem();

                    if (pago != null && onSelect != null) {
                        onSelect.accept(pago);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                PagoEmpresa pago = getTableRow().getItem();

                if (pago == null
                        || pago.getCuit() == null
                        || pago.getCuit().isBlank()) {

                    setText(null);
                    setGraphic(btn);

                } else {

                    setGraphic(null);
                    setText(pago.getCuit());
                }
            }
        });

        // Fecha
        TableColumn<PagoEmpresa, String> colFecha
                = new TableColumn<>("Fecha");

        colFecha.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getFecha() == null ? ""
                        : c.getValue().getFecha().toLocalDate().toString()));

        // N° de pago
        TableColumn<PagoEmpresa, String> colNum
                = new TableColumn<>("N° Pago");

        colNum.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getNumeroPago() == null ? ""
                        : String.valueOf(c.getValue().getNumeroPago())));

        // Monto
        TableColumn<PagoEmpresa, String> colMonto
                = new TableColumn<>("Monto");

        colMonto.setCellValueFactory(c
                -> new SimpleStringProperty(
                        MonedaUtils.formatearMoneda(c.getValue().getMonto())));

        // Monto con IVA
        TableColumn<PagoEmpresa, String> colMontoIva
                = new TableColumn<>("21%+");

        colMontoIva.setCellValueFactory(c
                -> new SimpleStringProperty(
                        MonedaUtils.formatearMoneda(
                                c.getValue().getMontoConIva())));

        // Factura
        TableColumn<PagoEmpresa, String> colFactura
                = new TableColumn<>("Factura");

        colFactura.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getFactura() == null
                        ? "" : c.getValue().getFactura()));

        // Estado
        TableColumn<PagoEmpresa, EstadoPago> colEstado
                = new TableColumn<>("Estado");

        colEstado.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        c.getValue().getEstado()));

        colEstado.setCellFactory(tc -> new TableCell<>() {

            private final ComboBox<EstadoPago> combo
                    = new ComboBox<>(
                            FXCollections.observableArrayList(
                                    EstadoPago.values()));

            {
                combo.setOnAction(e -> {

                    PagoEmpresa pago = getTableRow().getItem();

                    if (pago != null) {

                        pago.setEstado(combo.getValue());

                        service.modificar(pago.getId(), pago);

                        if (onEstadoCambiado != null) {
                            onEstadoCambiado.run();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(
                    EstadoPago estado,
                    boolean empty) {

                super.updateItem(estado, empty);

                if (empty || estado == null) {

                    setGraphic(null);

                } else {

                    combo.setValue(estado);

                    setGraphic(combo);
                }
            }
        });
        // Observación
        TableColumn<PagoEmpresa, String> colObs
                = new TableColumn<>("Observación");

        colObs.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue().getObservacion() == null
                        ? "" : c.getValue().getObservacion()));

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
        colCuit.setSortable(false);
        colFecha.setSortable(false);
        colNum.setSortable(false);
        colMonto.setSortable(false);
        colMontoIva.setSortable(false);
        colFactura.setSortable(false);
        colEstado.setSortable(false);
        colObs.setSortable(false);
        tabla.getColumns().add(
                colEmpresa);
        tabla.getColumns().add(colCuit);
        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colNum);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colMontoIva);
        tabla.getColumns().add(colFactura);
        tabla.getColumns().add(colEstado);
        tabla.getColumns().add(colObs);

        tabla.setItems(FXCollections.observableArrayList());

        getChildren().add(tabla);
    }

    public void setPagos(List<PagoEmpresa> pagos) {

        tabla.setItems(
                FXCollections.observableArrayList(pagos));

        ajustarAltura();
    }

    private void ajustarAltura() {

        int filas = tabla.getItems().size();

        double alturaCabecera = 30;
        double alturaFila = 28;

        tabla.setPrefHeight(
                alturaCabecera
                + (filas * alturaFila)
                + 55);
    }

    public PagoEmpresa getSeleccionado() {
        return tabla.getSelectionModel().getSelectedItem();
    }
}
