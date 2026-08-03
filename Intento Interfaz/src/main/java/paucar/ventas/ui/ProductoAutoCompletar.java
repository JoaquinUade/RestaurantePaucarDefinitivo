package paucar.ventas.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import paucar.service.ProductosService;

public final class ProductoAutoCompletar {

    private ProductoAutoCompletar() {
    }

    public static void configurar(
            ComboBox<ProductosService.ProductoItem> cbProd,
            ObservableList<ProductosService.ProductoItem> productos) {

        FilteredList<ProductosService.ProductoItem> productosFiltrados
                = new FilteredList<>(productos, p -> true);

        cbProd.setItems(productosFiltrados);

        AtomicBoolean actualizandoProd
                = new AtomicBoolean(false);

        configurarConverter(
                cbProd,
                productos);

        configurarAutocompletar(
                cbProd,
                productosFiltrados,
                actualizandoProd);

        configurarRenderer(
                cbProd,
                actualizandoProd);
    }

    // pegar acá:
    // configurarConverter()
    private static void configurarConverter(ComboBox<ProductosService.ProductoItem> cbProd,
            ObservableList<ProductosService.ProductoItem> productos) {
        cbProd.setConverter(new StringConverter<>() {
            @Override
            /*toString:es un metodo que convierte un objeto en texto */
            public String toString(ProductosService.ProductoItem p) {/*p es un objeto productoitem */

                if (p == null) {/*si no tiene valor retorna vacio*/
                    return "";
                } else {
                    return p.nombre();/*sino retorna nombre */
                }/*La condición sirve para que el ComboBox muestre el nombre del producto cuando existe,
                   y muestre vacío sin errores cuando no hay ningún producto seleccionado por ejemplo,
                   las casillas vacias de la tabla*/
            }

            @Override
            public ProductosService.ProductoItem fromString(String text) {
                if (text == null) {
                    return null;
                }
                String s = text.trim();
                if (s.isEmpty()) {
                    return null;
                }
                for (ProductosService.ProductoItem p : productos) {
                    if (p.nombre().equalsIgnoreCase(s)) {
                        return p; // solo match exacto

                    }
                }
                return null;
            }
        });
    }

    // configurarAutocompletar()
    private static void configurarAutocompletar(
            ComboBox<ProductosService.ProductoItem> cbProd,
            FilteredList<ProductosService.ProductoItem> productosFiltrados,
            java.util.concurrent.atomic.AtomicBoolean actualizandoProd) {

        // 1) Filtrar en vivo mientras escribe (contiene)
        cbProd.getEditor().textProperty().addListener((obs, TextoPrevio, TextoActual) -> {
            if (actualizandoProd.get()) {
                return; // NO filtrar si estoy seteando por código

            }
            String txt = (TextoActual == null ? "" : TextoActual.trim().toLowerCase());
            if (txt.isEmpty()) {
                // Mostrar Todo cuando no hay texto
                productosFiltrados.setPredicate(p -> true);
            } else {
                productosFiltrados.setPredicate(p
                        -> p != null && p.nombre() != null && p.nombre().toLowerCase().contains(txt)
                );
                if (!cbProd.isShowing()) {
                    cbProd.show();
                }
            }
        });
// 2) Al seleccionar: reflejar selección SIN tocar predicate ni limpiar editor
        cbProd.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) {
                Platform.runLater(() -> {
                    actualizandoProd.set(true);
                    try {
                        cbProd.setValue(b);
                        cbProd.getEditor().setText(b.nombre());
                        cbProd.getEditor().positionCaret(b.nombre().length());
                    } finally {
                        actualizandoProd.set(false);
                    }
                });
            }
        });

        // 3) Al abrir el popup, asegurate de mostrar todo
        cbProd.showingProperty().addListener((o, was, is) -> {
            if (is) {
                productosFiltrados.setPredicate(p -> true);
            }
        });

        // 4) Al perder foco, intentá resolver el texto contra la lista (match exacto),
        //    pero NO borres la selección si no hay match y NO limpies value cuando el editor queda vacío.
        cbProd.getEditor().focusedProperty().addListener((o, was, is) -> {
            if (!is) {
                var elegido = cbProd.getConverter().fromString(cbProd.getEditor().getText());
                if (elegido != null) {
                    actualizandoProd.set(true);
                    try {
                        cbProd.setValue(elegido);
                        cbProd.getEditor().setText(elegido.nombre());
                        cbProd.getEditor().positionCaret(elegido.nombre().length());
                        productosFiltrados.setPredicate(p -> true);
                    } finally {
                        actualizandoProd.set(false);
                    }
                } else {
                    productosFiltrados.setPredicate(p -> true);
                }
            }
        });
    }

    // configurarRenderer()
    private static void configurarRenderer(ComboBox<ProductosService.ProductoItem> cbProd,
            java.util.concurrent.atomic.AtomicBoolean actualizandoProd) {
        cbProd.setCellFactory(list -> {
            javafx.scene.control.ListCell<ProductosService.ProductoItem> cell
                    = new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(ProductosService.ProductoItem item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.nombre());
                }
            };

            // Interceptar el clic: seleccionar por OBJETO, actualizar editor, cerrar y consumir
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, ev -> {
                if (!cell.isEmpty()) {
                    var item = cell.getItem();
                    actualizandoProd.set(true);
                    try {
                        cbProd.getSelectionModel().select(item); // seleccionar por objeto (no índice)
                        cbProd.setValue(item);
                        cbProd.getEditor().setText(item.nombre());
                        cbProd.getEditor().positionCaret(item.nombre().length());
                    } finally {
                        actualizandoProd.set(false);
                    }
                    cbProd.hide();
                    ev.consume(); // evita que el SelectionModel re-mapée por índice
                }
            });

            return cell;
        });
        cbProd.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ProductosService.ProductoItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.nombre());
            }
        });
    }
}
