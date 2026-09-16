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

    private static final Preferences PREFERENCIAS
            = Preferences.userNodeForPackage(PasswordManager.class);
    private static final Dotenv DOTENV = Dotenv.configure()
            .directory("../")
            .ignoreIfMissing()
            .load();
    private static final String PIN_RECUPERACION = DOTENV.get("RECOVERY_PIN");

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

        PREFERENCIAS.put(CLAVE_HASH, BCrypt.hashpw(nueva, BCrypt.gensalt(12)));
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
        return pin != null && PIN_RECUPERACION != null
                && MessageDigest.isEqual(
                pin.getBytes(StandardCharsets.UTF_8),
                PIN_RECUPERACION.getBytes(StandardCharsets.UTF_8));
    }

    private static synchronized String obtenerHash() {
        String hashGuardado = PREFERENCIAS.get(CLAVE_HASH, null);
        if (hashGuardado != null && !hashGuardado.isBlank()) {
            return hashGuardado;
        }

        String passwordInicial = DOTENV.get("ADMIN_PASSWORD");
        if (passwordInicial == null || passwordInicial.isBlank()) {
            throw new IllegalStateException(
                    "No se encontró una contraseña inicial de administrador.");
        }

        String hashInicial = BCrypt.hashpw(passwordInicial, BCrypt.gensalt(12));
        PREFERENCIAS.put(CLAVE_HASH, hashInicial);
        return hashInicial;
    }
}
