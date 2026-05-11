package paucar.resumen.empresas;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import com.uade.tpo.demo.entity.TipoCliente;
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

public class TablaSemanalDebe {

    private final VentasBackend backend;/*declara un campo backend que es una instancia de VentasBackend,
                                         se usará para cargar las ventas del día y filtrar las deudas de
                                         empresas*/

    private final TableView<Map<String, Object>> tabla;/*crea una tabla para mostrar las deudas semanales
                                                        de empresas*/

    private static final Locale LOCALE_AR = Locale.of("es", "AR");/*Define un locale específico para español de
                                                                                    Argentina*/

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("d MMMM yyyy", LOCALE_AR);/*Define un formato de fecha específico para
                                                                                                                          mostrar las fechas en español de Argentina*/

    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(LOCALE_AR);/*Define un formato de moneda específico
                                                                                            para mostrar los valores en español de
                                                                                            Argentina*/

    public TablaSemanalDebe(VentasBackend backend) {
        this.backend = backend;
        this.tabla = new TableView<>();
        definirColumnas();
    }

    public TableView<Map<String, Object>> getTabla() {
        return tabla;/* Devuelve la tabla creada para mostrar las deudas semanales de empresas */
    }

    private void definirColumnas() {

        TableColumn<Map<String, Object>, String> colFecha = new TableColumn<>("Fecha");/*Crea una columna llamada “Fecha”
                                                                                             para una TableView donde cada fila
                                                                                             es un Map<String, Object> y cada
                                                                                             celda muestra un String y la guarda
                                                                                             en la variable colFecha*/

        colFecha.setCellValueFactory(fila -> {/* por cada fila de colFecha, haremos lo siguiente */

            LocalDate fecha = (LocalDate) fila.getValue().get("fecha");/*Obtiene la fecha de la fila
                                                                            actual*/

            return new SimpleObjectProperty<>(fecha == null ? "" : fecha.format(FECHA_FORMATO));/*devuelve la fecha, si la fecha es null se
                                                                                                 deja vacía sino muestra formateada la fecha*/
        });

        TableColumn<Map<String, Object>, String> colDescripcion = crearColumnaTexto("Descripción", "descripcion",
                13);/*Crea una columna llamada colDescripcion usando un método que arma columnas
                            de texto, y la configura para mostrar la descripción de cada fila*/

        TableColumn<Map<String, Object>, String> colMonto = new TableColumn<>("Monto");/*Crea una columna llamada colMonto*/

        colMonto.setCellValueFactory(fila -> {/*por cada fila de la columna se hace el siguiente
                                               bloque de codigo*/

            Number m = (Number) fila.getValue().get("monto");/* Obtiene el monto de la fila actual */

            return new SimpleObjectProperty<>(MONEDA.format(m == null ? 0 : m.doubleValue()));/*si el monto es null muestra
                                                                                              cero, sino muestra el monto
                                                                                              formateado a moneda*/
        });

        TableColumn<Map<String, Object>, String> colTipo = new TableColumn<>("Tipo de pago");/*Crea una columna llamada
                                                                                                   colTipo*/
        colTipo.setCellValueFactory(fila -> {
            TipoDePago estado = (TipoDePago) fila.getValue().get("estado");

            String texto;

            if (estado == TipoDePago.DEUDA_PAGADA) {
                texto = "DEUDA PAGADA";
            } else {
                texto = "PENDIENTE";
            }
            return new SimpleObjectProperty<>(texto);
        });

        TableColumn<Map<String, Object>, String> colObs = crearColumnaTexto
                ("Observaciones", "observaciones", 16);/*Crea una columna de la tabla llamada
                                                                           “Observaciones” que muestra texto tomado
                                                                           de la clave "observaciones" de cada fila,
                                                                           usando un padding de 16 píxeles*/

        colFecha.setSortable(false);/*le quita a todas las filas el ordenamiento sort, porque no
                                           corresponde*/
        colDescripcion.setSortable(false);
        colMonto.setSortable(false);
        colTipo.setSortable(false);
        colObs.setSortable(false);

        tabla.getColumns().add(colFecha);/* agrega cada una de las columnas, en el orden deseado */
        tabla.getColumns().add(colDescripcion);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colTipo);
        tabla.getColumns().add(colObs);

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);/*Le indica a la tabla que las
                                                                                          columnas se ajusten automáticamente
                                                                                          al ancho disponible, y que la
                                                                                          última columna sea la más flexible*/
    }

    private TableColumn<Map<String, Object>, String> crearColumnaTexto(
            String titulo, String key, int padding) {

        TableColumn<Map<String, Object>, String> col = new TableColumn<>(titulo);/*Crea una nueva columna de una tabla,
                                                                                  la guarda en la variable col y le
                                                                                  pone como título el texto recibido en
                                                                                  titulo*/

        col.setCellValueFactory(fila -> new SimpleObjectProperty<>((String) fila.getValue().get(key)));/*por cada fila de la columna,
                                                                                                         obtiene el valor asociado a la
                                                                                                         clave key de esa fila, lo convierte
                                                                                                         a String y lo muestra en la celda*/

        col.setCellFactory(columna -> new TableCell<>() {/*Para cada celda de esta columna, usá este tipo de
                                                         celda personalizada*/

            private final Text text = new Text();/* crea un nodo de texto para mostrar el contenido de la celda */
            {
                text.wrappingWidthProperty().bind(columna.widthProperty().subtract(padding));/*Hace que el texto en la celda se ajuste al ancho
                                                                                               de la columna, dejando un margen (padding) y
                                                                                               ajustándose cuando la columna cambia de tamaño*/

                setGraphic(text);/* Usa el objeto Text como contenido visual de la celda */
                setPrefHeight(Region.USE_COMPUTED_SIZE);/*Permite que la altura de la celda se ajuste
                                                         automáticamente según el tamaño del texto*/
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);/*Cada vez que se actualiza el contenido de la celda, se
                                                ejecuta este método*/

                if (empty || item == null || item.isBlank()) {/*si la celda esta vacia o es null o esta
                                                                en blanco*/
                    text.setText(null);/* Establece el texto de la celda como null */
                    setGraphic(null);/* Establece el gráfico de la celda como null */
                    setTooltip(null);/* Establece el tooltip de la celda como null */

                } else {/* si si tiene contenido */

                    text.setText(item);/* Establece el texto de la celda como el valor recibido */

                    setGraphic(text);/* Establece el gráfico de la celda como el texto */

                    setTooltip(new Tooltip(item));/*Establece el tooltip de la celda como el valor
                                                    recibido*/
                }
            }
        });
        return col;/* Devuelve la columna creada */
    }

    public void cargarDeudasEmpresa(String empresa, LocalDate desde) {

        tabla.getItems().clear();/* Limpia los elementos de la tabla */
        LocalDate hoy = LocalDate.now();/* Obtiene la fecha actual */

        while (!desde.isAfter(hoy)) {/* Mientras la fecha desde no sea posterior a la fecha actual */

            var ventas = backend.cargarVentasDelDia(desde);/* Carga las ventas del día */

            for (Map<String, Object> v : ventas) {/* recorremos las ventas */

                if (v.get("tipoCliente") == TipoCliente.EMPRESA/* si el tipo de cliente es empresa */
                        && empresa.equals(v.get("nombre"))/* y el nombre coincide */
                        && (v.get("estado") == TipoDePago.DEBE
                                || v.get("estado") == TipoDePago.DEUDA_PAGADA)) {/* y ademas el estado es debe */

                    v.put("fecha", desde);/* Asigna la fecha a cada venta */
                    tabla.getItems().add(v);/* Agrega la venta a la tabla */
                }
            }
            desde = desde.plusDays(1);/* Avanza al siguiente día */
        }
    }

    public void mostrarVentanaPago() {
        Stage ventana = new Stage();/* crea una ventana emergente y la guardo en la variable ventana */
        ventana.setTitle("Pagar Deudas");/* le pongo como titulo a la ventana */

        VBox layout = new VBox(10);/*creamos una caja vertical llamada layout con 10px de separacion
                                           entre cada elemento*/
        layout.setPadding(new Insets(15));/* le da un relleno alrededor de 15px */

        Label lblPass = new Label("Contraseña:");/* creamos un texto visual label llamado contraseña */
        PasswordField txtPass = new PasswordField();/* un campo de texto donde introducir creamos contraseña */

        TableView<Map<String, Object>> tablaDeudas = new TableView<>();/* creamos una tabla deudas */

        TableColumn<Map<String, Object>, Boolean> colCheck = new TableColumn<>("Seleccionar");/*crea la columna seleccionar*/
        colCheck.setSortable(false);
        colCheck.setCellValueFactory(fila -> {
            Boolean seleccionado = (Boolean) fila.getValue().get("selected");/*Obtiene el valor "selected"
                                                                                 de la fila y lo guarda como un
                                                                                 Boolean (true/false)*/

            if (seleccionado == null)
                seleccionado = false;/*si seleccionado es null entonces no hay nada seleccionado(false)*/

            SimpleObjectProperty<Boolean> prop = new SimpleObjectProperty<>(seleccionado);/*Crear una propiedad llamada prop que guarda un valor
                                                                                            booleano (true/false) y que puede detectar cambios*/

            prop.addListener((obs, oldVal, newVal) -> {/* cuando cambies, ejecutá este código */
                fila.getValue().put("selected", newVal);/* Guarda en la fila si está seleccionada */
            });

            return prop;/* retorna la propiedad booleana prop */
        });
        colCheck.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();/* crea una casilla para marcar */

            {
                checkBox.setOnAction(e -> {/* si presionas la checkbox ose la casilla para marcar */

                    Map<String, Object> fila = getTableView().getItems().get(getIndex());/*Obtiene la fila actual de la
                                                                                           tabla*/

                    fila.put("selected", checkBox.isSelected());/* Guarda en la fila si el checkbox está marcado */
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {/* si la celda esta vacia */
                    setGraphic(null);/* no mostrar nada */
                } else {
                    Map<String, Object> fila = getTableView().getItems().get(getIndex());/*Obtiene los datos de la
                                                                                          fila actual de la tabla*/

                    Boolean seleccionado = (Boolean) fila.get("selected");/*Obtiene si la fila está
                                                                              seleccionada (true/false)*/

                    checkBox.setSelected(seleccionado != null && seleccionado);/*Marca el checkbox según el estado
                                                                                de la fila (evita null)*/

                    setGraphic(checkBox);/* Coloca el checkbox como contenido visual dentro de la celda */
                }
            }
        });
        tablaDeudas.getColumns().add(colCheck);/* Agrega la columna colCheck a la tabla deudas */
        colCheck.setSortable(false);
        TableColumn<Map<String, Object>, String> colMonto = new TableColumn<>("Monto");

        colMonto.setCellValueFactory(fila -> {
            Number m = (Number) fila.getValue().get("monto");
            return new SimpleObjectProperty<>(MONEDA.format(m == null ? 0 : m.doubleValue()));
        });

        TableColumn<Map<String, Object>, String> colDesc = crearColumnaTexto("Descripción", "descripcion", 13);
        colDesc.setSortable(false);

        
TableColumn<Map<String, Object>, String> colObs = new TableColumn<>("Observaciones");

colObs.setCellValueFactory(fila ->
    new SimpleObjectProperty<>((String) fila.getValue().get("observaciones"))
);

colObs.setCellFactory(tc -> new TableCell<>() {

    private final TextField textField = new TextField();

    {
        textField.setOnAction(e -> guardar());
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) guardar(); // cuando pierde foco
        });
    }

    private void guardar() {
        Map<String, Object> fila = getTableView().getItems().get(getIndex());
        fila.put("observaciones", textField.getText());
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            textField.setText(item != null ? item : "");
            setGraphic(textField);
        }
    }
});

        colObs.setSortable(false);
        colMonto.setSortable(false);

        tablaDeudas.getColumns().addAll(colDesc, colMonto, colObs);
