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

import com.uade.tpo.demo.entity.TipoDePago;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.service.VentasBackend;

public class Mensual extends BorderPane {

    private static final Locale LOCALE_AR = Locale.of("es", "AR");/*Locale es una clase que representa una configuración
                                                                                     regional específica, se utiliza para formatear los
                                                                                     números como moneda en formato argentino*/

    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(LOCALE_AR);/*NumberFormat es una clase que se utiliza para
                                                                                           formatear números como moneda, aqui se utiliza
                                                                                           para formatear los valores en formato argentino*/

    private final VentasBackend backend;

    // Contenedor principal scrolleable
    private final VBox contenido = new VBox(0);/*VBox es un layout que organiza los elementos en
                                                        una columna vertical, aqui se utiliza para
                                                        organizar las filas del resumen mensual*/

    // Acumuladores del mes
    private BigDecimal totalMes = BigDecimal.ZERO;/*inicializo la variable totalmes con el valor de cero,
                                                  esta variable se utiliza para acumular el total de
                                                  ventas del mes*/

    private BigDecimal debeMes = BigDecimal.ZERO;/*empieza en cero */
    private BigDecimal debitoMes = BigDecimal.ZERO;/*empieza en cero */
    private BigDecimal creditoMes = BigDecimal.ZERO;/*empieza en cero */
    private BigDecimal transferenciaMes = BigDecimal.ZERO;/*empieza en cero */
    private BigDecimal mpMes = BigDecimal.ZERO;/*empieza en cero */
    private BigDecimal efectivoMes = BigDecimal.ZERO;/*empieza en cero */

    public Mensual(VentasBackend backend, int anio, int mes) {
        this.backend = backend;
        setPadding(new Insets(16));

        getStylesheets().add(
                getClass().getResource("/stylemensual.css").toExternalForm()
        );

        initUI();
        cargarMes(anio, mes);
    }

    private void initUI() {

        Label titulo = new Label("Resumen Mensual");/*Label es un componente que muestra un texto,
                                                         aqui se utiliza para mostrar el título del
                                                         resumen mensual*/
        titulo.getStyleClass().add("title-xl");/*agrega la clase CSS "title-xl" al título para darle
                                                  un estilo específico*/

        var sep = new Region();/*Region es un componente vacío que se utiliza como separador entre el
                               título y otros elementos en la barra superior*/

        HBox.setHgrow(sep, Priority.ALWAYS);/*el espacio vacio a lado del titulo resumen mensual estiralo
                                            para que ocupe todo el espacio sobrante*/

        var header = new HBox(12, titulo, sep);/*ubica el titulo horizontalmente con  12 pixeles
                                                        de distancia con otros elementos y el otro es sep
                                                        que hace que complete el resto del espacio de la
                                                        pantalla, y todo eso se guarda en header*/

        header.setAlignment(Pos.CENTER_LEFT);/*posiciona el header a la izquierda */

        ScrollPane scroll = new ScrollPane(contenido);/*crea una barra de desplazamiento para el contenido
                                                      del resumen mensual*/
        scroll.setFitToWidth(true);/*Hace que el contenido del ScrollPane se estire y ocupe todo el
                                         ancho de la pantalla*/

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);/*oculta la barra de desplazamiento
                                                               horizontal*/

        setTop(header);/*coloca el header en la parte superior del BorderPane, que es el contenedor
                       principal de la interfaz*/

