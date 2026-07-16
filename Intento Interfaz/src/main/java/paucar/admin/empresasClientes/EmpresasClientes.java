package paucar.admin.empresasClientes;

import java.util.List;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import paucar.service.ClientesService;

public class EmpresasClientes extends BorderPane {

    private final ClientesService clientesService;

    private final TablaEmpresasClientes panelClientes;
    private final TablaEmpresasClientes panelEmpresas;

    private final ListView<String> listaClientes;
    private final ListView<String> listaEmpresas;

    private List<String> clientesOriginal;
    private List<String> empresasOriginal;

    public EmpresasClientes(ClientesService clientesService) {
        this.clientesService = clientesService;

        Label titulo = new Label("Administración de Clientes y Empresas");
        titulo.getStyleClass().setAll("administracion-de-empresasclientes");

        panelClientes = new TablaEmpresasClientes("Clientes");/*Creame una tabla de tipo TablaEmpresasClientes,
                                                              configurada para mostrar Clientes, y guardala
                                                              en panelClientes */
        panelEmpresas = new TablaEmpresasClientes("Empresas");

        listaClientes = panelClientes.getLista();/*Obtené la lista interna del panel de clientes y
                                                  guardala en listaClientes para poder llenarla */
        listaEmpresas = panelEmpresas.getLista();

        cargarDatos();/*Traé los clientes y empresas desde el servicio y mostralos en las listas */

        listaClientes.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {/*Cuando cambie el elemento seleccionado en la lista de
                                                    clientes, ejecutá ese bloque de codigo */

                    if (newVal != null) {/*Si seleccionaste un cliente*/
                        listaEmpresas.getSelectionModel().clearSelection();/*entonces deseleccioná las empresas */
                    }
                });

