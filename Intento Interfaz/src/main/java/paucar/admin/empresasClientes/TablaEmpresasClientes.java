package paucar.admin.empresasClientes;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TablaEmpresasClientes extends VBox {

    private final ListView<String> lista;

    public TablaEmpresasClientes(String tituloTexto) {

        this.lista = new ListView<>();

        Label titulo = new Label(tituloTexto);
        titulo.getStyleClass().add("card-header");

        this.getStyleClass().add("panel-card");
        this.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        configurarCelda();

        this.getChildren().addAll(titulo, lista);
    }

    private void configurarCelda() {
        lista.setCellFactory(list -> new ListCell<String>() {
            @Override
            protected void updateItem(String nombre, boolean empty) {
                super.updateItem(nombre, empty);

                if (empty || nombre == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    Label lblNombre = new Label(nombre);
                    lblNombre.getStyleClass().add("nombre-empresasclientes");

                    HBox top = new HBox(10, lblNombre);

                    VBox card = new VBox(5, top);
                    card.setPadding(new Insets(10));
                    card.getStyleClass().setAll("card");

                    setGraphic(card);
                }
            }
        });
    }

    public ListView<String> getLista() {
        return lista;
    }
}