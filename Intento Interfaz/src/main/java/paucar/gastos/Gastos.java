package paucar.gastos;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import paucar.gastos.Variables.GastosVariablesView;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;

public class Gastos extends BorderPane {

    private final GastosVariablesService gastosVService;
    private final CategoriasGastosService categoriasService;

    public Gastos(GastosVariablesService gastosVService, CategoriasGastosService categoriasService) {
        this.gastosVService = gastosVService;
        this.categoriasService = categoriasService;
        getStylesheets().add(getClass().getResource("/admin.css").toExternalForm());/*Cargar CSS específico
                                                                                         para Admin*/
        botones();
    }

    private void botones() {
        GridPane grid = new GridPane();/*Crea un contenedor GridPane para organizar componentes
                                        en forma de grilla (filas y columnas)*/
        grid.getStyleClass().add("boton");
        grid.setAlignment(Pos.CENTER);/* Centra el contenido del GridPane */

        grid.setPadding(new Insets(40));/* Establece el relleno del GridPane */
        grid.setHgap(20);
        /* Establece el espacio horizontal entre los elementos del GridPane */

        Button btnGastosVariables = crearTarjeta("VARIABLES", "/img/platos.png");
        btnGastosVariables.setOnAction(click -> {
            marcarActivo(btnGastosVariables);
            setCenter(new GastosVariablesView(gastosVService, categoriasService));
        });

        grid.add(btnGastosVariables, 0, 0);
        setCenter(grid);
    }

    private Button crearTarjeta(String titulo, String rutaIcono) {
        Image img = new Image(getClass().getResourceAsStream(rutaIcono));/* Carga la imagen del icono */

        ImageView icono = new ImageView(img);/* Crea un ImageView para mostrar el icono */

        icono.setFitWidth(90);/* Establece el ancho fit del icono */
        icono.setFitHeight(90);/* Establece el alto fit del icono */

        icono.setPreserveRatio(true);/*
                                              * Mantiene la proporción de la imagen evitando que
                                              * se deforme
         */

        Button btn = new Button(titulo);/* Crea un botón con el texto especificado */

        btn.setGraphic(icono);/* Establece el icono como gráfico del botón */

        btn.setContentDisplay(ContentDisplay.TOP);/* Establece la posición del contenido del botón */

        btn.getStyleClass().add("admin-card");/*
                                                       * Agrega la clase CSS para el estilo de la
                                                       * tarjeta*/
        return btn;
    }

    private void marcarActivo(Button activo, Button... otros) {
        if (!activo.getStyleClass().contains("active")) {
            /* si no esta activo el color del boton */
            activo.getStyleClass().add("active");
            /* le activa el color */
        }
        for (Button b : otros) {
            b.getStyleClass().remove("active");
            /* desactiva los otros botones */
        }
    }
}