tablaDeudas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        for (Map<String, Object> v : tabla.getItems()) {/* Recorre todas las filas de la tabla una por una */
            if (v.get("estado") == TipoDePago.DEBE) {/* si el estado es debe */
                tablaDeudas.getItems().add(v);/* Agrega esta fila a la tabla de deudas */
            }
        }

        Button btnConfirmar = new Button("Confirmar Pago");/* crea el boton para confirmar el pago */

        btnConfirmar.setOnAction(e -> {/* si se presiona el btnconfirmar */

            String pass = txtPass.getText();/*Obtiene el texto que escribió el usuario en el campo de
                                              contraseña y lo guarda en pass*/

            // ⚠️ Cambiá esto por tu contraseña real
            if (!"1234".equals(pass)) {/* si la contraseña es incorrecta */
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Contraseña incorrecta");/*crea alerta de error de contraseña
                                                              incorrecta*/
                alert.show();/* muestra la alerta */
                return;/* sale */
            }

            for (Map<String, Object> v : tablaDeudas.getItems()) {/*Recorre todas las filas de la tabla
                                                                    tablaDeudas*/

                Boolean seleccionado = (Boolean) v.get("selected");/* Obtiene si la fila está seleccionada */

                if (Boolean.TRUE.equals(seleccionado)) {/*Solo entro si está en true, y me aseguro de que no
                                                          haya error si está vacío*/
                    Long idVenta = ((Number) v.get("idVenta")).longValue();/*Obtiene el valor "idVenta" de
                                                                               la fila, lo convierte a número y
                                                                               después a tipo Long*/

                    backend.actualizarEstadoVenta(idVenta, TipoDePago.DEUDA_PAGADA);/*Cambia el estado de la
                                                                                      venta a pagada*/

                    v.put("estado", TipoDePago.DEUDA_PAGADA);/*Actualiza el estado de la fila v a
                                                               “DEUDA_PAGADA”*/
                }
            }

            tabla.refresh();/* Actualiza visualmente la tabla para reflejar los cambios realizados */
            ventana.close();/* Cierra la ventana actual */
        });

        layout.getChildren().addAll(lblPass, txtPass, tablaDeudas, btnConfirmar);/*Agrega varios elementos(texto,
                                                                                   campo, tabla y
                                                                                   botón) al layout para que se
                                                                                   muestren en la ventana*/

        Scene scene = new Scene(layout, 600, 400);/*Crea una escena (pantalla) usando el layout y
                                                                define su tamaño (600x400)*/

        ventana.setScene(scene);/* Asigna la escena a la ventana para mostrar el contenido */

        ventana.show();/* Muestra la ventana en pantalla */
    }
}
