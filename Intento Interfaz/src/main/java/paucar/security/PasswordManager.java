package paucar.security;

import org.mindrot.jbcrypt.BCrypt;

import io.github.cdimascio.dotenv.Dotenv;

public class PasswordManager {

    private static final Dotenv DOTENV =
        Dotenv.configure()
              .directory("../../")
              .ignoreIfMissing()
              .load();

    private static final String PASSWORD_ENV
            = DOTENV.get("ADMIN_PASSWORD");

    private static final String HASH;

    static {
        System.out.println(
                "Directorio actual: "
                + System.getProperty("user.dir"));

        System.out.println(
                "ADMIN_PASSWORD: "
                + DOTENV.get("ADMIN_PASSWORD")
        );
        if (PASSWORD_ENV == null || PASSWORD_ENV.isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD no definida en el .env"
            );
        }

        HASH = BCrypt.hashpw(
                PASSWORD_ENV,
                BCrypt.gensalt()
        );
    }

    private PasswordManager() {
    }

    public static boolean verificar(String passwordIngresada) {

        if (passwordIngresada == null) {
            return false;
        }

        return BCrypt.checkpw(
                passwordIngresada,
                HASH
        );
    }
    /**
     * Verifica la contraseña teniendo en cuenta el token de sesión.
     *
     * - Si el token sigue activo: devuelve true sin pedir contraseña.
     * - Si el token expiró/no existe: verifica la contraseña y, si es
     *   correcta, emite un nuevo token.
     */
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
}
