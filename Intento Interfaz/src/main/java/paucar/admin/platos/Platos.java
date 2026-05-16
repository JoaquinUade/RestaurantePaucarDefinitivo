package paucar.admin.platos;

import com.uade.tpo.demo.entity.Categoria;
import com.uade.tpo.demo.entity.Producto;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import paucar.service.AdminService;

public class Platos extends BorderPane {

    private final AdminService adminService;
    private GridPane filaSeleccionada;
    private GridPane gridCategorias;

    private final ObjectProperty<Producto> productoSeleccionado = new SimpleObjectProperty<>();

    public Platos(AdminService adminService) {
        this.adminService = adminService;
        getStyleClass().add("platos-root");
        Label titulo = new Label("Administración de Productos");
        titulo.getStyleClass().add("administracion-de-productos");
        ubicarTablas();
        construirCategorias();
        ScrollPane scroll = crearScroll(gridCategorias);
        HBox botones = crearBotones();
        VBox contenedor = new VBox(20, titulo, scroll, botones);
        contenedor.setPadding(new Insets(20));

        setCenter(contenedor);
    }
    private ScrollPane crearScroll(GridPane grid) {
        ScrollPane scroll = new ScrollPane(grid);/*Crea un ScrollPane que contiene al grid y permite
                                                 desplazarse con barras de scroll cuando el contenido no
                                                 entra en pantalla */

        scroll.getStyleClass().add("platos-scroll");/*Agrega una clase CSS personalizada para estilos
                                                       específicos del scroll */

        scroll.setFitToWidth(true);/*Hace que el scroll se ajuste al ancho del contenedor */

        scroll.setPannable(false);/*Habilita o deshabilita la capacidad de desplazar el contenido
                                         con el ratón */

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);/*Oculta la barra de scroll horizontal */

        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);/*Muestra la barra de scroll vertical
                                                                   solo si es necesario */
        return scroll;/*Devuelve el ScrollPane creado */
    }

    private GridPane ubicarTablas(){
        gridCategorias = new GridPane();/*Crea un GridPane para organizar las tablas de productos por
                                        categoría */
        ColumnConstraints col1 = new ColumnConstraints();/*Crea una restricción para la primera columna */

        col1.setPercentWidth(33.33);/*Establece el ancho de la primera columna como el 33.33% del
                                           ancho total */

        col1.setHgrow(Priority.ALWAYS);/*Hace que la primera columna se ajuste al ancho del contenedor */

        ColumnConstraints col2 = new ColumnConstraints();/*crea una restriccion para la segunda columna*/

        col2.setPercentWidth(33.33);/*establece el ancho de la segunda columna como el 33.33% del
                                           ancho total*/

        col2.setHgrow(Priority.ALWAYS);/*hace que la segunda columna se ajuste al ancho del contenedor*/

       ColumnConstraints col3 = new ColumnConstraints();/*crea uan restricción para la tercera columna*/
        col3.setPercentWidth(33.33);/*establece el ancho de la tercera columna como el 33.33% del
                                           ancho total*/
        col3.setHgrow(Priority.ALWAYS);/*hace que la tercera columna se ajuste al ancho del contenedor*/

        gridCategorias.getColumnConstraints().addAll(col1, col2, col3);/*le agrega las restricciones al
                                                                      conjunto de 3 columnas */

        gridCategorias.setHgap(20);/*Dejá 20 píxeles de espacio horizontal entre cada columna de
                                         la grilla */
        gridCategorias.setVgap(20);/*deja 20 pixeles de espacio vertical entre cada columna de
                                          la grilla */
        gridCategorias.setPadding(new Insets(10));/*Dejá un espacio de 10 píxeles entre
                                                                     el borde de la grilla y el contenido */
        return gridCategorias;/* retorna gridCategorias*/
    }
    private HBox crearBotones() {
        Button btnCrear = new Button("Crear");/*Estoy creando un botón que dice ‘Crear’ y lo guardo
                                                    en una variable llamada btnCrear*/
        btnCrear.getStyleClass().add("btn-crear");

        btnCrear.setOnAction(e -> {/*Cuando el usuario haga clic en el botón Crear*/
            Producto p = DialogBotones.abrirDialogCrear();

            if (p != null) {
                adminService.crearProducto(p);
                refrescarVista();
            }
        });

        Button btnEditar = new Button("Editar");/*creo un boton que dice editar y lo guardo en
                                                     btnEditar */
        btnEditar.getStyleClass().add("btn-editar");
        btnEditar.setOnAction(e -> {
            Producto seleccionado = productoSeleccionado.get();/*pone el productoseleccionado en la
                                                               variable seleccionado */

            if (seleccionado == null) {/*si el seleccionado es null */
                new Alert(Alert.AlertType.WARNING,"Seleccioná un producto para editar")
                        .showAndWait();/*Muestra un cartel de advertencia diciendo que hay que
                                      seleccionar un producto antes de editar */
                return;
            }
            Producto editado = DialogBotones.abrirDialogEditar(seleccionado);
            if (editado != null) {
                adminService.editarProducto(editado);
                refrescarVista();/*refresca la vista para que se actualice y se vean los cambios */
            }
        });
        Button btnEliminar = new Button("Eliminar");/*crea el boton con el texto eliminar y lo pone
                                                          en la variable btnEliminar */
        btnEliminar.getStyleClass().add("btn-eliminar");
        btnEliminar.setOnAction(e -> {
    Producto seleccionado = productoSeleccionado.get();

    if (seleccionado == null) {
        new Alert(Alert.AlertType.WARNING,"Seleccioná un producto para eliminar")
            .showAndWait();
        return;
    }

    if (DialogBotones.confirmarEliminacion()) {
        adminService.eliminarProducto(seleccionado.getIdProducto());
        refrescarVista();
    }
});
        HBox botones = new HBox(10, btnCrear, btnEditar, btnEliminar);/*pone los 3 botones en
                                                                               una caja horizontal llamada
                                                                                botones */
        botones.setPadding(new Insets(0));/*le pone un padding alrededor de botones
                                                              de 10px */                                                         
        btnCrear.setFocusTraversable(false);
        btnEditar.setFocusTraversable(false);
        btnEliminar.setFocusTraversable(false);

        return botones;/*retorna botones */
    }

    private void construirCategorias() {
        gridCategorias.getChildren().clear();/*Borra todos los nodos del GridPane para volver a cargarlos */

        Categoria[] categorias = Categoria.values();/*obtiene todas las categorías del enum 
                                                    automáticamente y lo guarda en la variable categorias*/

        int columnas = 3;/*defino que la grilla tendra 3 columnas nada mas */

        for (int i = 0; i < categorias.length; i++) {/*Recorre todas las categorías una por una */

            int columna = i % columnas;/*Calcula en qué columna se va a ubicar el elemento dentro de la
                                       grilla, por ejemplo, si hay 3 columnas, el resultado será: 0, 1, 2,
                                       0, 1, 2... permitiendo que los elementos se distribuyan
                                       correctamente en filas y columnas*/

            int fila = i / columnas;/*Calcula en qué fila se ubicará el elemento dentro de la grilla
                                    Usa división entera para agrupar los elementos según la cantidad de
                                    columnas Por ejemplo, si hay 3 columnas, cada 3 elementos se avanza a 
                                    la siguiente fila (0,0,0,1,1,1,...)*/

            gridCategorias.add(TablaProductosCategoria.crearTablaPorCategoria(categorias[i]
                .getDescripcion(),categorias[i], adminService.obtenerProductosAdmin(),
                (filaNodo, producto) -> {

            if (filaSeleccionada != null) {
                filaSeleccionada.getStyleClass().remove("fila-seleccionada");
                filaSeleccionada.getStyleClass().add("fila-deseleccionada");
            }

            filaNodo.getStyleClass().remove("fila-deseleccionada");
            filaNodo.getStyleClass().add("fila-seleccionada");

            filaSeleccionada = filaNodo;
            productoSeleccionado.set(producto);
         }
         ),columna, fila);
        }
    }

    private void refrescarVista() {
        productoSeleccionado.set(null);/* Limpia la selección del producto */

        filaSeleccionada = null;/* Limpia la selección de la fila */

        construirCategorias();/* Construye las categorías*/
    }
}