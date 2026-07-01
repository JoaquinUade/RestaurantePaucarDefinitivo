package paucar.gastos.Fijos;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.Empleado;
import com.uade.tpo.demo.entity.dto.GastoFijoRequest;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell; // 👈 ESTE TE FALTA
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DialogGastosFijos {

    private static final String PASSWORD = "1234";

    // ✅ AGREGAR
    public static GastoFijoRequest mostrar(List<Empleado> empleados) {

    Boolean esPersonal = preguntarSiEsPersonal();

    if (esPersonal == null) {
        return null; // canceló
    }

    if (esPersonal) {
        return mostrarPersonal(empleados);
    } else {
        return mostrarGeneral();
    }
}
public static Boolean preguntarSiEsPersonal() {

    Dialog<Boolean> dialog = new Dialog<>();
    dialog.setTitle("Tipo de gasto");

    ButtonType btnPersonal = new ButtonType("Personal", ButtonBar.ButtonData.OK_DONE);
    ButtonType btnGeneral = new ButtonType("General", ButtonBar.ButtonData.OTHER);
    ButtonType btnCancelar = ButtonType.CANCEL;

    dialog.getDialogPane().getButtonTypes().addAll(btnPersonal, btnGeneral, btnCancelar);

    dialog.setResultConverter(btn -> {
        if (btn == btnPersonal) return true;
        if (btn == btnGeneral) return false;
        return null;
    });

    return dialog.showAndWait().orElse(null);
}
    // ✅ EDITAR
    public static GastoFijoRequest mostrarEditar(GastoFijoRequest original) {

        Dialog<GastoFijoRequest> dialog = new Dialog<>();
        dialog.setTitle("Editar Gasto Fijo");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();
        DatePicker fecha = new DatePicker(original.getFecha());

        TextField txtDetalle = new TextField(original.getDetalle());
        TextField txtMonto = new TextField(original.getMonto().toString());
        TextField txtObs = new TextField(original.getObservacion());

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Fecha"), fecha,
                new Label("Detalle"), txtDetalle,
                new Label("Monto"), txtMonto,
                new Label("Observación"), txtObs
        );

        form.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (!txtPass.getText().equals(PASSWORD)) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }

                GastoFijoRequest req = new GastoFijoRequest();

                req.setFecha(fecha.getValue());
                req.setDetalle(txtDetalle.getText());
                req.setObservacion(txtObs.getText());
                req.setEstado(original.getEstado());
                req.setEsPersonal(original.getEsPersonal());

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
private static GastoFijoRequest mostrarPersonal(List<Empleado> empleados) {

    Dialog<GastoFijoRequest> dialog = new Dialog<>();
    dialog.setTitle("Pago a personal");

    ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

    PasswordField txtPass = new PasswordField();
    DatePicker fecha = new DatePicker(LocalDate.now());

    ComboBox<Empleado> comboEmpleado = new ComboBox<>();
    comboEmpleado.getItems().addAll(empleados);

    comboEmpleado.setCellFactory(lv -> new ListCell<>() {
        @Override
        protected void updateItem(Empleado emp, boolean empty) {
            super.updateItem(emp, empty);
            setText(empty || emp == null ? "" : emp.getNombre());
        }
    });

    comboEmpleado.setButtonCell(new ListCell<>() {
        @Override
        protected void updateItem(Empleado emp, boolean empty) {
            super.updateItem(emp, empty);
            setText(empty || emp == null ? "" : emp.getNombre());
        }
    });

    TextField txtMonto = new TextField();
    TextField txtObs = new TextField();

    VBox form = new VBox(10,
            new Label("Contraseña"), txtPass,
            new Label("Empleado"), comboEmpleado,
            new Label("Fecha"), fecha,
            new Label("Monto"), txtMonto,
            new Label("Observación"), txtObs
    );

    form.setPadding(new Insets(15));
    dialog.getDialogPane().setContent(form);

    dialog.setResultConverter(btn -> {
        if (btn == btnGuardar) {

            if (!txtPass.getText().equals(PASSWORD)) {
                new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                return null;
            }

            if (comboEmpleado.getValue() == null || txtMonto.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Complete los datos").showAndWait();
                return null;
            }

            GastoFijoRequest req = new GastoFijoRequest();

            req.setFecha(fecha.getValue());
            req.setEsPersonal(true);
            req.setDetalle("Pago a " + comboEmpleado.getValue().getNombre());
            req.setObservacion(txtObs.getText());

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
private static GastoFijoRequest mostrarGeneral() {

    Dialog<GastoFijoRequest> dialog = new Dialog<>();
    dialog.setTitle("Gasto fijo");

    ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

    PasswordField txtPass = new PasswordField();
    DatePicker fecha = new DatePicker(LocalDate.now());

    TextField txtDetalle = new TextField();
    TextField txtMonto = new TextField();
    TextField txtObs = new TextField();

    VBox form = new VBox(10,
            new Label("Contraseña"), txtPass,
            new Label("Fecha"), fecha,
            new Label("Detalle"), txtDetalle,
            new Label("Monto"), txtMonto,
            new Label("Observación"), txtObs
    );

    form.setPadding(new Insets(15));
    dialog.getDialogPane().setContent(form);

    dialog.setResultConverter(btn -> {
        if (btn == btnGuardar) {

            if (!txtPass.getText().equals(PASSWORD)) {
                new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                return null;
            }

            if (txtDetalle.getText().isBlank() || txtMonto.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Complete los datos").showAndWait();
                return null;
            }

            GastoFijoRequest req = new GastoFijoRequest();

            req.setFecha(fecha.getValue());
            req.setDetalle(txtDetalle.getText());
            req.setObservacion(txtObs.getText());
            req.setEsPersonal(false);

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
    // ✅ ELIMINAR
    public static boolean confirmarEliminacion() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Eliminar Gasto");

        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnEliminar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass
        );

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        final boolean[] confirmado = {false};

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
