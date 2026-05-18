
package paucar.admin.empresasClientes;

import java.util.List;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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

        panelClientes = new TablaEmpresasClientes("Clientes");
        panelEmpresas = new TablaEmpresasClientes("Empresas");

        listaClientes = panelClientes.getLista();
        listaEmpresas = panelEmpresas.getLista();

        cargarDatos();

        // Selección exclusiva
        listaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                listaEmpresas.getSelectionModel().clearSelection();
            }
        });

        listaEmpresas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                listaClientes.getSelectionModel().clearSelection();
            }
        });

        // Buscador
        TextField txtBuscar = new TextField();
        txtBuscar.getStyleClass().add("buscador");
        txtBuscar.setPromptText("Buscar cliente o empresa...");

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> {

            String texto = newVal.toLowerCase();

            List<String> clientesFiltrados = clientesOriginal.stream()
                    .filter(c -> c.toLowerCase().contains(texto))
                    .toList();

            listaClientes.getItems().setAll(clientesFiltrados);

            List<String> empresasFiltradas = empresasOriginal.stream()
                    .filter(e -> e.toLowerCase().contains(texto))
                    .toList();

            listaEmpresas.getItems().setAll(empresasFiltradas);
        });

        // Layout tablas
        HBox listas = new HBox(20, panelClientes, panelEmpresas);

        HBox.setHgrow(panelClientes, Priority.ALWAYS);
        HBox.setHgrow(panelEmpresas, Priority.ALWAYS);

        listaClientes.setMaxWidth(Double.MAX_VALUE);
        listaEmpresas.setMaxWidth(Double.MAX_VALUE);

        listaClientes.setMaxHeight(Double.MAX_VALUE);
        listaEmpresas.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(listaClientes, Priority.ALWAYS);
        VBox.setVgrow(listaEmpresas, Priority.ALWAYS);
        VBox.setVgrow(listas, Priority.ALWAYS);

        HBox botones = crearBotones();

        VBox root = new VBox(20, titulo, txtBuscar, listas, botones);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("fondo-empresasclientes");

        setCenter(root);
    }
    private void cargarDatos() {
        clientesOriginal = clientesService.obtenerNombresPorTipo(TipoCliente.CLIENTE);
        empresasOriginal = clientesService.obtenerNombresPorTipo(TipoCliente.EMPRESA);

        listaClientes.getItems().setAll(clientesOriginal);
        listaEmpresas.getItems().setAll(empresasOriginal);
    }

    
 private HBox crearBotones() {

        Button btnCrear = new Button("Crear");
        btnCrear.getStyleClass().add("btn-crear");

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        btnCrear.setOnAction(e -> abrirDialogo(null));

        btnEditar.setOnAction(e -> {
            String seleccionado = obtenerSeleccionado();
            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un elemento").showAndWait();
                return;
            }
            abrirDialogo(seleccionado);
        });

        btnEliminar.setOnAction(e -> {
            String seleccionado = obtenerSeleccionado();

            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un elemento").showAndWait();
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Eliminar Cliente / Empresa");

            ButtonType btnEliminarConfirm = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnEliminarConfirm, ButtonType.CANCEL);

            PasswordField txtPass = new PasswordField();

            VBox contenido = new VBox(10,
                    new Label("Contraseña"), txtPass);
            contenido.setPadding(new Insets(10));

            dialog.getDialogPane().setContent(contenido);

            dialog.setResultConverter(btn -> {
                if (btn == btnEliminarConfirm) {

                    if (!txtPass.getText().equals("1234")) {
                        new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                        return null;
                    }

                    clientesService.eliminarCliente(seleccionado);
                    cargarDatos();
                }
                return null;
            });

            dialog.showAndWait();
        });

        return new HBox(10, btnCrear, btnEditar, btnEliminar);
    }

    private String obtenerSeleccionado() {

        if (listaClientes.getSelectionModel().getSelectedItem() != null) {
            return listaClientes.getSelectionModel().getSelectedItem();
        }

        if (listaEmpresas.getSelectionModel().getSelectedItem() != null) {
            return listaEmpresas.getSelectionModel().getSelectedItem();
        }

        return null;
    }

    private void abrirDialogo(String nombreOriginal) {
        boolean esEdicion = (nombreOriginal != null);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(esEdicion ? "Editar" : "Crear");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField txtNombre = new TextField();
        ComboBox<TipoCliente> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll(TipoCliente.CLIENTE, TipoCliente.EMPRESA);

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña (1234)");

        if (esEdicion) {
            txtNombre.setText(nombreOriginal);

            if (listaClientes.getItems().contains(nombreOriginal)) {
                cmbTipo.setValue(TipoCliente.CLIENTE);
            } else {
                cmbTipo.setValue(TipoCliente.EMPRESA);
            }
        } else {
            cmbTipo.setValue(TipoCliente.CLIENTE);
        }

        VBox contenido = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Nombre"), txtNombre,
                new Label("Tipo"), cmbTipo);

        contenido.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(contenido);

        dialog.setResultConverter(btn -> {

            if (btn == btnGuardar) {

                if (!txtPass.getText().equals("1234")) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }

                String nombre = txtNombre.getText();
                TipoCliente tipo = cmbTipo.getValue();

                if (nombre == null || nombre.isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Ingrese un nombre").showAndWait();
                    return null;
                }

                if (esEdicion) {
                    clientesService.editarCliente(nombreOriginal, nombre, tipo);
                } else {
                    clientesService.crearClienteSiNoExiste(nombre, tipo);
                }

                cargarDatos();
            }

            return null;
        });

        dialog.showAndWait();
    }
}