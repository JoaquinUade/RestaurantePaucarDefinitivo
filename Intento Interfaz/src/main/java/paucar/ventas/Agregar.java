package paucar.ventas;

import java.time.LocalDate;
import java.util.Optional;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaRequest;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import paucar.service.ClientesService;
import paucar.service.ProductosService;
import paucar.ventas.ui.ClienteAutoCompletar;
import paucar.ventas.ui.ClienteTipoManager;
import paucar.ventas.ui.EstadoPagoCombo;
import paucar.ventas.ui.FormularioPedidoBuilder;
import paucar.ventas.ui.PanelPagadores;
import paucar.ventas.ui.PanelProductos;
import paucar.ventas.ui.ProductoLinea;
import paucar.ventas.ui.SelectorTipoCliente;
import paucar.ventas.util.CalculadoraVenta;
import paucar.ventas.util.ValidadorVenta;
import paucar.ventas.util.VentaBuilder;

public class Agregar {

    public static record Formulario(Long idProducto, Integer cantidad) {

    }

    // ====== Datos de trabajo que vienen de Ventas ======
    private final ObservableList<String> clientes; // lista base
    private final ObservableList<ProductosService.ProductoItem> productos; // lista base
    private final ClientesService clientesService;
    private final VentaRequest venta;

    public Agregar(ObservableList<String> clientes,
            ObservableList<ProductosService.ProductoItem> productos, ClientesService clientesService, VentaRequest venta) {
        // Usamos directamente las listas provistas por Ventas
        this.clientes = clientes;
        this.clientesService = clientesService;
        this.productos = productos;
        this.venta = venta;
    }

    private TipoCliente tipoSeleccionado = null;

    public TipoCliente getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public Optional<String> Mostrar(Window owner) {
        Dialog<String> VentanaEmergente = construirDialogoAgregar();/*creo una variable y le asigno todo
                                                                    el contenido que construye el metodo */

        if (owner != null) {/*Esta línea pregunta si existe una ventana principal (owner) antes de asociar
                            el diálogo a ella */
            VentanaEmergente.initOwner(owner);/*Le asigna una ventana principal para que la ventana
                                               emergente se muestre encima y funcione como un diálogo
                                               modal (ventana emergente que no te permite acceder a la
                                               ventana de atras hasta que la cierres*/
        }
        return VentanaEmergente.showAndWait();/*Devuelve el resultado de mostrar la ventana emergente y
                                              esperar hasta que el usuario la cierre */
    }

