package paucar.security;

/**
 * Token temporal de autorización de contraseña.
 *
 * Cuando el usuario ingresa la contraseña correcta una vez (verificada por
 * {@link PasswordManager}), se emite un token que permite realizar otras
 * operaciones del MISMO diálogo/vista sin volver a pedir la contraseña.
 *
 * El token se invalida:
 *  - al cambiar de pestaña/sección en la aplicación, y
 *  - automáticamente después de {@link #DURACION_TOKEN_MS}.
 */
public final class SesionPassword {

    /** Duración del token en milisegundos (5 minutos). */
    private static final long DURACION_TOKEN_MS = 5 * 60 * 1000L;

    /** Timestamp (ms) en el que el token deja de ser válido. */
    private static long expiraEn = 0L;

    private SesionPassword() {
    }

    /** @return true si el token sigue vigente (sin expirar). */
    public static boolean estaAutorizado() {
        return System.currentTimeMillis() < expiraEn;
    }

    /** Emite (o renueva) el token de sesión. */
    public static void autorizar() {
        expiraEn = System.currentTimeMillis() + DURACION_TOKEN_MS;
    }

    /** Invalida el token: la próxima acción volverá a pedir la contraseña. */
    public static void invalidar() {
        expiraEn = 0L;
    }
}