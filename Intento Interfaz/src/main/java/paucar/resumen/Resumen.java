package paucar.resumen;

import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import paucar.service.VentasBackend;

public class Resumen extends BorderPane {

    private final VentasBackend backend;

    // filtros
    private final ComboBox<String> comboTipo = new ComboBox<>();
    private final ComboBox<Integer> comboAnio = new ComboBox<>();
    private final ComboBox<Integer> comboMes = new ComboBox<>();
    private final javafx.scene.control.DatePicker pickerDia = new javafx.scene.control.DatePicker();

    // acá se va a mostrar mensual o semanal
    private final BorderPane contenedorResultado = new BorderPane();

    public Resumen(VentasBackend backend) {
        this.backend = backend;

        setPadding(new Insets(16));

        initFiltros();
        setTop(crearBarraFiltros());
        setCenter(contenedorResultado);
    }

    private void initFiltros() {
        comboTipo.getItems().addAll("Mensual", "Semanal");
        comboTipo.setValue("Mensual");

        int anioActual = LocalDate.now().getYear();
        comboAnio.getItems().addAll(anioActual - 1, anioActual, anioActual + 1);
        comboAnio.setValue(anioActual);

        for (int i = 1; i <= 12; i++) {
            comboMes.getItems().add(i);
        }
        comboMes.setValue(LocalDate.now().getMonthValue());
        pickerDia.setValue(LocalDate.now());
    }

    private Node crearBarraFiltros() {

        Button btnVer = new Button("Ver");

        btnVer.setOnAction(e -> aplicarFiltros());

        HBox barra = new HBox(10,
                comboTipo,
                comboAnio,
                comboMes,
                pickerDia,
                btnVer
        );

        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(0, 0, 10, 0));

        return barra;
    }

    private void aplicarFiltros() {

        String tipo = comboTipo.getValue();
        int anio = comboAnio.getValue();
        int mes = comboMes.getValue();

        if ("Mensual".equals(tipo)) {
            contenedorResultado.setCenter(
                    new Mensual(backend, anio, mes)
            );
        } else {
            LocalDate diaSeleccionado = pickerDia.getValue();

            Semanal semanal = new Semanal(backend, anio, mes);
            semanal.cargarSemanaDesde(diaSeleccionado);

            contenedorResultado.setCenter(semanal);
        }
    }
}
