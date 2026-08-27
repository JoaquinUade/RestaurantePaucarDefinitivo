package paucar.resumen.empresas.semanal;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.Venta;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import paucar.service.VentasBackend;
import paucar.shared.FechaUtils;
import paucar.shared.MonedaUtils;

public class TablaSemanalDebe {

    private final VentasBackend backend;/*declara un campo backend que es una instancia de VentasBackend,
                                          se usará para cargar las ventas del día y filtrar las deudas de
                                          empresas*/

    private final VBox contenedor;

    private final TableView<Venta> tablaSemana1;
    private final TableView<Venta> tablaSemana2;
    private final TableView<Venta> tablaSemana3;
    private final TableView<Venta> tablaSemana4;

    private String empresaActual;
    private LocalDate desdeActual;

    public TablaSemanalDebe(VentasBackend backend) {
        this.backend = backend;
        tablaSemana1 = new TableView<>();
        tablaSemana2 = new TableView<>();
        tablaSemana3 = new TableView<>();
        tablaSemana4 = new TableView<>();

        contenedor = new VBox(10);

        definirColumnas();
        contenedor.getChildren().addAll(
                new javafx.scene.control.Label("Semana 1 (1-7)"),
                tablaSemana1,
                new javafx.scene.control.Label("Semana 2 (8-14)"),
                tablaSemana2,
                new javafx.scene.control.Label("Semana 3 (15-21)"),
                tablaSemana3,
                new javafx.scene.control.Label("Semana 4 (22-fin)"),
                tablaSemana4
        );
    }

    public VBox getVista() {
        return contenedor;
    }

    private void definirColumnas() {

        TableColumn<Venta, String> colFecha
                = new TableColumn<>("Fecha");/*Crea una columna llamada “Fecha” para una
                                                            TableView donde cada fila es un Map<String,
                                                            Object> y cada celda muestra un String y la
                                                            guarda en la variable colFecha*/

        colFecha.setCellValueFactory(fila -> {/* por cada fila de colFecha, haremos lo siguiente */

            LocalDate fecha = fila.getValue().getFecha().toLocalDate();

            return new SimpleObjectProperty<>(fecha == null ? "" : FechaUtils.formatearTitulo(fecha));/*devuelve la fecha, si la fecha es null se
                                                                                                  deja vacía sino muestra formateada la fecha*/
        });
        TableColumn<Venta, String> colConsumidor
                = new TableColumn<>("Consumidor");

        colConsumidor.setCellValueFactory(fila
                -> new SimpleObjectProperty<>(
                        fila.getValue().getConsumidor() == null
                        ? ""
                        : fila.getValue().getConsumidor()
                ));

        TableColumn<Venta, String> colDescripcion = crearColumnaTexto("Descripción", "descripcion",
                13);/*Crea una columna llamada colDescripcion usando un método que arma columnas
                            de texto, y la configura para mostrar la descripción de cada fila*/

        TableColumn<Venta, String> colMonto = new TableColumn<>("Monto");/*Crea una columna llamada
                                                                                             colMonto*/

        colMonto.setCellValueFactory(fila -> {/*por cada fila de la columna se hace el siguiente
                                                bloque de codigo*/

            Number m = (Number) fila.getValue().getMonto();/* Obtiene el monto de la fila actual */

            return new SimpleObjectProperty<>(MonedaUtils.formatearMoneda(m));/*si el monto es null muestra
                                                                                                cero, sino muestra el monto
                                                                                                formateado a moneda*/
        });

        TableColumn<Venta, String> colTipo = new TableColumn<>("Tipo de pago");/*Crea una columna llamada
                                                                                                   colTipo*/
        colTipo.setCellValueFactory(fila -> {/*define el contenido de colTipo */
            TipoDePago estado = (TipoDePago) fila.getValue().getEstado();/*obtiene el estado */

            String texto;

            if (estado == TipoDePago.DEBE) {/*si el estado es deuda pagada */
                texto = "PENDIENTE";
            } else {
                texto = "PAGADO";
            }
            return new SimpleObjectProperty<>(texto);
        });

        TableColumn<Venta, String> colObs = crearColumnaTexto("Observaciones", "observaciones", 16);/*Crea una columna  Observaciones que muestra texto
                                                                                                                                       tomado de la clave "observaciones" de cada fila,
                                                                                                                                       usando un padding de 16 píxeles*/

        TableColumn<Venta, String> colFechaPago = new TableColumn<>("Fecha de pago");/*Crea una columna que muestra en qué día se pagó la deuda*/
        colFechaPago.setCellValueFactory(fila -> {
            LocalDateTime fp = fila.getValue().getFechaPago();
            String txt = (fp == null) ? "" : FechaUtils.formatearTitulo(fp.toLocalDate());
            return new SimpleObjectProperty<>(txt);
        });

        colFecha.setSortable(false);/*le quita a todas las filas el ordenamiento sort, porque no
                                           corresponde*/
        colConsumidor.setSortable(false);
        colDescripcion.setSortable(false);
        colMonto.setSortable(false);
        colTipo.setSortable(false);
        colObs.setSortable(false);
        colFechaPago.setSortable(false);

        configurarTabla(tablaSemana1);
        configurarTabla(tablaSemana2);
        configurarTabla(tablaSemana3);
        configurarTabla(tablaSemana4);

    }

