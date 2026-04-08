package paucar.resumen.general;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.service.VentasBackend;
import paucar.ventas.Tabla;
import paucar.ventas.Ventas;

public class SemanalGeneral extends BorderPane {

    private static final Locale LOCALE_AR = Locale.of("es", "AR");

    private final TableView<VentaResumenDiarioDTO> tablaTotalSemanal = new TableView<>();

    private final ObservableList<VentaResumenDiarioDTO> filaTotalSemana = FXCollections.observableArrayList();

    private static final NumberFormat MONEDA
            = NumberFormat.getCurrencyInstance(LOCALE_AR);

    private final VentasBackend backend;
    private final VBox contenido = new VBox(8);/*contenedor vertical en el que pondremos los
                                                       bloques que representan la tabla y la fecha de
                                                       ese dia */

    public SemanalGeneral(VentasBackend backend, LocalDate fechaBase) {
        this.backend = backend;

        initUI();

        LocalDate lunes = obtenerLunes(fechaBase);
        cargarSemanaDesdeLunes(lunes);

        tablaTotalSemanal.setItems(filaTotalSemana);
        tablaTotalSemanal.getColumns().addAll(crearColumnas());
        tablaTotalSemanal.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        tablaTotalSemanal.setEditable(false);
        tablaTotalSemanal.setFixedCellSize(38);
        tablaTotalSemanal.setPrefHeight(38 + 30); // fila + header
        tablaTotalSemanal.setSelectionModel(null);
        tablaTotalSemanal.getStyleClass().add("tabla-total-dorada");
        setBottom(tablaTotalSemanal);
    }

