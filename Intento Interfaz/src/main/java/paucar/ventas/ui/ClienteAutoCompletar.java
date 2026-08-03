package paucar.ventas.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;

public final class ClienteAutoCompletar {

    private ClienteAutoCompletar() {
    }

    public static ComboBox<String> crear(
            FilteredList<String> clientesFiltrados) {

        ComboBox<String> cbCliente =
                new ComboBox<>(clientesFiltrados);

        cbCliente.setEditable(true);
        cbCliente.setPromptText(
                "Nombre (cliente/mesa/empresa)");

        AtomicBoolean actualizandoEditor =
                new AtomicBoolean(false);

        cbCliente.getEditor().textProperty().addListener(
                (obs, previo, actual) -> {

                    if (actualizandoEditor.get()) {
                        return;
                    }

                    String txt =
                            actual == null
                            ? ""
                            : actual.trim().toLowerCase();

                    clientesFiltrados.setPredicate(
                            s -> s == null
                            || txt.isEmpty()
                            || s.toLowerCase().contains(txt));

                    if (!cbCliente.isShowing()
                            && !txt.isEmpty()) {

                        cbCliente.show();
                    }
                });

        cbCliente.setCellFactory(listView -> {

            var cell =
                    new javafx.scene.control.ListCell<String>() {

                @Override
                protected void updateItem(
                        String item,
                        boolean empty) {

                    super.updateItem(item, empty);

                    setText(
                            empty || item == null
                            ? ""
                            : item);
                }
            };

            cell.addEventFilter(
                    MouseEvent.MOUSE_PRESSED,
                    ev -> {

                        if (!cell.isEmpty()) {

                            String item =
                                    cell.getItem();

                            actualizandoEditor.set(true);

                            try {

                                cbCliente.getSelectionModel()
                                        .select(item);

                                cbCliente.setValue(item);

                                cbCliente.getEditor()
                                        .setText(item);

                                cbCliente.getEditor()
                                        .positionCaret(
                                                item.length());

                            } finally {

                                actualizandoEditor.set(false);
                            }

                            cbCliente.hide();
                            ev.consume();
                        }
                    });

            return cell;
        });

        cbCliente.setButtonCell(
                new javafx.scene.control.ListCell<>() {

            @Override
            protected void updateItem(
                    String item,
                    boolean empty) {

                super.updateItem(item, empty);

                setText(
                        empty || item == null
                        ? ""
                        : item);
            }
        });

        cbCliente.showingProperty().addListener(
                (o, was, is) -> {

                    if (is) {
                        clientesFiltrados.setPredicate(
                                s -> true);
                    }
                });

        cbCliente.setOnAction(e -> {

            String v = cbCliente.getValue();

            if (v != null) {

                actualizandoEditor.set(true);

                try {

                    cbCliente.getEditor()
                            .setText(v);

                    cbCliente.getEditor()
                            .positionCaret(
                                    v.length());

                } finally {

                    actualizandoEditor.set(false);
                }
            }
        });

        return cbCliente;
    }
}