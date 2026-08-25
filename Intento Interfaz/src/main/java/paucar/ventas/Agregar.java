package paucar.ventas;

import java.util.Optional;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.dto.VentaRequest;

import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import paucar.service.ClientesService;
import paucar.service.ProductosService;


public class Agregar {

    private final ObservableList<String> clientes;
    private final ObservableList<ProductosService.ProductoItem> productos;
    private final ClientesService clientesService;
    private final VentaRequest venta;

    private TipoCliente tipoSeleccionado;

    public Agregar(
            ObservableList<String> clientes,
            ObservableList<ProductosService.ProductoItem> productos,
            ClientesService clientesService,
            VentaRequest venta) {

        this.clientes = clientes;
        this.productos = productos;
        this.clientesService = clientesService;
        this.venta = venta;
    }

    public Optional<String> mostrar(Window owner) {

        AgregarDialogBuilder builder =
                new AgregarDialogBuilder(
                        clientes,
                        productos,
                        clientesService,
                        venta);

        Dialog<String> dialog =
                builder.construirDialogoAgregar();

        if (owner != null) {
            dialog.initOwner(owner);
        }

        Optional<String> resultado =
                dialog.showAndWait();

        tipoSeleccionado =
                builder.getTipoSeleccionado();

        return resultado;
    }

    public TipoCliente getTipoSeleccionado() {
        return tipoSeleccionado;
    }
}