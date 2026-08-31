package paucar.admin.empleados;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import paucar.security.PasswordManager;
import paucar.security.SesionPassword;

public class DialogEmpleados {

    // ✅ CREAR
    public static String abrirDialogCrear() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Crear Empleado");
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        PasswordField txtPass = new PasswordField();
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");
        VBox form = new VBox(10);
        if (!SesionPassword.estaAutorizado()) {
            form.getChildren().addAll(
                    new Label("Contraseña"), txtPass
            );
        }
        form.getChildren().addAll(
                new Label("Nombre"), txtNombre
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (!PasswordManager.verificarConSesion(txtPass.getText())) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }
                if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Ingrese un nombre").showAndWait();
                    return null;
                }
                return txtNombre.getText();
            }
            return null;
        });
        return dialog.showAndWait().orElse(null);
    }

    // ✅ EDITAR
    public static String abrirDialogEditar(String nombreOriginal) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Editar Empleado");
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        PasswordField txtPass = new PasswordField();
        TextField txtNombre = new TextField(nombreOriginal);
        VBox form = new VBox(10);
        if (!SesionPassword.estaAutorizado()) {
            form.getChildren().addAll(
                    new Label("Contraseña"), txtPass
            );
        }
        form.getChildren().addAll(
                new Label("Nombre"), txtNombre
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (!PasswordManager.verificarConSesion(txtPass.getText())) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }
                if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Ingrese un nombre").showAndWait();
                    return null;
                }
                return txtNombre.getText();
            }
            return null;
        });
        return dialog.showAndWait().orElse(null);
    }

    // ✅ ELIMINAR
    public static boolean confirmarEliminacion() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Eliminar Empleado");
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEliminar, ButtonType.CANCEL);
        PasswordField txtPass = new PasswordField();
        VBox form = new VBox(10);
        if (!SesionPassword.estaAutorizado()) {
            form.getChildren().addAll(
                    new Label("Contraseña"), txtPass
            );
        } else {
            form.getChildren().add(
                    new Label("Sesión autorizada. Pulse Eliminar para confirmar la operación.")
            );
        }
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        final boolean[] confirmado = {false};
        dialog.setResultConverter(btn -> {
            if (btn == btnEliminar && PasswordManager.verificarConSesion(txtPass.getText())) {
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
