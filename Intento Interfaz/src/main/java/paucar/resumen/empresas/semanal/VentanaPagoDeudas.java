package paucar.resumen.empresas.semanal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import paucar.security.PasswordManager;
import paucar.service.VentasBackend;
import paucar.shared.MonedaUtils;

public class VentanaPagoDeudas {

    private final VentasBackend backend;
    private final Map<Long, Boolean> seleccionados
            = new java.util.HashMap<>();

    private final Map<Long, DatosAdmin> datosAdmin
            = new java.util.HashMap<>();

    public VentanaPagoDeudas(VentasBackend backend) {
        this.backend = backend;
    }

    public void mostrar(TableView<Venta> tabla, String empresaActual,
            java.time.LocalDate desdeActual, Runnable refrescarTabla) {

        Stage ventana = new Stage();/*crea ventana emergente */
        ventana.setTitle("Pagar Deudas");

        VBox layout = new VBox(10);/*crea una caja vertical con espaciado de 10px entre
                                           objetos visuales*/

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

        TableView<Venta> tablaDeudas = new TableView<>();/*crea la tabladeudas*/

        TableColumn<Venta, Boolean> colCheck = new TableColumn<>("Seleccionar");
        colCheck.setSortable(false);

        colCheck.setCellValueFactory(fila -> {
            Boolean seleccionado
                    = seleccionados.getOrDefault(
                            fila.getValue().getIdVenta(),
                            false
                    );
            if (seleccionado == null) {
                seleccionado = false;
            }

            SimpleObjectProperty<Boolean> prop = new SimpleObjectProperty<>(seleccionado);/*Crea una propiedad observable (SimpleObjectProperty) con
                                                                                          el valor seleccionado, permitiendo que JavaFX detecte
                                                                                          cambios y actualice la interfaz*/

            prop.addListener((obs, oldVal, newVal)
                    -> seleccionados.put(
                            fila.getValue().getIdVenta(),
                            newVal
                    ));

            return prop;/*retorna prop*/
        });

        colCheck.setCellFactory(tc -> new TableCell<>() {/*Defino cómo se construyen y se muestran las
                                                         celdas de esta columna*/
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {/*si presionas la checkbox */
                    Venta fila = getTableView().getItems().get(getIndex());

                    seleccionados.put(
                            fila.getIdVenta(),
                            checkBox.isSelected()
                    );
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Venta fila
                            = getTableView().getItems().get(getIndex());

                    Boolean seleccionado
                            = seleccionados.getOrDefault(
                                    fila.getIdVenta(),
                                    false
                            );

                    checkBox.setSelected(seleccionado != null && seleccionado);
                    setGraphic(checkBox);
                }
            }
        });

        tablaDeudas.getColumns().add(colCheck);

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
        tablaDeudas.getColumns().add(colFecha);
        tablaDeudas.getColumns().add(colDesc);
        tablaDeudas.getColumns().add(colMonto);
        tablaDeudas.getColumns().add(colMontoIva);
        tablaDeudas.getColumns().add(colEstado);

        tablaDeudas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);/*Define que las columnas se
                                                                           redimensionen para ocupar todo el
                                                                           ancho disponible */

        for (Venta v : tabla.getItems()) {/*recorre las filas de la tabla */

            if (v.getEstado() == TipoDePago.DEBE) {/*si el tipo de pago es debe */

                tablaDeudas.getItems().add(v);/*la agrega a la tablaDeudas*/

                datosAdmin.put(v.getIdVenta(),
                        new DatosAdmin(
                                v.getConsumidor(),
                                null,
                                null,
                                v.getObservaciones()));
            }
        }
        Button btnConfirmar = new Button("Confirmar Pago");

        btnConfirmar.setOnAction(e -> {/*si presiona el boton confirmar */
            if (!PasswordManager.verificar(txtPass.getText())) {
                Alert alert = new Alert(
                        Alert.AlertType.ERROR,
                        "Contraseña incorrecta"
                );
                alert.show();
                return;
            }

            List<Long> idsSeleccionadas = new ArrayList<>();
            DatosAdmin datosPago = null;

            for (Venta v : tablaDeudas.getItems()) {/*recorre todas las filas */

                Boolean seleccionado
                        = seleccionados.getOrDefault(
                                v.getIdVenta(),
                                false
                        );

                if (Boolean.TRUE.equals(seleccionado)) {/*Verifica si la fila está seleccionada */

                    idsSeleccionadas.add(v.getIdVenta());

                    DatosAdmin da = datosAdmin.get(v.getIdVenta());
                    if (da != null && datosPago == null) {
                        datosPago = da;
                    }
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

        layout.getChildren().addAll(
        lblPass,
        txtPass,

        lblNombre,
        txtNombre,

        lblCuit,
        txtCuit,

        lblFactura,
        txtFactura,

        lblObs,
        txtObs,

        tablaDeudas,
        btnConfirmar
);

        Scene scene = new Scene(layout, 1180, 500);/*le mete a la escena el contenido de vbox
                                                                 y le da tamaño*/

        ventana.setScene(scene);/*a la ventana le pasa la escena*/
        ventana.show();/*muestra la ventana */
    }

    private TableColumn<Venta, String> crearColumnaEditable(
            String titulo,
            Function<DatosAdmin, String> lector,
            BiConsumer<DatosAdmin, String> aplicador) {

        TableColumn<Venta, String> col = new TableColumn<>(titulo);
        col.setSortable(false);

        col.setCellValueFactory(fila -> {
            DatosAdmin da = datosAdmin.computeIfAbsent(
                    fila.getValue().getIdVenta(),
                    k -> new DatosAdmin(null, null, null, null));
            return new SimpleObjectProperty<>(lector.apply(da));
        });

        col.setCellFactory(tc -> new TableCell<>() {

            private final TextField campo = new TextField();

            {
                campo.setPrefWidth(110);/*Ancho razonable para editar el dato*/
                campo.setOnAction(e -> guardar());/*si presionas Enter en el campo*/
                campo.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {/*Detecta cuándo el usuario deja de editar el campo*/

                        guardar();/*Guarda el nuevo valor editado*/
                    }
                });
            }

            private void guardar() {
                if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    return;
                }
                Venta fila = getTableView().getItems().get(getIndex());
                DatosAdmin da = datosAdmin.computeIfAbsent(
                        fila.getIdVenta(),
                        k -> new DatosAdmin(null, null, null, null));
                aplicador.accept(da, campo.getText() == null ? "" : campo.getText());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    campo.setText(item != null ? item : "");/*si el texto es null lo deja vacio*/
                    setGraphic(campo);
                }
            }
        });

        return col;
    }

    /**
     * Datos administrativos editables que se guardan en el PagoParcial al
     * confirmar.
     */
    private static class DatosAdmin {

        private String consumidor;
        private String cuit;
        private String factura;
        private String observaciones;

        DatosAdmin(String consumidor, String cuit, String factura, String observaciones) {
            this.consumidor = consumidor;
            this.cuit = cuit;
            this.factura = factura;
            this.observaciones = observaciones;
        }

        public String getConsumidor() {
            return consumidor;
        }

        public void setConsumidor(String consumidor) {
            this.consumidor = consumidor;
        }

        public String getCuit() {
            return cuit;
        }

        public void setCuit(String cuit) {
            this.cuit = cuit;
        }

        public String getFactura() {
            return factura;
        }

        public void setFactura(String factura) {
            this.factura = factura;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }
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

                setPrefHeight(Region.USE_COMPUTED_SIZE);/*Calculá automáticamente tu altura según el
                                                        contenido que tengas */
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
}