    private TableColumn<Venta, String> crearColumnaTexto(
            String titulo, String key, int padding) {

        TableColumn<Venta, String> col
                = new TableColumn<>(titulo);/*Crea una nueva columna de una tabla, la guarda en la variable
                                           col y le pone como título el texto recibido en titulo*/

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

        col.setCellFactory(columna -> new TableCell<>() {/*Para cada celda de esta columna, usá este tipo
                                                        de celda personalizada*/

            private final Text text = new Text();/*crea un nodo de texto para mostrar el contenido de la
                                                 celda */
            {
                text.wrappingWidthProperty()
                        .bind(columna.widthProperty().subtract(padding));/*Hace que el texto en la celda se
                                                                     ajuste al ancho de la columna,
                                                                     dejando un margen (padding) y
                                                                     ajustándose cuando la columna cambia
                                                                     de tamaño*/

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

    public void actualizar() {
        cargarDeudasEmpresa(empresaActual, desdeActual);
    }

    public void cargarDeudasEmpresa(String empresa, LocalDate desde) {
        this.empresaActual = empresa;
        this.desdeActual = desde;
        System.out.println("EMPRESA: " + empresa);
        System.out.println("DESDE: " + desde);
        tablaSemana1.getItems().clear();
        tablaSemana2.getItems().clear();
        tablaSemana3.getItems().clear();
        tablaSemana4.getItems().clear();
        LocalDate inicioMes = desde.withDayOfMonth(1);
        LocalDate finMes = desde.withDayOfMonth(desde.lengthOfMonth());

        LocalDate fecha = inicioMes;

        while (!fecha.isAfter(finMes)) {/* Mientras la fecha desde no sea posterior a la fecha actual */
            System.out.println("Fecha consultada: " + fecha);
            var ventas = backend.cargarVentasDelDia(fecha);/* Carga las ventas del día */
            System.out.println("Ventas encontradas: " + ventas.size());
            for (Venta v : ventas) {/* recorremos las ventas */

                if (v.getCliente() != null
                        && v.getCliente().getTipoCliente() == TipoCliente.EMPRESA
                        && empresa.equals(v.getCliente().getNombre())
                        && (v.getEstado() == TipoDePago.DEBE
                        || v.getEstado() == TipoDePago.DEUDA_PAGADA)) {
                    System.out.println(
                            "Cliente encontrado: "
                            + v.getCliente().getNombre()
                    );
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
            fecha = fecha.plusDays(1);/* Avanza al siguiente día */
        }
        ajustarAltura(tablaSemana1);
ajustarAltura(tablaSemana2);
ajustarAltura(tablaSemana3);
ajustarAltura(tablaSemana4);
    }

    public void mostrarVentanaPago() {

        TableView<Venta> tablaCompleta = new TableView<>();

        tablaCompleta.getItems().addAll(tablaSemana1.getItems());
        tablaCompleta.getItems().addAll(tablaSemana2.getItems());
        tablaCompleta.getItems().addAll(tablaSemana3.getItems());
        tablaCompleta.getItems().addAll(tablaSemana4.getItems());

        VentanaPagoDeudas ventana = new VentanaPagoDeudas(backend);

        ventana.mostrar(
                tablaCompleta,
                empresaActual,
                desdeActual,
                () -> cargarDeudasEmpresa(
                        empresaActual,
                        desdeActual)
        );
    }

    private void configurarTabla(TableView<Venta> tabla) {

        TableColumn<Venta, String> colFecha = crearColFecha();
        TableColumn<Venta, String> colConsumidor = crearColConsumidor();
        TableColumn<Venta, String> colDescripcion
                = crearColumnaTexto("Descripción", "descripcion", 13);

        TableColumn<Venta, String> colMonto = crearColMonto();

        TableColumn<Venta, String> colTipo = crearColTipo();

        TableColumn<Venta, String> colFechaPago = crearColFechaPago();

        TableColumn<Venta, String> colObs
                = crearColumnaTexto("Observaciones", "observaciones", 16);

        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colConsumidor);
        tabla.getColumns().add(colDescripcion);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colTipo);
        tabla.getColumns().add(colFechaPago);
        tabla.getColumns().add(colObs);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private TableColumn<Venta, String> crearColFecha() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Fecha");

        col.setCellValueFactory(fila -> {

            LocalDate fecha = fila.getValue()
                    .getFecha()
                    .toLocalDate();

            return new SimpleObjectProperty<>(
                    fecha == null
                            ? ""
                            : FechaUtils.formatearTitulo(fecha)
            );
        });

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> crearColConsumidor() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Consumidor");

        col.setCellValueFactory(fila
                -> new SimpleObjectProperty<>(
                        fila.getValue().getConsumidor() == null
                        ? ""
                        : fila.getValue().getConsumidor()
                ));

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> crearColMonto() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Monto");

        col.setCellValueFactory(fila -> {

            Number m = fila.getValue().getMonto();

            return new SimpleObjectProperty<>(
                    MonedaUtils.formatearMoneda(m)
            );
        });

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> crearColTipo() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Tipo de pago");

        col.setCellValueFactory(fila -> {

            TipoDePago estado = fila.getValue().getEstado();

            String texto;

            if (estado == TipoDePago.DEBE) {
                texto = "PENDIENTE";
            } else {
                texto = "PAGADO";
            }

            return new SimpleObjectProperty<>(texto);
        });

        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> crearColFechaPago() {

        TableColumn<Venta, String> col
                = new TableColumn<>("Fecha de pago");

        col.setCellValueFactory(fila -> {

            LocalDateTime fp = fila.getValue().getFechaPago();

            String txt = (fp == null)
                    ? ""
                    : FechaUtils.formatearTitulo(fp.toLocalDate());

            return new SimpleObjectProperty<>(txt);
        });

        col.setSortable(false);

        return col;
    }
    private void ajustarAltura(TableView<Venta> tabla) {

    int filas = tabla.getItems().size();

    tabla.setFixedCellSize(-1);

    tabla.setPrefHeight(
            Math.max(
                    80,
                    30 + filas * 60
            )
    );
}
}