    private Dialog<String> construirDialogoAgregar() {
        Dialog<String> dialog = new Dialog<>();/*Creá una ventana emergente llamada dialog que, cuando se
                                               cierre presionando ‘Agregar’, va a devolver un texto como
                                               resultado */

        dialog.setTitle("Agregar pedido");/*nombra a la ventana emergente “Agregar pedido” como
                                                título en la barra superior */
        dialog.setResizable(true);

        ButtonType okType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        // --- 1) Selector tipo cliente (Mesa - Cliente - Empresa) ---
        SelectorTipoCliente selector
                = SelectorTipoCliente.crear();

        ToggleGroup tgTipoCliente
                = selector.getToggleGroup();

        HBox selectorTipoCliente
                = selector.getContenedor();
        selectorTipoCliente.setAlignment(Pos.CENTER_LEFT);

        // --- 2) Combo de clientes (tu autocompletar por texto se mantiene)
        FilteredList<String> clientesFiltrados = new FilteredList<>(clientes, s -> true);
        ComboBox<String> cbCliente
                = ClienteAutoCompletar.crear(
                        clientesFiltrados);
        cbCliente.getStyleClass().add("combo-agregar");

        ClienteTipoManager.cargarInicial(
                tgTipoCliente,
                clientesService,
                clientes,
                clientesFiltrados,
                cbCliente);

        ClienteTipoManager.configurarCambioTipo(
                tgTipoCliente,
                clientesService,
                clientes,
                clientesFiltrados,
                cbCliente);

        // --- 5) Productos (líneas dinámicas) ---
        PanelProductos panelProductos
                = new PanelProductos(productos);

        VBox contLineas
                = panelProductos.getContLineas();

        Label lblTotal
                = panelProductos.getLblTotal();

        Label lblRestante
                = panelProductos.getLblRestante();

        Button btnAgregarLinea
                = panelProductos.getBtnAgregarLinea();
        contLineas.getChildren().add(
        ProductoLinea.crear(
                contLineas,
                productos));
        DatePicker dpFecha = crearSelectorFecha();
        dpFecha.getStyleClass().add("date-agregar");
        // --- 6) Estado y observaciones ---
        ComboBox<TipoDePago> cbEstado
                = EstadoPagoCombo.crear();

        cbEstado.getStyleClass()
                .add("combo-agregar");

        TextField tfObs = TextFieldObservaciones();
        VBox contPagadores
                = PanelPagadores.crearContenedor();

        TextField tfCantidadPagadores
                = PanelPagadores.crearCantidadPagadores(
                        contPagadores,
                        () -> actualizarTotales(
                                contLineas,
                                contPagadores,
                                lblTotal,
                                lblRestante));
        tfCantidadPagadores.setText("1");
        actualizarTotales(contLineas, contPagadores, lblTotal, lblRestante);
        // --- 7) Layout ---
        GridPane grid = FormularioPedidoBuilder.construir(
                cbCliente,
                dpFecha,
                contLineas,
                btnAgregarLinea,
                cbEstado,
                tfObs,
                selectorTipoCliente,
                tfCantidadPagadores,
                contPagadores,
                lblTotal,
                lblRestante
        );
        // --- 8) Validación del botón OK ---
        HBox fila0 = (HBox) contLineas.getChildren().get(0);
        @SuppressWarnings("unchecked")
        ComboBox<ProductosService.ProductoItem> cbProd0 = (ComboBox<ProductosService.ProductoItem>) fila0.getChildren().get(0);
        TextField tfCant0 = (TextField) fila0.getChildren().get(1);
cbProd0.valueProperty().addListener((obs, oldValue, newValue) ->
        actualizarTotales(
                contLineas,
                contPagadores,
                lblTotal,
                lblRestante));

tfCant0.textProperty().addListener((obs, oldValue, newValue) ->
        actualizarTotales(
                contLineas,
                contPagadores,
                lblTotal,
                lblRestante));
        Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> {

                            boolean invalido
                            = ValidadorVenta.botonAgregarInhabilitado(
                                    cbCliente,
                                    clientes,
                                    contLineas)
                            || tgTipoCliente.getSelectedToggle() == null;

                            double total
                            = CalculadoraVenta.calcularTotal(contLineas);

                            double pagado = 0;

                            for (Node n : contPagadores.getChildren()) {

                                HBox fila = (HBox) n;

                                TextField tfMonto
                                = (TextField) fila.getChildren().get(1);

                                if (!tfMonto.getText().isBlank()) {

                                    try {
                                        pagado += Double.parseDouble(
                                                tfMonto.getText());
                                    } catch (NumberFormatException ex) {
                                    }
                                }
                            }

                            return invalido || (total - pagado) > 0;
                        },
                        cbCliente.getEditor().textProperty(),
                        contLineas.getChildren(),
                        cbProd0.valueProperty(),
                        tfCant0.textProperty(),
                        tgTipoCliente.selectedToggleProperty()
                )
        );

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialog.getDialogPane().setContent(sp);

        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(600, 500);

        dialog.setResultConverter(btn -> {
            if (btn == okType) {
                if (tgTipoCliente.getSelectedToggle() == null) {
                    return null;
                }
                // Guardar el tipo
                this.tipoSeleccionado = (TipoCliente) tgTipoCliente.getSelectedToggle().getUserData();
                // Construir la venta y devolver el nombre
                return VentaBuilder.construirVenta(
                        venta,
                        cbCliente,
                        dpFecha,
                        cbEstado,
                        tfObs,
                        contLineas);
            }
            return null;
        });

        return dialog;
    }

    private TextField TextFieldObservaciones() {
        TextField inputObservaciones = new TextField();/*crea un objeto visual en el que se escribe */
        inputObservaciones.setPromptText("Observaciones (opcional)");/*escribe dentro de el objeto
                                                                        el texto observaciones (opcional)
                                                                     en color gris claro como sugerencia y
                                                                 se elimina cuando el usuario escribe algo */
        return inputObservaciones;/*retorna inputobservaciones */
    }

    private DatePicker crearSelectorFecha() {
        DatePicker dpFecha = new DatePicker();

        // Fecha actual por defecto
        dpFecha.setValue(LocalDate.now());

        return dpFecha;
    }

    private void actualizarTotales(
            VBox contLineas,
            VBox contPagadores,
            Label lblTotal,
            Label lblRestante) {
        double total
                = CalculadoraVenta.calcularTotal(contLineas);
int cantidadPagadores =
        contPagadores.getChildren().size();

if (cantidadPagadores > 0) {

    double base =
            Math.floor((total / cantidadPagadores) * 100)
            / 100;

    double restanteDivision = total;

    for (int i = 0; i < cantidadPagadores; i++) {

        HBox fila =
                (HBox) contPagadores
                        .getChildren()
                        .get(i);

        TextField tfMonto =
                (TextField) fila.getChildren().get(1);

        double sugerencia;

        if (i == cantidadPagadores - 1) {
            sugerencia = restanteDivision;
        } else {
            sugerencia = base;
            restanteDivision -= base;
        }

        tfMonto.setPromptText(
                String.format("$ %.2f", sugerencia));
    }
}
        lblTotal.setText("Total: $" + total);
        System.out.println("TOTAL = " + total);
        lblTotal.setText("Total: $" + total);
        double pagado = 0;

        for (Node n : contPagadores.getChildren()) {

            HBox fila = (HBox) n;

            TextField tfMonto
                    = (TextField) fila.getChildren().get(1);

            if (!tfMonto.getText().isBlank()) {

                try {
                    pagado += Double.parseDouble(
                            tfMonto.getText());
                } catch (NumberFormatException ex) {
                }
            }
        }
        if (contPagadores.getChildren().isEmpty()) {

    lblRestante.setVisible(false);
    lblRestante.setManaged(false);

    return;
}

// Si hay 2 o más pagadores, mostrar el label
        lblRestante.setVisible(true);
        lblRestante.setManaged(true);

        double restante = total - pagado;

        lblRestante.getStyleClass().removeAll(
                "restante-pendiente",
                "restante-completo",
                "restante-excedido");

        if (restante > 0) {
            lblRestante.setText(
                    String.format("Restan pagar: $%.2f", restante));

            lblRestante.getStyleClass().add(
                    "restante-pendiente");
        } else if (restante < 0) {
            lblRestante.setText(
                    String.format(
                            "El cliente está pagando de más: $%.2f",
                            Math.abs(restante)));

            lblRestante.getStyleClass().add(
                    "restante-excedido");
        } else {
            lblRestante.setText("Pago completo");

            lblRestante.getStyleClass().add(
                    "restante-completo");
        }
    }
}
