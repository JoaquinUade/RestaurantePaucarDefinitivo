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

    private TablaSemanalCliente tablaSemanal;
    private TablaSemanalDebeCliente tablaDebe;

    private TableView<Map<String, Object>> tablaVentas;
    private TableView<Map<String, Object>> tablaVentasDebe;

    private Label lblTotal;

    public SemanalClientes(VentasBackend backend, LocalDate fecha) {
        this.backend = backend;
        this.inicioSemana = fecha.with(DayOfWeek.MONDAY);/*convierte cualquier fecha en el lunes de esa
                                                          semana y lo guardo en variable iniciosemana */
        this.finSemana = fecha.with(DayOfWeek.SUNDAY);
        initUI();
    }

    private void initUI() {
        setPadding(new Insets(10));/*relleno de 10px*/
        cargarComboClientes();
        setTop(crearBarraSuperior());
        setCenter(crearVistaResumenSemanal());
    }

    private VBox crearVistaResumenSemanal() {
        VBox contenedor = new VBox(5);/*crea una caja vertical llamada contenedor */

        tablaSemanal = new TablaSemanalCliente(backend);
        tablaDebe = new TablaSemanalDebeCliente(backend);

        tablaVentas = tablaSemanal.getTabla();/*obtiene la tabla ventas */
        tablaVentasDebe = tablaDebe.getTabla();/*obtiene la tabla de deudas */

        lblTotal = new Label("Total: $ 0");
        lblTotal.getStyleClass().add("lbl-total");

        HBox contenedorTotal = new HBox(lblTotal);/*en el contenedor total le pone el lbl total*/

        contenedorTotal.setAlignment(Pos.CENTER_RIGHT);/*lo centra verticalmente, y lo pone a la derecha */

        Label lblDebe = new Label("Deudas (cualquier fecha)");/*le pone titulo a la tabla */
        lblDebe.getStyleClass().add("lbl-debe");/*lo stylea */

        Button btnPagarDeudas = new Button("Pagar Deudas");
        btnPagarDeudas.setOnAction(e -> tablaDebe.mostrarVentanaPago());/*hace que si se presiona el btn
                                                                        pagar deudas, se ejecute ese metodo*/

        contenedor.getChildren().addAll(tablaVentas, contenedorTotal, lblDebe, btnPagarDeudas,
                                        tablaVentasDebe);

        VBox.setVgrow(tablaVentas, Priority.ALWAYS);/*tablaventas toma todo el espacio posible*/
        VBox.setVgrow(tablaVentasDebe, Priority.ALWAYS);

        return contenedor;
    }

    private HBox crearBarraSuperior() {
        comboCliente.setPromptText("Seleccionar cliente");

        Label rangoSemana = new Label("Semana: " + inicioSemana + " a " + finSemana);/*formatea el rango
                                                                                     que abarca esa semana */

        HBox barra = new HBox(15, comboCliente, rangoSemana);/*mete en la caja horizontal */
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(0, 0, 10, 0));

        return barra;
    }

    private void cargarComboClientes() {
        var clientes = backend.obtenerClientesPorTipo(TipoCliente.CLIENTE);

        clientesFiltrados = new FilteredList<>(javafx.collections.FXCollections.observableArrayList(clientes),
                s -> true);/*Crea una lista observable de clientes con filtro inicial que permite
                           todos los elementos*/

        comboCliente = crearCombo(clientesFiltrados);

        comboCliente.setOnAction(e -> {/*si presiona en combocliente*/
            clienteSeleccionado = comboCliente.getValue();/*obtiene el cliente elegido */
            cargarVentasSemanalClientes();/*carga las ventas de ese cliente */
        });
    }

    private void cargarVentasSemanalClientes() {
        String cliente = comboCliente.getValue();/* */

        if (cliente == null || cliente.isBlank()) {
            return;
        }

        double total = tablaSemanal
            .cargarSemanaCliente(cliente, inicioSemana, finSemana);/*Carga las ventas semanales del
                                                                    cliente y devuelve el total*/

        tablaDebe.cargarDeudasCliente
                (cliente, inicioSemana.minusMonths(12));/*Carga las deudas del cliente
                                                                        desde hace un año hasta la fecha
                                                                        actual */

        lblTotal.setText("Total: " + formatoDineroAR(total));
    }

    private ComboBox<String> crearCombo(FilteredList<String> datos) {
        ComboBox<String> cb = new ComboBox<>(datos);/*Crea un ComboBox con la lista de clientes*/
        cb.setEditable(true);

        AtomicBoolean actualizar = new AtomicBoolean(false);/*Bandera para saber si el cambio
                                                                          lo hace el usuario o el programa */

        cb.getEditor().textProperty().addListener((obs, a, newVal) -> {

            if (actualizar.get()) return;/*si el cambio fue hecho por el programa y no por
                                          el usuario salgo*/

            String txt = (newVal == null ? "" : newVal.trim().toLowerCase());/*Si el valor es null usa
                                                                             vacío, sino elimina espacios
                                                                             y convierte a minúsculas*/
            datos.setPredicate(item -> item != null &&
                (txt.isEmpty() || item.toLowerCase().contains(txt)));/*Filtra los clientes mostrando solo
                                                                  los que coinciden con el texto ingresado */

            if (!cb.isShowing() && !txt.isEmpty()) {/*Si el ComboBox está cerrado Y el usuario
                                                    escribió algo */
                cb.show();/*se abre*/
            }
        });

        cb.setCellFactory(list -> {
            var celda = new javafx.scene.control.ListCell<String>() {/*Crea una celda personalizada para
                                                                    definir cómo se muestra cada cliente
                                                                    en el ComboBox */
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item);
                }
            };

            celda.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {/*Detecta cuando se hace clic en una
                                                                  celda del ComboBox */
                if (!celda.isEmpty()) {
                    String item = celda.getItem();/*Obtiene el valor (cliente) de la celda seleccionada */

                    actualizar.set(true);
                    try {
                        cb.getSelectionModel().select(item);/*Selecciona el elemento en el ComboBox */

                        cb.setValue(item);/*Establece el valor seleccionado para que se muestre en
                                           el ComboBox*/
                        cb.getEditor().setText(item);/*Actualiza el texto visible en el campo editable
                                                      del ComboBox */
                        cb.getEditor().positionCaret(item.length());/*Coloca el cursor al final del texto
                                                                    en el ComboBox */
                    } finally {
                        actualizar.set(false);
                    }

                    cb.hide();/*Cierra el ComboBox */
                    e.consume();/*Evita que el evento siga propagándose (ya fue manejado manualmente) */
                }
            });

            return celda;
        });

        return cb;
    }

    private String formatoDineroAR(double monto) {
        Locale localeAR = Locale.of("es", "AR");

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(localeAR);
        DecimalFormat df = new DecimalFormat("$ #,##0.00", symbols);/*Formatea números como dinero
                                                                             en pesos con separador de
                                                                             miles y 2 decimales */

        df.setRoundingMode(RoundingMode.UP);/*Configura el redondeo para sea hacia arriba */
        return df.format(monto);
    }

    public void actualizarFecha(LocalDate fecha) {
        this.inicioSemana = fecha.with(DayOfWeek.MONDAY);
        this.finSemana = fecha.with(DayOfWeek.SUNDAY);

        if (clienteSeleccionado != null && !clienteSeleccionado.isBlank()) {/*si cliente seleccionado no 
                                                                            es null y no esta en blanco*/
            cargarVentasSemanalClientes();/*carga las ventas de ese cliente*/
        }
    }
}