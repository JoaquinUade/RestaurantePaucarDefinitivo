package paucar.ventas.ui;

import java.util.List;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleGroup;
import paucar.service.ClientesService;

public final class ClienteTipoManager {

    private ClienteTipoManager() {
    }

    public static void cargarInicial(
            ToggleGroup tgTipoCliente,
            ClientesService clientesService,
            ObservableList<String> clientes,
            FilteredList<String> clientesFiltrados,
            ComboBox<String> cbCliente) {

        if (tgTipoCliente.getSelectedToggle() == null
                || clientesService == null) {
            return;
        }

        TipoCliente tipo =
                (TipoCliente) tgTipoCliente
                        .getSelectedToggle()
                        .getUserData();

        cbCliente.setDisable(true);

        new Thread(() -> {

            List<String> nombres;

            try {

                nombres = clientesService
                        .obtenerNombresPorTipo(tipo);

            } catch (Exception ex) {

                nombres = List.of();
            }

            List<String> nombresFinal = nombres;

            Platform.runLater(() -> {

                clientes.setAll(nombresFinal);

                String txt =
                        cbCliente.getEditor().getText();

                String lower =
                        txt == null
                        ? ""
                        : txt.trim().toLowerCase();

                clientesFiltrados.setPredicate(
                        s -> s != null
                        && (lower.isEmpty()
                        || s.toLowerCase()
                                .contains(lower)));

                cbCliente.setDisable(false);
            });

        }, "cargar-clientes-inicial").start();
    }

    public static void configurarCambioTipo(
            ToggleGroup tgTipoCliente,
            ClientesService clientesService,
            ObservableList<String> clientes,
            FilteredList<String> clientesFiltrados,
            ComboBox<String> cbCliente) {

        tgTipoCliente.selectedToggleProperty()
                .addListener((o, a, b) -> {

                    if (b == null
                            || clientesService == null) {
                        return;
                    }

                    TipoCliente tipo =
                            (TipoCliente)
                            b.getUserData();

                    cbCliente.setDisable(true);

                    new Thread(() -> {

                        List<String> nombres;

                        try {

                            nombres =
                                    clientesService
                                            .obtenerNombresPorTipo(
                                                    tipo);

                        } catch (Exception ex) {

                            nombres = List.of();
                        }

                        List<String> nombresFinal =
                                nombres;

                        Platform.runLater(() -> {

                            String sel =
                                    cbCliente.getValue();

                            clientes.setAll(
                                    nombresFinal);

                            String txt =
                                    cbCliente
                                            .getEditor()
                                            .getText();

                            String lower =
                                    txt == null
                                    ? ""
                                    : txt.trim()
                                            .toLowerCase();

                            clientesFiltrados
                                    .setPredicate(
                                            s -> s != null
                                            && (lower.isEmpty()
                                            || s.toLowerCase()
                                                    .contains(lower)));

                            if (sel != null
                                    && !nombresFinal.contains(
                                            sel)) {

                                cbCliente.setValue(
                                        null);

                                cbCliente
                                        .getEditor()
                                        .clear();
                            }

                            cbCliente.setDisable(
                                    false);
                        });

                    }, "cargar-clientes-por-tipo")
                            .start();
                });
    }
}