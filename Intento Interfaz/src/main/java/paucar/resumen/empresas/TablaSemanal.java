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

public class TablaSemanal {

    private final VentasBackend backend;
    private final TableView<Map<String, Object>> tabla;

    private static final Locale LOCALE_AR = Locale.of("es", "AR");
    private static final DateTimeFormatter FECHA_FORMATO =
            DateTimeFormatter.ofPattern("d MMMM yyyy", LOCALE_AR);
    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(LOCALE_AR);

    public TablaSemanal(VentasBackend backend) {
        this.backend = backend;
        this.tabla = new TableView<>();
        definirColumnas();
    }

    public TableView<Map<String, Object>> getTabla() {
        return tabla;
    }

    private void definirColumnas() {

        TableColumn<Map<String, Object>, String> colFecha = new TableColumn<>("Fecha");/*Esa línea crea una columna de la
                                                                                             tabla que se llama “Fecha” y que va
                                                                                             a mostrar un texto para cada fila*/

        colFecha.setCellValueFactory(fila -> {/*Para cada fila de la tabla, yo te voy a explicar qué
                                               escribir en la columna Fecha*/

            LocalDate fecha = (LocalDate) fila.getValue().get("fecha");/*Obtiene la fecha de la fila 
                                                                            actual*/
            return new SimpleObjectProperty<>(fecha == null ? "" : fecha.format(FECHA_FORMATO));/*si la fecha es null se deja vacia sino se
                                                                                                 pone la fecha formateada */
        });

        TableColumn<Map<String, Object>, String> colDescripcion =
                crearColumnaTexto("Descripción", "descripcion", 13);/*Crea la columna colDescripcion usando un
                                                                                        método que arma columnas de texto, y la
                                                                                        configura para mostrar la descripción de cada
                                                                                        fila*/

        TableColumn<Map<String, Object>, String> colMonto = new TableColumn<>("Monto");/*Esa línea crea una columna nueva en la tabla
                                                                                            que se llama “Monto” y que va a mostrar un texto */

        colMonto.setCellValueFactory(fila -> {/*por cada fila se va a fijar el valor de la columna Monto
                                              usando esta función*/

            Number m = (Number) fila.getValue().get("monto");/*Obtiene el monto de la fila actual*/

            return new SimpleObjectProperty<>(MONEDA.format(m == null ? 0 : m.doubleValue()));/*si el monto es null se usa 0, de
                                                                                              locontrario se usa el valor double */
        });

        TableColumn<Map<String, Object>, String> colTipo = new TableColumn<>("Tipo de pago");/*Esa línea crea una columna nueva en la tabla que se
                                                                                                   llama “Tipo de pago” y que va a mostrar un texto */

         colTipo.setCellValueFactory(fila -> {/*por cada fila se va a fijar el valor de la columna Tipo de
                                              pago usando esta función*/

            TipoDePago estado = (TipoDePago) fila.getValue().get("estado");/*Obtiene el estado de la fila actual*/

            return new SimpleObjectProperty<>(estado == null ? "" : estado.name());/*si el estado es null se deja vacio, de lo contrario se muestra el nombre del estado*/
        });
        colTipo.setCellValueFactory(fila -> {/*por cada fila se va a fijar el valor de la columna Tipo de
                                             pago usando esta función*/

            TipoDePago estado = (TipoDePago) fila.getValue().get("estado");/*Obtiene el estado de la
                                                                                fila actual*/

            return new SimpleObjectProperty<>(estado == null ? "" : estado.name());/*si el estado es null se deja vacio,
                                                                                   sino se muestra el nombre del estado*/
        });

        TableColumn<Map<String, Object>, String> colObs = crearColumnaTexto("Observaciones", "observaciones", 16);/*Crea una columna para mostrar las observaciones */

        colFecha.setSortable(false);/*quita la posibilidad de ordenar las columnas */
        colDescripcion.setSortable(false);
        colMonto.setSortable(false);
        colTipo.setSortable(false);
        colObs.setSortable(false);

        tabla.getColumns().add(colFecha);/*añade las columnas en orden */
        tabla.getColumns().add(colDescripcion);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colTipo);
        tabla.getColumns().add(colObs);

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);/*hace que las columnas se adapten al
                                                                                          ancho de toda la tabla y la ultima
                                                                                          sea la mas flexible */
    }

    private TableColumn<Map<String, Object>, String> crearColumnaTexto(String titulo, String key,
        int padding) {

        TableColumn<Map<String, Object>, String> col = new TableColumn<>(titulo);/*crea una columna nueva en la tabla que
                                                                                 se llama como el titulo que se le pasa y
                                                                                 que va a mostrar un texto */

        col.setCellValueFactory(fila -> new SimpleObjectProperty<>((String) fila.getValue().get(key)));/*por cada fila se fija en el valor de la clave
                                                                                                       key y se muestra como texto en la columna*/

        col.setCellFactory(columna -> new TableCell<>() {/*decido como se va a ver cada celda en la
                                                         columna*/

            private final Text text = new Text();/* Crea un objeto para escribir texto en la pantalla
                                                 dentro de la celda de la tabla*/

            {
                text.wrappingWidthProperty()
                        .bind(columna.widthProperty().subtract(padding));/*Hace que el texto se ajuste al ancho de la columna 
                                                                          y le agrega un padding*/
                setGraphic(text);/* Establece el texto como gráfico de la celda */

                setPrefHeight(Region.USE_COMPUTED_SIZE);/* Establece la altura preferida de la celda */
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);/* Actualiza el elemento de la celda */

                if (empty || item == null || item.isBlank()) {/*si esta vacia o es nulol o esta en blanco*/
                    text.setText(null);/* Establece el texto como nulo */
                    setGraphic(null);/* Establece el gráfico de la celda como nulo */
                    setTooltip(null);/* Establece el tooltip de la celda como nulo */

                } else {/*sino */

                    text.setText(item);/* Establece el texto de la celda */
                    setGraphic(text);/* Establece el gráfico de la celda como el texto */
                    setTooltip(new Tooltip(item));/* Establece el tooltip de la celda */
                }
            }
        });

        return col;/* Devuelve la columna creada */
    }

    public double cargarSemanaEmpresa(String empresa, LocalDate inicio, LocalDate fin) {

        tabla.getItems().clear();/* Limpia los elementos de la tabla */
        double total = 0;/* Variable para acumular el total de las ventas de la semana */

        LocalDate cursor = inicio;/* Variable para recorrer los días de la semana, comenzando desde el
                                  inicio */

        while (!cursor.isAfter(fin)) {/* Mientras el cursor no sea posterior a la fecha final */

            var ventas = backend.cargarVentasDelDia(cursor);/* Carga las ventas del día */

            for (Map<String, Object> v : ventas) {/*recorre cada una de las ventas */

                if (v.get("tipoCliente") == TipoCliente.EMPRESA/*si el tipo de cliente es una empresa */
                        && empresa.equals(v.get("nombre"))/* y el nombre coincide */
                        && v.get("estado") != TipoDePago.DEBE) {/*y ademas no es debe */

                    v.put("fecha", cursor);/* Asigna la fecha al registro de venta */
                    tabla.getItems().add(v);/* Agrega el registro de venta a la tabla */
                    total += ((Number) v.get("monto")).doubleValue();/* Acumula el monto de la venta */
                }
            }
            cursor = cursor.plusDays(1);/* Avanza al siguiente día */
        }
        return total;/* Devuelve el total de las ventas */
    }
}
