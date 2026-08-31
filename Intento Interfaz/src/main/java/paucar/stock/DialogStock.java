package paucar.stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;

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
import paucar.security.PasswordManager;
import paucar.security.SesionPassword;
import paucar.service.GastosVariablesService;

public class DialogStock {

    public static StockRequest mostrar(List<CategoriaGastoVariable> categorias,
            List<GastosVariables> gastos, List<Stock> stocks) {
        Dialog<StockRequest> dialog = new Dialog<>();

        dialog.setTitle("Agregar Stock");

        ButtonType btnGuardar
                = new ButtonType(
                        "Guardar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(btnGuardar, ButtonType.CANCEL);

        ComboBox<CategoriaGastoVariable> comboCategoria
                = new ComboBox<>();

        comboCategoria.getItems().addAll(categorias);

        comboCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });

        comboCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });

        TextField txtProducto = new TextField();
        TextField txtStockMinimo = new TextField();
        TextField txtUnidad = new TextField();

        txtUnidad.setPromptText(
                "kg, unidad, caja, cajón..."
        );
        DatePicker dateFecha
                = new DatePicker(LocalDate.now());
        VBox form = new VBox(
                10,
                new Label("Fecha"),
                dateFecha,
                new Label("Categoría"),
                comboCategoria,
                new Label("Producto"),
                txtProducto,
                new Label("Stock mínimo"),
                txtStockMinimo,
                new Label("Unidad"),
                txtUnidad);

        form.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {

            if (btn == btnGuardar) {

                if (comboCategoria.getValue() == null) {

                    new Alert(
                            Alert.AlertType.WARNING,
                            "Seleccione una categoría"
                    ).showAndWait();

                    return null;
                }
                if (txtProducto.getText().isBlank()) {

                    new Alert(
                            Alert.AlertType.WARNING,
                            "Ingrese un producto"
                    ).showAndWait();

                    return null;
                }

                StockRequest request
                        = new StockRequest();

                request.setCategoriaId(
                        comboCategoria
                                .getValue()
                                .getIdCategoria()
                );

                request.setNombreProducto(
                        txtProducto.getText().trim()
                );
                request.setGastoVariableId(null);

                request.setCantidad(BigDecimal.ZERO);

                request.setUnidadCantidad(txtUnidad.getText().trim());
                request.setFecha(
                        dateFecha.getValue());
                try {

                    request.setStockMinimo(
                            new BigDecimal(
                                    txtStockMinimo.getText().trim()
                            )
                    );

                } catch (Exception e) {

                    request.setStockMinimo(
                            BigDecimal.ZERO
                    );
                }
                System.out.println(
                        "FECHA: "
                        + request.getFecha());
                System.out.println("SE CREO EL REQUEST");
                System.out.println(request.getNombreProducto());
                System.out.println(request.getCantidad());
                return request;
            }

            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    public static Stock mostrarEditar(
            List<CategoriaGastoVariable> categorias,
            Stock original,
            GastosVariablesService gastosService) {

        Dialog<Stock> dialog = new Dialog<>();

        dialog.setTitle("Editar Stock");

        ButtonType btnGuardar
                = new ButtonType(
                        "Guardar",
                        ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(btnGuardar, ButtonType.CANCEL);

        ComboBox<CategoriaGastoVariable> comboCategoria
                = new ComboBox<>();

        comboCategoria.getItems().addAll(categorias);

        comboCategoria.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });

        comboCategoria.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(
                    CategoriaGastoVariable item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                                ? null
                                : item.getNombre());
            }
        });

        categorias.stream()
                .filter(c
                        -> c.getIdCategoria().equals(
                        original.getCategoriaGastoVariable()
                                .getIdCategoria()))
                .findFirst()
                .ifPresent(comboCategoria::setValue);

        TextField txtProducto
                = new TextField(
                        original.getNombreProducto());

        TextField txtStockMinimo
                = new TextField(
                        original.getStockMinimo()
                                .stripTrailingZeros()
                                .toPlainString());

        TextField txtUnidad
                = new TextField(
                        original.getUnidadCantidad());

        VBox form = new VBox(
                10,
                new Label("Categoría"),
                comboCategoria,
                new Label("Producto"),
                txtProducto,
                new Label("Stock mínimo"),
                txtStockMinimo,
                new Label("Unidad"),
                txtUnidad
        );

        form.setPadding(new Insets(15));

        dialog.getDialogPane()
                .setContent(form);

        dialog.setResultConverter(btn -> {

            if (btn == btnGuardar) {

                Stock stock = new Stock();

                stock.setNombreProducto(
                        txtProducto.getText().trim());

                stock.setCantidad(
                        original.getCantidad());

                stock.setStockMinimo(
                        new BigDecimal(
                                txtStockMinimo.getText()));

                stock.setUnidadCantidad(
                        txtUnidad.getText().trim());

                stock.setCategoriaGastoVariable(
                        comboCategoria.getValue());

                return stock;
            }

            return null;
        });

        return dialog.showAndWait().orElse(null);
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

        dialog.getDialogPane()
                .setContent(form);

        final boolean[] confirmado
                = {false};

        dialog.setResultConverter(btn -> {

            if (btn == btnEliminar
                    && PasswordManager.verificarConSesion(txtPass.getText())) {

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
