package paucar.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import paucar.service.AdminService;

public class Admin extends BorderPane {
        private final AdminService adminService;

        public Admin(AdminService adminService) {
                this.adminService = adminService;
                getStylesheets().add(getClass().getResource("/admin.css").toExternalForm());/*Cargar CSS específico
                                                                                                   para Admin*/
                botones();/* Crea los botones de la interfaz de administración */
        }

        private void botones() {
                GridPane grid = new GridPane();/*Crea un contenedor GridPane para organizar componentes
                                                en forma de grilla (filas y columnas)*/
                grid.getStyleClass().add("boton");
                grid.setAlignment(Pos.CENTER);/* Centra el contenido del GridPane */

                grid.setPadding(new Insets(40));/* Establece el relleno del GridPane */

                Button btnPlatos = crearTarjeta("PLATOS", "/img/platos.png");/*Crea un botón con una tarjeta para los
                                                                                                platos*/

                btnPlatos.setOnAction(click -> {/* cuando se presione el boton platos */
                        marcarActivo(btnPlatos);/* Marca el botón de platos como activo (cambia su estilo) */
                        setCenter(new Platos(adminService));/* entra en la vista de platos */
                });

                grid.add(btnPlatos, 0, 0);
                setCenter(grid);
        }

        private Button crearTarjeta(String titulo, String rutaIcono) {
                Image img = new Image(getClass().getResourceAsStream(rutaIcono));/* Carga la imagen del icono */

                ImageView icono = new ImageView(img);/* Crea un ImageView para mostrar el icono */

                icono.setFitWidth(90);/* Establece el ancho fit del icono */
                icono.setFitHeight(90);/* Establece el alto fit del icono */

                icono.setPreserveRatio(true);/*Mantiene la proporción de la imagen evitando que
                                                   se deforme*/

                Button btn = new Button(titulo);/* Crea un botón con el texto especificado */

                btn.setGraphic(icono);/* Establece el icono como gráfico del botón */

                btn.setContentDisplay(ContentDisplay.TOP);/* Establece la posición del contenido del botón */

                btn.getStyleClass().add("admin-card");/*Agrega la clase CSS para el estilo de la
                                                         tarjeta*/
                return btn;
        }

        private void marcarActivo(Button activo, Button... otros) {
                if (!activo.getStyleClass().contains("active")) {
                        activo.getStyleClass().add("active");
                }
                for (Button b : otros) {
                        b.getStyleClass().remove("active");
                }
        }
}