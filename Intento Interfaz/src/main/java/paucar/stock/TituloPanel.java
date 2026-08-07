package paucar.stock;

import javafx.scene.control.Label;

public class TituloPanel extends Label {

    public TituloPanel(String texto) {

        super(texto);

        getStyleClass().add("card-header");
        setMaxWidth(Double.MAX_VALUE);
    }
}