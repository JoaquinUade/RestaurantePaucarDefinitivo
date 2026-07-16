package paucar.resumen.clientes.semanal;

import java.time.LocalDate;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.Venta;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import paucar.service.VentasBackend;
import paucar.shared.FechaUtils;
import paucar.shared.MonedaUtils;

public class TablaSemanalCliente {

    private final VentasBackend backend;
    private final TableView<Venta> tabla;

    public TablaSemanalCliente(VentasBackend backend) {
        this.backend = backend;
        this.tabla = new TableView<>();
        definirColumnas();
    }

    public TableView<Venta> getTabla() {
        return tabla;
    }

    private void definirColumnas() {

        TableColumn<Venta, String> colFecha = new TableColumn<>("Fecha");/*Esa línea crea una columna de la
                                                                                             tabla que se llama “Fecha” y que va
                                                                                             a mostrar un texto para cada fila*/

        colFecha.setCellValueFactory(fila -> {/*Para cada fila de la tabla, yo te voy a explicar qué
                                               escribir en la columna Fecha*/
            var fechaHora = fila.getValue().getFecha();

            LocalDate fecha = fechaHora == null
                    ? null
                    : fechaHora.toLocalDate();

            return new SimpleObjectProperty<>(
                    fecha == null ? "" : FechaUtils.formatearTitulo(fecha));
        });

        TableColumn<Venta, String> colDescripcion
                = crearColumnaTexto("Descripción", "descripcion", 13);/*Crea la columna colDescripcion usando un
                                                                                        método que arma columnas de texto, y la
                                                                                        configura para mostrar la descripción de cada
                                                                                        fila*/

        TableColumn<Venta, String> colMonto = new TableColumn<>("Monto");/*Esa línea crea una columna nueva en la tabla
                                                                                            que se llama “Monto” y que va a mostrar un texto */

        colMonto.setCellValueFactory(fila -> {/*por cada fila se va a fijar el valor de la columna Monto
                                              usando esta función*/

            Number m = fila.getValue().getMonto();
            return new SimpleObjectProperty<>(MonedaUtils.formatearMoneda(m));/*si el monto es null se usa 0, de
                                                                                              locontrario se usa el valor double */
        });

        TableColumn<Venta, String> colTipo = new TableColumn<>("Tipo de pago");/*Esa línea crea una columna nueva en la tabla que se
                                                                                                   llama “Tipo de pago” y que va a mostrar un texto */

        colTipo.setCellValueFactory(fila -> {/*por cada fila se va a fijar el valor de la columna Tipo de
                                              pago usando esta función*/

            TipoDePago estado
                    = fila.getValue().getEstado();
            return new SimpleObjectProperty<>(estado == null ? "" : estado.name());/*si el estado es null se deja vacio, de lo contrario se muestra el nombre del estado*/
        });

        TableColumn<Venta, String> colObs = crearColumnaTexto("Observaciones", "observaciones", 16);/*Crea una columna para mostrar las observaciones */

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

    private TableColumn<Venta, String> crearColumnaTexto(String titulo, String key,
            int padding) {

        TableColumn<Venta, String> col = new TableColumn<>(titulo);/*crea una columna nueva en la tabla que
                                                                                 se llama como el titulo que se le pasa y
                                                                                 que va a mostrar un texto */

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

    public double cargarSemanaCliente(String cliente,
            LocalDate inicio,
            LocalDate fin) {

        tabla.getItems().clear();
        double total = 0;

        LocalDate cursor = inicio;

        while (!cursor.isAfter(fin)) {

            var ventas = backend.cargarVentasDelDia(cursor);

            for (Venta v : ventas) {

                if (v.getCliente() != null
                        && v.getCliente().getTipoCliente() == TipoCliente.CLIENTE
                        && cliente.equals(v.getCliente().getNombre())
                        && v.getEstado() != TipoDePago.DEBE
                        && v.getEstado() != TipoDePago.DEUDA_PAGADA) {

                    tabla.getItems().add(v);

                    total += v.getMonto().doubleValue();
                }
            }

            cursor = cursor.plusDays(1);
        }

        return total;
    }
}
