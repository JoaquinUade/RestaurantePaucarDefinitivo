package paucar.stock;

import javafx.scene.control.Label;

public final class TituloPanel extends Label {

    public TituloPanel(String texto) {

        super(texto);

        getStyleClass().add("card-header");
        setMaxWidth(Double.MAX_VALUE);
    }
}