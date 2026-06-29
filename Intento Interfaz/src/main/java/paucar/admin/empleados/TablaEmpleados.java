package paucar.admin.empleados;

import com.uade.tpo.demo.entity.Empleado;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TablaEmpleados extends VBox {
    private final ListView<Empleado> lista;

    public TablaEmpleados(String tituloTexto) {
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
            protected void updateItem(Empleado item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label lblNombre = new Label(item.getNombre());
                    lblNombre.getStyleClass().add("nombre-empresasclientes");
                    HBox top = new HBox(10, lblNombre);
                    VBox card = new VBox(5, top);
                    card.setPadding(new Insets(15));
                    card.getStyleClass().setAll("card");
                    setGraphic(card);
                }
            }
        });
    }

    public ListView<Empleado> getLista() {
        return lista;
    }
}
