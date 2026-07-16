package paucar.gastos.Individuales;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.Empleado;
import com.uade.tpo.demo.entity.dto.GastoIndividualRequest;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import paucar.gastos.Fijos.DialogGastosFijos;

public class DialogGastosIndividuales {

    private static final String PASSWORD = "1234";

    public static GastoIndividualRequest mostrar(List<Empleado> empleados) {

        Dialog<GastoIndividualRequest> dialog = new Dialog<>();
        dialog.setTitle("Agregar Gasto");
        dialog.getDialogPane().getStylesheets().add(
                DialogGastosFijos.class
                        .getResource("/gastos.css")
                        .toExternalForm()
        );
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();
        DatePicker fecha = new DatePicker(LocalDate.now());
        fecha.getStyleClass().add("date-agregar");
        ComboBox<Empleado> combo = new ComboBox<>();
        combo.getItems().addAll(empleados);

        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Empleado item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Empleado item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        TextField txtDetalle = new TextField();
        TextField txtMonto = new TextField();

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Fecha"), fecha,
                new Label("Empleado"), combo,
                new Label("Detalle"), txtDetalle,
                new Label("Monto"), txtMonto);

        form.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (!txtPass.getText().equals(PASSWORD)) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }

                if (combo.getValue() == null || txtMonto.getText().isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Complete los datos").showAndWait();
                    return null;
                }

                GastoIndividualRequest req = new GastoIndividualRequest();

                req.setFecha(fecha.getValue());
                req.setDetalle(txtDetalle.getText());
                req.setEmpleadoId(combo.getValue().getIdEmpleado());

                try {
                    req.setMonto(new java.math.BigDecimal(txtMonto.getText()));
                } catch (Exception e) {
                    req.setMonto(java.math.BigDecimal.ZERO);
                }

                return req;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public static GastoIndividualRequest mostrarEditar(
            List<Empleado> empleados,
            GastoIndividualRequest original) {

        Dialog<GastoIndividualRequest> dialog = new Dialog<>();
        dialog.setTitle("Editar Gasto");
        dialog.getDialogPane().getStylesheets().add(
                DialogGastosFijos.class
                        .getResource("/gastos.css")
                        .toExternalForm()
        );
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();
        DatePicker fecha = new DatePicker(original.getFecha());
        fecha.getStyleClass().add("date-agregar");
        ComboBox<Empleado> combo = new ComboBox<>();
        combo.getItems().addAll(empleados);

        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Empleado item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Empleado item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        // seleccionar empleado original
        empleados.stream()
                .filter(e -> e.getIdEmpleado().equals(original.getEmpleadoId()))
                .findFirst()
                .ifPresent(combo::setValue);

        TextField txtDetalle = new TextField(original.getDetalle());
        TextField txtMonto = new TextField(original.getMonto().toString());

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Fecha"), fecha,
                new Label("Empleado"), combo,
                new Label("Detalle"), txtDetalle,
                new Label("Monto"), txtMonto);

        form.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (!txtPass.getText().equals(PASSWORD)) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }

                GastoIndividualRequest req = new GastoIndividualRequest();

                req.setFecha(fecha.getValue());
                req.setDetalle(txtDetalle.getText());
                req.setEmpleadoId(combo.getValue().getIdEmpleado());

                try {
                    req.setMonto(new java.math.BigDecimal(txtMonto.getText()));
                } catch (Exception e) {
                    req.setMonto(java.math.BigDecimal.ZERO);
                }

                return req;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public static boolean confirmarEliminacion() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Eliminar Gasto");
        dialog.getDialogPane().getStylesheets().add(
                DialogGastosFijos.class
                        .getResource("/gastos.css")
                        .toExternalForm()
        );
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes()
                .addAll(btnEliminar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass);

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        final boolean[] confirmado = {false};

        dialog.setResultConverter(btn -> {
            if (btn == btnEliminar
                    && txtPass.getText().equals(PASSWORD)) {

                confirmado[0] = true;

            } else if (btn == btnEliminar) {

                new Alert(Alert.AlertType.ERROR,
                        "Contraseña incorrecta").showAndWait();
            }
            return null;
        });

        dialog.showAndWait();

        return confirmado[0];
    }
}
