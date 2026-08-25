package paucar.ventas;

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

public class AgregarDialogBuilder {

    private final ObservableList<String> clientes;
    private final ObservableList<ProductosService.ProductoItem> productos;
    private final ClientesService clientesService;
    private final VentaRequest venta;

    private TipoCliente tipoSeleccionado;

    public AgregarDialogBuilder(ObservableList<String> clientes,
            ObservableList<ProductosService.ProductoItem> productos,
            ClientesService clientesService, VentaRequest venta) {

        this.clientes = clientes;
        this.productos = productos;
        this.clientesService = clientesService;
        this.venta = venta;
    }

    public Dialog<String> construirDialogoAgregar() {
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
        DatePicker dpFecha = FormularioFactory.crearSelectorFecha();
        dpFecha.getStyleClass().add("date-agregar");
        // --- 6) Estado y observaciones ---
        ComboBox<TipoDePago> cbEstado
                = EstadoPagoCombo.crear();

        cbEstado.getStyleClass()
                .add("combo-agregar");

        TextField tfObs = FormularioFactory.crearObservaciones();
        VBox contPagadores
                = PanelPagadores.crearContenedor();

        TextField tfCantidadPagadores
                = PanelPagadores.crearCantidadPagadores(
                        contPagadores,
                        () -> VentaTotalManager.actualizarTotales(
                                contLineas,
                                contPagadores,
                                lblTotal,
                                lblRestante));
        tfCantidadPagadores.setText("1");
        VentaTotalManager.actualizarTotales(contLineas, contPagadores, lblTotal, lblRestante);
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
        cbProd0.valueProperty().addListener((obs, oldValue, newValue)
                -> VentaTotalManager.actualizarTotales(
                        contLineas,
                        contPagadores,
                        lblTotal,
                        lblRestante));

        tfCant0.textProperty().addListener((obs, oldValue, newValue)
                -> VentaTotalManager.actualizarTotales(
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

                            double total = CalculadoraVenta.calcularTotal(contLineas);

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
                            System.out.println("invalido = " + invalido);
                            System.out.println("total = " + total);
                            System.out.println("pagado = " + pagado);
                            System.out.println("toggle = " + tgTipoCliente.getSelectedToggle());

                            boolean variosPagadores
                            = !contPagadores.getChildren().isEmpty();

                            if (!variosPagadores) {
                                return invalido;
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

    public TipoCliente getTipoSeleccionado() {
        return tipoSeleccionado;
    }
}
