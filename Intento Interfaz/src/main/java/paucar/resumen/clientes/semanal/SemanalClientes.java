package paucar.resumen.clientes.semanal;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import paucar.service.VentasBackend;

public class SemanalClientes extends BorderPane {

    private final VentasBackend backend;
    private LocalDate inicioSemana;
    private LocalDate finSemana;
    private String clienteSeleccionado;

    // UI
    private ComboBox<String> comboCliente;
    private FilteredList<String> clientesFiltrados;

    private TablaSemanal tablaSemanal;
    private TablaSemanalDebe tablaDebe;

    private TableView<Map<String, Object>> tablaVentas;
    private TableView<Map<String, Object>> tablaVentasDebe;

    private Label lblTotal;

    public SemanalClientes(VentasBackend backend, LocalDate fecha) {
        this.backend = backend;
        this.inicioSemana = fecha.with(DayOfWeek.MONDAY);
        this.finSemana = fecha.with(DayOfWeek.SUNDAY);
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(10));
        cargarComboClientes();
        setTop(crearBarraSuperior());
        setCenter(crearVistaResumenSemanal());
    }

    private VBox crearVistaResumenSemanal() {
        VBox contenedor = new VBox(5);

        tablaSemanal = new TablaSemanal(backend);
        tablaDebe = new TablaSemanalDebe(backend);

        tablaVentas = tablaSemanal.getTabla();
        tablaVentasDebe = tablaDebe.getTabla();

        lblTotal = new Label("Total: $ 0");
        lblTotal.getStyleClass().add("lbl-total");

        HBox contenedorTotal = new HBox(lblTotal);
        contenedorTotal.setAlignment(Pos.CENTER_RIGHT);

        Label lblDebe = new Label("Deudas (cualquier fecha)");
        lblDebe.getStyleClass().add("lbl-debe");

        Button btnPagarDeudas = new Button("Pagar Deudas");
        btnPagarDeudas.setOnAction(e -> tablaDebe.mostrarVentanaPago());

        contenedor.getChildren().addAll(
                tablaVentas,
                contenedorTotal,
                lblDebe,
                btnPagarDeudas,
                tablaVentasDebe
        );

        VBox.setVgrow(tablaVentas, Priority.ALWAYS);
        VBox.setVgrow(tablaVentasDebe, Priority.ALWAYS);

        return contenedor;
    }

    private HBox crearBarraSuperior() {
        comboCliente.setPromptText("Seleccionar cliente");

        Label rangoSemana = new Label(
                "Semana: " + inicioSemana + " a " + finSemana);

        HBox barra = new HBox(15, comboCliente, rangoSemana);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(0, 0, 10, 0));

        return barra;
    }

    private void cargarComboClientes() {
        var clientes = backend.obtenerClientesPorTipo(TipoCliente.CLIENTE);

        clientesFiltrados = new FilteredList<>(
                javafx.collections.FXCollections.observableArrayList(clientes),
                s -> true
        );

        comboCliente = crearCombo(clientesFiltrados);

        comboCliente.setOnAction(e -> {
            clienteSeleccionado = comboCliente.getValue();
            cargarVentasSemanalesClientes();
        });
    }

    private void cargarVentasSemanalesClientes() {
        String cliente = comboCliente.getValue();

        if (cliente == null || cliente.isBlank()) {
            return;
        }

        double total = tablaSemanal.cargarSemanaCliente(cliente, inicioSemana, finSemana);

        tablaDebe.cargarDeudasCliente(cliente, inicioSemana.minusMonths(12));

        lblTotal.setText("Total: " + formatoDineroAR(total));
    }

    private ComboBox<String> crearCombo(FilteredList<String> datos) {
        ComboBox<String> cb = new ComboBox<>(datos);
        cb.setEditable(true);

        AtomicBoolean actualizar = new AtomicBoolean(false);

        cb.getEditor().textProperty().addListener((obs, a, newVal) -> {
            if (actualizar.get()) return;

            String txt = (newVal == null ? "" : newVal.trim().toLowerCase());

            datos.setPredicate(item ->
                    item != null && (txt.isEmpty() || item.toLowerCase().contains(txt)));

            if (!cb.isShowing() && !txt.isEmpty()) {
                cb.show();
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

            celda.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (!celda.isEmpty()) {
                    String item = celda.getItem();

                    actualizar.set(true);
                    try {
                        cb.getSelectionModel().select(item);
                        cb.setValue(item);
                        cb.getEditor().setText(item);
                        cb.getEditor().positionCaret(item.length());
                    } finally {
                        actualizar.set(false);
                    }

                    cb.hide();
                    e.consume();
                }
            });

            return celda;
        });

        return cb;
    }

    private String formatoDineroAR(double monto) {
        Locale localeAR = Locale.of("es", "AR");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(localeAR);
        DecimalFormat df = new DecimalFormat("$ #,##0.00", symbols);
        df.setRoundingMode(RoundingMode.UP);

        return df.format(monto);
    }

    public void actualizarFecha(LocalDate fecha) {
        this.inicioSemana = fecha.with(DayOfWeek.MONDAY);
        this.finSemana = fecha.with(DayOfWeek.SUNDAY);

        if (clienteSeleccionado != null && !clienteSeleccionado.isBlank()) {
            cargarVentasSemanalesClientes();
        }
    }
}