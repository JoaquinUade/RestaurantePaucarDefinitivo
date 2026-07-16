package paucar.resumen.empresas.semanal;

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

public class TablaSemanal {

    private final VentasBackend backend;
    private final TableView<Venta> tabla;

    public TablaSemanal(VentasBackend backend) {
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

            LocalDate fecha
                    = fila.getValue().getFecha().toLocalDate();
            return new SimpleObjectProperty<>(fecha == null ? "" : FechaUtils.formatearTitulo(fecha));/*si la fecha es null se deja vacia sino se
                                                                                                 pone la fecha formateada */
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

            var m = fila.getValue().getMonto();

            return new SimpleObjectProperty<>(MonedaUtils.formatearMoneda(m));/*si el monto es null se usa 0, de
                                                                                              locontrario se usa el valor double */
        });

        TableColumn<Venta, String> colTipo = new TableColumn<>("Tipo de pago");/*Esa línea crea una columna nueva en la tabla que se
                                                                                                   llama “Tipo de pago” y que va a mostrar un texto */

        colTipo.setCellValueFactory(fila -> {/*por cada fila se va a fijar el valor de la columna Tipo de
                                             pago usando esta función*/

            TipoDePago estado = fila.getValue().getEstado();

            return new SimpleObjectProperty<>(estado == null ? "" : estado.name());/*si el estado es null se deja vacio,
                                                                                   sino se muestra el nombre del estado*/
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

    private TableColumn<Venta, String> crearColumnaTexto(
            String titulo,
            String key,
            int padding) {

        TableColumn<Venta, String> col
                = new TableColumn<>(titulo);

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

        col.setCellFactory(columna -> new TableCell<>() {

            private final Text text = new Text();

            {
                text.wrappingWidthProperty()
                        .bind(columna.widthProperty().subtract(padding));

                setGraphic(text);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    text.setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        return col;
    }

    public double cargarSemanaEmpresa(String empresa, LocalDate inicio, LocalDate fin) {

        tabla.getItems().clear();/* Limpia los elementos de la tabla */
        double total = 0;/* Variable para acumular el total de las ventas de la semana */

        LocalDate cursor = inicio;/* Variable para recorrer los días de la semana, comenzando desde el
                                  inicio */

        while (!cursor.isAfter(fin)) {/* Mientras el cursor no sea posterior a la fecha final */

            var ventas = backend.cargarVentasDelDia(cursor);/* Carga las ventas del día */

            for (Venta v : ventas) {/*recorre cada una de las ventas */

                if (v.getCliente() != null
                        && v.getCliente().getTipoCliente() == TipoCliente.EMPRESA
                        && empresa.equals(v.getCliente().getNombre())
                        && v.getEstado() != TipoDePago.DEBE
                        && v.getEstado() != TipoDePago.DEUDA_PAGADA) {

                    tabla.getItems().add(v);

                    total += v.getMonto().doubleValue();
                }
            }
            cursor = cursor.plusDays(1);/* Avanza al siguiente día */
        }
        return total;/* Devuelve el total de las ventas */
    }
}
