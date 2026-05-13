package paucar.resumen.empresas;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Locale;

import com.uade.tpo.demo.entity.TipoDePago;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import paucar.service.VentasBackend;

public class VentanaPagoDeudas {

    private final VentasBackend backend;
    private static final Locale LOCALE_AR = Locale.of("es", "AR");
    private static final NumberFormat MONEDA = NumberFormat.getCurrencyInstance(LOCALE_AR);

    public VentanaPagoDeudas(VentasBackend backend) {
        this.backend = backend;
    }

    public void mostrar(
            TableView<Map<String, Object>> tabla,
            String empresaActual,
            java.time.LocalDate desdeActual,
            Runnable refrescarTabla) {

        Stage ventana = new Stage();
        ventana.setTitle("Pagar Deudas");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label lblPass = new Label("Contraseña:");
        PasswordField txtPass = new PasswordField();

        TableView<Map<String, Object>> tablaDeudas = new TableView<>();

        // ✅ CHECKBOX (IGUAL)
        TableColumn<Map<String, Object>, Boolean> colCheck = new TableColumn<>("Seleccionar");
        colCheck.setSortable(false);

        colCheck.setCellValueFactory(fila -> {
            Boolean seleccionado = (Boolean) fila.getValue().get("selected");
            if (seleccionado == null)
                seleccionado = false;

            SimpleObjectProperty<Boolean> prop = new SimpleObjectProperty<>(seleccionado);

            prop.addListener((obs, oldVal, newVal) ->
                    fila.getValue().put("selected", newVal)
            );

            return prop;
        });

        colCheck.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    Map<String, Object> fila = getTableView().getItems().get(getIndex());
                    fila.put("selected", checkBox.isSelected());
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Map<String, Object> fila = getTableView().getItems().get(getIndex());
                    Boolean seleccionado = (Boolean) fila.get("selected");

                    checkBox.setSelected(seleccionado != null && seleccionado);
                    setGraphic(checkBox);
                }
            }
        });

        tablaDeudas.getColumns().add(colCheck);

        // ✅ MONTO (IGUAL FORMATO AR)
        TableColumn<Map<String, Object>, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(fila -> {
            Number m = (Number) fila.getValue().get("monto");
            return new SimpleObjectProperty<>(MONEDA.format(m == null ? 0 : m.doubleValue()));
        });

        // ✅ DESCRIPCIÓN CON WRAP + TOOLTIP (IGUAL)
        TableColumn<Map<String, Object>, String> colDesc = crearColumnaTexto("Descripción", "descripcion", 13);

        // ✅ OBSERVACIONES EDITABLE (IGUAL)
        TableColumn<Map<String, Object>, String> colObs = new TableColumn<>("Observaciones");

        colObs.setCellValueFactory(fila ->
                new SimpleObjectProperty<>((String) fila.getValue().get("observaciones"))
        );

        colObs.setCellFactory(tc -> new TableCell<>() {

            private final TextField textField = new TextField();

            {
                textField.setOnAction(e -> guardar());
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal)
                        guardar();
                });
            }

            private void guardar() {
                Map<String, Object> fila = getTableView().getItems().get(getIndex());
                fila.put("observaciones", textField.getText());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    textField.setText(item != null ? item : "");
                    setGraphic(textField);
                }
            }
        });

        colObs.setSortable(false);
        colMonto.setSortable(false);
        colDesc.setSortable(false);

        tablaDeudas.getColumns().add(colDesc);
        tablaDeudas.getColumns().add(colMonto);
        tablaDeudas.getColumns().add(colObs);

        tablaDeudas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // ✅ FILTRADO IGUAL
        for (Map<String, Object> v : tabla.getItems()) {
            if (v.get("estado") == TipoDePago.DEBE) {
                tablaDeudas.getItems().add(v);
            }
        }

        // ✅ BOTÓN (IGUAL)
        Button btnConfirmar = new Button("Confirmar Pago");

        btnConfirmar.setOnAction(e -> {

            String pass = txtPass.getText();

            if (!"1234".equals(pass)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta");
                alert.show();
                return;
            }

            for (Map<String, Object> v : tablaDeudas.getItems()) {

                Boolean seleccionado = (Boolean) v.get("selected");

                if (Boolean.TRUE.equals(seleccionado)) {

                    Long idVenta = ((Number) v.get("idVenta")).longValue();

                    backend.actualizarEstadoVenta(idVenta, TipoDePago.DEUDA_PAGADA);

                    v.put("estado", TipoDePago.DEUDA_PAGADA);
                }
            }

            refrescarTabla.run(); // 🔄 exactamente igual a tu recarga

            ventana.close();
        });

        layout.getChildren().addAll(lblPass, txtPass, tablaDeudas, btnConfirmar);

        Scene scene = new Scene(layout, 600, 400);

        ventana.setScene(scene);
        ventana.show();
    }

    // ✅ MÉTODO EXACTO TUYO
    private TableColumn<Map<String, Object>, String> crearColumnaTexto(
            String titulo, String key, int padding) {

        TableColumn<Map<String, Object>, String> col = new TableColumn<>(titulo);

        col.setCellValueFactory(fila ->
                new SimpleObjectProperty<>((String) fila.getValue().get(key)));

        col.setCellFactory(columna -> new TableCell<>() {

            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(columna.widthProperty().subtract(padding));
                setGraphic(text);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    text.setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        return col;
    }
}
