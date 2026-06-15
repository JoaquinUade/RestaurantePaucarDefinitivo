package paucar.gastos;

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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DialogGastos {

    public static GastoVariableRequest mostrar(List<CategoriaGastoVariable> categorias) {

        Dialog<GastoVariableRequest> dialog = new Dialog<>();
        dialog.setTitle("Agregar Gasto");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        DatePicker fecha = new DatePicker(LocalDate.now());

        ComboBox<CategoriaGastoVariable> combo = new ComboBox<>();
        combo.getItems().addAll(categorias);

        TextField txtNombre = new TextField();
        TextField txtCantidad = new TextField();
        TextField txtPrecio = new TextField();

        VBox form = new VBox(10,
                new Label("Fecha"), fecha,
                new Label("Categoría"), combo,
                new Label("Nombre"), txtNombre,
                new Label("Cantidad"), txtCantidad,
                new Label("Precio"), txtPrecio
        );

        form.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {

                if (combo.getValue() == null || txtPrecio.getText().isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Complete los datos").showAndWait();
                    return null;
                }

                GastoVariableRequest req = new GastoVariableRequest();

                req.setFecha(fecha.getValue());
                req.setCategoriaId(combo.getValue().getIdCategoria());
                req.setProducto(txtNombre.getText());

// separar cantidad y medida (ej: "2 kilos")
String textoCantidad = txtCantidad.getText().trim();
String[] partes = textoCantidad.split(" ", 2);

try {
    req.setCantidad(new java.math.BigDecimal(partes[0]));
} catch (Exception e) {
    req.setCantidad(java.math.BigDecimal.ZERO);
}

if (partes.length > 1) {
    req.setMedida(partes[1]);
} else {
    req.setMedida(""); 
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
}