package paucar.ventas.ui;

import com.uade.tpo.demo.entity.TipoDePago;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public final class FormularioPedidoBuilder {

    private FormularioPedidoBuilder() {
    }

    public static GridPane construir(
            ComboBox<String> cbCliente,
            DatePicker dpFecha,
            VBox listaProductos,
            Button btnAgregarProducto,
            ComboBox<TipoDePago> cbEstado,
            TextField inputObservaciones,
            Node selectorTipoCliente,
            TextField tfCantidadPagadores,
            VBox contPagadores,
            Label lblTotal,
            Label lblRestante,
            TextField inputConsumidor) {

        GridPane grid = new GridPane();

        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        int r = 1;

        grid.add(new Label("Tipo de cliente:"), 0, r);
        grid.add(selectorTipoCliente, 1, r++);

        grid.add(new Label("Nombre:"), 0, r);
        grid.add(cbCliente, 1, r++);

        grid.add(new Label("Fecha:"), 0, r);
        grid.add(dpFecha, 1, r++);

        grid.add(new Label("Productos:"), 0, r);

        VBox productosBox =
                new VBox(
                        6,
                        listaProductos,
                        btnAgregarProducto);

        grid.add(productosBox, 1, r++);

        grid.add(new Label("Estado:"), 0, r);
        grid.add(cbEstado, 1, r++);

        grid.add(new Label("Cantidad de pagadores:"), 0, r);
        grid.add(tfCantidadPagadores, 1, r++);

        grid.add(new Label("Pagos:"), 0, r);
        grid.add(contPagadores, 1, r++);

        grid.add(lblTotal, 1, r++);
        grid.add(lblRestante, 1, r++);

        grid.add(new Label("Observaciones:"), 0, r);
        grid.add(inputObservaciones, 1, r++);

        grid.add(new Label("Consumidor:"), 0, r);
        grid.add(inputConsumidor, 1, r++);

        return grid;
    }
}