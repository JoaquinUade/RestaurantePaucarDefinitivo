package paucar.admin.empresasClientes;


import com.uade.tpo.demo.entity.Cliente;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.config.Responsive;

public final class TablaEmpresasClientes extends VBox {

    private final ListView<Cliente> lista;

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
        lista.setCellFactory(list -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);

                if (empty || cliente == null) {
                    setText(null);/*borra el texto de la celda */
                    setGraphic(null);/*Elimina el contenido gráfico (visual) de la celda */
                } else {

                    Label lblNombre = new Label(cliente.getNombre());/*Crea un texto visual (Label) con el nombre
                                                        que recibió */
                    lblNombre.getStyleClass().add("nombre-empresasclientes");

                    String periodicidad = cliente.getPeriodicidadPago() == null ? "Sin periodicidad"
                            : switch (cliente.getPeriodicidadPago()) {
                                case MENSUAL -> "Mensual";
                                case QUINCENAL -> "Quincenal";
                                case SEMANAL -> "Semanal";
                                case CONSUMOVARIOSDIAS -> "Consumo varios días";
                            };
                    // Solo presentación: no modificar el nombre ni el objeto del cliente.
                    Label lblPeriodicidad = new Label("Periodicidad: " + periodicidad);
                    lblPeriodicidad.setStyle("-fx-text-fill: #334155; -fx-background-color: #e2e8f0;"
                            + " -fx-background-radius: 8; -fx-padding: 4 8 4 8;");
                    setText(null);
                    HBox top = new HBox(Responsive.pe(10), lblNombre);/*guarda el nombre en una caja horizontal */

                    VBox card = new VBox(Responsive.pe(5), top, lblPeriodicidad);/*guarda la caja horizontal en una caja vertical
                                                llamada card */
                    card.setPadding(Responsive.insets(15));/*le pone un relleno alrededor de 15px */
                    card.getStyleClass().setAll("card");

                    setGraphic(card);/*Muestra la tarjeta (diseño personalizado) en la celda */
                }
            }
        });
    }

    public ListView<Cliente> getLista() {
        return lista;
    }
}