        setCenter(scroll);/*coloca el scroll en la parte central del BorderPane*/
    }

    private void cargarMes(int anio, int mes) {
        //contenido.getChildren().clear();/*Borra todo lo que hay dentro de contenido para iniciar vacio */

        // Resetear acumuladores mensuales
        totalMes = BigDecimal.ZERO;
        debeMes = BigDecimal.ZERO;
        debitoMes = BigDecimal.ZERO;
        creditoMes = BigDecimal.ZERO;
        transferenciaMes = BigDecimal.ZERO;
        mpMes = BigDecimal.ZERO;
        efectivoMes = BigDecimal.ZERO;

        // Encabezado tipo Excel
        contenido.getChildren().add(crearEncabezado());/*añade la fila de titulos de cada columna al
                                                       contenido*/

        LocalDate fecha = LocalDate.of(anio, mes, 1);/*Crea una variable fecha que guarda el
                                                                 primer día del mes seleccionado, y se usa
                                                                como punto de partida para recorrer todos
                                                                los días hábiles de ese mes*/

        while (fecha.getMonthValue() == mes) {/*mientras el mes de la variable fecha sea igual al mes
                                              seleccionado, se ejecuta el while*/
            // saltear fines de semana
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY
                    && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {/*si el día de la semana de la variable
                                                                  fecha no es sábado ni domingo, entonces
                                                                  se llama al método agregarDia para
                                                                  agregar una fila al resumen mensual*/
                agregarDia(fecha);
            }
            fecha = fecha.plusDays(1);/*avanza al siguiente día*/
        }
        contenido.getChildren().add(crearFilaTotalMes());/*agrega la fila con los totales del mes*/
    }

    private GridPane crearEncabezado() {
        GridPane grid = baseGrid();/*Creá una grilla con la configuración base y guardala en la variable
                                   grid */
        int columna = 0;/*inicializa la variable c en cero, esta variable se utiliza para llevar el conteo
                        de las columnas*/

        grid.add(celdaHeader("Fecha"), columna++, 0);/*Agrega una celda con el texto “Fecha”
                                                                   en la columna actual del GridPane y luego
                                                                    avanza a la siguiente columna */

        grid.add(celdaHeader("V. Total"), columna++, 0);/*lo mismo con las demás columnas */
        grid.add(celdaHeader("Debe"), columna++, 0);
        grid.add(celdaHeader("Débito"), columna++, 0);
        grid.add(celdaHeader("Crédito"), columna++, 0);
        grid.add(celdaHeader("Transferencia"), columna++, 0);
        grid.add(celdaHeader("MERCADO_PAGO"), columna++, 0);
        grid.add(celdaHeader("Efectivo"), columna, 0);
        return grid;/*retorna la grid */
    }

    private void agregarDia(LocalDate fecha) {
        // 1) Traer ventas del día (List<Map<String,Object>>) desde VentasBackend
        List<Map<String, Object>> ventasDelDia = backend.cargarVentasDelDia(fecha);

        // 2) Calcular agregados (sin crear DTOs)
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal debe = BigDecimal.ZERO;
        BigDecimal debito = BigDecimal.ZERO;
        BigDecimal credito = BigDecimal.ZERO;
        BigDecimal transferencia = BigDecimal.ZERO;
        BigDecimal mp = BigDecimal.ZERO;
        BigDecimal efectivo = BigDecimal.ZERO;

        for (Map<String, Object> dto : ventasDelDia) {
            BigDecimal monto = safeBD(dto.get("monto"));
            TipoDePago estado = safeTipo(dto.get("estado"));

            if (estado == TipoDePago.DEBE) {
                debe = debe.add(monto);
                continue; // no suma a total
            }

            total = total.add(monto);

            switch (estado) {
                case DEBITO ->
                    debito = debito.add(monto);
                case CREDITO ->
                    credito = credito.add(monto);
                case TRANSFERENCIA ->
                    transferencia = transferencia.add(monto);
                case MERCADO_PAGO ->
                    mp = mp.add(monto);
                case EFECTIVO ->
                    efectivo = efectivo.add(monto);
                default -> {
                    // nada
                }
            }
        }

        // 3) Acumular en el total del mes
        totalMes = totalMes.add(total);
        debeMes = debeMes.add(debe);
        debitoMes = debitoMes.add(debito);
        creditoMes = creditoMes.add(credito);
        transferenciaMes = transferenciaMes.add(transferencia);
        mpMes = mpMes.add(mp);
        efectivoMes = efectivoMes.add(efectivo);

        // 4) Renderizar fila
        GridPane fila = baseGrid();

        int c = 0;
        fila.add(celdaFecha(fecha), c++, 0);
        fila.add(celdaMonto(total), c++, 0);
        fila.add(celdaMontoRoja(debe), c++, 0);
        fila.add(celdaMonto(debito), c++, 0);
        fila.add(celdaMonto(credito), c++, 0);
        fila.add(celdaMonto(transferencia), c++, 0);
        fila.add(celdaMonto(mp), c++, 0);
        fila.add(celdaMonto(efectivo), c, 0);

        contenido.getChildren().add(fila);
    }

    // =========================
    // Fila TOTAL MES (al pie)
    // =========================
    private GridPane crearFilaTotalMes() {
        GridPane fila = baseGrid();
        fila.getStyleClass().add("fila-total");

        int c = 0;
        Label lbl = new Label("TOTAL MES");
        lbl.getStyleClass().add("celda");
        fila.add(lbl, c++, 0);

        fila.add(celdaMonto(totalMes), c++, 0);
        fila.add(celdaMontoRoja(debeMes), c++, 0);
        fila.add(celdaMonto(debitoMes), c++, 0);
        fila.add(celdaMonto(creditoMes), c++, 0);
        fila.add(celdaMonto(transferenciaMes), c++, 0);
        fila.add(celdaMonto(mpMes), c++, 0);
        fila.add(celdaMonto(efectivoMes), c, 0);

        return fila;
    }

    private void configurarColumnas(GridPane grid) {
        grid.getColumnConstraints().clear();

        grid.getColumnConstraints().add(new ColumnConstraints(100));  // Fecha
        grid.getColumnConstraints().add(new ColumnConstraints(100));  // V. Total
        grid.getColumnConstraints().add(new ColumnConstraints(100));  // Debe
        grid.getColumnConstraints().add(new ColumnConstraints(100));  // Débito
        grid.getColumnConstraints().add(new ColumnConstraints(100));  // Crédito
        grid.getColumnConstraints().add(new ColumnConstraints(100)); // Transferencia
        grid.getColumnConstraints().add(new ColumnConstraints(130)); // Mercado Pago
        grid.getColumnConstraints().add(new ColumnConstraints(100));  // Efectivo
    }

    // =========================
    // Helpers visuales
    // =========================
    private GridPane baseGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("tabla-grid");

        grid.setHgap(0);
        grid.setVgap(0);

        configurarColumnas(grid);
        return grid;
    }

    private Label celdaHeader(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("header-celda");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private Label celdaFecha(LocalDate f) {

        String mes = f.getMonth()
                .getDisplayName(TextStyle.SHORT, LOCALE_AR);
        String s = String.format("%02d-%s", f.getDayOfMonth(), mes);

        Label l = new Label(s);
        l.getStyleClass().addAll("celda", "celda-fecha");
        return l;

    }

    private Label celdaMonto(BigDecimal v) {
        Label l = new Label(format(v));
        l.getStyleClass().addAll("celda", "celda-monto");
        return l;
    }

    private Label celdaMontoRoja(BigDecimal v) {
        Label l = new Label(format(v));
        l.getStyleClass().addAll("celda", "celda-monto", "celda-debe");
        return l;
    }

    // =========================
    // Helpers de datos (sin DTOs)
    // =========================
    private BigDecimal safeBD(Object o) {
        if (o instanceof BigDecimal bd) {
            return bd.setScale(2, RoundingMode.HALF_UP);
        }
        if (o instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        if (o instanceof String s && !s.isBlank()) {
            try {
                // el backend a veces puede enviar string de número
                return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
            } catch (Exception ignore) {
            }
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private TipoDePago safeTipo(Object o) {
        if (o instanceof TipoDePago t) {
            return t;
        }
        if (o instanceof String s) {
            try {
                return TipoDePago.valueOf(s);
            } catch (Exception ignore) {
            }
        }
        return TipoDePago.DEBE; // por defecto (conservador)
    }

    private String format(BigDecimal v) {
        return MONEDA.format(v == null ? BigDecimal.ZERO : v);
    }
}
