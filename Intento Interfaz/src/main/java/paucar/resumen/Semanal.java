package paucar.resumen;

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

public class Semanal extends BorderPane {

    private static final Locale LOCALE_AR = Locale.of("es", "AR");

    private final TableView<VentaResumenDiarioDTO> tablaTotalSemanal = new TableView<>();

    private final ObservableList<VentaResumenDiarioDTO> filaTotalSemana = FXCollections.observableArrayList();

    private static final NumberFormat MONEDA
            = NumberFormat.getCurrencyInstance(LOCALE_AR);

    private final VentasBackend backend;
    private final VBox contenido = new VBox(8);/*contenedor vertical en el que pondremos los
                                                       bloques que representan la tabla y la fecha de
                                                       ese dia */

    public Semanal(VentasBackend backend) {
        this.backend = backend;

        initUI();

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

            BigDecimal monto = safeBD(dto.get("monto"));/*Toma el monto de la venta y se asegura de
                                                            convertirlo correctamente a un número usable
                                                            en la tabla*/

            TipoDePago tipo = safeTipo(dto.get("estado"));/*Toma el estado de la venta (que es el tipo de pago) y se asegura de convertirlo
                                                            correctamente a un valor del enum TipoDePago*/

            Ventas.Fila f = new Ventas.Fila();/*Crea una nueva fila vacía (f) que va a representar una
                                              venta en la tabla */

            f.setNombre((String) dto.getOrDefault("nombre", ""));/*Le asigna a la fila el nombre de la venta, tomando el valor del Map con la clave "nombre" y asegurándose de que sea una cadena de texto, o dejando una cadena vacía si no existe esa clave*/
            f.setDescripcion((String) dto.getOrDefault("descripcion", ""));
            f.setMonto(monto);
            f.setEstado(tipo);
            f.setObservaciones((String) dto.getOrDefault("observaciones", ""));

            filas.add(f);

            // ✅ SUMAR SOLO SI NO ES DEBE
            if (tipo != TipoDePago.DEBE) {
                totalDia = totalDia.add(monto);
            }

        }

        Tabla tabla = new Tabla(
                filas,
                LOCALE_AR,
                null,
                null
        );
        tabla.setSoloLectura(true);

        Label lblTotalDia = new Label("Total: " + MONEDA.format(totalDia));
        lblTotalDia.setMaxWidth(Double.MAX_VALUE);
        lblTotalDia.setAlignment(Pos.CENTER_RIGHT);
        lblTotalDia.getStyleClass().add("total-dia");

        return new VBox(6, tituloDia, tabla, lblTotalDia);
    }

    private BigDecimal safeBD(Object o) {
        if (o instanceof BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_UP);
        }
        if (o instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private TipoDePago safeTipo(Object o) {
        if (o instanceof TipoDePago t) {
            return t;
        }
        if (o instanceof String s) {
            try {
                return TipoDePago.valueOf(s);
            } catch (Exception e) {
            }
        }
        return TipoDePago.DEBE;
    }

    public void mostrarSemana(LocalDate fechaBase) {
        cargarSemanaDesde(fechaBase);
    }

    public void cargarSemanaDesde(LocalDate diaBuscado) {

        contenido.getChildren().clear();

        // buscar el lunes de la semana del día buscado
        LocalDate lunes = diaBuscado;
        while (lunes.getDayOfWeek() != DayOfWeek.MONDAY) {
            lunes = lunes.minusDays(1);
        }
        actualizarTotalSemana(lunes);

        List<LocalDate> semana = List.of(
                lunes,
                lunes.plusDays(1),
                lunes.plusDays(2),
                lunes.plusDays(3),
                lunes.plusDays(4)
        );

        int indice = semana.indexOf(diaBuscado);
        if (indice == -1) {
            // si es sábado o domingo, mostramos la semana normal
            for (LocalDate d : semana) {
                agregarDia(d);
            }
            return;
        }

        // día buscado primero
        agregarDia(diaBuscado);

        // anteriores (arriba)
        for (int i = indice - 1; i >= 0; i--) {
            agregarDiaArriba(semana.get(i));
        }

        // posteriores (abajo)
        for (int i = indice + 1; i < semana.size(); i++) {
            agregarDia(semana.get(i));
        }
    }

    private void agregarDiaArriba(LocalDate fecha) {
        VBox bloque = crearBloqueDia(fecha);
        contenido.getChildren().add(0, bloque);
    }

    private void actualizarTotalSemana(LocalDate lunes) {

        VentaResumenDiarioDTO total = new VentaResumenDiarioDTO(null); // 👈 clave

        for (int i = 0; i < 5; i++) {
            LocalDate dia = lunes.plusDays(i);

            for (var v : backend.cargarVentasDelDia(dia)) {

                double monto = ((Number) v.get("monto")).doubleValue();
                TipoDePago tipo = (TipoDePago) v.get("estado");

                switch (tipo) {
                    case EFECTIVO ->
                        total.setEfectivo(total.getEfectivo() + monto);
                    case DEBITO ->
                        total.setDebito(total.getDebito() + monto);
                    case CREDITO ->
                        total.setCredito(total.getCredito() + monto);
                    case TRANSFERENCIA ->
                        total.setTransferencia(total.getTransferencia() + monto);
                    case MERCADO_PAGO ->
                        total.setMercadoPago(total.getMercadoPago() + monto);
                    case DEBE ->
                        total.setDebe(total.getDebe() + monto);
                }

                if (tipo != TipoDePago.DEBE) {
                    total.setVentaTotal(total.getVentaTotal() + monto);
                }
            }
        }

        filaTotalSemana.setAll(total);
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

        TableColumn<VentaResumenDiarioDTO, Double> col = new TableColumn<>("Debe");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        c.getValue().getDebe()
                )
        );

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setAlignment(Pos.CENTER);

                if (empty || v == null) {
                    setText("");
                    setTextFill(null); // limpia color previo
                } else {
                    setText(MONEDA.format(v));
                    if (v > 0) {
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

    private TableColumn<VentaResumenDiarioDTO, Double> colMonto(
            String titulo,
            Function<VentaResumenDiarioDTO, Double> getter) {

        TableColumn<VentaResumenDiarioDTO, Double> col = new TableColumn<>(titulo);

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        getter.apply(c.getValue())
                )
        );

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setAlignment(Pos.CENTER);
                setText(empty || v == null ? "" : MONEDA.format(v));
            }
        });

        col.setSortable(false);
        return col;
    }
}
