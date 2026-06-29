package paucar.admin.empleados;

import com.uade.tpo.demo.entity.Empleado;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.EmpleadoService;

public class EmpleadosView extends BorderPane {

    private final EmpleadoService service;
    private final TablaEmpleados panel;
    private final ListView<Empleado> lista;

    public EmpleadosView(EmpleadoService service) {
        this.service = service;

        Label titulo = new Label("Empleados");
        titulo.getStyleClass().add("administracion-de-empresasclientes");
        panel = new TablaEmpleados("Empleados");
        lista = panel.getLista();

        cargarDatos();

        HBox botones = crearBotones();

        VBox layout = new VBox(20, titulo, panel, botones);
        layout.getStyleClass().add("fondo-empresasclientes");
        layout.setPadding(new Insets(20));

        setCenter(layout);
    }

    // ===== BOTONES =====
    private HBox crearBotones() {
        Button btnCrear = new Button("Crear");
        btnCrear.getStyleClass().add("btn-crear");
        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");

        // ✅ CREAR
        btnCrear.setOnAction(e -> {
            String nombre = DialogEmpleados.abrirDialogCrear();

            if (nombre != null) {
                Empleado empleado = new Empleado();
                empleado.setNombre(nombre);

                service.crearEmpleado(empleado);
                cargarDatos();
            }
        });

        // ✅ EDITAR
        btnEditar.setOnAction(e -> {
            Empleado seleccionado = lista.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un empleado").showAndWait();
                return;
            }

            String nuevo = DialogEmpleados.abrirDialogEditar(seleccionado.getNombre());

            if (nuevo != null) {
                seleccionado.setNombre(nuevo);

                service.editarEmpleado(seleccionado.getIdEmpleado(), seleccionado);
                cargarDatos();
            }
        });

        // ✅ ELIMINAR
        btnEliminar.setOnAction(e -> {
            Empleado seleccionado = lista.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione un empleado").showAndWait();
                return;
            }

            if (DialogEmpleados.confirmarEliminacion()) {
                service.eliminarEmpleado(seleccionado.getIdEmpleado());
                cargarDatos();
            }
        });

        return new HBox(10, btnCrear, btnEditar, btnEliminar);
    }

    // ===== CARGAR DATOS =====
    private void cargarDatos() {
        lista.getItems().setAll(service.obtenerTodosLosEmpleados());
    }
}
