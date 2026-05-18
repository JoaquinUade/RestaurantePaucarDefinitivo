package paucar.admin.empresasClientes;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DialogEmpresasClientes {

    private static final String PASSWORD = "1234";

    // ✅ CREAR
    public static Object[] abrirDialogCrear() {

        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Crear Cliente / Empresa");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();
        TextField txtNombre = new TextField();
        ComboBox<TipoCliente> cmbTipo = new ComboBox<>();

        txtNombre.setPromptText("Nombre");

        cmbTipo.getItems().addAll(TipoCliente.CLIENTE, TipoCliente.EMPRESA);
        cmbTipo.setValue(TipoCliente.CLIENTE);

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Nombre"), txtNombre,
                new Label("Tipo"), cmbTipo
        );

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (!txtPass.getText().equals(PASSWORD)) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }

                String nombre = txtNombre.getText();

                if (nombre == null || nombre.isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Ingrese un nombre").showAndWait();
                    return null;
                }

                return new Object[]{nombre, cmbTipo.getValue()};
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    // ✅ EDITAR
    public static Object[] abrirDialogEditar(String nombreOriginal, TipoCliente tipoOriginal) {

        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Editar Cliente / Empresa");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();
        TextField txtNombre = new TextField(nombreOriginal);
        ComboBox<TipoCliente> cmbTipo = new ComboBox<>();

        cmbTipo.getItems().addAll(TipoCliente.CLIENTE, TipoCliente.EMPRESA);
        cmbTipo.setValue(tipoOriginal);

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Nombre"), txtNombre,
                new Label("Tipo"), cmbTipo
        );

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (!txtPass.getText().equals(PASSWORD)) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }

                String nombre = txtNombre.getText();

                if (nombre == null || nombre.isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Ingrese un nombre").showAndWait();
                    return null;
                }

                return new Object[]{nombre, cmbTipo.getValue()};
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    // ✅ ELIMINAR
    public static boolean confirmarEliminacion() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Eliminar Cliente / Empresa");

        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEliminar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass
        );

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        final boolean[] confirmado = { false };

        dialog.setResultConverter(btn -> {
            if (btn == btnEliminar && txtPass.getText().equals(PASSWORD)) {
                confirmado[0] = true;
            } else if (btn == btnEliminar) {
                new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
            }
            return null;
        });

        dialog.showAndWait();
        return confirmado[0];
    }
}