package paucar.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javafx.geometry.Insets;
import javafx.stage.Screen;

/**
 * Utilidad que convierte las medidas fijas de la interfaz en porcentajes
 * que se adaptan a cualquier pantalla, de forma totalmente automática:
 *
 * 1. La PRIMERA vez que se ejecuta la app, mide la pantalla actual y la
 *    guarda como "referencia de diseño" en un archivo dentro de la carpeta
 *    del usuario (sin que haya que cambiar nada en el código).
 * 2. En las siguientes ejecuciones lee esa referencia guardada, mide la
 *    pantalla actual y calcula la escala.
 *
 * En la pantalla donde se diseñó la app la escala es 1.0 (se ve igual que
 * siempre) y en cualquier otra pantalla se multiplica por el factor
 * correspondiente para que todo ocupe el mismo porcentaje de la pantalla.
 */
public final class Responsive {

    /** Referencias de diseño por defecto (solo se usan si no hay configuración guardada). */
    public static final double ANCHO_DISENO_DEFECTO = 1366;

    /** Ver {@link #ANCHO_DISENO_DEFECTO}. */
    public static final double ALTO_DISENO_DEFECTO = 720;

    private static final String NOMBRE_ARCHIVO = ".paucar-responsive.properties";
    private static final String CLAVE_ANCHO = "anchoDiseno";
    private static final String CLAVE_ALTO = "altoDiseno";

    private static double anchoDiseno = ANCHO_DISENO_DEFECTO;
    private static double altoDiseno = ALTO_DISENO_DEFECTO;

    private static double anchoActual = ANCHO_DISENO_DEFECTO;
    private static double altoActual = ALTO_DISENO_DEFECTO;

    private static double escX = 1.0;
    private static double escY = 1.0;
    private static double escU = 1.0;

    private Responsive() {
    }

    /**
     * Mide la pantalla actual, carga (o genera) la referencia de diseño y
     * calcula las escalas. Debe llamarse UNA vez al arrancar la app antes
     * de construir la interfaz.
     */
    public static void inicializar() {
        try {
            final var vb = Screen.getPrimary().getVisualBounds();
            anchoActual = Math.max(1.0, vb.getWidth());
            altoActual = Math.max(1.0, vb.getHeight());
        } catch (Exception e) {
            // Sin pantalla disponible: escala 1:1 (nunca debería pasar).
            anchoActual = ANCHO_DISENO_DEFECTO;
            altoActual = ALTO_DISENO_DEFECTO;
        }

        cargarOGenerarDiseno();

        escX = anchoActual / anchoDiseno;
        escY = altoActual / altoDiseno;
        escU = Math.min(escX, escY);
    }

    public static double anchoActual() {
        return anchoActual;
    }

    public static double altoActual() {
        return altoActual;
    }

    /** Escala horizontal (para anchos). */
    public static double escX() {
        return escX;
    }

    /** Escala vertical (para altos). */
    public static double escY() {
        return escY;
    }

    /** Escala uniforme (para fuentes, radios y padding). */
    public static double escU() {
        return escU;
    }

    /** Convierte una medida horizontal de diseño a la pantalla actual. */
    public static double px(double valorDiseno) {
        return valorDiseno * escX;
    }

    /** Convierte una medida vertical de diseño a la pantalla actual. */
    public static double py(double valorDiseno) {
        return valorDiseno * escY;
    }

    /** Convierte una medida uniforme (fuente, radio, gap) a la pantalla actual. */
    public static double pe(double valorDiseno) {
        return valorDiseno * escU;
    }

    /** Como {@link #pe(double)} pero con un mínimo en píxeles reales. */
    public static double peMin(double valorDiseno, double minimo) {
        return Math.max(minimo, valorDiseno * escU);
    }

    /** Insets escalados de forma uniforme (arriba, derecha, abajo, izquierda). */
    public static Insets insets(double valor) {
        final double s = pe(valor);
        return new Insets(s, s, s, s);
    }

    /** Insets escalados con valores individuales. */
    public static Insets insets(double top, double right, double bottom, double left) {
        return new Insets(pe(top), pe(right), pe(bottom), pe(left));
    }

    /**
     * Lee la referencia de diseño guardada. Si no existe (primera vez),
     * usa la pantalla actual como referencia y la guarda automáticamente.
     */
    private static void cargarOGenerarDiseno() {
        final Path archivo = archivoConfig();
        try {
            if (Files.exists(archivo)) {
                final Properties p = new Properties();
                try (var in = Files.newBufferedReader(archivo)) {
                    p.load(in);
                }
                anchoDiseno = parsePositivo(p.getProperty(CLAVE_ANCHO), anchoDiseno);
                altoDiseno = parsePositivo(p.getProperty(CLAVE_ALTO), altoDiseno);
            } else {
                // Primera ejecución: la pantalla actual es la referencia de diseño.
                anchoDiseno = anchoActual;
                altoDiseno = altoActual;
                guardarDiseno();
            }
        } catch (java.io.IOException e) {
    System.err.println(
        "Responsive: no se pudo leer la configuracion de pantalla: " + e);
}
    }

    private static void guardarDiseno() {
        try {
            final Properties p = new Properties();
            p.setProperty(CLAVE_ANCHO, String.valueOf(Math.round(anchoDiseno)));
            p.setProperty(CLAVE_ALTO, String.valueOf(Math.round(altoDiseno)));
            try (var out = Files.newBufferedWriter(archivoConfig())) {
                p.store(out, "Resolucion de diseno de la interfaz (auto-generado, no editar)");
            }
        } catch (java.io.IOException e) {
            // No crítico: si no se puede guardar, se sigue con los valores por defecto.
            System.err.println("Responsive: no se pudo guardar la configuracion de pantalla: " + e);
        }
    }

    private static Path archivoConfig() {
        return Path.of(
                System.getProperty("user.home", "."),
                NOMBRE_ARCHIVO);
    }

    private static double parsePositivo(String valor, double fallback) {
        if (valor == null || valor.isBlank()) {
            return fallback;
        }
        try {
            final double v = Double.parseDouble(valor.trim());
            return v > 0 ? v : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}