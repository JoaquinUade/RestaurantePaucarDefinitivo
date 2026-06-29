package paucar.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import paucar.admin.categoriasgastos.CategoriasGastosView;
import paucar.admin.empleados.EmpleadosView;
import paucar.admin.empresasClientes.EmpresasClientes;
import paucar.admin.platos.Platos;
import paucar.service.AdminService;
import paucar.service.CategoriasGastosService;
import paucar.service.ClientesService;
import paucar.service.EmpleadoService;

public class Admin extends BorderPane {

    private final AdminService adminService;
    private final ClientesService clientesService;
    private final CategoriasGastosService categoriaService;
    private final EmpleadoService empleadoService;

    public Admin(AdminService adminService, ClientesService clientesService,
            CategoriasGastosService categoriaService, EmpleadoService empleadoService) {

        this.adminService = adminService;
        this.clientesService = clientesService;
        this.categoriaService = categoriaService;
        this.empleadoService = empleadoService;

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

        grid.setPadding(new Insets(40));/* Establece el relleno del GridPane */
        grid.setHgap(20);

        Button btnPlatos = crearTarjeta("PLATOS", "/img/platos.png");/*Crea un botón con una tarjeta para los
                                                                                       platos*/
        Button btnEmpresasClientes = crearTarjeta("EMPRESAS / CLIENTES", "/img/platos.png");
        Button btnCategoriasGastos = crearTarjeta("GASTOS VARIABLES", "/img/gastos.png");
        Button btnEmpleados = crearTarjeta("EMPLEADOS", "/img/gastos.png");
        
btnPlatos.setOnAction(click -> {
        marcarActivo(btnPlatos, btnEmpresasClientes, btnCategoriasGastos, btnEmpleados);
        setCenter(new Platos(adminService));
    });

    btnEmpresasClientes.setOnAction(click -> {
        marcarActivo(btnEmpresasClientes, btnPlatos, btnCategoriasGastos, btnEmpleados);
        setCenter(new EmpresasClientes(clientesService));
    });

    btnCategoriasGastos.setOnAction(click -> {
        marcarActivo(btnCategoriasGastos, btnPlatos, btnEmpresasClientes, btnEmpleados);
        setCenter(new CategoriasGastosView(categoriaService));
    });

    btnEmpleados.setOnAction(click -> {
        marcarActivo(btnEmpleados, btnPlatos, btnEmpresasClientes, btnCategoriasGastos);
        setCenter(new EmpleadosView(empleadoService));
    });

        grid.add(btnPlatos, 0, 0);
        grid.add(btnEmpresasClientes, 1, 0);
        grid.add(btnCategoriasGastos, 2, 0);
        grid.add(btnEmpleados, 3, 0);
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
        if (!activo.getStyleClass().contains("active")) {/* si no esta activo el color del boton */
            activo.getStyleClass().add("active");/* le activa el color */
        }
        for (Button b : otros) {
            b.getStyleClass().remove("active");/* desactiva los otros botones */
        }
    }
}
