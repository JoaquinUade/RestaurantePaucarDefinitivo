package paucar.stock;

import java.math.BigDecimal;
import java.util.List;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.HistorialStock;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.security.PasswordManager;
import paucar.stock.aumentoydisminucion.TablaItemsComprados;

public class DialogHistorialEditar {

    public static HistorialStock mostrarEditar(
            HistorialStock historial, List<GastosVariables> gastos) {

        Dialog<HistorialStock> dialog
                = new Dialog<>();

        dialog.setTitle("Editar movimiento");

        ButtonType btnGuardar
                = new ButtonType(
                        "Guardar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnGuardar,
                        ButtonType.CANCEL);

        DatePicker dateFecha
                = new DatePicker(
                        historial.getFecha());

        TextField txtMovimiento
                = new TextField(
                        historial.getMovimiento()
                                .stripTrailingZeros()
                                .toPlainString());

        TextField txtCantidad
                = new TextField(
                        historial.getCantidad()
                                .stripTrailingZeros()
                                .toPlainString());

        VBox datosBox = new VBox(
                10,
                new Label("Fecha"),
                dateFecha,
                new Label("Movimiento"),
                txtMovimiento,
                new Label("Cantidad"),
                txtCantidad
        );

        TableView<GastosVariables> tabla
                = TablaItemsComprados.crear();
        tabla.getItems().addAll(
                gastos.stream()
                        .filter(g
                                -> Boolean.FALSE.equals(
                                g.getCargadoEnStock())
                        || (historial.getGastoVariable() != null
                        && g.getIdGastoVariable().equals(
                                historial.getGastoVariable()
                                        .getIdGastoVariable())))
                        .toList()
        );

        if (historial.getGastoVariable() != null) {

            for (GastosVariables gasto : tabla.getItems()) {

                if (gasto.getIdGastoVariable().equals(
                        historial.getGastoVariable()
                                .getIdGastoVariable())) {

                    tabla.getSelectionModel().select(gasto);
                    tabla.scrollTo(gasto);

                    break;
                }
            }
        }

        VBox historialBox = new VBox(
                10,
                new Label("Gasto asociado"),
                tabla
        );

        tabla.setPrefWidth(450);

        HBox form = new HBox(
                20,
                datosBox,
                historialBox
        );

        form.setPadding(
                new Insets(15));

        dialog.getDialogPane()
                .setContent(form);

        dialog.setResultConverter(btn -> {

            if (btn == btnGuardar) {

                try {

                    historial.setFecha(
                            dateFecha.getValue());

                    historial.setMovimiento(
                            new BigDecimal(
                                    txtMovimiento.getText()));

                    historial.setCantidad(
                            new BigDecimal(
                                    txtCantidad.getText()));

                    historial.setGastoVariable(
                            tabla.getSelectionModel()
                                    .getSelectedItem());

                    return historial;

                } catch (Exception e) {

                    new Alert(
                            Alert.AlertType.ERROR,
                            "Datos inválidos")
                            .showAndWait();
                }
            }

            return null;
        });

        return dialog.showAndWait()
                .orElse(null);
    }

    public static boolean confirmarEliminacion() {

        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Eliminar Producto");

        ButtonType btnEliminar
                = new ButtonType(
                        "Eliminar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        btnEliminar,
                        ButtonType.CANCEL);

        PasswordField txtPass
                = new PasswordField();

        VBox form = new VBox(
                10,
                new Label("Contraseña"),
                txtPass
        );

        form.setPadding(new Insets(10));

        dialog.getDialogPane()
                .setContent(form);

        final boolean[] confirmado
                = {false};

        dialog.setResultConverter(btn -> {

            if (btn == btnEliminar && PasswordManager.verificar(txtPass.getText())) {

                confirmado[0] = true;

            } else if (btn == btnEliminar) {

                new Alert(
                        Alert.AlertType.ERROR,
                        "Contraseña incorrecta")
                        .showAndWait();
            }

            return null;
        });

        dialog.showAndWait();

        return confirmado[0];
    }
}
