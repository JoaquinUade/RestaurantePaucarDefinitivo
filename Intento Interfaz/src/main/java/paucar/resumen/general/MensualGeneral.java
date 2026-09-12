package paucar.resumen.general;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.function.Function;

import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import paucar.config.Responsive;
import paucar.service.GastosFijosService;
import paucar.service.GastosIndividualesService;
import paucar.service.GastosVariablesService;
import paucar.service.VentasBackend;
import paucar.shared.LocaleUtils;
import paucar.shared.MonedaUtils;

public final class MensualGeneral extends BorderPane {

    private final VentasBackend ventasBackend;/*variable que guarda la instancia del backend de ventas */
    private final GastosVariablesService gastosVariablesService;
    private final GastosFijosService gastosFijosService;
    private final GastosIndividualesService gastosIndividualesService;

    private int anio;
    private int mes;

    private final TableView<VentaResumenDiarioDTO> tabla = new TableView<>();/*variable que guarda la tabla de visualización
                                                                             de los datos resumidos diarios */

    private final BorderPane footerTotal = new BorderPane();/*variable que guarda el pie de página con el total */

    private final Label mensajeSinDatos = new Label(
            "No hay datos ingresados para mostrar en este mes.");

    private final ObservableList<VentaResumenDiarioDTO> RenglonResumenDiario = FXCollections.observableArrayList();/*variable que guarda la lista observable de los datos resumidos diarios */

    public MensualGeneral(
            VentasBackend backend,
            GastosVariablesService gastosVariablesService,
            GastosFijosService gastosFijosService,
            GastosIndividualesService gastosIndividualesService,
            int anio,
            int mes) {

        this.ventasBackend = backend;
        this.gastosVariablesService = gastosVariablesService;
        this.gastosFijosService = gastosFijosService;
        this.gastosIndividualesService = gastosIndividualesService;

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

        mensajeSinDatos.setStyle("-fx-padding: 8 0; -fx-text-fill: #6b7280;");
        mensajeSinDatos.setVisible(false);
        mensajeSinDatos.setManaged(false);

        VBox contenedorTabla = new VBox(mensajeSinDatos, tabla);

        contenedorTabla.setPadding(
                new Insets(15, Responsive.px(20), 15, Responsive.px(20))
        );
        VBox.setVgrow(tabla, javafx.scene.layout.Priority.ALWAYS);
        setCenter(contenedorTabla);

        footerTotal.getStyleClass().add("footer-total");

        setBottom(footerTotal);

        cargarMes(anio, mes);
    }

