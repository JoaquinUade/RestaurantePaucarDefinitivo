package paucar.config;

import javafx.scene.Scene;

public class CssLoader {

    public static void cargar(Scene scene) {

        String[] archivos = {
            "/app.css",
            "/stylemensual.css",
            "/stylesemanal.css",
            "/styletabla.css",
            "/platos.css",
            "/empresasclientes.css",
            "/agregar.css",
            "/resumen.css",
            "/gastos.css"
        };

        for (String archivo : archivos) {
            scene.getStylesheets().add(
                    CssLoader.class
                            .getResource(archivo)
                            .toExternalForm()
            );
        }
    }
}