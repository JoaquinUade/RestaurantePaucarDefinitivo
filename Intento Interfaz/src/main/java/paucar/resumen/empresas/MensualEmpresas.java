package paucar.resumen.empresas;

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

public class MensualEmpresas extends BorderPane {

    private final VentasBackend backend;

    private final TableView<VentaResumenDiarioDTO> tabla = new TableView<>();
    private final ObservableList<VentaResumenDiarioDTO> datos = FXCollections.observableArrayList();
    private final BorderPane footerTotal = new BorderPane();
    private ComboBox<String> comboEmpresa;
    private String empresaSeleccionada;

    private final int anio;
    private final int mes;

    public MensualEmpresas(VentasBackend backend, int anio, int mes) {
        this.backend = backend;
        this.anio = anio;
        this.mes = mes;

        tabla.setItems(datos);

        cargarEmpresas();
        crearColumnas();

        javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox(comboEmpresa);
        topBar.setPadding(new javafx.geometry.Insets(10)); // 🔥 espacio alrededor
        topBar.setSpacing(10); // (por si después agregás más cosas)

        setTop(topBar);
        setCenter(tabla);
        setBottom(footerTotal);

        footerTotal.getStyleClass().add("footer-total");
    }

    private void cargarEmpresas() {

        var empresas = backend.obtenerClientesPorTipo(TipoCliente.EMPRESA);

        javafx.collections.transformation.FilteredList<String> filtradas = new javafx.collections.transformation.FilteredList<>(
                javafx.collections.FXCollections.observableArrayList(empresas),
                s -> true);

        comboEmpresa = crearComboEmpresas(filtradas);

        comboEmpresa.setPromptText("Seleccionar empresa");

        if (empresaSeleccionada != null) {
            comboEmpresa.setValue(empresaSeleccionada);
            comboEmpresa.getEditor().setText(empresaSeleccionada);
        }

        comboEmpresa.setOnAction(e -> {
            empresaSeleccionada = comboEmpresa.getValue();
            cargarMes();
        });
    }

    private ComboBox<String> crearComboEmpresas(
            javafx.collections.transformation.FilteredList<String> empresasFiltradas) {

        ComboBox<String> cb = new ComboBox<>(empresasFiltradas);
        
        cb.setEditable(true);

        java.util.concurrent.atomic.AtomicBoolean updating = new java.util.concurrent.atomic.AtomicBoolean(false);

        cb.getEditor().textProperty().addListener((obs, old, txt) -> {

            if (updating.get())
                return;

            String filtro = (txt == null ? "" : txt.trim().toLowerCase());

            empresasFiltradas
                    .setPredicate(emp -> emp != null && (filtro.isEmpty() || emp.toLowerCase().contains(filtro)));

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

        col.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getFecha()));

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
                    setText(String.format(
                            "%02d-%s",
                            fecha.getDayOfMonth(),
                            fecha.getMonth().getDisplayName(
                                    java.time.format.TextStyle.FULL,
                                    java.util.Locale.of("es", "AR"))));
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

        col.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                getter.apply(c.getValue())));

        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                } else {
                    setText(java.text.NumberFormat
                            .getCurrencyInstance(java.util.Locale.of("es", "AR"))
                            .format(v));
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private TableColumn<VentaResumenDiarioDTO, BigDecimal> colDebe() {

        TableColumn<VentaResumenDiarioDTO, BigDecimal> col = new TableColumn<>("Debe");

        col.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getDebe()));

        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                    setTextFill(null);
                } else {
                    setText(java.text.NumberFormat
                            .getCurrencyInstance(java.util.Locale.of("es", "AR"))
                            .format(v));

                    if (v.compareTo(BigDecimal.ZERO) > 0) {
                        setTextFill(javafx.scene.paint.Color.RED);
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
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

        col.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                getter.apply(c.getValue())));

        col.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                    setTextFill(null);
                } else {
                    setText(java.text.NumberFormat
                            .getCurrencyInstance(java.util.Locale.of("es", "AR"))
                            .format(v));

                    if (v.compareTo(BigDecimal.ZERO) > 0) {
                        setTextFill(javafx.scene.paint.Color.GREEN);
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private String formato(BigDecimal v) {
        return java.text.NumberFormat
                .getCurrencyInstance(java.util.Locale.of("es", "AR"))
                .format(v);
    }

    private void RenderTotalMensual(VentaResumenDiarioDTO t) {

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();

        // ✅ ajustes visuales
        grid.setHgap(5);
        grid.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        for (TableColumn<?, ?> columna : tabla.getColumns()) {

            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            cc.prefWidthProperty().bind(columna.widthProperty());
            grid.getColumnConstraints().add(cc);
        }

        grid.add(new javafx.scene.control.Label("TOTAL MES"), 0, 0);
        grid.add(new javafx.scene.control.Label(formato(t.getVentaTotal())), 1, 0);

        javafx.scene.control.Label totalDebe = new javafx.scene.control.Label(formato(t.getDebe()));
        totalDebe.setTextFill(t.getDebe().compareTo(BigDecimal.ZERO) > 0
                ? javafx.scene.paint.Color.RED
                : javafx.scene.paint.Color.BLACK);
        grid.add(totalDebe, 2, 0);

        javafx.scene.control.Label totalPagado = new javafx.scene.control.Label(formato(t.getDeudaPagada()));
        totalPagado.setTextFill(t.getDeudaPagada().compareTo(BigDecimal.ZERO) > 0
                ? javafx.scene.paint.Color.GREEN
                : javafx.scene.paint.Color.BLACK);
        grid.add(totalPagado, 3, 0);

        grid.add(new javafx.scene.control.Label(formato(t.getDebito())), 4, 0);
        grid.add(new javafx.scene.control.Label(formato(t.getCredito())), 5, 0);
        grid.add(new javafx.scene.control.Label(formato(t.getTransferencia())), 6, 0);
        grid.add(new javafx.scene.control.Label(formato(t.getMercadoPago())), 7, 0);
        grid.add(new javafx.scene.control.Label(formato(t.getEfectivo())), 8, 0);

        footerTotal.setCenter(grid);
    }

    private void cargarMes() {
        datos.clear();

        if (empresaSeleccionada == null)
            return;

        LocalDate fecha = LocalDate.of(anio, mes, 1);

        while (fecha.getMonthValue() == mes) {

            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {

                VentaResumenDiarioDTO resumen = new VentaResumenDiarioDTO(fecha);

                var ventas = backend.cargarVentasDelDia(fecha);

                for (var v : ventas) {

                    if (v.get("tipoCliente") == TipoCliente.EMPRESA &&
                            empresaSeleccionada.equals(v.get("nombre"))) {

                        BigDecimal monto = (BigDecimal) v.get("monto");
                        TipoDePago tipo = (TipoDePago) v.get("estado");

                        switch (tipo) {
                            case EFECTIVO -> resumen.setEfectivo(resumen.getEfectivo().add(monto));
                            case DEBITO -> resumen.setDebito(resumen.getDebito().add(monto));
                            case CREDITO -> resumen.setCredito(resumen.getCredito().add(monto));
                            case TRANSFERENCIA -> resumen.setTransferencia(resumen.getTransferencia().add(monto));
                            case MERCADO_PAGO -> resumen.setMercadoPago(resumen.getMercadoPago().add(monto));
                            case DEBE -> resumen.setDebe(resumen.getDebe().add(monto));
                            case DEUDA_PAGADA -> resumen.setDeudaPagada(resumen.getDeudaPagada().add(monto));
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
