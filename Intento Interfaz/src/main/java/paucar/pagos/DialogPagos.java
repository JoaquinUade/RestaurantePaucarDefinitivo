package paucar.pagos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.uade.tpo.demo.entity.EstadoPago;
import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoPeriodicidad;

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
import paucar.service.ClientesService;

public class DialogPagos {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DialogPagos() {
    }

    /** Abre el diálogo para crear un pago. Devuelve el PagoEmpresa a guardar o null. */
    public static PagoEmpresa mostrar(
            List<String> empresas,
            ClientesService clientesService) {

        return abrirFormulario(empresas, clientesService, null);
    }

    /** Abre el diálogo precargado para editar un pago existente. */
    public static PagoEmpresa mostrarEditar(
            List<String> empresas,
            ClientesService clientesService,
            PagoEmpresa original) {

        return abrirFormulario(empresas, clientesService, original);
    }

    // ===== FORMULARIO (crear y editar) =====
    private static PagoEmpresa abrirFormulario(
            List<String> empresas,
            ClientesService clientesService,
            PagoEmpresa original) {

        boolean edicion = original != null;

        Dialog<PagoEmpresa> dialog = new Dialog<>();
        dialog.setTitle(edicion ? "Editar Pago" : "Nuevo Pago");
        dialog.getDialogPane().getStylesheets().addAll(
                DialogPagos.class.getResource("/gastos.css").toExternalForm(),
                DialogPagos.class.getResource("/agregar.css").toExternalForm());

        ButtonType btnGuardar =
                new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(btnGuardar, ButtonType.CANCEL);

        // Seguridad
        PasswordField txtPass = new PasswordField();

        // Empresa
        ComboBox<String> comboSempras = new ComboBox<>();
        comboSempras.getItems().addAll(empresas);
        comboSempras.setMaxWidth(Double.MAX_VALUE);

        TextField txtCuit = new TextField();

        // Periodicidad
        ComboBox<TipoPeriodicidad> comboPer = new ComboBox<>();
        comboPer.getItems().addAll(TipoPeriodicidad.values());
        comboPer.setCellFactory(lv -> celdaPeriodicidad());
        comboPer.setButtonCell(celdaPeriodicidad());
        comboPer.setMaxWidth(Double.MAX_VALUE);

        // Fecha
        DatePicker fecha = new DatePicker(LocalDate.now());
        fecha.getStyleClass().add("date-agregar");

        TextField txtMonto = new TextField();
        TextField txtNumeroPago = new TextField();
        TextField txtFactura = new TextField();

        // Estado
        ComboBox<EstadoPago> comboEstado = new ComboBox<>();
        comboEstado.getItems().addAll(EstadoPago.values());
        comboEstado.setCellFactory(lv -> celdaEstado());
        comboEstado.setButtonCell(celdaEstado());
        comboEstado.setMaxWidth(Double.MAX_VALUE);

        TextField txtObservacion = new TextField();

        // ---- Precargar en edición ----
        if (edicion) {
            if (original.getNombre() != null) {
                comboSempras.setValue(original.getNombre());
            }
            txtCuit.setText(original.getCuit() == null ? "" : original.getCuit());
            if (original.getTipoPeriodicidad() != null) {
                comboPer.setValue(original.getTipoPeriodicidad());
            }
            if (original.getFecha() != null) {
                fecha.setValue(original.getFecha().toLocalDate());
            }
            if (original.getMonto() != null) {
                txtMonto.setText(original.getMonto()
                        .stripTrailingZeros().toPlainString());
            }
            if (original.getNumeroPago() != null) {
                txtNumeroPago.setText(String.valueOf(original.getNumeroPago()));
            }
            txtFactura.setText(original.getFactura() == null ? "" : original.getFactura());
            if (original.getEstado() != null) {
                comboEstado.setValue(original.getEstado());
            }
            txtObservacion.setText(original.getObservacion() == null
                    ? "" : original.getObservacion());
        } else {
            comboPer.setValue(TipoPeriodicidad.MENSUAL);
            comboEstado.setValue(EstadoPago.PAGADO);
        }

        VBox form = new VBox(10,
                new Label("Contraseña"), txtPass,
                new Label("Empresa"), comboSempras,
                new Label("CUIT"), txtCuit,
                new Label("Periodicidad"), comboPer,
                new Label("Fecha"), fecha,
                new Label("Monto"), txtMonto,
                new Label("N° de pago (opcional)"), txtNumeroPago,
                new Label("Factura (opcional)"), txtFactura,
                new Label("Estado"), comboEstado,
                new Label("Observación (opcional)"), txtObservacion);

        form.setPadding(new Insets(15));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {

            if (btn != btnGuardar) {
                return null;
            }

            if (!PasswordManager.verificar(txtPass.getText())) {
                new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta")
                        .showAndWait();
                return null;
            }

            String empresa = comboSempras.getValue();
            if (empresa == null || empresa.isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Seleccione una empresa")
                        .showAndWait();
                return null;
            }
            if (txtCuit.getText() == null || txtCuit.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Complete el CUIT")
                        .showAndWait();
                return null;
            }
            if (comboPer.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione la periodicidad")
                        .showAndWait();
                return null;
            }
            if (fecha.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Seleccione la fecha")
                        .showAndWait();
                return null;
            }

            BigDecimal monto;
            try {
                monto = new BigDecimal(txtMonto.getText().trim());
            } catch (Exception e) {
                new Alert(Alert.AlertType.WARNING, "Monto inválido")
                        .showAndWait();
                return null;
            }

            Long empresaId = clientesService
                    .obtenerClienteIdPorNombre(empresa, TipoCliente.EMPRESA);

            PagoEmpresa pago = new PagoEmpresa();
            pago.setEmpresaId(empresaId);
            pago.setNombre(empresa);
            pago.setCuit(txtCuit.getText().trim());
            pago.setTipoPeriodicidad(comboPer.getValue());
            pago.setFecha(fecha.getValue().atTime(LocalTime.NOON));
            pago.setMonto(monto);
            pago.setEstado(comboEstado.getValue());
            pago.setFactura(txtFactura.getText().trim().isEmpty()
                    ? null : txtFactura.getText().trim());
            pago.setObservacion(txtObservacion.getText().trim().isEmpty()
                    ? null : txtObservacion.getText().trim());

            if (!txtNumeroPago.getText().trim().isEmpty()) {
                try {
                    pago.setNumeroPago(
                            Integer.parseInt(txtNumeroPago.getText().trim()));
                } catch (NumberFormatException e) {
                    pago.setNumeroPago(null);
                }
            }

            return pago;
        });

