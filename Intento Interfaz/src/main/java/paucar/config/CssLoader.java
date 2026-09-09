package paucar.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import javafx.scene.Scene;

/**
 * Carga las hojas de estilo de la app y, si la pantalla actual no es la
 * pantalla de diseño (1366 x 768), escala automáticamente todos los
 * valores en píxeles (font-size, padding, radios, min/pref/max, etc.)
 * usando {@link Responsive}, para que la interfaz se vea igual en
 * cualquier pantalla.
 */
public class CssLoader {

    private static final String[] ARCHIVOS = {
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

    /** Propiedades cuyo valor es un ancho (escalan por X). */
    private static final Set<String> PROPS_X = Set.of(
            "-fx-min-width", "-fx-pref-width", "-fx-max-width");

    /** Propiedades cuyo valor es un alto (escalan por Y). */
    private static final Set<String> PROPS_Y = Set.of(
            "-fx-min-height", "-fx-pref-height", "-fx-max-height");

    /** Propiedades uniformes (escalan con la escala mínima). */
    private static final Set<String> PROPS_U = Set.of(
            "-fx-font-size",
            "-fx-padding",
            "-fx-label-padding",
            "-fx-background-radius",
            "-fx-border-radius",
            "-fx-border-width",
            "-fx-background-insets",
            "-fx-graphic-text-gap",
            "-fx-translate-x",
            "-fx-translate-y");

    /** Detecta un número (con signo opcional y decimales) seguido de "px" opcional. */
    private static final Pattern NUMERO = Pattern.compile("(-?\\d+(?:\\.\\d+)?)(px)?");

    private static final List<Path> temporales = new ArrayList<>();

    private static boolean hookRegistrado = false;

    private CssLoader() {
    }

    public static void cargar(Scene scene) {
        for (String archivo : ARCHIVOS) {
            try {
                final String css = leerRecurso(archivo);

                if (escalaNecesaria()) {
                    scene.getStylesheets().add(
                            guardarTemporal(archivo, escalarCss(css)));
                } else {
                    // Pantalla de diseño: usamos el CSS original tal cual.
                    scene.getStylesheets().add(
                            CssLoader.class.getResource(archivo).toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("No se pudo aplicar el estilo " + archivo + ": " + e);
            }
        }
    }

    private static boolean escalaNecesaria() {
        return Math.abs(Responsive.escX() - 1.0) > 0.001
                || Math.abs(Responsive.escY() - 1.0) > 0.001
                || Math.abs(Responsive.escU() - 1.0) > 0.001;
    }

    private static String leerRecurso(String archivo) throws IOException {
        try (var in = CssLoader.class.getResourceAsStream(archivo)) {
            if (in == null) {
                throw new IOException("Recurso no encontrado: " + archivo);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String guardarTemporal(String nombre, String contenido) throws IOException {
        Path tmp = Files.createTempFile(
                Path.of(System.getProperty("java.io.tmpdir")),
                "paucar_css_", ".css");
        Files.writeString(tmp, contenido, StandardCharsets.UTF_8);
        temporales.add(tmp);

        if (!hookRegistrado) {
            hookRegistrado = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (Path p : temporales) {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception ignorado) {
                        // No crítico.
                    }
                }
            }));
        }

        return tmp.toUri().toString();
    }

    /**
     * Escala los valores numéricos de las propiedades conocidas,
     * dejando intactos colores y efectos.
     */
    private static String escalarCss(String css) {
        final StringBuilder out = new StringBuilder(css.length() + 64);
        for (String linea : css.split("\n", -1)) {
            out.append(escalarLinea(linea)).append('\n');
        }
        return out.toString();
    }

    private static String escalarLinea(String linea) {
        final int dosPuntos = linea.indexOf(':');
        if (dosPuntos <= 0) {
            return linea;
        }

        final String prop = linea.substring(0, dosPuntos).trim().toLowerCase();

        final double factor;
        if (PROPS_X.contains(prop)) {
            factor = Responsive.escX();
        } else if (PROPS_Y.contains(prop)) {
            factor = Responsive.escY();
        } else if (PROPS_U.contains(prop)) {
            factor = Responsive.escU();
        } else {
            return linea;
        }

        final String antes = linea.substring(0, dosPuntos + 1);
        final String valor = linea.substring(dosPuntos + 1);

        final Matcher m = NUMERO.matcher(valor);
        final String valorEscalado = m.replaceAll(r -> {

            final double n = Double.parseDouble(r.group(1));
            final double escalado = Math.round(n * factor * 100.0) / 100.0;

            final String numero = (escalado == Math.floor(escalado))
                    ? String.valueOf((long) escalado)
                    : String.valueOf(escalado);

            return numero + (r.group(2) == null ? "" : "px");
        });

        return antes + valorEscalado;
    }
}