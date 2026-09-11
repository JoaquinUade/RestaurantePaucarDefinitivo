package paucar.stock;


import java.math.BigDecimal;
import java.util.List;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.HistorialStock;

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
import paucar.config.Responsive;
import paucar.security.PasswordManager;
import paucar.security.SesionPassword;
import paucar.stock.aumentoydisminucion.TablaItemsComprados;

public class DialogHistorialEditar {

    public static HistorialStock mostrarEditar(
            HistorialStock historial, List<GastosVariables> gastos, paucar.service.StockService stockService) {

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

        txtCantidad.setEditable(false);

        VBox datosBox = new VBox(Responsive.pe(10),
                new Label("Fecha"),
                dateFecha,
                new Label("Movimiento"),
                txtMovimiento,
                new Label("Stock (calculado automáticamente)"),
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

        VBox historialBox = new VBox(Responsive.pe(10),
                new Label("Gasto asociado"),
                tabla
        );

        tabla.setPrefWidth(Responsive.px(450));

        HBox form = new HBox(Responsive.pe(20),
                datosBox,
                historialBox
        );

        form.setPadding(
                Responsive.insets(15));

        dialog.getDialogPane()
                .setContent(form);

        final HistorialStock[] guardado = {null};
        dialog.getDialogPane().lookupButton(btnGuardar).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
            try {
                dateFecha.commitValue();
                if (dateFecha.getValue() == null) {
                    throw new IllegalArgumentException("Ingresá una fecha válida");
                }
                HistorialStock cambios = new HistorialStock();
                cambios.setFecha(dateFecha.getValue());
                cambios.setMovimiento(new BigDecimal(txtMovimiento.getText().trim()));
                cambios.setGastoVariable(tabla.getSelectionModel().getSelectedItem());
                guardado[0] = stockService.editarHistorial(historial.getId(), cambios);
            } catch (Exception e) {
                event.consume();
                new Alert(Alert.AlertType.ERROR, e.getMessage() == null
                        ? "Datos inválidos" : e.getMessage()).showAndWait();
            }
        });
        dialog.setResultConverter(btn -> btn == btnGuardar ? guardado[0] : null);

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

        VBox form = new VBox(Responsive.pe(10));
        if (!SesionPassword.estaAutorizado()) {
            form.getChildren().addAll(
                    new Label("Contraseña"), txtPass
            );
        } else {
            form.getChildren().add(
                    new Label("Sesión autorizada. Pulse Eliminar para confirmar la operación.")
            );
        }

        form.setPadding(Responsive.insets(10));

        dialog.getDialogPane()
                .setContent(form);

        final boolean[] confirmado
                = {false};

        dialog.setResultConverter(btn -> {

            if (btn == btnEliminar && PasswordManager.verificarConSesion(txtPass.getText())) {

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