        return dialog.showAndWait().orElse(null);
    }

    // ===== ELIMINAR =====
    public static boolean confirmarEliminacion() {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Eliminar Pago");
        dialog.getDialogPane().getStylesheets().add(
                DialogPagos.class.getResource("/gastos.css").toExternalForm());

        ButtonType btnEliminar =
                new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
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
                    && PasswordManager.verificar(txtPass.getText())) {
                confirmado[0] = true;
            } else if (btn == btnEliminar) {
                new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta")
                        .showAndWait();
            }
            return null;
        });

        dialog.showAndWait();

        return confirmado[0];
    }

    // ===== helpers de celdas =====
    private static ListCell<TipoPeriodicidad> celdaPeriodicidad() {

        return new ListCell<>() {

            @Override
            protected void updateItem(TipoPeriodicidad item, boolean empty) {

                super.updateItem(item, empty);

                setText(empty || item == null
                        ? null : periodicidadTexto(item));
            }
        };
    }

    private static ListCell<EstadoPago> celdaEstado() {

        return new ListCell<>() {

            @Override
            protected void updateItem(EstadoPago item, boolean empty) {

                super.updateItem(item, empty);

                setText(empty || item == null ? null : estadoTexto(item));
            }
        };
    }

    public static String periodicidadTexto(TipoPeriodicidad tipo) {

        return switch (tipo) {
            case MENSUAL -> "Mensual";
            case QUINCENAL -> "Quincenal";
            case SEMANAL -> "Semanal";
            case CONSUMOVARIOSDIAS -> "Consumo varios días";
        };
    }

    public static String estadoTexto(EstadoPago estado) {

        return estado == EstadoPago.PAGADO ? "Pagado" : "Debe";
    }
}
