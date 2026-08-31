package paucar.resumen.empresas.semanal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.Venta;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import paucar.security.PasswordManager;
import paucar.security.SesionPassword;
import paucar.service.VentasBackend;
import paucar.shared.MonedaUtils;

public class VentanaPagoDeudas {

    private final VentasBackend backend;
    private final Map<Long, Boolean> seleccionados
            = new java.util.HashMap<>();

    public VentanaPagoDeudas(VentasBackend backend) {
        this.backend = backend;
    }

    public void mostrar(TableView<Venta> tabla, String empresaActual,
            java.time.LocalDate desdeActual, Runnable refrescarTabla) {

        Stage ventana = new Stage();/*crea ventana emergente */
        ventana.setTitle("Pagar Deudas");

        HBox layout = new HBox(15);
        

        layout.setPadding(new Insets(15));/*le agrega relleno de 15px alrededor */

        Label lblPass = new Label("Contraseña:");
        PasswordField txtPass = new PasswordField();/*Campo de texto para ingresar contraseña*/

        Label lblNombre = new Label("Nombre");
        TextField txtNombre = new TextField();

        Label lblCuit = new Label("CUIT");
        TextField txtCuit = new TextField();

        Label lblFactura = new Label("Factura");
        TextField txtFactura = new TextField();

        Label lblObs = new Label("Observaciones");
        TextField txtObs = new TextField();

        TableView<Venta> tablaSemana1 = new TableView<>();
        TableView<Venta> tablaSemana2 = new TableView<>();
        TableView<Venta> tablaSemana3 = new TableView<>();
        TableView<Venta> tablaSemana4 = new TableView<>();

        tablaSemana1.getColumns().add(crearColumnaCheck());
        tablaSemana2.getColumns().add(crearColumnaCheck());
        tablaSemana3.getColumns().add(crearColumnaCheck());
        tablaSemana4.getColumns().add(crearColumnaCheck());
        tablaSemana1.setPrefHeight(90);
        tablaSemana2.setPrefHeight(90);
        tablaSemana3.setPrefHeight(90);
        tablaSemana4.setPrefHeight(90);

        // ✅ FECHA
        TableColumn<Venta, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(fila -> {
            var f = fila.getValue().getFecha();
            String txt = (f == null) ? "" : f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return new SimpleObjectProperty<>(txt);
        });
        colFecha.setSortable(false);

        // ✅ DESCRIPCIÓN (con wrap)
        TableColumn<Venta, String> colDesc = crearColumnaTexto("Descripción", "descripcion", 12);

        // ✅ MONTO
        TableColumn<Venta, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(fila -> {
            Number m = fila.getValue().getMonto();
            return new SimpleObjectProperty<>(MonedaUtils.formatearMoneda(m));
        });
        colMonto.setSortable(false);

        // ✅ MONTO CON IVA (21%)
        TableColumn<Venta, String> colMontoIva = new TableColumn<>("Monto con IVA");
        colMontoIva.setCellValueFactory(fila -> {
            BigDecimal m = fila.getValue().getMonto();
            BigDecimal iva = (m == null) ? null
                    : m.multiply(new BigDecimal("1.21"))
                            .setScale(2, RoundingMode.HALF_UP);
            return new SimpleObjectProperty<>(MonedaUtils.formatearMoneda(iva));
        });
        colMontoIva.setSortable(false);

        // ✅ ESTADO
        TableColumn<Venta, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(fila
                -> new SimpleObjectProperty<>(fila.getValue().getEstado() == null
                        ? "" : fila.getValue().getEstado().name()));
        colEstado.setSortable(false);
        colDesc.setSortable(false);
        configurarTabla(
                tablaSemana1,
                colFecha,
                colDesc,
                colMonto,
                colMontoIva,
                colEstado);

        configurarTabla(
                tablaSemana2,
                crearColFecha(),
                crearColDescripcion(),
                crearColMonto(),
                crearColMontoIva(),
                crearColEstado());

        configurarTabla(
                tablaSemana3,
                crearColFecha(),
                crearColDescripcion(),
                crearColMonto(),
                crearColMontoIva(),
                crearColEstado());

        configurarTabla(
                tablaSemana4,
                crearColFecha(),
                crearColDescripcion(),
                crearColMonto(),
                crearColMontoIva(),
                crearColEstado());

        tablaSemana1.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tablaSemana2.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tablaSemana3.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tablaSemana4.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        for (Venta v : tabla.getItems()) {/*recorre las filas de la tabla */

            if (v.getEstado() == TipoDePago.DEBE) {/*si el tipo de pago es debe */

                int dia = v.getFecha().getDayOfMonth();

                if (dia <= 7) {
                    tablaSemana1.getItems().add(v);
                } else if (dia <= 14) {
                    tablaSemana2.getItems().add(v);
                } else if (dia <= 21) {
                    tablaSemana3.getItems().add(v);
                } else {
                    tablaSemana4.getItems().add(v);
                }

            }
        }
        Label lblSemana1 = new Label("Semana 1 (1-7)");
        Label lblSemana2 = new Label("Semana 2 (8-14)");
        Label lblSemana3 = new Label("Semana 3 (15-21)");
        Label lblSemana4 = new Label("Semana 4 (22-fin)");
        Button btnConfirmar = new Button("Confirmar Pago");
        VBox panelInputs = new VBox(10);

        if (!SesionPassword.estaAutorizado()) {
            panelInputs.getChildren().addAll(lblPass, txtPass);
        }
        panelInputs.getChildren().addAll(
                lblNombre,
                txtNombre,
                lblCuit,
                txtCuit,
                lblFactura,
                txtFactura,
                lblObs,
                txtObs,
                btnConfirmar
        );

        panelInputs.setPrefWidth(250);

        VBox panelTablas = new VBox(10);

        panelTablas.getChildren().addAll(
                lblSemana1,
                tablaSemana1,
                lblSemana2,
                tablaSemana2,
                lblSemana3,
                tablaSemana3,
                lblSemana4,
                tablaSemana4
        );
        btnConfirmar.setOnAction(e -> {/*si presiona el boton confirmar */
            if (!PasswordManager.verificarConSesion(txtPass.getText())) {
                Alert alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Contraseña incorrecta"
                );
                alert.show();
                return;
            }

            List<Long> idsSeleccionadas = new ArrayList<>();

            List<Venta> todasLasVentas = new ArrayList<>();

            todasLasVentas.addAll(tablaSemana1.getItems());
            todasLasVentas.addAll(tablaSemana2.getItems());
            todasLasVentas.addAll(tablaSemana3.getItems());
            todasLasVentas.addAll(tablaSemana4.getItems());

            for (Venta v : todasLasVentas) {

                Boolean seleccionado
                        = seleccionados.getOrDefault(
                                v.getIdVenta(),
                                false
                        );

                if (Boolean.TRUE.equals(seleccionado)) {/*Verifica si la fila está seleccionada */

                    idsSeleccionadas.add(v.getIdVenta());
                }
            }

            if (idsSeleccionadas.isEmpty()) {
                Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        "Seleccioná al menos una venta para pagar"
                );
                alert.show();
                return;
            }

            String nombre = txtNombre.getText().trim();
            String cuit = txtCuit.getText().trim();
            String factura = txtFactura.getText().trim();
            String obs = txtObs.getText().trim();

            if (nombre.isBlank()) {
                nombre = null;
            }

            if (cuit.isBlank()) {
                cuit = null;
            }

            if (factura.isBlank()) {
                factura = null;
            }

            if (obs.isBlank()) {
                obs = null;
            }

            backend.registrarPagoParcial(idsSeleccionadas, nombre, cuit, factura, obs);

            refrescarTabla.run();

            ventana.close();
        });
        ScrollPane scrollTablas = new ScrollPane(panelTablas);
        HBox.setHgrow(scrollTablas, javafx.scene.layout.Priority.ALWAYS);
        scrollTablas.setFitToHeight(true);
        scrollTablas.setFitToWidth(true);

        layout.getChildren().addAll(
                panelInputs,
                scrollTablas
        );

        Scene scene = new Scene(layout, 1180, 500);/*le mete a la escena el contenido de vbox
                                                                 y le da tamaño*/

        ventana.setScene(scene);/*a la ventana le pasa la escena*/
        ventana.show();/*muestra la ventana */
    }

    private TableColumn<Venta, String> crearColumnaTexto(
            String titulo, String key, int padding) {

        TableColumn<Venta, String> col = new TableColumn<>(titulo);

        col.setCellValueFactory(fila -> {

            Venta v = fila.getValue();

            String valor = switch (key) {
                case "descripcion" ->
                    v.getDescripcion();
                case "observaciones" ->
                    v.getObservaciones();
                default ->
                    "";
            };

            return new SimpleObjectProperty<>(valor);
        });

        col.setCellFactory(columna -> new TableCell<>() {/*Por cada columna, devolveme una nueva celda
                                                         (TableCell) */

            private final Text text = new Text();

            {
                text.wrappingWidthProperty()
                        .bind(columna.widthProperty().subtract(padding));/*Hacé que el ancho máximo del texto
                                                                    sea igual al ancho menos margen, y que
                                                                    se actualice si cambia de tamaño*/

                setGraphic(text);/*Usá este objeto visual (Text) como contenido de la celda */


            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    text.setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    text.setText(item);/*Carga el texto */
                    setGraphic(text);/*Lo muestra en la celda */
                    setTooltip(new Tooltip(item));/*Muestra texto completo al pasar el mouse */
                }
            }
        });

        return col;/*retorna la columna*/
    }

    private void configurarTabla(
            TableView<Venta> tabla,
            TableColumn<Venta, String> colFecha,
            TableColumn<Venta, String> colDesc,
            TableColumn<Venta, String> colMonto,
            TableColumn<Venta, String> colMontoIva,
            TableColumn<Venta, String> colEstado) {

        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colDesc);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colMontoIva);
        tabla.getColumns().add(colEstado);
    }

    private TableColumn<Venta, String> crearColFecha() {

        TableColumn<Venta, String> col = new TableColumn<>("Fecha");

        col.setCellValueFactory(fila -> {
            var f = fila.getValue().getFecha();

            String txt = (f == null)
                    ? ""
                    : f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            return new SimpleObjectProperty<>(txt);
        });

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> crearColDescripcion() {
        return crearColumnaTexto(
                "Descripción",
                "descripcion",
                12
        );
    }

    private TableColumn<Venta, String> crearColMonto() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Monto");

        col.setCellValueFactory(fila -> {

            Number m = fila.getValue().getMonto();

            return new SimpleObjectProperty<>(
                    MonedaUtils.formatearMoneda(m));
        });

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> crearColMontoIva() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Monto con IVA");

        col.setCellValueFactory(fila -> {

            BigDecimal m = fila.getValue().getMonto();

            BigDecimal iva = (m == null)
                    ? null
                    : m.multiply(
                            new BigDecimal("1.21"))
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP);

            return new SimpleObjectProperty<>(
                    MonedaUtils.formatearMoneda(iva));
        });

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> crearColEstado() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Estado");

        col.setCellValueFactory(fila
                -> new SimpleObjectProperty<>(
                        fila.getValue().getEstado() == null
                        ? ""
                        : fila.getValue().getEstado().name()));

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, Boolean> crearColumnaCheck() {

        TableColumn<Venta, Boolean> colCheck
                = new TableColumn<>("Seleccionar");

        colCheck.setSortable(false);

        colCheck.setCellValueFactory(fila -> {

            Boolean seleccionado
                    = seleccionados.getOrDefault(
                            fila.getValue().getIdVenta(),
                            false);

            SimpleObjectProperty<Boolean> prop
                    = new SimpleObjectProperty<>(seleccionado);

            prop.addListener((obs, oldVal, newVal)
                    -> seleccionados.put(
                            fila.getValue().getIdVenta(),
                            newVal));

            return prop;
        });

        colCheck.setCellFactory(tc -> new TableCell<>() {

            private final CheckBox checkBox
                    = new CheckBox();

            {
                checkBox.setOnAction(e -> {

                    Venta fila
                            = getTableView()
                                    .getItems()
                                    .get(getIndex());

                    seleccionados.put(
                            fila.getIdVenta(),
                            checkBox.isSelected());
                });
            }

            @Override
            protected void updateItem(
                    Boolean item,
                    boolean empty) {

                super.updateItem(item, empty);

                if (empty) {

                    setGraphic(null);

                } else {

                    Venta fila
                            = getTableView()
                                    .getItems()
                                    .get(getIndex());

                    boolean seleccionado
                            = seleccionados.getOrDefault(
                                    fila.getIdVenta(),
                                    false);

                    checkBox.setSelected(seleccionado);

                    setGraphic(checkBox);
                }
            }
        });

        return colCheck;
    }
}
