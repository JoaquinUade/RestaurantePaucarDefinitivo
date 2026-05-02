package paucar.admin;

import java.text.NumberFormat;
import java.util.Locale;

import com.uade.tpo.demo.entity.Categoria;
import com.uade.tpo.demo.entity.Producto;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
    Locale localeAR = Locale.forLanguageTag("es-AR");
    private final NumberFormat formatoAR = NumberFormat.getCurrencyInstance(localeAR);

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

        btnCrear.setOnAction(e -> abrirDialogCrear());/*Cuando el usuario haga clic en el botón Crear, se
                                                      ejecuta el método abrirDialogCrear() */
        Button btnEditar = new Button("Editar");/*creo un boton que dice editar y lo guardo en
                                                     btnEditar */
        btnEditar.setOnAction(e -> {

            Producto seleccionado = productoSeleccionado.get();/*pone el productoseleccionado en la
                                                               variable seleccionado */

            if (seleccionado == null) {/*si el seleccionado es null */
                new Alert(Alert.AlertType.WARNING,"Seleccioná un producto para editar")
                        .showAndWait();/*Muestra un cartel de advertencia diciendo que hay que
                                      seleccionar un producto antes de editar */
                return;
            }
            abrirDialogEditar(seleccionado);/*Muestra un cartel de advertencia diciendo que hay que
                                            seleccionar un producto antes de editar*/

            refrescarVista();/*refresca la vista para que se actualice y se vean los cambios */
        });
        Button btnEliminar = new Button("Eliminar");/*crea el boton con el texto eliminar y lo pone
                                                          en la variable btnEliminar */
        btnEliminar.setOnAction(e -> {

            Producto seleccionado = productoSeleccionado.get();/*le asigna el producto seleccionado a la
                                                               variable seleccionado */

            if (seleccionado == null) {/*si es null */
                new Alert(Alert.AlertType.WARNING,"Seleccioná un producto para eliminar")
                        .showAndWait();/*tira mensaje de error */
                return;
            }
            Long idProducto = seleccionado.getIdProducto();/*guarda el id del producto seleccionado en la
                                                           variable idproducto */

            adminService.eliminarProducto(idProducto);/*elimina el producto usando el id */

            refrescarVista();/*refresca la vista para ver los cambios reflejados */
        });
        HBox botones = new HBox(10, btnCrear, btnEditar, btnEliminar);/*pone los 3 botones en
                                                                               una caja horizontal llamada
                                                                                botones */
        botones.setPadding(new Insets(10));/*le pone un padding alrededor de botones
                                                              de 10px */
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

            gridCategorias.add(crearTablaPorCategoria(categorias[i].getDescripcion(),
                           categorias[i]), columna,fila);/*Crea un componente visual para la categoría
                                                         actual (con su nombre y objeto),y lo agrega al
                                                         GridPane en la posición correspondiente (columna,
                                                         fila) */
        }
    }
    private VBox crearTablaPorCategoria(String titulo, Categoria categoria) {

        Label lblTitulo = new Label(titulo);/*creo un titulo visual y lo guardo en la variable lbltitulo */
        lblTitulo.getStyleClass().add("titulo-categoria");/*le pone estilos al titulo de la categoria */

        lblTitulo.setMaxWidth(Double.MAX_VALUE);/*Le digo al Label que puede ocupar todo el ancho posible,
                                                sin límite */

        lblTitulo.setAlignment(Pos.CENTER);/*lo pongo centrado */

        VBox listaProductos = new VBox(4);/*creo una caja vertical y la llamo lista de producto
                                                   con 4px de espaciado*/
        listaProductos.setPadding(new Insets(5));/*le da un padding alrededor de 5px */

        adminService.obtenerProductosAdmin().stream().filter(p -> p.getCategoria() == categoria)
                .forEach(p -> {/*Toma la lista de productos, filtra los que pertenecen a una categoría
                               y hace algo con cada uno*/

                    GridPane fila = crearFilaProducto(p);/*Crea una fila visual (un componente) para un
                                                         producto y la guarda en la variable fila*/

                    fila.setOnMouseClicked(e -> {/*Define lo que va a pasar cuando el usuario hace clic en
                                                 esa fila */

                        if (filaSeleccionada != null) {/*si hay una fila seleccionada*/
                            filaSeleccionada.getStyleClass().remove("fila-seleccionada");
                            filaSeleccionada.getStyleClass().add("fila-deseleccionada");/*le da estilo a la 
                                                                                    fila seleccionada */
                        }
                        fila.getStyleClass().remove("fila-deseleccionada");
                        fila.getStyleClass().add("fila-seleccionada");
                        filaSeleccionada = fila;
                        productoSeleccionado.set(p);
                    });
                    listaProductos.getChildren().add(fila);
                });

        VBox contenedor = new VBox(8, lblTitulo, listaProductos);
        contenedor.setPadding(new Insets(10));
        contenedor.setMaxWidth(Double.MAX_VALUE);
        contenedor.setFocusTraversable(false);
        contenedor.setStyle("""
                    -fx-background-color: white;
                    -fx-border-color: #cccccc;
                    -fx-border-radius: 6;
                    -fx-background-radius: 6;
                """);
        return contenedor;
    }

    private GridPane crearFilaProducto(Producto p) {

        Label lblNombre = new Label(p.getNombre());
        lblNombre.setWrapText(true);
        lblNombre.setMaxWidth(Double.MAX_VALUE);

        Label lblPrecio = new Label(formatoAR.format(p.getPrecio()));
        lblPrecio.setAlignment(Pos.CENTER_RIGHT);

        GridPane fila = new GridPane();
        fila.setHgap(10);
        fila.setPadding(new Insets(4, 2, 4, 2));

        ColumnConstraints colNombre = new ColumnConstraints();
        colNombre.setHgrow(Priority.ALWAYS);

        ColumnConstraints colPrecio = new ColumnConstraints();
        colPrecio.setMinWidth(90);
        colPrecio.setHgrow(Priority.NEVER);

        fila.getColumnConstraints().addAll(colNombre, colPrecio);

        fila.add(lblNombre, 0, 0);
        fila.add(lblPrecio, 1, 0);

        // ✅ ESTA ES LA CLAVE
        GridPane.setHalignment(lblPrecio, javafx.geometry.HPos.RIGHT);

        fila.setStyle("""
                    -fx-border-color: #dddddd;
                    -fx-border-width: 0 0 1 0;
                """);

        fila.setOnMouseClicked(e -> {
            productoSeleccionado.set(p);
            fila.setStyle("""
                        -fx-background-color: #cce5ff;
                        -fx-border-color: #88bfff;
                        -fx-border-width: 0 0 1 0;
                    """);
        });

        return fila;
    }

    private void abrirDialogCrear() {

        Dialog<Producto> dialog = new Dialog<>();/*crea un dialog, osea una ventana emergente */
        dialog.setTitle("Crear producto");/*le pone el titulo crear producto */

        ButtonType btnGuardar = new ButtonType
                ("Guardar", ButtonBar.ButtonData.OK_DONE);/*añade el boton guardar y se le asigna el
                                                                  valor de "ok"*/
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);/*añade el boton cancelar */

        TextField txtNombre = new TextField();/*crea un campo de texto para el nombre del producto */

        txtNombre.setPromptText("Nombre");/*pone un texto de ayuda en el campo de texto */

        TextField txtPrecio = new TextField();/*crea un campo de texto para el precio del producto */

        txtPrecio.setPromptText("Precio");/*pone un texto de ayuda en el campo de texto */

        txtPrecio.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {/*Restringe lo que el usuario
                                                                                       puede escribir en el campo precio */

            if (change.getControlNewText().matches("[0-9.,]*")) {/*Verifica que el texto solo
                                                                       contenga números, puntos o comas */
                return change;/*Si el texto es válido, permite el cambio */
            }
            return null;/*Si el texto no es válido, no permite el cambio */
        }));

        ComboBox<String> cmbCategoria = new ComboBox<>();/*crea un combo box para seleccionar la categoría
                                                         del producto */
        cmbCategoria.getItems().addAll("OTROS", "ENTRADA", "BEBIDA", "MILANESAS", "WOKS",
                "SANDWICHES", "ENSALADAS", "FAJITAS", "PASTAS", "VINOS", "DESAYUNO", "GUARNICIONES",
                "CARNE", "POSTRES");/*agrega las opciones de categoría al combo box */

        VBox form = new VBox(10, new Label("Nombre"), txtNombre, new Label("Precio"),
                   txtPrecio, new Label("Categoría"), cmbCategoria);/*crea un contenedor vertical (VBox) que organiza
                                                                         los elementos del formulario uno debajo del otro,
                                                                         con un espacio de 10 píxeles entre ellos */

        form.setPadding(new Insets(10));/*pone relleno alrededor del dialog */

        dialog.getDialogPane().setContent(form);/* Establece el contenido del panel de diálogo */

        dialog.setResultConverter(btn -> { /*Según el botón presionado, devuelve el objeto creado o null*/
            if (btn == btnGuardar) {/*si el boton es guardar */
                Producto p = new Producto();/*crea un nuevo producto y le asigna los valores ingresados
                                            en el formulario */

                p.setNombre(txtNombre.getText());/*asigna el nombre ingresado al producto */

                String textoPrecio = txtPrecio.getText()
                        .replace(".", "")
                        .replace(",", ".");/*quita los puntos para eliminar separadores de miles
                                                               y las comas por puntos para convertir el texto a un
                                                               formato numérico válido */

                double precio;/*declara una variable para almacenar el precio convertido a número*/
                try {
                    precio = Double.parseDouble(textoPrecio);/*intenta convertir el texto del precio a un
                                                             número decimal */

                } catch (NumberFormatException ex) {/*si no se puede convertir el precio */

                    new Alert(Alert.AlertType.ERROR, "Precio inválido").showAndWait();/*muestra un mensaje de error*/
                    return null;/*retorna null para indicar que no se guardó el producto */
                }
                p.setPrecio(precio);/*asigna el precio convertido al producto */

                p.setCategoria(Categoria.valueOf(cmbCategoria.getValue()));/*asigna la categoría al producto */
                return p;/*retorna el producto creado */
            }
            return null;/*retorna null si no se pudo crear el producto */
        });

        dialog.showAndWait().ifPresent(p -> {/*Muestra el diálogo y, si el usuario confirma, procesa el
                                             resultado */
            adminService.crearProducto(p);/*llama al servicio para crear el producto */

            refrescarVista();/*refresca la vista para mostrar el producto creado */
        });
    }

    private void abrirDialogEditar(Producto producto) {

        Dialog<Producto> dialog = new Dialog<>();/*abre una ventana que va a retornar un objeto del tipo
                                                  producto, lo usaremos para editar el producto */
        dialog.setTitle("Editar producto");/*le pone un título */

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);/*crea un botón de guardar, el segundo parámetro
                                                                                              es para indicar que es un botón de tipo "OK" */
                                                                                              
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);/*agrega al dialog el boton guardar y el
                                                                                      boton cancelar */

        TextField txtNombre = new TextField(producto.getNombre());/*crea un campo de texto para el nombre, y
                                                                  le asigna el nombre del producto que se va
                                                                  a editar */

        TextField txtPrecio = new TextField(
                String.valueOf(producto.getPrecio()).replace(".", ","));/*Crea un TextField con el precio del producto convertido a texto,
                                                                                            reemplazando el punto decimal por coma para mostrarlo con formato
                                                                                            local */

        ComboBox<String> cmbCategoria = new ComboBox<>();/*Crea un ComboBox para seleccionar la categoría
                                                         del producto, empezando vacio*/

        cmbCategoria.getItems().addAll("OTROS", "ENTRADA", "BEBIDA", "MILANESAS", "WOKS",
                "SANDWICHES", "ENSALADAS", "FAJITAS", "PASTAS",
                "VINOS", "DESAYUNO", "GUARNICIONES", "CARNE");/*Agrega las opciones de categoría al
                                                              ComboBox */

        cmbCategoria.setValue(producto.getCategoria().name());/*Asigna al ComboBox la categoría del
                                                              producto para que aparezca seleccionada
                                                              automáticamente cuando se muestra*/

        VBox form = new VBox(10, new Label("Nombre"), txtNombre, new Label("Precio"),
                    txtPrecio, new Label("Categoría"), cmbCategoria);/*Crea un contenedor vertical
                                                                          (VBox) que organiza elementos
                                                                          uno debajo del otro */
        form.setPadding(new Insets(10));/*Establece relleno a los lados del
                                                             contenedor editar*/

        dialog.getDialogPane().setContent(form);/*Establece el contenido de la ventana */

        dialog.setResultConverter(btn -> {/*Según el botón presionado, devuelve un objeto editado o null*/
            
            if (btn == btnGuardar) {/*Si el botón presionado es guardar, se actualizan los datos del
                                    producto con los valores ingresados en el formulario */

                producto.setNombre(txtNombre.getText());/*Asigna el nombre del producto */

                String textoPrecio = txtPrecio.getText()
                        .replace(".", "")/*elimina separadores de miles */
                        .replace(",", ".");/*intercambia coma por punto para tener un
                                                                numero valido*/
                double precio;/*declara una variable para almacenar el precio convertido a número*/
                try {
                    precio = Double.parseDouble(textoPrecio);/*intenta convertir el texto a número */

                } catch (NumberFormatException ex) {/*si no se puede convertir */

                    new Alert(Alert.AlertType.ERROR, "Precio inválido").showAndWait();/*muestra un mensaje de error */
                    return null;/* retorna null para indicar que no se guardó el producto editado */
                }
                producto.setPrecio(precio);/*Asigna el precio del producto */

                producto.setCategoria(com.uade.tpo.demo.entity.Categoria.valueOf(cmbCategoria.getValue()));/*Asigna la categoría del producto */
                return producto;/*retorna el producto editado */
            }
            return null;/* retorna null para indicar que no se guardó el producto editado */
        });
        dialog.showAndWait().ifPresent(p -> {/*Muestra el diálogo y, si el usuario confirma, procesa el
                                             resultado */
            adminService.editarProducto(p);/* Llama a un método llamado editarProducto del objeto
                                           adminService, y le pasa p*/
            refrescarVista();/*Refresca la vista */
        });
    }

    private void refrescarVista() {
        productoSeleccionado.set(null);/* Limpia la selección del producto */

        filaSeleccionada = null;/* Limpia la selección de la fila */

        construirCategorias();/* Construye las categorías*/
    }
}