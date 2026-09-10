package paucar.shared;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/** Utilidad visual común para las tablas sin registros. */
public final class TablaUtils {

    private TablaUtils() {
    }

    public static void configurarMensajeSinDatos(TableView<?> tabla) {
        Label mensaje = new Label("No hay datos ingresados para mostrar.");
        mensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");
        tabla.setPlaceholder(mensaje);
    }
}
