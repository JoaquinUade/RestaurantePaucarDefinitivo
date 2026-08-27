package paucar.ventas.util;

import java.util.ArrayList;
import java.util.Optional;

import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaRequest;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import paucar.service.ProductosService;
import paucar.ventas.model.LineaVenta;

public final class VentaBuilder {

    private VentaBuilder() {
    }

    public static String construirVenta(
            VentaRequest venta,
            ComboBox<String> cbCliente,
            DatePicker dpFecha,
            ComboBox<TipoDePago> cbEstado,
            TextField tfConsumidor,
            VBox contLineas,
            TextField tfObs) {

        String nombre = cbCliente.getEditor().getText();

        if (nombre == null || nombre.isBlank()) {
            nombre = cbCliente.getValue();
        }

        String nombreLimpio
                = nombre == null ? ""
                        : nombre.trim();

        venta.setEstado(cbEstado.getValue());
        venta.setFecha(dpFecha.getValue());
        venta.setObservaciones(
                tfObs.getText() == null
                ? ""
                : tfObs.getText().trim());

        String consumidor = tfConsumidor.getText();

        venta.setConsumidor(
                consumidor == null || consumidor.isBlank()
                ? null
                : consumidor.trim()
        );

        if (venta.getIdProductos() == null) {
            venta.setIdProductos(
                    new ArrayList<>());
        } else {
            venta.getIdProductos().clear();
        }

        if (venta.getCantidades() == null) {
            venta.setCantidades(
                    new ArrayList<>());
        } else {
            venta.getCantidades().clear();
        }

        for (var n : contLineas.getChildren()) {

            if (n instanceof HBox fila) {

                fichaPedido(fila).ifPresent(
                        linea -> {

                            venta.getIdProductos()
                                    .add(linea.idProducto());

                            venta.getCantidades()
                                    .add(linea.cantidad());

                        });
            }
        }

        return nombreLimpio;
    }

    private static Optional<LineaVenta> fichaPedido(
            HBox fila) {

        @SuppressWarnings("unchecked")
        ComboBox<ProductosService.ProductoItem> comboProducto
                = (ComboBox<ProductosService.ProductoItem>) fila.getChildren().get(0);

        TextField cant
                = (TextField) fila.getChildren().get(1);

        var prodElegido
                = comboProducto.getValue();

        if (prodElegido == null) {

            return Optional.empty();
        }

        int cantidadElegida
                = Integer.parseInt(
                        cant.getText());

        if (cantidadElegida >= 1) {

            return Optional.of(
                    new LineaVenta(
                            prodElegido.id(),
                            cantidadElegida));
        }

        return Optional.empty();
    }
}
