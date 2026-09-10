package paucar.ventas;

import paucar.config.Responsive;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import paucar.security.PasswordManager;
import paucar.security.SesionPassword;
import paucar.service.ClientesService;
import paucar.service.VentasBackend;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import com.uade.tpo.demo.entity.Venta;

@SuppressWarnings("unused")
public class Tabla extends VBox {

    private final TableView<Venta> tabla = new TableView<>();

    private final VentasBackend backend;
    private final ClientesService clientesService;

    private final Button btnEliminar = new Button("Eliminar Venta");/*crea un botón para eliminar una venta seleccionada en la tabla*/
    private final Button btnEditar = new Button("Editar Venta");
    private final Consumer<Venta> OnEliminar;/*declara un campo OnEliminar que es una función que acepta una fila de venta y
                                                   no devuelve nada, se usará para manejar la eliminación de ventas*/

    private final BiConsumer<Venta, TipoDePago> onCambiarEstado;
    private final NumberFormat moneda;

    private boolean soloLectura = false;

    public Tabla(ObservableList<Venta> items, Locale locale,
            Consumer<Venta> onEliminar, BiConsumer<Venta, TipoDePago> onCambiarEstado,
            VentasBackend backend,
            ClientesService clientesService) {

        this.moneda = NumberFormat.getCurrencyInstance(locale);
        this.OnEliminar = onEliminar;
        this.onCambiarEstado = onCambiarEstado;
        this.backend = backend;
        this.clientesService = clientesService;

        setSpacing(Responsive.pe(8));
        setPadding(Responsive.insets(0));

        tabla.setEditable(true);
        tabla.setItems(items);
        tabla.setPlaceholder(new Label("No hay ventas ingresadas para mostrar."));
        tabla.getColumns().setAll(crearColumnas());
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        btnEliminar.disableProperty()
                .bind(Bindings.isNull(tabla.getSelectionModel().selectedItemProperty()));

        btnEliminar.setOnAction(e -> {
            var sel = tabla.getSelectionModel().getSelectedItem();
            if (sel != null && onEliminar != null) {
                onEliminar.accept(sel); // ✅ avisa a Ventas
            }
        });

        btnEditar.disableProperty()
                .bind(Bindings.isNull(tabla.getSelectionModel().selectedItemProperty()));

        btnEditar.setOnAction(e -> {

            Venta venta
                    = tabla.getSelectionModel()
                            .getSelectedItem();

            if (venta != null) {
                mostrarDialogoEditar(venta);
            }
        });

        VBox.setVgrow(tabla, Priority.ALWAYS);
        getChildren().addAll(tabla, btnEditar, btnEliminar);
    }

    private List<TableColumn<Venta, ?>> crearColumnas() {
        return List.of(
                colNombre(),
                colDescripcion(),
                colMonto(),
                colEstado(),
                colObservaciones()
        );
    }

