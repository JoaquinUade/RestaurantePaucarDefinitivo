package paucar.resumen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.uade.tpo.demo.entity.TipoDePago;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.service.VentasBackend;

public class Mensual extends BorderPane {

    private static final Locale LOCALE_AR = Locale.of("es", "AR");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(LOCALE_AR);

    private final VentasBackend backend;

    // Contenedor principal scrolleable
    private final VBox contenido = new VBox(6);

    // Acumuladores del mes
    private BigDecimal totalMes = BigDecimal.ZERO;
    private BigDecimal debeMes = BigDecimal.ZERO;
    private BigDecimal debitoMes = BigDecimal.ZERO;
    private BigDecimal creditoMes = BigDecimal.ZERO;
    private BigDecimal transferenciaMes = BigDecimal.ZERO;
    private BigDecimal mpMes = BigDecimal.ZERO;
    private BigDecimal efectivoMes = BigDecimal.ZERO;

    public Mensual(VentasBackend backend, int anio, int mes) {
        this.backend = backend;
        setPadding(new Insets(16));
        initUI();
        cargarMes(anio, mes);
    }

    // =========================
    // UI base
    // =========================
    private void initUI() {

        Label titulo = new Label("Resumen Mensual");
        titulo.getStyleClass().add("title-xl");

        // barra superior con título y separador (opcional: selector de mes/año luego)
        var sep = new Region();
        HBox.setHgrow(sep, Priority.ALWAYS);
        var header = new HBox(12, titulo, sep);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setTop(header);
        setCenter(scroll);
    }

    // =========================
    // Carga del mes (pide a backend día x día y arma filas)
    // =========================
    private void cargarMes(int anio, int mes) {
        contenido.getChildren().clear();

        // Resetear acumuladores mensuales
        totalMes = BigDecimal.ZERO;
        debeMes = BigDecimal.ZERO;
        debitoMes = BigDecimal.ZERO;
        creditoMes = BigDecimal.ZERO;
        transferenciaMes = BigDecimal.ZERO;
        mpMes = BigDecimal.ZERO;
        efectivoMes = BigDecimal.ZERO;

        // Encabezado tipo Excel
        contenido.getChildren().add(crearEncabezado());

        LocalDate fecha = LocalDate.of(anio, mes, 1);

        while (fecha.getMonthValue() == mes) {
            // saltear fines de semana
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY
                    && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                agregarDia(fecha);
            }
            fecha = fecha.plusDays(1);
        }

        // Fila TOTAL MES
        contenido.getChildren().add(crearFilaTotalMes());
    }

    // =========================
    // Encabezado (nombres de columnas)
    // =========================
    private GridPane crearEncabezado() {
        GridPane grid = baseGrid();
        // Columnas: Fecha + 7 montos
        int c = 0;
        grid.add(celdaHeader("Fecha"), c++, 0);
        grid.add(celdaHeader("V. Total"), c++, 0);
        grid.add(celdaHeader("Debe"), c++, 0);
        grid.add(celdaHeader("Débito"), c++, 0);
        grid.add(celdaHeader("Crédito"), c++, 0);
        grid.add(celdaHeader("Transferencia"), c++, 0);
        grid.add(celdaHeader("MERCADO_PAGO"), c++, 0);
        grid.add(celdaHeader("Efectivo"), c, 0);
        return grid;
    }

    // =========================
    // Agregar una fila de día (cálculo real + render)
    // =========================
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
        fila.setStyle("-fx-background-color: #fff2cc; -fx-font-weight: bold;"); // amarillo suave

        int c = 0;
        Label lbl = new Label("TOTAL MES");
        lbl.setPadding(new Insets(4, 8, 4, 8));
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

    // =========================
    // Helpers visuales
    // =========================
    private GridPane baseGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(2);
        grid.setPadding(new Insets(2));
        // 8 columnas: fijamos anchos mínimos para aspecto tabla
        // (opcional: ajustar a gusto o mover a CSS)
        return grid;
    }

    private Label celdaHeader(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-background-color: #e6e6e6; -fx-padding: 6 8;");
        return l;
    }

    private Label celdaFecha(LocalDate f) {
        String s = String.format("%02d-%s",
                f.getDayOfMonth(),
                f.getMonth().name().substring(0, 3).toLowerCase(LOCALE_AR));
        Label l = new Label(s);
        l.setStyle("-fx-padding: 6 8; -fx-font-weight: bold; -fx-background-color: #f5f5f5;");
        return l;
    }

    private Label celdaMonto(BigDecimal v) {
        Label l = new Label(format(v));
        l.setStyle("-fx-padding: 6 8; -fx-background-color: white;");
        l.setAlignment(Pos.CENTER_RIGHT);
        return l;
    }

    private Label celdaMontoRoja(BigDecimal v) {
        Label l = new Label(format(v));
        l.setStyle("-fx-padding: 6 8; -fx-background-color: #fde2e1; -fx-text-fill: #9b2226; -fx-font-weight: bold;");
        l.setAlignment(Pos.CENTER_RIGHT);
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
