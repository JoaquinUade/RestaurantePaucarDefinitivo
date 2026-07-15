package paucar.resumen.clientes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import paucar.service.VentasBackend;
import paucar.shared.FechaUtils;
import paucar.shared.MonedaUtils;

public class MensualClientes extends BorderPane {

    private final VentasBackend backend;

    private final TableView<VentaResumenDiarioDTO> tabla = new TableView<>();
    private final ObservableList<VentaResumenDiarioDTO> datos = FXCollections.observableArrayList();
    private final BorderPane footerTotal = new BorderPane();

    private ComboBox<String> comboCliente;
    private String clienteSeleccionado;

    private final int anio;
    private final int mes;

    public MensualClientes(VentasBackend backend, int anio, int mes) {
        this.backend = backend;
        this.anio = anio;
        this.mes = mes;

        tabla.setItems(datos);

        cargarClientes();
        crearColumnas();

        javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox(comboCliente);
        topBar.setPadding(new javafx.geometry.Insets(10));
        topBar.setSpacing(10);

        setTop(topBar);
        setCenter(tabla);
        setBottom(footerTotal);

        footerTotal.getStyleClass().add("footer-total");
    }

    private void cargarClientes() {

        var clientes = backend.obtenerClientesPorTipo(TipoCliente.CLIENTE);

        javafx.collections.transformation.FilteredList<String> filtradas
                = new javafx.collections.transformation.FilteredList<>(
                        javafx.collections.FXCollections.observableArrayList(clientes),
                        s -> true);

        comboCliente = crearCombo(filtradas);

        comboCliente.setPromptText("Seleccionar cliente");

        if (clienteSeleccionado != null) {
            comboCliente.setValue(clienteSeleccionado);
            comboCliente.getEditor().setText(clienteSeleccionado);
        }

        comboCliente.setOnAction(e -> {
            clienteSeleccionado = comboCliente.getValue();
            cargarMes();
        });
    }

