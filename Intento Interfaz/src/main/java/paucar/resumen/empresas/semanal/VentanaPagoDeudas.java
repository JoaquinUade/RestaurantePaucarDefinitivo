package paucar.resumen.empresas.semanal;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import com.uade.tpo.demo.entity.TipoDePago;

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
import paucar.service.VentasBackend;

public class VentanaPagoDeudas {

    private final VentasBackend backend;
    private static final Locale LOCALE_AR = Locale.of("es", "AR");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(LOCALE_AR);

    public VentanaPagoDeudas(VentasBackend backend) {
        this.backend = backend;
    }

    public void mostrar(TableView<Map<String, Object>> tabla, String empresaActual,
                       java.time.LocalDate desdeActual, Runnable refrescarTabla) {

        Stage ventana = new Stage();/*crea ventana emergente */
        ventana.setTitle("Pagar Deudas");

        VBox layout = new VBox(10);/*crea una caja vertical con espaciado de 10px entre
                                           objetos visuales*/

        layout.setPadding(new Insets(15));/*le agrega relleno de 15px alrededor */

        Label lblPass = new Label("Contraseña:");
        PasswordField txtPass = new PasswordField();/*Campo de texto para ingresar contraseña*/

        TableView<Map<String, Object>> tablaDeudas = new TableView<>();/*crea la tabladeudas*/

        TableColumn<Map<String, Object>, Boolean> colCheck = new TableColumn<>("Seleccionar");
        colCheck.setSortable(false);

        colCheck.setCellValueFactory(fila -> {
            Boolean seleccionado = (Boolean) fila.getValue().get("selected");/*Obtiene el valor
                                                                                  "selected" de la fila y 
                                                                                  lo guarda como Boolean*/
            if (seleccionado == null)
                seleccionado = false;

            SimpleObjectProperty<Boolean> prop = new SimpleObjectProperty<>(seleccionado);/*Crea una propiedad observable (SimpleObjectProperty) con
                                                                                          el valor seleccionado, permitiendo que JavaFX detecte
                                                                                          cambios y actualice la interfaz*/

            prop.addListener((obs, oldVal, newVal) ->
                    fila.getValue().put("selected", newVal));/*Cuando cambia la propiedad (por ejemplo
                                                                 el CheckBox), guarda el nuevo valor en
                                                                 "selected" dentro de la fila*/

            return prop;/*retorna prop*/
        });

        colCheck.setCellFactory(tc -> new TableCell<>() {/*Defino cómo se construyen y se muestran las
                                                         celdas de esta columna*/
            private final CheckBox checkBox = new CheckBox();
            {
             checkBox.setOnAction(e -> {/*si presionas la checkbox */
                 Map<String, Object> fila = getTableView().getItems().get(getIndex());/*obtiene la fila
                                                                                     a la que presionaste */
                 fila.put("selected", checkBox.isSelected());/*Guarda en la fila si el CheckBox
                                                                  está marcado o no*/
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, Object> fila = getTableView().getItems().get(getIndex());
                    Boolean seleccionado = (Boolean) fila.get("selected");

                    checkBox.setSelected(seleccionado != null && seleccionado);
                    setGraphic(checkBox);
                }
            }
        });

        tablaDeudas.getColumns().add(colCheck);

        // ✅ MONTO (IGUAL FORMATO AR)
        TableColumn<Map<String, Object>, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(fila -> {
            Number m = (Number) fila.getValue().get("monto");
            return new SimpleObjectProperty<>(MONEDA.format(m == null ? 0 : m.doubleValue()));
        });

        TableColumn<Map<String, Object>, String> colDesc = crearColumnaTexto
                    ("Descripción", "descripcion", 13);/*crea la descripcion con wrap*/

        TableColumn<Map<String, Object>, String> colObs =
                    new TableColumn<>("Observaciones");/*crea la columna observaciones */

        colObs.setCellValueFactory(fila ->
                new SimpleObjectProperty<>((String) fila.getValue().get("observaciones"))
        );

        colObs.setCellFactory(tc -> new TableCell<>() {

            private final TextField txtObservaciones = new TextField();
            {
                txtObservaciones.setOnAction(e -> guardar());/*si presionas el campo observaciones*/
                txtObservaciones.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal){/*Detecta cuándo el usuario deja de editar el campo*/

                        guardar();/*Guarda el nuevo contenido de observaciones*/
                    }
                });
            }

            private void guardar() {
                Map<String, Object> fila = getTableView().getItems().get(getIndex());
                fila.put("observaciones", txtObservaciones.getText());/*Guardá en la fila el texto que el
                                                                    usuario escribió en el campo de
                                                                    observaciones */
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    txtObservaciones.setText(item != null ? item : "");/*si el texto es null lo deja vacio*/
                    setGraphic(txtObservaciones);/*Muestra el TextField en la celda como componente editable,
                                          permitiendo visualizar y modificar el valor*/
                }
            }
        });

        colObs.setSortable(false);
        colMonto.setSortable(false);
        colDesc.setSortable(false);
        tablaDeudas.getColumns().add(colDesc);
        tablaDeudas.getColumns().add(colMonto);
        tablaDeudas.getColumns().add(colObs);

        tablaDeudas.setColumnResizePolicy
                    (TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);/*Define que las columnas se
                                                                           redimensionen para ocupar todo el
                                                                           ancho disponible */

        for (Map<String, Object> v : tabla.getItems()) {/*recorre las filas de la tabla */

            if (v.get("estado") == TipoDePago.DEBE) {/*si el tipo de pago es debe */

                tablaDeudas.getItems().add(v);/*la agrega a la tablaDeudas*/
            }
        }
        Button btnConfirmar = new Button("Confirmar Pago");

        btnConfirmar.setOnAction(e -> {/*si presiona el boton confirmar */
            String pass = txtPass.getText();/*obtiene lo que el usuario escribió en el campo de contraseña*/

            if (!"1234".equals(pass)) {/*si la contraseña es incorrecta */
                Alert alert = new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta");
                alert.show();
                return;
            }

            for (Map<String, Object> v : tablaDeudas.getItems()) {/*recorre todas las filas */

                Boolean seleccionado = (Boolean) v.get("selected");/*Guarda si la fila está
                                                                        seleccionada (true o false) */

                if (Boolean.TRUE.equals(seleccionado)) {/*Verifica si la fila está seleccionada */

                    Long idVenta = ((Number) v.get("idVenta")).longValue();/*Obtené el id de la venta
                                                                                de la fila y guardalo como
                                                                                un número tipo Long */

                    backend.actualizarEstadoVenta(idVenta, TipoDePago.DEUDA_PAGADA);/*Actualiza el estado de
                                                                                    la venta a DEUDA_PAGADA*/
                }
            }

            refrescarTabla.run();

            ventana.close();
        });

        layout.getChildren().addAll(lblPass, txtPass, tablaDeudas, btnConfirmar);/*añade a la caja vertical
                                                                                 todos los elementos*/

        Scene scene = new Scene(layout, 600, 400);/*le mete a la escena el contenido de vbox
                                                                 y le da tamaño*/

        ventana.setScene(scene);/*a la ventana le pasa la escena*/
        ventana.show();/*muestra la ventana */
    }

    private TableColumn<Map<String, Object>, String> crearColumnaTexto(
            String titulo, String key, int padding) {

        TableColumn<Map<String, Object>, String> col = new TableColumn<>(titulo);

        col.setCellValueFactory(fila ->
                new SimpleObjectProperty<>((String) fila.getValue().get(key)));/* define qué valor
                                                                               específico de la fila se
                                                                               muestra en esa columna*/

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
