package paucar.admin.platos;

import java.math.BigDecimal;

import com.uade.tpo.demo.entity.Categoria;
import com.uade.tpo.demo.entity.Producto;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class DialogBotones {
private static final String PASSWORD = "1234";

public static Producto abrirDialogCrear() {

        Dialog<Producto> dialog = new Dialog<>();/*crea un dialog, osea una ventana emergente */
        dialog.setTitle("Crear producto");/*le pone el titulo crear producto */

        ButtonType btnGuardar = new ButtonType
                ("Guardar", ButtonBar.ButtonData.OK_DONE);/*añade el boton guardar y se le asigna el
                                                                  valor de "ok"*/
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);/*añade el boton cancelar */
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña");
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

        VBox form = new VBox(10, new Label("Contraseña"), txtPass, new Label("Nombre"), txtNombre, new Label("Precio"),
                   txtPrecio, new Label("Categoría"), cmbCategoria);/*crea un contenedor vertical (VBox) que organiza los elementos del formulario
                            uno debajo del otro, con un espacio de 10 píxeles entre ellos */

        form.setPadding(new Insets(10));/*pone relleno alrededor del dialog */

        dialog.getDialogPane().setContent(form);/* Establece el contenido del panel de diálogo */

        dialog.setResultConverter(btn -> { /*Según el botón presionado, devuelve el objeto creado o null*/
            if (btn == btnGuardar) {/*si el boton es guardar */
                 if (!txtPass.getText().equals(PASSWORD)) {
        new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
        return null;
}
                Producto p = new Producto();/*crea un nuevo producto y le asigna los valores ingresados
                                            en el formulario */

                p.setNombre(txtNombre.getText());/*asigna el nombre ingresado al producto */

                String textoPrecio = txtPrecio.getText()
                        .replace(".", "")
                        .replace(",", ".");/*quita los puntos para eliminar separadores de miles
                                                               y las comas por puntos para convertir el texto a un
                                                               formato numérico válido */

                BigDecimal precio;/*declara una variable para almacenar el precio convertido a número*/
                try {
                    precio = new BigDecimal(textoPrecio);/*intenta convertir el texto del precio a un
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
        return dialog.showAndWait().orElse(null);
    }
    
    public static Producto abrirDialogEditar(Producto producto) {

        Dialog<Producto> dialog = new Dialog<>();/*abre una ventana que va a retornar un objeto del tipo
                                                  producto, lo usaremos para editar el producto */
        dialog.setTitle("Editar producto");/*le pone un título */

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);/*crea un botón de guardar, el segundo parámetro
                                                                                              es para indicar que es un botón de tipo "OK" */
                                                                                              
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);/*agrega al dialog el boton guardar y el
                                                                                      boton cancelar */
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña");
        TextField txtNombre = new TextField(producto.getNombre());/*crea un campo de texto para el nombre, y
                                                                  le asigna el nombre del producto que se va
                                                                  a editar */

        java.text.DecimalFormat formato = new java.text.DecimalFormat("#,##0.00");

        TextField txtPrecio = new TextField(formato.format(producto.getPrecio()));

        ComboBox<String> cmbCategoria = new ComboBox<>();/*Crea un ComboBox para seleccionar la categoría
                                                         del producto, empezando vacio*/

        cmbCategoria.getItems().addAll("OTROS", "ENTRADA", "BEBIDA", "MILANESAS", "WOKS",
                "SANDWICHES", "ENSALADAS", "FAJITAS", "PASTAS",
                "VINOS", "DESAYUNO", "GUARNICIONES", "CARNE");/*Agrega las opciones de categoría al
                                                              ComboBox */

        cmbCategoria.setValue(producto.getCategoria().name());/*Asigna al ComboBox la categoría del
                                                              producto para que aparezca seleccionada
                                                              automáticamente cuando se muestra*/

        VBox form = new VBox(10, new Label("Contraseña"), txtPass,new Label("Nombre"), txtNombre, new Label("Precio"),
                    txtPrecio, new Label("Categoría"), cmbCategoria);/*Crea un contenedor vertical
                                                                          (VBox) que organiza elementos
                                                                          uno debajo del otro */
        form.setPadding(new Insets(10));/*Establece relleno a los lados del
                                                             contenedor editar*/

        dialog.getDialogPane().setContent(form);/*Establece el contenido de la ventana */

        dialog.setResultConverter(btn -> {/*Según el botón presionado, devuelve un objeto editado o null*/
            
            if (btn == btnGuardar) {/*Si el botón presionado es guardar, se actualizan los datos del
                                    producto con los valores ingresados en el formulario */
            if (!txtPass.getText().equals(PASSWORD)) {
                new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                return null;
            }
                producto.setNombre(txtNombre.getText());/*Asigna el nombre del producto */

                String textoPrecio = txtPrecio.getText()
                        .replace(".", "")/*elimina separadores de miles */
                        .replace(",", ".");/*intercambia coma por punto para tener un
                                                                numero valido*/
                BigDecimal precio;/*declara una variable para almacenar el precio convertido a número*/
                try {
                    precio = new BigDecimal(textoPrecio);/*intenta convertir el texto a número */

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
        return dialog.showAndWait().orElse(null);
    }
    
public static boolean confirmarEliminacion() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Eliminar producto");

        ButtonType btnConfirmar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();

        VBox form = new VBox(10,
            new Label("Contraseña"), txtPass
        );
        form.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(form);

        final boolean[] confirmado = { false };

        dialog.setResultConverter(btn -> {
            if (btn == btnConfirmar && txtPass.getText().equals(PASSWORD)) {
                confirmado[0] = true;
            } else if (btn == btnConfirmar) {
                new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
            }
            return null;
        });

        dialog.showAndWait();
        return confirmado[0];
    }
}