    private void initUI() {

        Label titulo = new Label("Resumen Semanal");
        titulo.getStyleClass().add("title-xl");

        // Header igual al mensual
        Region sep = new Region();
        HBox.setHgrow(sep, Priority.ALWAYS);

        HBox header = new HBox(12, titulo, sep);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-semanal");

        setTop(header);
        setCenter(scroll);

    }
private void cargarSemanaDesdeLunes(LocalDate lunes) {
    contenido.getChildren().clear();/*Borrá de la pantalla todos los días que estaban mostrados antes*/

    actualizarTotalSemana(lunes);/*Actualiza el total de la semana*/

    for (int i = 0; i < 5; i++) {/*for que da 5 vueltas, una por cada día de la semana */

        LocalDate dia = lunes.plusDays(i);/*Calcula una fecha sumando i días al lunes y la guarda en la
                                          variable dia */
        agregarDia(dia);/*Agrega el día a la pantalla */
    }
}
private LocalDate obtenerLunes(LocalDate fecha) {
    LocalDate lunes = fecha;/*Inicializa la variable lunes con la fecha proporcionada */
    while (lunes.getDayOfWeek() != DayOfWeek.MONDAY) {/*Mientras que el día de la semana que tenga la
                                                      variable lunes no sea monday*/
        lunes = lunes.minusDays(1);/*iremos retrocediendo en 1 dia la fecha obviamente con
                                                   un maximo de 5 veces*/
    }
    return lunes;/*Devuelve la fecha del lunes de esa semana */
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
                colMonto("Débito", dto -> dto.getDebito()),/*columna que muestra el total de débitos
                                                                   del día */
                colMonto("Crédito", dto -> dto.getCredito()),/*columna que muestra el total de
                                                                     créditos del día */
                colMonto("Transferencia", dto -> dto.getTransferencia()),/*columna que muestra el
                                                                                total de transferencias del
                                                                                día */
                colMonto("MERCADO_PAGO", dto -> dto.getMercadoPago()),/*columna que muestra el total
                                                                             de pagos en Mercado Pago del
                                                                             día */
                colMonto("Efectivo", dto -> dto.getEfectivo())/*columna que muestra el total de
                                                                      pagos en efectivo del día */
        );
    }

    private void agregarDia(LocalDate fecha) {
        VBox bloque = crearBloqueDia(fecha);/*Crea un VBox que representa visualmente un día (con su
                                            título y la tabla de ventas) llamando al método
                                            crearBloqueDia(fecha) y lo guarda en la variable bloque*/

        contenido.getChildren().add(bloque);/*Agrega los bloques de los días uno debajo del otro en el
                                            contenedor, mostrando el lunes arriba y el viernes abajo*/
    }

    private VBox crearBloqueDia(LocalDate fecha) {
        BigDecimal totalDia = BigDecimal.ZERO;/*Variable que acumula el total de ventas del día,
                                              inicializada en cero. Acumula todos los tipos de pago
                                              excepto el debe*/

        String NombreDia = fecha.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, LOCALE_AR);/*Esta línea obtiene el nombre completo del día de la
                                                                                                     semana (por ejemplo “lunes”) a partir de la fecha y
                                                                                                     lo guarda en la variable dia, usando el idioma español
                                                                                                     de Argentina (LOCALE_AR */

        NombreDia = NombreDia.substring(0, 1).toUpperCase(LOCALE_AR) + NombreDia.substring(1);/*pone en mayúscula la primera letra del nombre del día
                                                                                                            (osea, transforma "lunes" en "Lunes")*/

        Label tituloDia = new Label(/*crea un Label que */
                NombreDia + " "/*muestra el nombre del dia */
                + fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/"
                + fecha.getYear());/*y la fecha en formato "Lunes 05/03/2024"*/

        tituloDia.getStyleClass().add("title-xl");/*agrega la clase CSS para el estilo del título */

        ObservableList<Ventas.Fila> filas = FXCollections.observableArrayList();/*agrupás todas las ventas del día en una
                                                                                lista para pasarlas juntas a la tabla y
                                                                                mostrarlas en pantalla */

        List<Map<String, Object>> ventas = backend.cargarVentasDelDia(fecha);/*Carga las ventas del día
                                                                             desde el backend */

        for (Map<String, Object> dto : ventas) {/*Para cada venta (cada Map osea por ejemplo monto 500
                                                descripcion empanada estado debito) guardada en ventas,
                                                el código entra al bloque */

            BigDecimal monto = ConvertirABigDecimal(dto.get("monto"));/*Toma el monto de la venta y se asegura de
                                                                          convertirlo correctamente a un número usable
                                                                          en la tabla*/

            TipoDePago tipo = ConvertirATipoDePago(dto.get("estado"));/*Toma el estado de la venta (que es el tipo de pago) y se asegura de convertirlo
                                                               correctamente a un valor del enum TipoDePago*/

            Ventas.Fila f = new Ventas.Fila();/*Crea una nueva fila vacía (f) que va a representar una
                                              venta en la tabla */

            f.setNombre((String) dto.getOrDefault("nombre", ""));/*Agarra el valor que
                                                                                  tenga el campo nombre en
                                                                                  esta venta y guardalo en
                                                                                  la fila de la tabla*/

            f.setDescripcion((String) dto.getOrDefault("descripcion", ""));/*Agarra el valor que tenga el
                                                                                             campo descripcion en esta venta
                                                                                             y guardalo en la fila de la tabla*/
            f.setMonto(monto);/*Pone el monto que se calculó antes en la fila de la tabla*/
            f.setEstado(tipo);/*Pone el estado que se calculó antes en la fila de la tabla*/
            f.setObservaciones((String) dto.getOrDefault("observaciones", ""));/*Agarra el valor que tenga el
                                                                                             campo observaciones en esta venta
                                                                                             y guardalo en la fila de la tabla*/

            filas.add(f);/*Agrega la fila f a la lista de filas que se van a mostrar en la tabla*/

            if (tipo != TipoDePago.DEBE) {/*si el tipo de pago no es "DEBE" */

                totalDia = totalDia.add(monto);/*entonces sumá el monto de esta venta al total del día,
                                               porque el "DEBE" no es plata que entra ese día, sino que
                                               es plata que se va a cobrar después, entonces no se cuenta
                                               en el total del día*/
            }

        }

        Tabla tabla = new Tabla(filas, LOCALE_AR,
                null, null);/*Creá una tabla nueva que muestre estas ventas,
                                                         usando formato argentino, sin acciones especiales
                                                         ni configuraciones extra */
        tabla.setSoloLectura(true);/*Establece que la tabla sea de solo lectura asi evitar
                                                cualquier modificacion*/

        Label lblTotalDia = new Label("Total: " + MONEDA.format(totalDia));/*Creá un Label que muestre el
                                                                           total de ventas del día,
                                                                           formateado como moneda argentina*/
        lblTotalDia.setMaxWidth(Double.MAX_VALUE);/*le dice al label que se expanda para utilizar todo el
                                                  espacio disponible*/

        lblTotalDia.setAlignment(Pos.CENTER_RIGHT);/*alinea el texto del label para que se muestre alineado
                                                   a la derecha, centrado en vertical*/

        lblTotalDia.getStyleClass().add("total-dia");/*Agrega la clase CSS "total-dia" al label para
                                                        aplicarle estilos*/

        return new VBox(6, tituloDia, tabla, lblTotalDia);/*Devuelve un contenedor vertical (VBox)
                                                                   que contiene el título del día, la
                                                                   tabla y el label del total*/
    }

    private BigDecimal ConvertirABigDecimal(Object o) {
        if (o instanceof BigDecimal bd) {/*si el parametro que se me pasa es un big decimal */

            return bd.setScale(2, RoundingMode.HALF_UP);/*lo formateo para que tenga 2 decimales
                                                                  y lo devuelvo */
        }
        if (o instanceof Number n) {/*si el parametro que se me pasa es un número y no un big decimal */
            return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);/*lo convierto en un big decimal
                                                                                                  con 2 decimales y lo devuelvo */
        }
        return BigDecimal.ZERO;/*si el parametro no es ni un big decimal ni un número, devuelvo cero para
                               evitar errores*/
    }

    private TipoDePago ConvertirATipoDePago(Object o) {
        if (o instanceof TipoDePago t) {/*si el parametro que se me pasa es un tipo de pago */
            return t;/*lo retorno tal cual */
        }
        if (o instanceof String s) {/*si el parametro que se me pasa es un string */

            return TipoDePago.valueOf(s);/*intenta convertir el string en un valor del enum TipoDePago,
                                             por ejemplo "DEBITO" se convierte en TipoDePago.DEBITO */
        }
        return TipoDePago.DEBE;/*si el parametro no es ni un tipo de pago ni un string, devuelvo el valor
                               por defecto */
    }

    private void actualizarTotalSemana(LocalDate lunes) {

        VentaResumenDiarioDTO total = new VentaResumenDiarioDTO(null);/*crea un objeto que se va a
                                                                            usar para acumular (sumar) los
                                                                            totales de toda la semana */

        for (int i = 0; i < 5; i++) {/*for que da 5 vueltas, una para cada día de la semana */

            LocalDate dia = lunes.plusDays(i);/*Hace que, en cada vuelta del for, el día vaya avanzando
                                              desde el lunes hacia los siguientes días de la semana */

            for (var ventaIndividual : backend.cargarVentasDelDia(dia)) {/*recorre una por una todas las ventas que hubo
                                                           en un día específico */

                double monto = ((Number) ventaIndividual.get("monto")).doubleValue();/*obtiene el monto de una venta y
                                                                                         lo transforma en un número usable */
                TipoDePago tipo = (TipoDePago) ventaIndividual.get("estado");/*obtiene el tipo de pago de una venta */

                switch (tipo) {/*Según el tipo de pago de la venta, se suma el monto al total correspondiente*/

                    case EFECTIVO ->/*si es efectivo */
                        total.setEfectivo(total.getEfectivo() + monto);/*Si la venta fue en efectivo, se
                                                                       suma al total de efectivo */
                    case DEBITO ->/*si es débito */
                        total.setDebito(total.getDebito() + monto);/*se suma al total de débito */
                    case CREDITO ->/*si es crédito */
                        total.setCredito(total.getCredito() + monto);/*se suma al total de crédito */
                    case TRANSFERENCIA ->/*si es transferencia */
                        total.setTransferencia(total.getTransferencia() + monto);/*se suma al total de
                                                                                 transferencia */
                    case MERCADO_PAGO ->/*si es Mercado Pago */
                        total.setMercadoPago(total.getMercadoPago() + monto);/*se suma al total de Mercado
                                                                             Pago */
                    case DEBE ->/*si es deuda */
                        total.setDebe(total.getDebe() + monto);/*Si aun no se ha pagado, se suma al total
                                                               de deuda */
                }
                if (tipo != TipoDePago.DEBE) {/*si el tipo de pago no es debe */

                    total.setVentaTotal(total.getVentaTotal() + monto);/*se suma al total de ventas */
                }
            }
        }
        filaTotalSemana.setAll(total);/*La lista que usa la tabla del total semanal ahora debe mostrar
                                      este nuevo total calculado*/
    }

    private TableColumn<VentaResumenDiarioDTO, LocalDate> colFecha() {
        TableColumn<VentaResumenDiarioDTO, LocalDate> col = new TableColumn<>("Fecha");/*Columna que muestra la fecha del día */

        col.setCellValueFactory(filaDeTotales -> new javafx.beans.property.SimpleObjectProperty<>(
                filaDeTotales.getValue().getFecha()));/*Para cada fila de la tabla, obtené el objeto
                                                      VentaResumenDiarioDTO, sacale la fecha, y usá esa fecha 
                                                      como valor de la celda de esta columna*/

        col.setCellFactory(ColumnaDeTabla -> new TableCell<>() {/*A esta columna le defino yo cómo se crea y cómo se
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
                            fecha.getMonth().getDisplayName(TextStyle.FULL, LOCALE_AR/*obtiene el nombre del mes en formato completo y en español de Argentina*/
                            )
                    ));
                    getStyleClass().clear();/*limpia cualquier estilo previo de la celda para que no se acumulen estilos al actualizar el contenido de la celda*/
                }
            }
        });
        col.setSortable(false);/*desactiva la opción de ordenar la tabla por esta columna, ya que no tiene sentido ordenar por fecha en este caso*/
        return col;/*retorna la columna configurada para mostrar la fecha en la tabla */
    }

    private TableColumn<VentaResumenDiarioDTO, Double> colDebe() {

        TableColumn<VentaResumenDiarioDTO, Double> col = new TableColumn<>("Debe");/*Crea una columna nueva de la tabla,
                                                                                         llamada “Debe”, que va a mostrar números
                                                                                         (Double) del resumen diario */

        col.setCellValueFactory(filaDeTotales
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        filaDeTotales.getValue().getDebe()));/*Para cada fila de la tabla, obtené el objeto
                                                 VentaResumenDiarioDTO, sacale el valor del debe, y usá
                                                 ese valor como valor de la celda de esta columna*/

        col.setCellFactory(ColumnaDeTabla -> new TableCell<>() {/*A esta columna le defino yo cómo se ve cada celda*/
            @Override
            protected void updateItem(Double v, boolean empty) {/*Este método se llama cada vez que una
                                                                celda necesita actualizar su contenido*/

                super.updateItem(v, empty);/*Llama al método de la clase padre para actualizar el contenido
                                           de la celda*/
                setAlignment(Pos.CENTER);/*Alinea el texto al centro de la celda*/

                if (empty || v == null) {/*si la celda está vacía o el valor es null*/

                    setText("");/*deja la celda vacia*/

                    setTextFill(null);/*le quita el color, para evitar que herede el color rojo si
                                             antes tenia deuda*/

                } else {/* no esta vacia osea tiene contenido */
                    setText(MONEDA.format(v));/*lo formatea como dinero argentino */

                    if (v > 0) {/* si el valor es mayor a cero*/
                        setTextFill(javafx.scene.paint.Color.RED);/*establece el color del texto como rojo */
                    } else {/*sino*/
                        setTextFill(javafx.scene.paint.Color.BLACK);/*establece el color del texto como
                                                                    negro */
                    }
                }
            }
        });
        col.setSortable(false);/*desactiva la opción de ordenar la tabla*/
        return col;/*retorna la columna configurada */
    }

    private TableColumn<VentaResumenDiarioDTO, Double> colMonto(String titulo,
            Function<VentaResumenDiarioDTO, Double> ExtractorDeMonto) {

        TableColumn<VentaResumenDiarioDTO, Double> col = new TableColumn<>(titulo);/*Crea una columna*/

//en otras palabras, esta línea le dice a la columna qué número del resumen diario mostrar en cada fila.
        col.setCellValueFactory(filaDeTotales->
              new javafx.beans.property.SimpleObjectProperty<>(/*define qué valor se muestra en esta
                                                                columna para cada fila */

                        ExtractorDeMonto.apply(filaDeTotales.getValue())));/*Extrae el monto correspondiente
                                                                           a esta columna desde el resumen
                                                                           del día*/

        col.setCellFactory(ColumnaDeTabla -> new TableCell<>() {/*Define cómo se muestra cada celda de esta columna*/
            @Override
            protected void updateItem(Double v, boolean empty) {/*Este método se llama cada vez que una
                                                                celda necesita actualizar su contenido*/

                super.updateItem(v, empty);/*Llama al método de la clase padre para actualizar el
                                           contenido de la celda*/

                setAlignment(Pos.CENTER);/*Alinea el texto al centro de la celda*/

                setText(empty || v == null ? "" : MONEDA.format(v));/*establece que si la celda esta vacia
                                                                    o es null se vea "" sino formatea el
                                                                    numero como dinero argentino */
            }
        });

        col.setSortable(false);/*desactiva la opción de ordenar la tabla*/
        return col;/*retorna la columna configurada */
    }
}
