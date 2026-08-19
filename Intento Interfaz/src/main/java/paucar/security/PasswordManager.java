package paucar.security;

import org.mindrot.jbcrypt.BCrypt;

import io.github.cdimascio.dotenv.Dotenv;

public class PasswordManager {

    private static final Dotenv DOTENV =
            Dotenv.configure()
                  .ignoreIfMissing()
                  .load();

    private static final String PASSWORD_ENV =
            DOTENV.get("ADMIN_PASSWORD");

    private static final String HASH;

    static {

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
}