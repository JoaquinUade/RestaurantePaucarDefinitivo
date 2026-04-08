package paucar.resumen.empresas;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import paucar.service.VentasBackend;

public class SemanalEmpresas extends BorderPane {

    private final VentasBackend backend;

    private final LocalDate inicioSemana;
    private final LocalDate finSemana;

    // UI
    private ComboBox<String> comboEmpresa;
    private FilteredList<String> empresasFiltradas;

    private SemanalTablaEmpresas tablaSemanal;
    private SemanalDebeEmpresas tablaDebe;

    private TableView<Map<String, Object>> tablaVentas;
    private TableView<Map<String, Object>> tablaVentasDebe;

    private Label lblTotal;

    public SemanalEmpresas(VentasBackend backend, LocalDate fecha) {
        this.backend = backend;

        this.inicioSemana = fecha.with(DayOfWeek.MONDAY);
        this.finSemana = fecha.with(DayOfWeek.SUNDAY);

        inicializarUI();
    }

    private void inicializarUI() {
        setPadding(new Insets(10));

        cargarEmpresas();
        setTop(crearBarraSuperior());
        setCenter(crearCentro());
        setBottom(crearPie());
    }

    private VBox crearCentro() {

        VBox contenedor = new VBox(15);

        tablaSemanal = new SemanalTablaEmpresas(backend);
        tablaDebe = new SemanalDebeEmpresas(backend);

        tablaVentas = tablaSemanal.getTabla();
        tablaVentasDebe = tablaDebe.getTabla();

        Label lblDebe = new Label("Deudas (cualquier fecha)");
        lblDebe.setStyle("-fx-font-weight: bold");

        contenedor.getChildren().addAll(
                tablaVentas,
                lblDebe,
                tablaVentasDebe
        );

        VBox.setVgrow(tablaVentas, Priority.ALWAYS);
        VBox.setVgrow(tablaVentasDebe, Priority.ALWAYS);

        return contenedor;
    }

    private HBox crearBarraSuperior() {
        comboEmpresa.setPromptText("Seleccionar empresa");

        Label rangoSemana = new Label(
                "Semana: " + inicioSemana + " a " + finSemana
        );

        HBox barra = new HBox(15, comboEmpresa, rangoSemana);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(0, 0, 10, 0));

        return barra;
    }

    private HBox crearPie() {
        lblTotal = new Label("Total: $ 0");

        HBox pie = new HBox(lblTotal);
        pie.setAlignment(Pos.CENTER_RIGHT);
        pie.setPadding(new Insets(10, 0, 0, 0));

        return pie;
    }

    private void cargarEmpresas() {

        var empresas = backend.obtenerClientesPorTipo(TipoCliente.EMPRESA);

        empresasFiltradas = new FilteredList<>(
                javafx.collections.FXCollections.observableArrayList(empresas),
                s -> true
        );

        comboEmpresa = crearComboEmpresas(empresasFiltradas);

        comboEmpresa.setOnAction(e -> cargarVentasEmpresaSemana());
    }

private void cargarVentasEmpresaSemana() {

    String empresa = comboEmpresa.getValue();
    if (empresa == null || empresa.isBlank()) {
        return;
    }

    double total = tablaSemanal.cargarSemanaEmpresa(
            empresa,
            inicioSemana,
            finSemana
    );

    tablaDebe.cargarDeudasEmpresa(
            empresa,
            inicioSemana.minusMonths(12)
    );

    lblTotal.setText("Total: $ " + String.format("%.2f", total));
}

    private ComboBox<String> crearComboEmpresas(FilteredList<String> empresasFiltradas) {

        ComboBox<String> cbEmpresa = new ComboBox<>(empresasFiltradas);
        cbEmpresa.setEditable(true);
        cbEmpresa.setPromptText("Empresa");

        AtomicBoolean actualizandoEditor = new AtomicBoolean(false);

        // 1) Filtrado mientras escribe
        cbEmpresa.getEditor().textProperty().addListener((obs, a, b) -> {

            if (actualizandoEditor.get()) {
                return;
            }

            String txt = (b == null ? "" : b.trim().toLowerCase());

            empresasFiltradas.setPredicate(s
                    -> s != null && (txt.isEmpty() || s.toLowerCase().contains(txt))
            );

            if (!cbEmpresa.isShowing() && !txt.isEmpty()) {
                cbEmpresa.show();
            }
        });

        // 2) Selección segura (por ítem, no por índice)
        cbEmpresa.setCellFactory(list -> {
            var cell = new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
                if (!cell.isEmpty()) {

                    String item = cell.getItem();
                    actualizandoEditor.set(true);
                    try {
                        cbEmpresa.getSelectionModel().select(item);
                        cbEmpresa.setValue(item);
                        cbEmpresa.getEditor().setText(item);
                        cbEmpresa.getEditor().positionCaret(item.length());
                    } finally {
                        actualizandoEditor.set(false);
                    }

                    cbEmpresa.hide();
                    ev.consume();
                }
            });
            return cell;
        });

        cbEmpresa.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });

        return cbEmpresa;
    }
}
