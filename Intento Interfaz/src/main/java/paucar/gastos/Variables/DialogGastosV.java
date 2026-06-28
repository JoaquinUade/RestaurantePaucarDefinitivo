package paucar.gastos.Variables;

import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastoVariableRequest;

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

public class DialogGastosV {
    private static final String PASSWORD = "1234";

    public static GastoVariableRequest mostrar(List<CategoriaGastoVariable> categorias) {

        Dialog<GastoVariableRequest> dialog = new Dialog<>();
        dialog.setTitle("Agregar Gasto");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();

        DatePicker fecha = new DatePicker(LocalDate.now());

        ComboBox<CategoriaGastoVariable> combo = new ComboBox<>();
        combo.getItems().addAll(categorias);
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CategoriaGastoVariable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CategoriaGastoVariable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });
        TextField txtNombre = new TextField();
        TextField txtCantidad = new TextField();
        TextField txtPrecio = new TextField();

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Fecha"), fecha,
                new Label("Categoría"), combo,
                new Label("Nombre"), txtNombre,
                new Label("Cantidad"), txtCantidad,
                new Label("Precio"), txtPrecio);

        form.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (!txtPass.getText().equals(PASSWORD)) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }
                if (combo.getValue() == null) {
                    new Alert(Alert.AlertType.WARNING, "Seleccione una categoría").showAndWait();
                    return null;
                }
                if (combo.getValue() == null || txtPrecio.getText().isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Complete los datos").showAndWait();
                    return null;
                }

                GastoVariableRequest req = new GastoVariableRequest();

                req.setFecha(fecha.getValue());
                req.setCategoriaId(combo.getValue().getIdCategoria());
                req.setProducto(txtNombre.getText());

                String textoCantidad = txtCantidad.getText().trim();

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)(.*)");
                java.util.regex.Matcher matcher = pattern.matcher(textoCantidad);

                if (matcher.matches()) {

                    // ✅ número (ej: 3 / 2 / 1.5)
                    try {
                        req.setCantidad(new java.math.BigDecimal(matcher.group(1)));
                    } catch (Exception e) {
                        req.setCantidad(java.math.BigDecimal.ZERO);
                    }

                    // ✅ medida (ej: kg / litros / packs)
                    String medida = matcher.group(2).trim();
                    req.setMedida(medida);

                } else {
                    new Alert(Alert.AlertType.WARNING, "Formato inválido. Ej: 3kg o 2 litros").showAndWait();
                    return null;
                }
                // precio → monto
                try {
                    req.setMonto(new java.math.BigDecimal(txtPrecio.getText()));
                } catch (Exception e) {
                    req.setMonto(java.math.BigDecimal.ZERO);
                }

                return req;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public static GastoVariableRequest mostrarEditar(
            List<CategoriaGastoVariable> categorias,
            GastoVariableRequest original) {

        Dialog<GastoVariableRequest> dialog = new Dialog<>();
        dialog.setTitle("Editar Gasto");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();

        DatePicker fecha = new DatePicker(original.getFecha());

        ComboBox<CategoriaGastoVariable> combo = new ComboBox<>();
        combo.getItems().addAll(categorias);

        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CategoriaGastoVariable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CategoriaGastoVariable item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombre());
            }
        });

        // buscar categoría original
        categorias.stream()
                .filter(c -> c.getIdCategoria().equals(original.getCategoriaId()))
                .findFirst()
                .ifPresent(combo::setValue);

        TextField txtNombre = new TextField(original.getProducto());
        String cantidadLimpia = original.getCantidad()
                .stripTrailingZeros()
                .toPlainString();

        TextField txtCantidad = new TextField(
                original.getMedida().isEmpty()
                        ? cantidadLimpia
                        : cantidadLimpia + " " + original.getMedida());
        java.text.NumberFormat formato = java.text.NumberFormat
                .getNumberInstance(java.util.Locale.forLanguageTag("es-AR"));

        String precioFormateado = formato.format(original.getMonto());

        TextField txtPrecio = new TextField(precioFormateado);

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Fecha"), fecha,
                new Label("Categoría"), combo,
                new Label("Nombre"), txtNombre,
                new Label("Cantidad"), txtCantidad,
                new Label("Precio"), txtPrecio);

        form.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (!txtPass.getText().equals(PASSWORD)) {
                    new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").showAndWait();
                    return null;
                }

                GastoVariableRequest req = new GastoVariableRequest();

                req.setFecha(fecha.getValue());
                req.setCategoriaId(combo.getValue().getIdCategoria());
                req.setProducto(txtNombre.getText());

                // misma lógica que ya tenías
                String textoCantidad = txtCantidad.getText().trim();

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)(.*)");

                java.util.regex.Matcher matcher = pattern.matcher(textoCantidad);

                if (matcher.matches()) {
                    try {
                        req.setCantidad(new java.math.BigDecimal(matcher.group(1)));
                    } catch (Exception e) {
                        req.setCantidad(java.math.BigDecimal.ZERO);
                    }

                    req.setMedida(matcher.group(2).trim());
                } else {
                    new Alert(Alert.AlertType.WARNING,
                            "Formato inválido. Ej: 3kg o 2 litros").showAndWait();
                    return null;
                }

                try {
                    req.setMonto(new java.math.BigDecimal(txtPrecio.getText()));
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

        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes()
                .addAll(btnEliminar, ButtonType.CANCEL);

        PasswordField txtPass = new PasswordField();

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass);

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        final boolean[] confirmado = { false };

        dialog.setResultConverter(btn -> {
            if (btn == btnEliminar &&
                    txtPass.getText().equals(PASSWORD)) {

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