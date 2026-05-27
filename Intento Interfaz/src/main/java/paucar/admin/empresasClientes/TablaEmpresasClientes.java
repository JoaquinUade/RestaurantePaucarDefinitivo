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
        this.setAlignment(javafx.geometry.Pos.TOP_CENTER);/*Alinea los elementos arriba y centrados */

        configurarCelda();

        this.getChildren().addAll(titulo, lista);/*Agrega el título y la lista al contenedor */
    }

    private void configurarCelda() {
        lista.setCellFactory(list -> new ListCell<String>() {
            @Override
            protected void updateItem(String nombre, boolean empty) {
                super.updateItem(nombre, empty);

                if (empty || nombre == null) {
                    setText(null);/*borra el texto de la celda */
                    setGraphic(null);/*Elimina el contenido gráfico (visual) de la celda */
                } else {

                    Label lblNombre = new Label(nombre);/*Crea un texto visual (Label) con el nombre
                                                        que recibió */
                    lblNombre.getStyleClass().add("nombre-empresasclientes");

                    HBox top = new HBox(10, lblNombre);/*guarda el nombre en una caja horizontal */

                    VBox card = new VBox(5, top);/*guarda la caja horizontal en una caja vertical
                                                llamada card */
                    card.setPadding(new Insets(15));/*le pone un relleno alrededor de 15px */
                    card.getStyleClass().setAll("card");

                    setGraphic(card);/*Muestra la tarjeta (diseño personalizado) en la celda */
                }
            }
        });
    }

    public ListView<String> getLista() {
        return lista;
    }
}