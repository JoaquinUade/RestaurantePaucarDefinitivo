package paucar.ventas;

import java.time.LocalDate;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class FormularioFactory {

    private FormularioFactory() {
    }

    public static DatePicker crearSelectorFecha() {

        DatePicker dpFecha = new DatePicker();

        dpFecha.setValue(LocalDate.now());

        return dpFecha;
    }

    public static TextField crearObservaciones() {

        TextField inputObservaciones = new TextField();

        inputObservaciones.setPromptText(
                "Observaciones (opcional)");

        return inputObservaciones;
    }
}