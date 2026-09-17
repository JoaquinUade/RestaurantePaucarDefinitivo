package paucar.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.prefs.Preferences;

import org.mindrot.jbcrypt.BCrypt;

import io.github.cdimascio.dotenv.Dotenv;

/** Maneja la contraseña sin guardarla ni mostrarla en texto plano. */
public final class PasswordManager {

    private static final String CLAVE_HASH = "admin.password.hash";
    private static final int MAX_INTENTOS_PIN = 5;
    private static final long BLOQUEO_PIN_MS = 5 * 60 * 1000L;

    private static final java.nio.file.Path CONFIG = java.nio.file.Path.of(System.getProperty("paucar.config.dir"), "admin.properties");
    private static final java.util.Properties CONFIGURACION = cargarConfiguracion();
    private static final String PIN_RECUPERACION = CONFIGURACION.getProperty("recovery.pin.hash");

    private static java.util.Properties cargarConfiguracion() {
        java.util.Properties config = new java.util.Properties();
        try (java.io.Reader reader = java.nio.file.Files.newBufferedReader(CONFIG)) { config.load(reader); }
        catch (java.io.IOException e) { throw new IllegalStateException("No se pudo leer la configuración de acceso.", e); }
        return config;
    }
    private static void guardarHash(String hash) {
        CONFIGURACION.setProperty(CLAVE_HASH, hash);
        try {
            java.nio.file.Path temporal = CONFIG.resolveSibling("admin.properties.tmp");
            try (java.io.Writer writer = java.nio.file.Files.newBufferedWriter(temporal)) { CONFIGURACION.store(writer, "Acceso Restaurante Paucar"); }
            java.nio.file.Files.move(temporal, CONFIG, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo guardar la contraseña.", e); }
    }
    private static int intentosPinFallidos;
    private static long pinBloqueadoHasta;

    private PasswordManager() {
    }

    public static boolean verificar(String passwordIngresada) {
        if (passwordIngresada == null || passwordIngresada.isBlank()) {
            return false;
        }
        return BCrypt.checkpw(passwordIngresada, obtenerHash());
    }

    /** Verifica la contraseña sólo cuando no hay una autorización vigente. */
    public static boolean verificarConSesion(String passwordIngresada) {
        if (SesionPassword.estaAutorizado()) {
            return true;
        }

        boolean resultado = verificar(passwordIngresada);
        if (resultado) {
            SesionPassword.autorizar();
        }
        return resultado;
    }

    /** Cambia la contraseña conociendo la actual. Devuelve null si fue bien. */
    public static synchronized String cambiarConContrasenaActual(String actual,
            String nueva, String confirmacion) {
        if (!verificar(actual)) {
            return "La contraseña actual es incorrecta.";
        }
        return guardarNuevaContrasena(nueva, confirmacion);
    }

    /** Restablece con el PIN y bloquea el PIN tras cinco fallos por 5 minutos. */
    public static synchronized String restablecerConPin(String pin,
            String nueva, String confirmacion) {
        if (PIN_RECUPERACION == null || PIN_RECUPERACION.isBlank()) {
            return "No está configurado el PIN de recuperación.";
        }

        long ahora = System.currentTimeMillis();
        if (ahora < pinBloqueadoHasta) {
            long minutos = Math.max(1,
                    (pinBloqueadoHasta - ahora + 59_999) / 60_000);
            return "El PIN está bloqueado. Esperá " + minutos + " minuto(s).";
        }

        if (!coincidePin(pin)) {
            intentosPinFallidos++;
            if (intentosPinFallidos >= MAX_INTENTOS_PIN) {
                pinBloqueadoHasta = ahora + BLOQUEO_PIN_MS;
                intentosPinFallidos = 0;
                return "Demasiados intentos. El PIN se bloqueó por 5 minutos.";
            }
            return "El PIN de recuperación es incorrecto.";
        }

        intentosPinFallidos = 0;
        return guardarNuevaContrasena(nueva, confirmacion);
    }

    private static String guardarNuevaContrasena(String nueva,
            String confirmacion) {
        String error = validarNuevaContrasena(nueva, confirmacion);
        if (error != null) {
            return error;
        }

        guardarHash(BCrypt.hashpw(nueva, BCrypt.gensalt(12)));
        SesionPassword.autorizar();
        return null;
    }

    private static String validarNuevaContrasena(String nueva,
            String confirmacion) {
        if (nueva == null || nueva.length() < 8) {
            return "La nueva contraseña debe tener al menos 8 caracteres.";
        }
        if (nueva.chars().anyMatch(Character::isWhitespace)) {
            return "La contraseña no puede tener espacios.";
        }
        if (!nueva.chars().anyMatch(Character::isLetter)
                || !nueva.chars().anyMatch(Character::isDigit)) {
            return "Usá al menos una letra y un número.";
        }
        if (!nueva.equals(confirmacion)) {
            return "La confirmación no coincide con la nueva contraseña.";
        }
        return null;
    }

    private static boolean coincidePin(String pin) {
        return pin != null && PIN_RECUPERACION != null && BCrypt.checkpw(pin, PIN_RECUPERACION);
    }
    private static synchronized String obtenerHash() {
        String hash = CONFIGURACION.getProperty(CLAVE_HASH);
        if (hash == null || hash.isBlank()) throw new IllegalStateException("Falta configurar el administrador.");
        return hash;
    }
}
