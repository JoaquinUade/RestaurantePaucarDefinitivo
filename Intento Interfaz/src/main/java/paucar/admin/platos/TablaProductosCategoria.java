package paucar.admin.platos;
import javafx.scene.control.Label;
import com.uade.tpo.demo.entity.Categoria;
import com.uade.tpo.demo.entity.Producto;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TablaProductosCategoria {
    
    private final static Locale localeAR = Locale.forLanguageTag("es-AR");
    private final static  NumberFormat formatoAR = NumberFormat.getCurrencyInstance(localeAR);

        public static VBox crearTablaPorCategoria(String titulo, Categoria categoria, List<Producto> productos,
                                            BiConsumer<GridPane, Producto> onSelect) {

        Label lblTitulo = new Label(titulo);/*creo un titulo visual y lo guardo en la variable lbltitulo */
        lblTitulo.getStyleClass().add("titulo-categoria");/*le pone estilos al titulo de la categoria */

        lblTitulo.setMaxWidth(Double.MAX_VALUE);/*Le digo al Label que puede ocupar todo el ancho posible,
                                                sin límite */

        lblTitulo.setAlignment(Pos.CENTER);/*lo pongo centrado */

        VBox listaProductos = new VBox(4);/*creo una caja vertical y la llamo lista de producto
                                                   con 4px de espaciado*/
        listaProductos.setPadding(new Insets(5));/*le da un padding alrededor de 5px */

        productos.stream().filter(p -> p.getCategoria() == categoria)
                .forEach(p -> {/*Toma la lista de productos, filtra los que pertenecen a una categoría
                               y hace algo con cada uno*/

                    GridPane fila = crearFilaProducto(p);/*Crea una fila visual (un componente) para un
                                                         producto y la guarda en la variable fila*/

                    fila.setOnMouseClicked(e -> onSelect.accept(fila, p));

                    listaProductos.getChildren().add(fila);/*Añade la fila a listaProductos, haciéndola
                                                           visible en pantalla*/
                });

        VBox contenedor = new VBox(8, lblTitulo, listaProductos);/*crea un vbox que muestra la
                                                                         listaproductos visible con su
                                                                         titulo, teniendo un espaciado
                                                                         entre ellos de 8px */

        contenedor.setPadding(new Insets(10));/*añade relleno de 10px alrededor del
                                                                 vbox*/
        contenedor.setMaxWidth(Double.MAX_VALUE);/*Permite que el contenedor se estire al máximo ancho
                                                  disponible */
        contenedor.setFocusTraversable(false);/*evita que el contenedor entero pueda ser focuseable
                                                    (se ve mal creeme) */
        contenedor.getStyleClass().add("categoria-listaproductos");/*le da estilo a la categoria con la
                                                                      lista de productos */
        return contenedor;/*retorna el contenedor */
    }
    
    public static GridPane crearFilaProducto(Producto p) {

        Label lblNombre = new Label(p.getNombre());/*crea un texto visible con el nombre del producto */

        lblNombre.setWrapText(true);/*Permite que el texto del label se divida en varias líneas */
        lblNombre.setMaxWidth(Double.MAX_VALUE);/*hace que el lbl nombre del producto abarque el mayor
                                                tamaño posible */

        Label lblPrecio = new Label(formatoAR.format(p.getPrecio()));/* Crea un texto (Label) con el
                                                                     precio del producto formateado */
        lblPrecio.setAlignment(Pos.CENTER_RIGHT);/*Alinea el texto del precio hacia la derecha y centrado
                                                 verticalmente */

        GridPane fila = new GridPane();/*crea un gridpane llamado fila */
        fila.setHgap(0);/*define una distancia de 10px entre los elementos del gridpane */
        fila.setPadding(new Insets(4, 2, 4, 2));

        ColumnConstraints colNombre = new ColumnConstraints();/*Configura la columna del nombre del producto */

        colNombre.setHgrow(Priority.ALWAYS);/*hace que la columna del nombre se expanda y ocupe todo el
                                            espacio disponible */

        ColumnConstraints colPrecio = new ColumnConstraints();/*configura la columna del precio */

        colPrecio.setMinWidth(90);/*evita que la columna del precio se haga más chica que 90 píxeles*/

        colPrecio.setHgrow(Priority.NEVER);/*La columna mantiene su tamaño fijo y no crece */

        fila.getColumnConstraints().addAll(colNombre, colPrecio);/*mete en la caja fila la columna nombre
                                                                 y columna precio */

        fila.add(lblNombre, 0, 0);/*posiciona el label del nombre en la columna 0,
                                                        fila 0 del GridPane */
        fila.add(lblPrecio, 1, 0);/*posiciona en la columna 1 pero fila 0 al precio
                                                        del gridpane*/

        GridPane.setHalignment(lblPrecio, javafx.geometry.HPos.RIGHT);/*Ubica el label del precio alineado
                                                                      a la derecha en la columna */

        fila.getStyleClass().add("raya-fila");/*stylea la fila para que tenga una raya dividiendo los
                                                platillos*/

        return fila;/*retorna fila*/
    }
}