    private ComboBox<String> crearCombo(
            javafx.collections.transformation.FilteredList<String> filtradas) {

        ComboBox<String> cb = new ComboBox<>(filtradas);
        cb.setEditable(true);

        java.util.concurrent.atomic.AtomicBoolean updating = new java.util.concurrent.atomic.AtomicBoolean(false);

        cb.getEditor().textProperty().addListener((obs, old, txt) -> {

            if (updating.get()) {
                return;
            }

            String filtro = (txt == null ? "" : txt.trim().toLowerCase());

            filtradas.setPredicate(item
                    -> item != null && (filtro.isEmpty() || item.toLowerCase().contains(filtro)));

            if (!cb.isShowing() && !filtro.isEmpty()) {
                cb.show();
            }
        });

        cb.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });

        cb.setCellFactory(list -> {
            var celda = new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                }
            };

            celda.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {

                if (!celda.isEmpty()) {

                    String item = celda.getItem();

                    updating.set(true);
                    try {
                        cb.getSelectionModel().select(item);
                        cb.setValue(item);
                        cb.getEditor().setText(item);
                        cb.getEditor().positionCaret(item.length());
                    } finally {
                        updating.set(false);
                    }

                    cb.hide();
                    e.consume();
                }
            });

            return celda;
        });

        return cb;
    }

    private void crearColumnas() {

        tabla.getColumns().add(colFecha());

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tabla.getColumns().add(colMonto("V. Total", d -> d.getVentaTotal()));
        tabla.getColumns().add(colDebe());
        tabla.getColumns().add(colDeudaPagada("Deuda Pagada", d -> d.getDeudaPagada()));
        tabla.getColumns().add(colMonto("Débito", d -> d.getDebito()));
        tabla.getColumns().add(colMonto("Crédito", d -> d.getCredito()));
        tabla.getColumns().add(colMonto("Transferencia", d -> d.getTransferencia()));
        tabla.getColumns().add(colMonto("Mercado Pago", d -> d.getMercadoPago()));
        tabla.getColumns().add(colMonto("Efectivo", d -> d.getEfectivo()));
    }

    private TableColumn<VentaResumenDiarioDTO, LocalDate> colFecha() {

        TableColumn<VentaResumenDiarioDTO, LocalDate> col = new TableColumn<>("Fecha");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getFecha()));

        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);

                setAlignment(javafx.geometry.Pos.CENTER);

                if (empty) {
                    setText(null);
                } else if (fecha == null) {
                    setText("TOTAL MES");
                } else {
                    setText(FechaUtils.fechaMes(fecha));
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private TableColumn<VentaResumenDiarioDTO, BigDecimal> colMonto(
            String titulo,
            java.util.function.Function<VentaResumenDiarioDTO, BigDecimal> getter) {

        TableColumn<VentaResumenDiarioDTO, BigDecimal> col = new TableColumn<>(titulo);

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(getter.apply(c.getValue())));

        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                } else {
                    setText(MonedaUtils.formatearMoneda(v));
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private TableColumn<VentaResumenDiarioDTO, BigDecimal> colDebe() {

        TableColumn<VentaResumenDiarioDTO, BigDecimal> col = new TableColumn<>("Debe");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDebe()));

        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                    setTextFill(null);
                } else {
                    setText(MonedaUtils.formatearMoneda(v));

                    setTextFill(v.compareTo(BigDecimal.ZERO) > 0
                            ? javafx.scene.paint.Color.RED
                            : javafx.scene.paint.Color.BLACK);
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private TableColumn<VentaResumenDiarioDTO, BigDecimal> colDeudaPagada(
            String titulo,
            java.util.function.Function<VentaResumenDiarioDTO, BigDecimal> getter) {

        TableColumn<VentaResumenDiarioDTO, BigDecimal> col = new TableColumn<>(titulo);

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(getter.apply(c.getValue())));

        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                    setTextFill(null);
                } else {
                    setText(MonedaUtils.formatearMoneda(v));

                    setTextFill(v.compareTo(BigDecimal.ZERO) > 0
                            ? javafx.scene.paint.Color.GREEN
                            : javafx.scene.paint.Color.BLACK);
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private void RenderTotalMensual(VentaResumenDiarioDTO t) {

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();

        grid.setHgap(5);
        grid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        for (TableColumn<?, ?> columna : tabla.getColumns()) {

            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.prefWidthProperty().bind(columna.widthProperty());
            grid.getColumnConstraints().add(cc);
        }

        grid.add(new javafx.scene.control.Label("TOTAL MES"), 0, 0);
        String texto = MonedaUtils.formatearMoneda(t.getVentaTotal());
        grid.add(new javafx.scene.control.Label(texto), 1, 0);

        javafx.scene.control.Label totalDebe = new javafx.scene.control.Label(MonedaUtils.formatearMoneda(t.getDebe()));
        totalDebe.setTextFill(t.getDebe().compareTo(BigDecimal.ZERO) > 0
                ? javafx.scene.paint.Color.RED
                : javafx.scene.paint.Color.BLACK);
        grid.add(totalDebe, 2, 0);

        javafx.scene.control.Label totalPagado = new javafx.scene.control.Label(MonedaUtils.formatearMoneda(t.getDeudaPagada()));
        totalPagado.setTextFill(t.getDeudaPagada().compareTo(BigDecimal.ZERO) > 0
                ? javafx.scene.paint.Color.GREEN
                : javafx.scene.paint.Color.BLACK);
        grid.add(totalPagado, 3, 0);

        grid.add(new javafx.scene.control.Label(MonedaUtils.formatearMoneda(t.getDebito())), 4, 0);
        grid.add(new javafx.scene.control.Label(MonedaUtils.formatearMoneda(t.getCredito())), 5, 0);
        grid.add(new javafx.scene.control.Label(MonedaUtils.formatearMoneda(t.getTransferencia())), 6, 0);
        grid.add(new javafx.scene.control.Label(MonedaUtils.formatearMoneda(t.getMercadoPago())), 7, 0);
        grid.add(new javafx.scene.control.Label(MonedaUtils.formatearMoneda(t.getEfectivo())), 8, 0);

        footerTotal.setCenter(grid);
    }

    private void cargarMes() {

        datos.clear();

        if (clienteSeleccionado == null) {
            return;
        }

        LocalDate fecha = LocalDate.of(anio, mes, 1);

        while (fecha.getMonthValue() == mes) {

            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY
                    && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {

                VentaResumenDiarioDTO resumen = new VentaResumenDiarioDTO(fecha);

                var ventas = backend.cargarVentasDelDia(fecha);

                for (var v : ventas) {

                    if (v.get("tipoCliente") == TipoCliente.CLIENTE
                            && clienteSeleccionado.equals(v.get("nombre"))) {

                        BigDecimal monto = (BigDecimal) v.get("monto");
                        TipoDePago tipo = (TipoDePago) v.get("estado");

                        switch (tipo) {
                            case EFECTIVO ->
                                resumen.setEfectivo(resumen.getEfectivo().add(monto));
                            case DEBITO ->
                                resumen.setDebito(resumen.getDebito().add(monto));
                            case CREDITO ->
                                resumen.setCredito(resumen.getCredito().add(monto));
                            case TRANSFERENCIA ->
                                resumen.setTransferencia(resumen.getTransferencia().add(monto));
                            case MERCADO_PAGO ->
                                resumen.setMercadoPago(resumen.getMercadoPago().add(monto));
                            case DEBE ->
                                resumen.setDebe(resumen.getDebe().add(monto));
                            case DEUDA_PAGADA ->
                                resumen.setDeudaPagada(resumen.getDeudaPagada().add(monto));
                        }

                        if (tipo != TipoDePago.DEBE) {
                            resumen.setVentaTotal(resumen.getVentaTotal().add(monto));
                        }
                    }
                }

                datos.add(resumen);
            }

            fecha = fecha.plusDays(1);
        }
        VentaResumenDiarioDTO total = new VentaResumenDiarioDTO(null);

        for (VentaResumenDiarioDTO d : datos) {

            total.setVentaTotal(total.getVentaTotal().add(d.getVentaTotal()));
            total.setDebe(total.getDebe().add(d.getDebe()));
            total.setDeudaPagada(total.getDeudaPagada().add(d.getDeudaPagada()));
            total.setDebito(total.getDebito().add(d.getDebito()));
            total.setCredito(total.getCredito().add(d.getCredito()));
            total.setTransferencia(total.getTransferencia().add(d.getTransferencia()));
            total.setMercadoPago(total.getMercadoPago().add(d.getMercadoPago()));
            total.setEfectivo(total.getEfectivo().add(d.getEfectivo()));
        }

        RenderTotalMensual(total);
    }
}