    private void cargarMes(int anio, int mes) {

        RenglonResumenDiario.clear();/*limpia la lista de datos resumidos diarios para evitar mostrar datos viejos al
                      cargar un nuevo mes */

        LocalDate fecha = LocalDate.of(anio, mes, 1);/*variable que guarda la fecha del primer
                                                                día del mes a cargar */
        var gastosVariables = gastosVariablesService.obtenerTodos();
        var gastosFijos = gastosFijosService.obtenerTodos();
        var gastosIndividuales = gastosIndividualesService.obtenerTodos();

        while (fecha.getMonthValue() == mes) {/*si la fecha actual sigue siendo del mes a cargar, se
                                             ejecuta el bloque de código para agregar los datos del día a 
                                             la tabla */
           
                VentaResumenDiarioDTO ResumenDelDia = new VentaResumenDiarioDTO(fecha);/*variable que guarda el resumen de las ventas de un día, donde
                                                                                   cada monto ya está acumulado por tipo de pago y por total*/

                var ventasDelDia = ventasBackend.cargarVentasDelDia(fecha);/*variable que guarda la lista de ventas de un día */

                for (var v : ventasDelDia) {

                    BigDecimal monto = v.getMonto();
                    TipoDePago tipo = v.getEstado();

                    switch (tipo) {

                        case EFECTIVO ->
                            ResumenDelDia.setEfectivo(
                                    ResumenDelDia.getEfectivo().add(monto));

                        case DEBITO ->
                            ResumenDelDia.setDebito(
                                    ResumenDelDia.getDebito().add(monto));

                        case CREDITO ->
                            ResumenDelDia.setCredito(
                                    ResumenDelDia.getCredito().add(monto));

                        case TRANSFERENCIA ->
                            ResumenDelDia.setTransferencia(
                                    ResumenDelDia.getTransferencia().add(monto));

                        case MERCADO_PAGO ->
                            ResumenDelDia.setMercadoPago(
                                    ResumenDelDia.getMercadoPago().add(monto));

                        case DEBE ->
                            ResumenDelDia.setDebe(
                                    ResumenDelDia.getDebe().add(monto));

                        case DEUDA_PAGADA ->
                            ResumenDelDia.setDeudaPagada(
                                    ResumenDelDia.getDeudaPagada().add(monto));
                    }

                    if (tipo != TipoDePago.DEBE) {
                        ResumenDelDia.setVentaTotal(
                                ResumenDelDia.getVentaTotal().add(monto));
                    }
                }
                for (var g : gastosVariables) {

                    if (fecha.equals(g.getFecha())) {

                        ResumenDelDia.setGastosVariables(
                                ResumenDelDia.getGastosVariables()
                                        .add(g.getMonto()));
                    }
                }
                for (var g : gastosFijos) {

                    if (fecha.equals(g.getFecha())) {

                        ResumenDelDia.setGastosFijos(
                                ResumenDelDia.getGastosFijos()
                                        .add(g.getMonto()));
                    }
                }
                for (var g : gastosIndividuales) {

                    if (fecha.equals(g.getFecha())) {

                        ResumenDelDia.setGastosIndividuales(
                                ResumenDelDia.getGastosIndividuales()
                                        .add(g.getMonto()));
                    }
                }
                RenglonResumenDiario.add(ResumenDelDia);/*Agrega el resumen del día a la lista, haciendo que
                                                    luego se muestre como un renglón más en la tabla con
                                                    todos los datos*/
            
            fecha = fecha.plusDays(1);/*avanza a la siguiente fecha */
        }
        VentaResumenDiarioDTO TotalMensual = new VentaResumenDiarioDTO(null);/*variable que guarda el resumen total del mes */

        for (VentaResumenDiarioDTO d : RenglonResumenDiario) {

            TotalMensual.setGastosFijos(
                    TotalMensual.getGastosFijos().add(d.getGastosFijos()));

            TotalMensual.setGastosVariables(
                    TotalMensual.getGastosVariables().add(d.getGastosVariables()));

            TotalMensual.setGastosIndividuales(
                    TotalMensual.getGastosIndividuales().add(d.getGastosIndividuales()));

            TotalMensual.setVentaTotal(
                    TotalMensual.getVentaTotal().add(d.getVentaTotal()));

            TotalMensual.setDebe(
                    TotalMensual.getDebe().add(d.getDebe()));

            TotalMensual.setDeudaPagada(
                    TotalMensual.getDeudaPagada().add(d.getDeudaPagada()));

            TotalMensual.setDebito(
                    TotalMensual.getDebito().add(d.getDebito()));

            TotalMensual.setCredito(
                    TotalMensual.getCredito().add(d.getCredito()));

            TotalMensual.setTransferencia(
                    TotalMensual.getTransferencia().add(d.getTransferencia()));

            TotalMensual.setMercadoPago(
                    TotalMensual.getMercadoPago().add(d.getMercadoPago()));

            TotalMensual.setEfectivo(
                    TotalMensual.getEfectivo().add(d.getEfectivo()));
        }
        BigDecimal totalGastos
                = TotalMensual.getGastosFijos()
                        .add(TotalMensual.getGastosVariables())
                        .add(TotalMensual.getGastosIndividuales());

        BigDecimal totalNeto
                = TotalMensual.getVentaTotal()
                        .subtract(TotalMensual.getDebe())
                        .subtract(totalGastos);

        TotalMensual.setVentaTotal(totalNeto);

        actualizarMensajeSinDatos();
        RenderTotalMensual(TotalMensual);/*Muestra en la interfaz el resumen total del mes*/
    }

    private void actualizarMensajeSinDatos() {
        boolean hayDatos = RenglonResumenDiario.stream().anyMatch(this::tieneImportes);
        mensajeSinDatos.setVisible(!hayDatos);
        mensajeSinDatos.setManaged(!hayDatos);
    }

    private boolean tieneImportes(VentaResumenDiarioDTO resumen) {
        return resumen.getVentaTotal().signum() != 0
                || resumen.getDebe().signum() != 0
                || resumen.getDeudaPagada().signum() != 0
                || resumen.getDebito().signum() != 0
                || resumen.getCredito().signum() != 0
                || resumen.getTransferencia().signum() != 0
                || resumen.getMercadoPago().signum() != 0
                || resumen.getEfectivo().signum() != 0
                || resumen.getGastosFijos().signum() != 0
                || resumen.getGastosVariables().signum() != 0
                || resumen.getGastosIndividuales().signum() != 0;
    }

