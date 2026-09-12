package paucar.admin;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import paucar.admin.categoriasgastos.CategoriasGastosView;
import paucar.admin.empleados.EmpleadosView;
import paucar.admin.empresasClientes.EmpresasClientes;
import paucar.admin.platos.Platos;
import paucar.config.Responsive;
import paucar.resumen.Resumen;
import paucar.security.PasswordManager;
import paucar.service.AdminService;
import paucar.service.CategoriasGastosService;
import paucar.service.ClientesService;
import paucar.service.EmpleadoService;

public final class Admin extends BorderPane {

    private final AdminService adminService;
    private final ClientesService clientesService;
    private final CategoriasGastosService categoriaService;
    private final EmpleadoService empleadoService;
    private final Resumen resumen;

    public Admin(AdminService adminService, ClientesService clientesService,
            CategoriasGastosService categoriaService, EmpleadoService empleadoService, Resumen resumen) {

        this.adminService = adminService;
        this.clientesService = clientesService;
        this.categoriaService = categoriaService;
        this.empleadoService = empleadoService;
        this.resumen = resumen;

        getStylesheets().add(getClass().getResource("/admin.css").toExternalForm());/*Cargar CSS específico
                                                                                          para Admin*/
        botones();/* Crea los botones de la interfaz de administración */
    }

    private void botones() {
        GridPane grid = new GridPane();/*
                                                * Crea un contenedor GridPane para organizar componentes
                                                * en forma de grilla (filas y columnas)
         */
        grid.getStyleClass().add("boton");
        grid.setAlignment(Pos.CENTER);/* Centra el contenido del GridPane */

        grid.setPadding(Responsive.insets(40));/* Establece el relleno del GridPane */
        grid.setHgap(Responsive.pe(20));
        grid.setVgap(Responsive.pe(20));

        Button btnPlatos = crearTarjeta("PLATOS", "/img/platos.png");/*Crea un botón con una tarjeta para los
                                                                                       platos*/
        Button btnEmpresasClientes = crearTarjeta("EMPRESAS / CLIENTES", "/img/empresas clientes.png");
        Button btnCategoriasGastos = crearTarjeta(" CATEGORIAS DE\n GASTOS VARIABLES", "/img/gastos variables.png");
        Button btnEmpleados = crearTarjeta("EMPLEADOS", "/img/empleado.png");
        Button btnSeguridad = crearTarjeta("SEGURIDAD\nCONTRASEÑA", "/img/empleado.png");

        btnPlatos.setOnAction(click -> {
            marcarActivo(btnPlatos, btnEmpresasClientes, btnCategoriasGastos, btnEmpleados);
            setCenter(new Platos(adminService));
        });

        btnEmpresasClientes.setOnAction(click -> {
            marcarActivo(btnEmpresasClientes, btnPlatos, btnCategoriasGastos, btnEmpleados);
            setCenter(new EmpresasClientes(clientesService, resumen));
        });

        btnCategoriasGastos.setOnAction(click -> {
            marcarActivo(btnCategoriasGastos, btnPlatos, btnEmpresasClientes, btnEmpleados);
            setCenter(new CategoriasGastosView(categoriaService));
        });

        btnEmpleados.setOnAction(click -> {
            marcarActivo(btnEmpleados, btnPlatos, btnEmpresasClientes, btnCategoriasGastos);
            setCenter(new EmpleadosView(empleadoService));
        });

        btnSeguridad.setOnAction(click -> mostrarDialogoSeguridad());

        grid.add(btnPlatos, 0, 0);
        grid.add(btnEmpresasClientes, 1, 0);
        grid.add(btnCategoriasGastos, 2, 0);
        grid.add(btnEmpleados, 3, 0);
        grid.add(btnSeguridad, 0, 1);
        setCenter(grid);
    }

    private void mostrarDialogoSeguridad() {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Seguridad");
        dialogo.setHeaderText("Cambiar contraseña");

        ButtonType cambiar = new ButtonType("Cambiar contraseña",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType restablecer = new ButtonType("Restablecer con PIN",
                ButtonBar.ButtonData.OTHER);
        dialogo.getDialogPane().getButtonTypes().addAll(cambiar, restablecer,
                ButtonType.CANCEL);

        PasswordField actual = new PasswordField();
        PasswordField nueva = new PasswordField();
        PasswordField confirmacion = new PasswordField();
        PasswordField pin = new PasswordField();
        actual.setPromptText("Para cambio normal");
        nueva.setPromptText("Mínimo 8 caracteres: letra y número");
        confirmacion.setPromptText("Repetí la nueva contraseña");
        pin.setPromptText("Solo si olvidaste la contraseña");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.add(new Label("Contraseña actual:"), 0, 0);
        formulario.add(actual, 1, 0);
        formulario.add(new Label("Nueva contraseña:"), 0, 1);
        formulario.add(nueva, 1, 1);
        formulario.add(new Label("Confirmación:"), 0, 2);
        formulario.add(confirmacion, 1, 2);
        formulario.add(new Label("PIN de recuperación:"), 0, 3);
        formulario.add(pin, 1, 3);
        formulario.add(new Label("Si olvidaste la contraseña, ingresá el PIN y elegí “Restablecer con PIN”."),
                0, 4, 2, 1);

        dialogo.getDialogPane().setContent(formulario);
        dialogo.showAndWait().ifPresent(opcion -> {
            String error;

            if (opcion == cambiar) {
                error = PasswordManager.cambiarConContrasenaActual(
                        actual.getText(), nueva.getText(), confirmacion.getText());
            } else if (opcion == restablecer) {
                error = PasswordManager.restablecerConPin(
                        pin.getText(), nueva.getText(), confirmacion.getText());
            } else {
                return;
            }

            if (error == null) {
                new Alert(Alert.AlertType.INFORMATION,
                        "La contraseña se actualizó correctamente.").showAndWait();
            } else {
                new Alert(Alert.AlertType.ERROR, error).showAndWait();
            }
        });
    }

    private Button crearTarjeta(String titulo, String rutaIcono) {
        Image img = new Image(getClass().getResource(rutaIcono).toExternalForm());/* Carga la imagen del icono */

        ImageView icono = new ImageView(img);/* Crea un ImageView para mostrar el icono */

        icono.setFitWidth(Responsive.px(90));/* Establece el ancho fit del icono */
        icono.setFitHeight(Responsive.py(90));/* Establece el alto fit del icono */

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
        if (!activo.getStyleClass().contains("active")) {/* si no esta activo el color del boton */
            activo.getStyleClass().add("active");/* le activa el color */
        }
        for (Button b : otros) {
            b.getStyleClass().remove("active");/* desactiva los otros botones */
        }
    }
}
