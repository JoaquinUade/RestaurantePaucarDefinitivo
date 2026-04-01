package paucar.resumen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.uade.tpo.demo.entity.TipoDePago;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import paucar.service.VentasBackend;
import paucar.ventas.Tabla;
import paucar.ventas.Ventas;

public class Semanal extends BorderPane {

    private static final Locale LOCALE_AR = Locale.of("es", "AR");

    private final VentasBackend backend;
    private final VBox contenido = new VBox(8);

    public Semanal(VentasBackend backend) {
    this.backend = backend;

    setPadding(new Insets(16));
    initUI();
}

    private void initUI() {
        Label titulo = new Label("Resumen Semanal");
        titulo.getStyleClass().add("title-xl");

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);

        setTop(titulo);
        setCenter(scroll);
    }

    private void agregarDia(LocalDate fecha) {
        VBox bloque = crearBloqueDia(fecha);
        contenido.getChildren().add(bloque);
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

    private VBox crearBloqueDia(LocalDate fecha) {

        Label tituloDia = new Label(
                fecha.getDayOfWeek() + " "
                + fecha.getDayOfMonth() + "/"
                + fecha.getMonthValue() + "/"
                + fecha.getYear()
        );
        tituloDia.getStyleClass().add("title-md");

        ObservableList<Ventas.Fila> filas = FXCollections.observableArrayList();
        List<Map<String, Object>> ventas = backend.cargarVentasDelDia(fecha);

        for (Map<String, Object> dto : ventas) {
            Ventas.Fila f = new Ventas.Fila();
            f.setNombre((String) dto.getOrDefault("nombre", ""));
            f.setDescripcion((String) dto.getOrDefault("descripcion", ""));
            f.setMonto(safeBD(dto.get("monto"))); // ✅ usar safeBD
            f.setEstado(safeTipo(dto.get("estado")));
            f.setObservaciones((String) dto.getOrDefault("observaciones", ""));
            filas.add(f);
        }

        Tabla tabla = new Tabla(
                filas,
                LOCALE_AR,
                null,
                null
        );

        return new VBox(6, tituloDia, tabla);
    }
}