    private List<TableColumn<VentaResumenDiarioDTO, ?>> crearColumnas() {/*Este método define las columnas
                                                                        de la tabla y qué información del 
                                                                        resumen diario va en cada columna*/
        return List.of(
                colFecha(),
                colMonto("V. Total", dto -> dto.getVentaTotal()),
                colDebe(),
                colDeudaPagada("Deuda Pagada", dto -> dto.getDeudaPagada()),
                colMonto("Débito",
                        dto -> dto.getDebito()),
                colMonto("Crédito",
                        dto -> dto.getCredito()),
                colMonto("Transferencia",
                        dto -> dto.getTransferencia()),
                colMonto("Mercado Pago",
                        dto -> dto.getMercadoPago()),
                colMonto("Efectivo",
                        dto -> dto.getEfectivo()),
                colMonto("G. Fijos",
                        dto -> dto.getGastosFijos()),
                colMonto("G. Variables",
                        dto -> dto.getGastosVariables()),
                colMonto("G. Individuales",
                        dto -> dto.getGastosIndividuales())
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

        GridPane grid = new GridPane();

        for (TableColumn<?, ?> columna : tabla.getColumns()) {
            ColumnConstraints configuracionColumna = new ColumnConstraints();
            configuracionColumna.prefWidthProperty().bind(columna.widthProperty());
            grid.getColumnConstraints().add(configuracionColumna);
        }

        grid.add(new Label("TOTAL MES"), 0, 0);

        grid.add(
                new Label(
                        MonedaUtils.formatearMoneda(
                                t.getVentaTotal())),
                1, 0);

        Label totalDebe = new Label(
                MonedaUtils.formatearMoneda(
                        t.getDebe()));

        Label totalDeudaPagada = new Label(
                MonedaUtils.formatearMoneda(
                        t.getDeudaPagada()));

        if (t.getDebe().compareTo(BigDecimal.ZERO) > 0) {
            totalDebe.setTextFill(javafx.scene.paint.Color.RED);
        }

        if (t.getDeudaPagada().compareTo(BigDecimal.ZERO) > 0) {
            totalDeudaPagada.setTextFill(javafx.scene.paint.Color.GREEN);
        }

        Label totalGastosFijos = new Label(
                MonedaUtils.formatearMoneda(
                        t.getGastosFijos()));

        Label totalGastosVariables = new Label(
                MonedaUtils.formatearMoneda(
                        t.getGastosVariables()));

        Label totalGastosIndividuales = new Label(
                MonedaUtils.formatearMoneda(
                        t.getGastosIndividuales()));

        if (t.getGastosFijos().compareTo(BigDecimal.ZERO) > 0) {
            totalGastosFijos.setTextFill(javafx.scene.paint.Color.RED);
        }

        if (t.getGastosVariables().compareTo(BigDecimal.ZERO) > 0) {
            totalGastosVariables.setTextFill(javafx.scene.paint.Color.RED);
        }

        if (t.getGastosIndividuales().compareTo(BigDecimal.ZERO) > 0) {
            totalGastosIndividuales.setTextFill(javafx.scene.paint.Color.RED);
        }

        // Fecha
        grid.add(totalDebe, 2, 0);
        grid.add(totalDeudaPagada, 3, 0);

        // Débito, Crédito, Transferencia, MP, Efectivo
        grid.add(
                new Label(MonedaUtils.formatearMoneda(t.getDebito())),
                4, 0);

        grid.add(
                new Label(MonedaUtils.formatearMoneda(t.getCredito())),
                5, 0);

        grid.add(
                new Label(MonedaUtils.formatearMoneda(t.getTransferencia())),
                6, 0);

        grid.add(
                new Label(MonedaUtils.formatearMoneda(t.getMercadoPago())),
                7, 0);

        grid.add(
                new Label(MonedaUtils.formatearMoneda(t.getEfectivo())),
                8, 0);

        // Gastos
        grid.add(totalGastosFijos, 9, 0);
        grid.add(totalGastosVariables, 10, 0);
        grid.add(totalGastosIndividuales, 11, 0);

        footerTotal.setCenter(grid);
    }

    public void actualizarMes(int anio, int mes) {
        this.anio = anio;
        this.mes = mes;

        cargarMes(anio, mes);
    }

    public void actualizarFecha(LocalDate fecha) {
        this.anio = fecha.getYear();
        this.mes = fecha.getMonthValue();
        cargarMes(anio, mes);
    }

    public void refrescar() {
        cargarMes(anio, mes);
    }
}
