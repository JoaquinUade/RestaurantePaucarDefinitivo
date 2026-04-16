package paucar.resumen.empresas;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import paucar.service.VentasBackend;

public class TablaSemanalDebe {

    private final VentasBackend backend;/*declara un campo backend que es una instancia de VentasBackend,
                                        se usará para cargar las ventas del día y filtrar las deudas de
                                        empresas*/
                                        
    private final TableView<Map<String, Object>> tabla;/*crea una tabla para mostrar las deudas semanales
                                                        de empresas*/

    private static final Locale LOCALE_AR = Locale.of("es", "AR");/*Define un locale específico para español de Argentina*/

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
        return tabla;/*Devuelve la tabla creada para mostrar las deudas semanales de empresas */
    }

    private void definirColumnas() {

        TableColumn<Map<String, Object>, String> colFecha = new TableColumn<>("Fecha");/*Crea una columna llamada “Fecha” para una TableView
                                                                                            donde cada fila es un Map<String, Object> y cada celda
                                                                                            muestra un String y la guarda en la variable colFecha*/

        colFecha.setCellValueFactory(fila -> {/*por cada fila de colFecha, haremos lo siguiente*/

            LocalDate fecha = (LocalDate) fila.getValue().get("fecha");/*Obtiene la fecha de la fila
                                                                            actual*/

            return new SimpleObjectProperty<>(fecha == null ? "" : fecha.format(FECHA_FORMATO));/*devuelve la fecha, si la fecha es null se
                                                                                                deja vacía sino muestra formateada la fecha */
        });

        TableColumn<Map<String, Object>, String> colDescripcion =
        crearColumnaTexto("Descripción", "descripcion", 13);/*Crea una columna llamada colDescripcion
                                                                                usando un método que arma columnas de texto,
                                                                                y la configura para mostrar la descripción de
                                                                                cada fila*/

        TableColumn<Map<String, Object>, String> colMonto = new TableColumn<>("Monto");/*Crea una columna llamada colMonto*/

        colMonto.setCellValueFactory(fila -> {/*por cada fila de la columna se hace el siguiente
                                               bloque de codigo */

            Number m = (Number) fila.getValue().get("monto");/*Obtiene el monto de la fila actual*/

            return new SimpleObjectProperty<>(MONEDA.format(m == null ? 0 : m.doubleValue()));/*si el monto es null muestra cero,
                                                                                              sino muestra el monto formateado a
                                                                                              moneda*/
        });

        TableColumn<Map<String, Object>, String> colTipo
                = new TableColumn<>("Tipo de pago");/*Crea una columna llamada colTipo*/
        colTipo.setCellValueFactory(c -> new SimpleObjectProperty<>("DEBE"));/*por cada fila de colTipo, se muestra la palabra “DEBE”*/

        TableColumn<Map<String, Object>, String> colObs
                = crearColumnaTexto("Observaciones", "observaciones", 16);/*Crea una columna de la tabla llamada “Observaciones”
                                                                                              que muestra texto tomado de la clave "observaciones"
                                                                                              de cada fila, usando un padding de 16 píxeles */

        colFecha.setSortable(false);/*le quita a todas las filas el ordenamiento sort, porque
                                           no corresponde */
        colDescripcion.setSortable(false);
        colMonto.setSortable(false);
        colTipo.setSortable(false);
        colObs.setSortable(false);

        tabla.getColumns().add(colFecha);/*agrega cada una de las columnas, en el orden deseado */
        tabla.getColumns().add(colDescripcion);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colTipo);
        tabla.getColumns().add(colObs);
        
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);/*Le indica a la tabla que las columnas se
                                                                                          ajusten automáticamente al ancho disponible,
                                                                                          y que la última columna sea la más flexible */
    }

    private TableColumn<Map<String, Object>, String> crearColumnaTexto(
            String titulo, String key, int padding) {

        TableColumn<Map<String, Object>, String> col = new TableColumn<>(titulo);/* Crea una nueva columna de una tabla, la guarda en la
                                                                                 variable col y le pone como título el texto recibido en
                                                                                 titulo*/

        col.setCellValueFactory(fila-> new SimpleObjectProperty<>((String) fila.getValue().get(key)));/*por cada fila de la columna, obtiene el valor
                                                                                                      asociado a la clave key de esa fila, lo convierte
                                                                                                      a String y lo muestra en la celda*/

        col.setCellFactory(columna -> new TableCell<>() {/*Para cada celda de esta columna, usá este tipo de celda personalizada*/

            private final Text text = new Text();/*crea un nodo de texto para mostrar el contenido de la celda*/
            {
                text.wrappingWidthProperty().bind(columna.widthProperty().subtract(padding));/*Hace que el texto en la celda se ajuste al ancho
                                                                                        de la columna, dejando un margen (padding) y
                                                                                        ajustándose cuando la columna cambia de tamaño*/

                setGraphic(text);/*Usa el objeto Text como contenido visual de la celda*/
                setPrefHeight(Region.USE_COMPUTED_SIZE);/*Permite que la altura de la celda se ajuste
                                                        automáticamente según el tamaño del texto*/
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);/*Cada vez que se actualiza el contenido de la celda, se
                                              ejecuta este método*/

                if (empty || item == null || item.isBlank()) {/*si la celda esta vacia o es null o esta
                                                               en blanco*/
                    text.setText(null);/*Establece el texto de la celda como null*/
                    setGraphic(null);/*Establece el gráfico de la celda como null*/
                    setTooltip(null);/*Establece el tooltip de la celda como null*/

                } else {/*si si tiene contenido*/

                    text.setText(item);/*Establece el texto de la celda como el valor recibido*/
                    setGraphic(text);/*Establece el gráfico de la celda como el texto*/
                    setTooltip(new Tooltip(item));/*Establece el tooltip de la celda como el valor
                                                   recibido*/
                }
            }
        });
        return col;/*Devuelve la columna creada*/
    }

    public void cargarDeudasEmpresa(String empresa, LocalDate desde) {

        tabla.getItems().clear();/* Limpia los elementos de la tabla */
        LocalDate hoy = LocalDate.now();/* Obtiene la fecha actual */

        while (!desde.isAfter(hoy)) {/* Mientras la fecha desde no sea posterior a la fecha actual */

            var ventas = backend.cargarVentasDelDia(desde);/* Carga las ventas del día */

            for (Map<String, Object> v : ventas) {/*recorremos las ventas*/

                if (v.get("tipoCliente") == TipoCliente.EMPRESA/*si el tipo de cliente es empresa*/
                        && empresa.equals(v.get("nombre"))/* y el nombre coincide */
                        && v.get("estado") == TipoDePago.DEBE) {/*y ademas el estado es debe */

                    v.put("fecha", desde);/* Asigna la fecha a cada venta */
                    tabla.getItems().add(v);/* Agrega la venta a la tabla */
                }
            }
            desde = desde.plusDays(1);/*Avanza al siguiente día */
        }
    }
}
