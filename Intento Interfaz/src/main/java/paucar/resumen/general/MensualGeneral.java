package paucar.resumen.general;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.function.Function;

import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import paucar.service.VentasBackend;
import paucar.shared.LocaleUtils;
import paucar.shared.MonedaUtils;

public class MensualGeneral extends BorderPane {

    private final VentasBackend ventasBackend;/*variable que guarda la instancia del backend de ventas */

    private int anio;
    private int mes;

    private final TableView<VentaResumenDiarioDTO> tabla = new TableView<>();/*variable que guarda la tabla de visualización
                                                                             de los datos resumidos diarios */

    private final BorderPane footerTotal = new BorderPane();/*variable que guarda el pie de página con el total */

    private final ObservableList<VentaResumenDiarioDTO> RenglonResumenDiario = FXCollections.observableArrayList();/*variable que guarda la lista observable de los datos resumidos diarios */

    public MensualGeneral(VentasBackend backend, int anio, int mes) {
        this.ventasBackend = backend;
        this.anio = anio;
        this.mes = mes;

        tabla.setItems(RenglonResumenDiario);
        tabla.getColumns().addAll(crearColumnas());
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        tabla.setEditable(false);
        Label titulo = new Label("Resumen Mensual");
        titulo.getStyleClass().add("titulo-xl");/*agrega la clase CSS "titulo-xl" al título para aplicar estilos específicos a esa etiqueta */

        setTop(titulo);
        setCenter(tabla);

        footerTotal.getStyleClass().add("footer-total");/*agrega la clase CSS "footer-total" al pie de
                                                           página para aplicar estilos específicos a esa
                                                           sección */
        setBottom(footerTotal);

        cargarMes(anio, mes);
    }

    private void cargarMes(int anio, int mes) {

        RenglonResumenDiario.clear();/*limpia la lista de datos resumidos diarios para evitar mostrar datos viejos al
                      cargar un nuevo mes */

        LocalDate fecha = LocalDate.of(anio, mes, 1);/*variable que guarda la fecha del primer
                                                                día del mes a cargar */

        while (fecha.getMonthValue() == mes) {/*si la fecha actual sigue siendo del mes a cargar, se
                                             ejecuta el bloque de código para agregar los datos del día a 
                                             la tabla */
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY
                    && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                VentaResumenDiarioDTO ResumenDelDia = new VentaResumenDiarioDTO(fecha);/*variable que guarda el resumen de las ventas de un día, donde
                                                                                   cada monto ya está acumulado por tipo de pago y por total*/

                var ventasDelDia = ventasBackend.cargarVentasDelDia(fecha);/*variable que guarda la lista de ventas de un día */

                for (var v : ventasDelDia) {/*recorre cada venta del día */
                    BigDecimal monto = v.getMonto();
                    TipoDePago tipo = v.getEstado();
                    switch (tipo) {/*segun el tipo de pago */
                        case EFECTIVO ->
                            ResumenDelDia.setEfectivo(ResumenDelDia.getEfectivo().add(monto));/*si es efectivo, se acumula el monto en el campo de efectivo del resumen del día */
                        case DEBITO ->
                            ResumenDelDia.setDebito(ResumenDelDia.getDebito().add(monto));/*si es débito, se acumula el monto en el campo de débito del resumen del día */
                        case CREDITO ->
                            ResumenDelDia.setCredito(ResumenDelDia.getCredito().add(monto));/*si es crédito, se acumula el monto en el campo de crédito del resumen del día */
                        case TRANSFERENCIA ->
                            ResumenDelDia.setTransferencia(ResumenDelDia.getTransferencia().add(monto));/*si es transferencia, se acumula el monto en el campo de transferencia
                                                                                                 del resumen del día */
                        case MERCADO_PAGO ->
                            ResumenDelDia.setMercadoPago(ResumenDelDia.getMercadoPago().add(monto));/*si es mercado pago, se acumula el monto en el campo de mercado pago del
                                                                                             resumen del día */
                        case DEBE ->
                            ResumenDelDia.setDebe(ResumenDelDia.getDebe().add(monto));/*si es debe, se acumula el monto en el campo de debe del resumen del día */
                        case DEUDA_PAGADA ->
                            ResumenDelDia.setDeudaPagada(ResumenDelDia.getDeudaPagada().add(monto));
                    }
                    if (tipo != TipoDePago.DEBE) {
                        ResumenDelDia.setVentaTotal(ResumenDelDia.getVentaTotal().add(monto));/*si no es debe, se acumula el monto en el campo de venta total del resumen
                                                                                       del día */
                    }
                }

                RenglonResumenDiario.add(ResumenDelDia);/*Agrega el resumen del día a la lista, haciendo que
                                                    luego se muestre como un renglón más en la tabla con
                                                    todos los datos*/
            }
            fecha = fecha.plusDays(1);/*avanza a la siguiente fecha */
        }
        VentaResumenDiarioDTO TotalMensual = new VentaResumenDiarioDTO(null);/*variable que guarda el resumen total del mes */