        listaEmpresas.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {/*si seleccionaste una empresa*/
                        listaClientes.getSelectionModel().clearSelection();/*entonces deselecciona clientes */
                    }
                });

        // Buscador
        TextField txtBuscar = new TextField();
        txtBuscar.getStyleClass().add("buscador");
        txtBuscar.setPromptText("Buscar cliente o empresa...");/*escribe dentro del buscador */

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {/*Escuchá lo que escribe el usuario
                                                                       y reaccioná automáticamente */
            String texto = newVal.toLowerCase();/*lo vuelve minuscula */

            List<String> clientesFiltrados = clientesOriginal.stream().filter(c
                    -> c.toLowerCase().contains(texto)).toList();/*Filtra la lista de clientes y se queda solo con
                                                       los que coinciden con lo que escribió el usuario */
            List<String> empresasFiltradas = empresasOriginal.stream()
                    .filter(e -> e.toLowerCase().contains(texto))
                    .toList();

            listaClientes.getItems().setAll(clientesFiltrados);/*Reemplaza los elementos de la lista visual
                                                                con los clientes filtrados */
            listaEmpresas.getItems().setAll(empresasFiltradas);
        });

        // Layout tablas
        HBox listas = new HBox(20, panelClientes, panelEmpresas);/*crea un contenedor horizontal con un
                                                                 espacio de 20px entre elementos*/

        HBox.setHgrow(panelClientes, Priority.ALWAYS);/*crece para ocupar todo el espacio disponible */
        HBox.setHgrow(panelEmpresas, Priority.ALWAYS);

        listaClientes.setMaxWidth(Double.MAX_VALUE);/*Podés agrandarte sin límite */
        listaEmpresas.setMaxWidth(Double.MAX_VALUE);

        listaClientes.setMaxHeight(Double.MAX_VALUE);/*Podés estirarte hacia abajo todo lo que se
                                                     necesario */
        listaEmpresas.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(listaClientes, Priority.ALWAYS);/*Dale espacio vertical extra a estas listas */
        VBox.setVgrow(listaEmpresas, Priority.ALWAYS);

        HBox botones = crearBotones();/*crea botones y los guarda en hbox botones */

        VBox pantalla = new VBox(20, titulo, txtBuscar, listas, botones);/*caja vertical */
        pantalla.setPadding(new Insets(20));/*le pone un relleno alrededor de 20px */
        pantalla.getStyleClass().add("fondo-empresasclientes");

        setCenter(pantalla);/*centra la caja vertical llamada pantalla */
    }

    private void cargarDatos() {
        clientesOriginal = clientesService
                .obtenerNombresPorTipo(TipoCliente.CLIENTE);/*Le pide al clientesService todos los nombres
                                                        de clientes y los guarda en una lista */
        empresasOriginal = clientesService.obtenerNombresPorTipo(TipoCliente.EMPRESA);

        listaClientes.getItems().setAll(clientesOriginal);/*Carga todos los clientes en la lista visual */
        listaEmpresas.getItems().setAll(empresasOriginal);
    }

    private HBox crearBotones() {

        Button btnCrear = new Button("Crear");
        btnCrear.getStyleClass().add("btn-crear");

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        btnCrear.setOnAction(e -> {/*si presiona btn crear */
            Object[] resultado = DialogEmpresasClientes.abrirDialogCrear();/*Abre el diálogo de creación y
                                                                           guarda los datos que ingresó el
                                                                           usuario en un arreglo */

            if (resultado != null) {/*Si el usuario ingresó datos en el dialogo (no canceló)*/

                String nombre = (String) resultado[0];/*Obtiene el nombre ingresado del diálogo*/
                TipoCliente tipo = (TipoCliente) resultado[1];/*obtiene el tipo ingresado en el dialogo */
                System.out.println("Tipo elegido: " + tipo);
                clientesService.crearClienteSiNoExiste(nombre, tipo);/*Crea el cliente si no existe
                                                                      previamente*/
                cargarDatos();/*Vuelve a cargar los datos y actualiza las listas en pantalla */
            }
        });

        btnEditar.setOnAction(e -> {
            String seleccionado = obtenerSeleccionado();

            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un elemento").showAndWait();
                return;
            }

            TipoCliente tipo = listaClientes.getSelectionModel().getSelectedItem() != null
                    ? TipoCliente.CLIENTE : TipoCliente.EMPRESA;/*Si hay un cliente seleccionado, es CLIENTE; si
                                                       no, es EMPRESA */

            Object[] resultado = DialogEmpresasClientes
                    .abrirDialogEditar(seleccionado, tipo);/*Abre el diálogo de editar y guarda los datos que
                                                       ingresó el usuario en un arreglo */

            if (resultado != null) {
                String nuevoNombre = (String) resultado[0];/*obtiene el nuevo nombre */
                TipoCliente nuevoTipo = (TipoCliente) resultado[1];/*obtiene el nuevo tipo */

                clientesService.editarCliente(seleccionado, nuevoNombre, nuevoTipo);/*Actualiza el cliente/empresa
                                                                                    con los nuevos datos */
                cargarDatos();/*Vuelve a cargar los datos y actualiza las listas en pantalla */
            }
        });
        btnEliminar.setOnAction(e -> {
            String seleccionado = obtenerSeleccionado();

            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un elemento").showAndWait();
                return;
            }

            if (DialogEmpresasClientes.confirmarEliminacion()) {/*Si el usuario confirma la eliminación*/

                clientesService.eliminarCliente(seleccionado);/*elimina el cliente o empresa */

                cargarDatos();/*Vuelve a cargar los datos y actualiza las listas en pantalla  */
            }
        });
        return new HBox(10, btnCrear, btnEditar, btnEliminar);/*Devuelve un contenedor horizontal con
                                                              los botones*/
    }

    private String obtenerSeleccionado() {

        if (listaClientes.getSelectionModel().getSelectedItem() != null) {/*Si hay un cliente seleccionado */
            return listaClientes.getSelectionModel().getSelectedItem();/*lo retorna */
        }

        if (listaEmpresas.getSelectionModel().getSelectedItem() != null) {
            return listaEmpresas.getSelectionModel().getSelectedItem();
        }

        return null;
    }
}
