package paucar.ventas.ui;

import com.uade.tpo.demo.entity.TipoDePago;

import javafx.scene.control.ComboBox;

public final class EstadoPagoCombo {

    private EstadoPagoCombo() {
    }

    public static ComboBox<TipoDePago> crear() {

        ComboBox<TipoDePago> cbEstado =
                new ComboBox<>();

        cbEstado.getItems().setAll(
                java.util.Arrays.stream(
                        TipoDePago.values())
                        .filter(tipo ->
                                tipo != TipoDePago.DEUDA_PAGADA)
                        .toList());

        cbEstado.setValue(
                TipoDePago.DEBE);

        return cbEstado;
    }
}
