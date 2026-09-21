package paucar;

import java.nio.file.*;
import java.nio.channels.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.uade.tpo.demo.DemoApplication;

/** Arranque de escritorio con MySQL local configurable. */
public final class Escritorio {
    public static void main(String[] args) {
        boolean verify = Arrays.asList(args).contains("--verificar");
        Path root = Path.of(System.getProperty("paucar.data.dir",
            Path.of(System.getenv("LOCALAPPDATA"), "RestaurantePaucar", "datos").toString()));
        int result = 0;
        try {
            Files.createDirectories(root);
            System.setProperty("paucar.config.dir", root.toString());
            System.setProperty("javafx.cachedir", root.resolve("cache").toString());
            PrintStream log = new PrintStream(new FileOutputStream(root.resolve("restaurante.log").toFile(), true), true, "UTF-8");
            System.setOut(log); System.setErr(log);
            try (FileChannel channel = FileChannel.open(root.resolve("aplicacion.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock lock = channel.tryLock()) {
                if (lock == null) throw new IllegalStateException("Restaurante Paucar ya está abierto.");
                Properties mysql = cargarMysql(root, verify, Arrays.asList(args).contains("--configurar"));
                if (mysql == null) return;
                if (!verify && !Files.exists(root.resolve("admin.properties")) && !configurarAcceso(root)) return;
                int port = Integer.getInteger("paucar.port", 4002);
                try (ServerSocket check = new ServerSocket()) {
                    check.bind(new InetSocketAddress("127.0.0.1", port));
                } catch (IOException e) {
                    throw new IllegalStateException("El puerto " + port + " está ocupado. Cerrá la otra instancia del restaurante e intentá de nuevo.", e);
                }
                Map<String,Object> props = new HashMap<>();
                props.put("server.address", "127.0.0.1");
                props.put("server.port", port);
                props.put("spring.datasource.url", mysqlUrl(mysql));
                props.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
                props.put("spring.datasource.username", mysql.getProperty("usuario"));
                props.put("spring.datasource.password", mysql.getProperty("password"));
                props.put("spring.jpa.hibernate.ddl-auto", "validate");
                props.put("spring.sql.init.mode", "always");
                props.put("spring.jpa.show-sql", "false");
                SpringApplication app = new SpringApplication(DemoApplication.class);
                app.addInitializers(context -> context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource("escritorio", props)));
                try (ConfigurableApplicationContext context = app.run()) {
                    if (verify) {
                        System.out.println("VERIFICACION CORRECTA: base local y servidor disponibles.");
                        if (Arrays.asList(args).contains("--esperar")) Thread.sleep(90000);
                    } else {
                        javafx.application.Application.launch(Aplicacion.class, args);
                    }
                }
            }
        } catch (Throwable e) {
            result = 1; e.printStackTrace();
            if (!verify) JOptionPane.showMessageDialog(null, "No se pudo abrir Restaurante Paucar.\n" + e.getMessage() + "\nDetalles: " + root.resolve("restaurante.log"), "Restaurante Paucar", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(result);
    }

    private static String mysqlUrl(Properties mysql) {
        return "jdbc:mysql://127.0.0.1:" + mysql.getProperty("puerto", "3306")
            + "/daina?createDatabaseIfNotExist=true&connectionTimeZone=LOCAL&connectTimeout=5000&socketTimeout=30000&allowPublicKeyRetrieval=true&sslMode=PREFERRED";
    }

    private static Properties cargarMysql(Path root, boolean verify, boolean configure) throws Exception {
        Path file = root.resolve("mysql.properties");
        Properties mysql = new Properties();
        if (Files.exists(file)) try (Reader reader = Files.newBufferedReader(file)) { mysql.load(reader); }
        boolean edit = configure || !Files.exists(file);
        while (true) {
            if (edit) {
                if (verify) throw new IllegalStateException("Falta configurar MySQL.");
                JTextField port = new JTextField(mysql.getProperty("puerto", "3306"));
                JTextField user = new JTextField(mysql.getProperty("usuario", "root"));
                JPasswordField password = new JPasswordField(mysql.getProperty("password", ""));
                Object[] form = {"Conectá con MySQL instalado en esta laptop.", "Base de datos: daina (se crea si no existe).", "Las tablas se preparan automáticamente. No se borran datos.", "Puerto:", port, "Usuario de MySQL:", user, "Contraseña de ese usuario en MySQL:", password};
                if (JOptionPane.showConfirmDialog(null, form, "Conexión a MySQL — Restaurante Paucar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;
                try {
                    int p = Integer.parseInt(port.getText().trim());
                    if (p < 1 || p > 65535 || user.getText().isBlank()) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) { JOptionPane.showMessageDialog(null, "Ingresá un puerto válido y un usuario."); continue; }
                mysql.setProperty("puerto", port.getText().trim());
                mysql.setProperty("usuario", user.getText().trim());
                mysql.setProperty("password", new String(password.getPassword()));
            }
            try (java.sql.Connection connection = java.sql.DriverManager.getConnection(mysqlUrl(mysql), mysql.getProperty("usuario"), mysql.getProperty("password", ""))) {
                if (!connection.isValid(5)) throw new java.sql.SQLException("La conexión no respondió.");
                if (edit) try (Writer writer = Files.newBufferedWriter(file)) { mysql.store(writer, "Conexion local a daina. Archivo privado."); }
                return mysql;
            } catch (java.sql.SQLException e) {
                if (verify) throw e;
                JOptionPane.showMessageDialog(null, "No se pudo conectar con MySQL.\nVerificá que MySQL esté instalado e iniciado y que el usuario y contraseña sean correctos.\nEl usuario necesita permisos sobre daina.\n\n" + e.getMessage(), "Revisar conexión", JOptionPane.ERROR_MESSAGE);
                edit = true;
            }
        }
    }

    private static boolean configurarAcceso(Path root) throws IOException {
        while (true) {
            JPasswordField password = new JPasswordField();
            JPasswordField confirmation = new JPasswordField();
            JPasswordField pin = new JPasswordField();
            Object[] form = {"Bienvenido. Creá tu acceso de administrador.", "Contraseña (8 caracteres, letras y números):", password, "Repetí la contraseña:", confirmation, "PIN de recuperación (mínimo 6 dígitos). Guardalo en un lugar seguro:", pin};
            if (JOptionPane.showConfirmDialog(null, form, "Primer inicio — Restaurante Paucar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return false;
            String pass = new String(password.getPassword()), repeat = new String(confirmation.getPassword()), recovery = new String(pin.getPassword());
            if (pass.length() < 8 || !pass.chars().anyMatch(Character::isLetter) || !pass.chars().anyMatch(Character::isDigit) || pass.chars().anyMatch(Character::isWhitespace) || !pass.equals(repeat) || !recovery.matches("[0-9]{6,}")) {
                JOptionPane.showMessageDialog(null, "Revisá la contraseña, su confirmación y el PIN."); continue;
            }
            Properties config = new Properties();
            config.setProperty("admin.password.hash", BCrypt.hashpw(pass, BCrypt.gensalt(12)));
            config.setProperty("recovery.pin.hash", BCrypt.hashpw(recovery, BCrypt.gensalt(12)));
            try (Writer writer = Files.newBufferedWriter(root.resolve("admin.properties"))) { config.store(writer, "Acceso Restaurante Paucar"); }
            return true;
        }
    }
}