    private TableColumn<Venta, String> colNombre() {

        var col = new TableColumn<Venta, String>("Nombre");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCliente().getNombre()
                ));

        col.setPrefWidth(Responsive.px(200));

        return col;
    }

    private TableColumn<Venta, String> colDescripcion() {
        var col = new TableColumn<Venta, String>("Descripción");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDescripcion()
                ));

        col.setCellFactory(tc -> new TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(16));
                setGraphic(text);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    text.setText(null);
                    setTooltip(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });
        col.setEditable(false);
        col.setPrefWidth(Responsive.px(420));
        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> colMonto() {
        var col = new TableColumn<Venta, String>("Monto");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(
                        formatMoneda(c.getValue().getMonto())
                ));

        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setEditable(false);
        col.setPrefWidth(Responsive.px(140));
        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, TipoDePago> colEstado() {
        var col = new TableColumn<Venta, TipoDePago>("Estado");
        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        c.getValue().getEstado()
                ));

        col.setCellFactory(tc -> new TableCell<>() {

            private final ComboBox<TipoDePago> combo = new ComboBox<>();
            private final Label label = new Label();

            {
                combo.getItems().setAll(
                        java.util.Arrays.stream(TipoDePago.values())
                                .filter(tipo -> tipo != TipoDePago.DEUDA_PAGADA)
                                .toList());
                combo.getStyleClass().add("combo-agregar");

                combo.valueProperty().addListener((obs, anterior, nuevo) -> {
                    if (Tabla.this.soloLectura) {
                        return;
                    }

                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Venta fila = getTableView().getItems().get(getIndex());
                        fila.setEstado(nuevo);

                        if (onCambiarEstado != null && fila.getIdVenta() != null) {
                            onCambiarEstado.accept(fila, nuevo);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(TipoDePago item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                if (Tabla.this.soloLectura) {
                    label.setText(item.name());
                    setGraphic(label);
                } else {
                    combo.setValue(item);
                    setGraphic(combo);
                }
            }
        });

        col.setPrefWidth(Responsive.px(180));
        col.setSortable(false);

        return col;
    }

    private TableColumn<Venta, String> colObservaciones() {
        var col = new TableColumn<Venta, String>("Observaciones");
        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getObservaciones()
                ));
        col.setCellFactory(tc -> new TableCell<>() {
            private final javafx.scene.text.Text text = new javafx.scene.text.Text();

            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(16));
                setGraphic(text);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    text.setText(null);
                    setTooltip(null);
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });
        col.setPrefWidth(Responsive.px(179));
        col.setSortable(false);

        return col;
    }

    private String formatMoneda(BigDecimal v) {
        if (v == null) {
            return "$ 0,00";
        }
        return moneda.format(v);
    }

    public TableView<Venta> getTable() {
        return tabla;
    }

    public Node asNode() {
        return this;
    }

    public void setSoloLectura(boolean soloLectura) {
        this.soloLectura = soloLectura;

        // desactiva botón eliminar también
        btnEliminar.setVisible(!soloLectura);
        btnEliminar.setManaged(!soloLectura);
    }

    private void mostrarDialogoEditar(Venta venta) {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Editar Venta");

        PasswordField txtPass = new PasswordField();
        ComboBox<String> cbCliente
                = new ComboBox<>();
        var tipoActual
                = venta.getCliente().getTipoCliente();
        cbCliente.getItems().setAll(
                clientesService.obtenerNombresPorTipo(
                        tipoActual
                )
        );
        cbCliente.setValue(
                venta.getCliente().getNombre()
        );

        TextField txtDescripcion
                = new TextField(
                        venta.getDescripcion()
                );

        TextField txtMonto
                = new TextField(
                        venta.getMonto() == null
                        ? "0"
                        : venta.getMonto().toString()
                );

        ComboBox<TipoDePago> cbEstado
                = new ComboBox<>();

        cbEstado.getItems().setAll(
                TipoDePago.values()
        );

        cbEstado.setValue(
                venta.getEstado()
        );

        TextField txtConsumidor
                = new TextField(
                        venta.getConsumidor() == null
                        ? ""
                        : venta.getConsumidor()
                );

        TextArea txtObs
                = new TextArea(
                        venta.getObservaciones() == null
                        ? ""
                        : venta.getObservaciones()
                );

        boolean esEmpresa
                = venta.getCliente() != null
                && venta.getCliente().getTipoCliente()
                == TipoCliente.EMPRESA;

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        if (!SesionPassword.estaAutorizado()) {

            root.getChildren().addAll(
                    new Label("Contraseña"),
                    txtPass
            );
        }

        root.getChildren().addAll(
                new Label("Cliente / Empresa"),
                cbCliente,
                new Label("Descripción"),
                txtDescripcion,
                new Label("Monto"),
                txtMonto,
                new Label("Estado"),
                cbEstado
        );

        if (esEmpresa) {

            root.getChildren().addAll(
                    new Label("Consumidor"),
                    txtConsumidor
            );
        }

        root.getChildren().addAll(
                new Label("Observaciones"),
                txtObs
        );

        dialog.getDialogPane().setContent(root);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(
                        ButtonType.OK,
                        ButtonType.CANCEL
                );

        var res = dialog.showAndWait();

        if (res.isEmpty()
                || res.get() != ButtonType.OK) {
            return;
        }

        if (!PasswordManager.verificarConSesion(
                txtPass.getText())) {

            new Alert(
                    Alert.AlertType.ERROR,
                    "Contraseña incorrecta"
            ).showAndWait();

            return;
        }

        try {
            String nombreSeleccionado
                    = cbCliente.getValue();

            Long idNuevoCliente
                    = clientesService.obtenerClienteIdPorNombre(
                            nombreSeleccionado,
                            tipoActual
                    );

            venta.getCliente().setNombre(
                    nombreSeleccionado
            );

            venta.getCliente().setIdCliente(
                    idNuevoCliente
            );
            venta.setDescripcion(
                    txtDescripcion.getText().trim()
            );

            venta.setMonto(
                    new BigDecimal(
                            txtMonto.getText().trim()
                    )
            );

            venta.setEstado(
                    cbEstado.getValue()
            );

            if (esEmpresa) {

                venta.setConsumidor(
                        txtConsumidor.getText().trim()
                );
            }

            venta.setObservaciones(
                    txtObs.getText().trim()
            );

            tabla.refresh();

            boolean ok
                    = backend.actualizarVenta(
                            venta
                    );

            if (!ok) {

                new Alert(
                        Alert.AlertType.ERROR,
                        "No se pudo guardar la venta"
                ).showAndWait();

                return;
            }
        } catch (NumberFormatException ex) {

            new Alert(
                    Alert.AlertType.ERROR,
                    "Monto inválido"
            ).showAndWait();
        }
    }
}
