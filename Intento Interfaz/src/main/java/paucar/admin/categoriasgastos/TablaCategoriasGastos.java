package paucar.admin.categoriasgastos;


import paucar.config.Responsive;
import com.uade.tpo.demo.entity.CategoriaGastoVariable;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class TablaCategoriasGastos extends VBox {

    private final ListView<CategoriaGastoVariable> lista;

    public TablaCategoriasGastos(String tituloTexto) {

        this.lista = new ListView<>();

        Label titulo = new Label(tituloTexto);
        titulo.getStyleClass().add("card-header");

        this.getStyleClass().add("panel-card");
        this.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        configurarCelda();

        this.getChildren().addAll(titulo, lista);
    }

    private void configurarCelda() {

        lista.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CategoriaGastoVariable item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    Label lblNombre = new Label(item.getNombre());
                    lblNombre.getStyleClass().add("nombre-empresasclientes");

                    HBox top = new HBox(Responsive.pe(10), lblNombre);
                    VBox card = new VBox(Responsive.pe(5), top);

                    card.setPadding(Responsive.insets(15));
                    card.getStyleClass().setAll("card");

                    setGraphic(card);
                }
            }
        });
    }

    public ListView<CategoriaGastoVariable> getLista() {
        return lista;
    }
}