        for (VentaResumenDiarioDTO d : RenglonResumenDiario) {/*recorre cada resumen diario */
            TotalMensual.setVentaTotal(TotalMensual.getVentaTotal().add(d.getVentaTotal()));/*acumula el total de ventas del mes sumando el
                                                                                         total de cada día */

            TotalMensual.setDebe(TotalMensual.getDebe().add(d.getDebe()));/*acumula el total de deudas del mes sumando el total de cada día */

            TotalMensual.setDeudaPagada(TotalMensual.getDeudaPagada().add(d.getDeudaPagada()));

            TotalMensual.setDebito(TotalMensual.getDebito().add(d.getDebito()));/*acumula el total de débitos del mes sumando el total de
                                                                             cada día */

            TotalMensual.setCredito(TotalMensual.getCredito().add(d.getCredito()));/*acumula el total de créditos del mes sumando el total de
                                                                                cada día */
            TotalMensual.setTransferencia(TotalMensual.getTransferencia().add(d.getTransferencia()));/*acumula el total de transferencias del
                                                                                                  mes sumando el total de cada día */

            TotalMensual.setMercadoPago(TotalMensual.getMercadoPago().add(d.getMercadoPago()));/*acumula el total de pagos en Mercado Pago del
                                                                                            mes sumando el total de cada día */

            TotalMensual.setEfectivo(TotalMensual.getEfectivo().add(d.getEfectivo()));/*acumula el total de pagos en efectivo del mes sumando
                                                                                   el total de cada día */
        }
        RenderTotalMensual(TotalMensual);/*Muestra en la interfaz el resumen total del mes*/
    }

    private List<TableColumn<VentaResumenDiarioDTO, ?>> crearColumnas() {/*Este método define las columnas
                                                                        de la tabla y qué información del 
                                                                        resumen diario va en cada columna*/
        return List.of(
                colFecha(),/*columna que muestra la fecha del día */
                colMonto("V. Total", dto -> dto.getVentaTotal()),/*columna que muestra el total de
                                                                        ventas del día, usando el método
                                                                        getVentaTotal del resumen diario */
                colDebe(),
                colDeudaPagada("Deuda Pagada", dto -> dto.getDeudaPagada()),
                colMonto("Débito", dto -> dto.getDebito()),/*columna que muestra el total de débitos
                                                                   del día */
                colMonto("Crédito", dto -> dto.getCredito()),/*columna que muestra el total de
                                                                     créditos del día */
                colMonto("Transferencia", dto -> dto.getTransferencia()),/*columna que muestra el
                                                                                total de transferencias del
                                                                                día */
                colMonto("Mercado Pago", dto -> dto.getMercadoPago()),/*columna que muestra el total
                                                                             de pagos en Mercado Pago del
                                                                             día */
                colMonto("Efectivo", dto -> dto.getEfectivo())/*columna que muestra el total de
                                                                      pagos en efectivo del día */
        );
    }

    private TableColumn<VentaResumenDiarioDTO, LocalDate> colFecha() {
        TableColumn<VentaResumenDiarioDTO, LocalDate> col = new TableColumn<>("Fecha");/*Columna que muestra la fecha del día */

        col.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getFecha()));/*Para cada fila de la tabla, obtené el objeto
                                                  VentaResumenDiarioDTO, sacale la fecha, y usá esa fecha 
                                                  como valor de la celda de esta columna*/

        col.setCellFactory(tc -> new TableCell<>() {/*A esta columna le defino yo cómo se crea y cómo se
                                                    ve cada celda*/
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {/*Este método se llama cada vez que
                                                                      una celda necesita actualizar su
                                                                      contenido. El parámetro f es la fecha
                                                                       que se va a mostrar en la celda, y
                                                                       empty indica si la celda está vacía*/

                super.updateItem(fecha, empty);/*Llama al método de la clase padre para actualizar el
                                               contenido de la celda*/

                setAlignment(javafx.geometry.Pos.CENTER);/*Establece la alineación de la celda al centro*/

                if (empty) {/*Si la celda está vacía */
                    setText(null);/*Establece el texto de la celda como null */

                } else if (fecha == null) {/*si la fecha es null, osea es la fila que dice total mes */

                    setText("TOTAL MES");/*Establece el texto de la celda como "TOTAL MES"*/
                    setStyle("celda-fecha");/*Establece el estilo de la celda como negrita*/

                } else {/*sino está vacía y tiene fecha, osea es un día normal del mes */
                    setText(String.format(
                            "%02d-%s",/*formatea la fecha para mostrar el día con dos dígitos y el mes con su nombre abreviado, por ejemplo "05-Mar" */
                            fecha.getDayOfMonth(),/*obtiene el día del mes */
                            fecha.getMonth().getDisplayName(TextStyle.FULL, LocaleUtils.ES_AR)/*obtiene el nombre del mes en formato completo y en español de Argentina*/
                    )
                    );
                    getStyleClass().clear();/*limpia cualquier estilo previo de la celda para que no se acumulen estilos al actualizar el contenido de la celda*/
                }
            }
        });
        col.setSortable(false);/*desactiva la opción de ordenar la tabla por esta columna, ya que no tiene sentido ordenar por fecha en este caso*/
        return col;/*retorna la columna configurada para mostrar la fecha en la tabla */
    }

    private TableColumn<VentaResumenDiarioDTO, BigDecimal> colDebe() {

        TableColumn<VentaResumenDiarioDTO, BigDecimal> col = new TableColumn<>("Debe");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        c.getValue().getDebe()
                )
        );

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                    setTextFill(null); // limpia color previo
                } else {
                    setText(MonedaUtils.formatearMoneda(v));
                    if (v.compareTo(BigDecimal.ZERO) > 0) {
                        setTextFill(javafx.scene.paint.Color.RED);
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private TableColumn<VentaResumenDiarioDTO, BigDecimal> colDeudaPagada(
            String titulo,
            Function<VentaResumenDiarioDTO, BigDecimal> getter) {

        TableColumn<VentaResumenDiarioDTO, BigDecimal> col = new TableColumn<>(titulo);

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        getter.apply(c.getValue())
                )
        );

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);

                if (empty || v == null) {
                    setText("");
                    setTextFill(null);
                } else {
                    setText(MonedaUtils.formatearMoneda(v));

                    if (v.compareTo(BigDecimal.ZERO) > 0) {
                        setTextFill(javafx.scene.paint.Color.GREEN); // ✅ VERDE
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });

        col.setSortable(false);
        return col;
    }

    private TableColumn<VentaResumenDiarioDTO, BigDecimal> colMonto(
            String titulo,
            Function<VentaResumenDiarioDTO, BigDecimal> getter) {

        TableColumn<VentaResumenDiarioDTO, BigDecimal> col = new TableColumn<>(titulo);

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        getter.apply(c.getValue())
                )
        );

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : MonedaUtils.formatearMoneda(v));
            }
        });

        col.setSortable(false);
        return col;
    }

    private void RenderTotalMensual(VentaResumenDiarioDTO t) {

        GridPane grid = new GridPane();/*crea un GridPane para organizar los totales del mes en una fila
                                       con varias columnas mostrando el total con cada tipo de pago (total
                                       de ventas, total de debe, total de débito, etc)*/

        for (TableColumn<?, ?> columna : tabla.getColumns()) {/*recorre todas las columnas de la tabla*/

            ColumnConstraints configuracionColumna = new ColumnConstraints();/*crea una nueva columna para
                                                                             el GridPane*/
            configuracionColumna.prefWidthProperty().bind(columna.widthProperty());/* enlaza el ancho de
                                                                                  la columna totales con el
                                                                                  ancho de la columna de la
                                                                                  tabla */
            grid.getColumnConstraints().add(configuracionColumna);/* agrega la configuración de la columna
                                                                  al GridPane */
        }

        grid.add(new Label("TOTAL MES"), 0, 0);/* agrega un label con el texto
                                                                           "TOTAL MES" en la primera
                                                                           columna y primera fila del
                                                                           GridPane */
        grid.add(new Label(MonedaUtils.formatearMoneda(t.getVentaTotal())), 1, 0);/* agrega un label con el total de ventas en la
                                                                                          segunda columna y primera fila del GridPane */


        Label totalDebe = new Label(MonedaUtils.formatearMoneda(t.getDebe()));/* crea un label con el total de debe del
                                                                mes para mostrarlo en la fila de totales */
        Label totalDeudaPagada = new Label(MonedaUtils.formatearMoneda(t.getDeudaPagada()));

        if (t.getDeudaPagada().compareTo(BigDecimal.ZERO) > 0) {
            totalDeudaPagada.setTextFill(javafx.scene.paint.Color.GREEN);
        } else {
            totalDeudaPagada.setTextFill(javafx.scene.paint.Color.BLACK);
        }

        if (t.getDebe().compareTo(BigDecimal.ZERO) > 0) {/*si el total de debe es mayor a 0*/

            totalDebe.setTextFill(javafx.scene.paint.Color.RED);/* establece el color del texto en rojo */
        } else {/*sino */
            totalDebe.setTextFill(javafx.scene.paint.Color.BLACK);/* establece el color del texto en negro */
        }

        grid.add(totalDebe, 2, 0);/* agrega el label con el total de debe en la
                                                        tercera columna y primera fila del GridPane */
        grid.add(totalDeudaPagada, 3, 0);
        grid.add(new Label(MonedaUtils.formatearMoneda(t.getDebito())), 4, 0);/* agrega un label con el total de débitos en la
                                                                                          cuarta columna y primera fila del GridPane */

        grid.add(new Label(MonedaUtils.formatearMoneda(t.getCredito())), 5, 0);/* agrega un label con el total de créditos en la
                                                                                          quinta columna y primera fila del GridPane */

        grid.add(new Label(MonedaUtils.formatearMoneda(t.getTransferencia())), 6, 0);/* agrega un label con el total de transferencias en la
                                                                                          sexta columna y primera fila del GridPane */

        grid.add(new Label(MonedaUtils.formatearMoneda(t.getMercadoPago())), 7, 0);/* agrega un label con el total de pagos por Mercado Pago en la
                                                                                          séptima columna y primera fila del GridPane */

        grid.add(new Label(MonedaUtils.formatearMoneda(t.getEfectivo())), 8, 0);/* agrega un label con el total de efectivo en la
                                                                                          octava columna y primera fila del GridPane */

        footerTotal.setCenter(grid);/* agrega el GridPane al centro del pie de página de los totales */
    }

    public void actualizarMes(int anio, int mes) {
        this.anio = anio;
        this.mes = mes;

        cargarMes(anio, mes);
    }

    public void refrescar() {
        cargarMes(anio, mes);
    }
}
