package paucar;

import com.uade.tpo.demo.entity.dto.VentaRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import paucar.admin.Admin;
import paucar.componentes.MenuLateral;
import paucar.config.CssLoader;
import paucar.config.HttpCompartido;
import paucar.config.Responsive;
import paucar.config.ServiceContainer;
import paucar.gastos.Gastos;
import paucar.pagos.PagosView;
import paucar.resumen.Resumen;
import paucar.security.SesionPassword;
import paucar.service.ClientesService;
import paucar.service.ExcelExportService;
import paucar.service.VentasBackend;
import paucar.stock.StockView;
import paucar.ventas.Ventas;

public class Aplicacion extends Application {

    private Ventas vistaVentas;
    private Resumen vistaResumen;
    @SuppressWarnings("unused")
    private Gastos vistaGastos;
    private StockView vistaStock;
    private PagosView vistaPagos;
    private Label estadoConexion;
    private Timeline verificadorConexion;

    private static final String API_BASE
            = "http://localhost:4002/api";

    private VentasBackend backend;

    @Override
    public void start(Stage stage) {

        // Mide la pantalla actual y calcula las escalas de la UI.
        Responsive.inicializar();

        BorderPane root = new BorderPane();

        configurarEstadoConexion(root);

        Scene scene = new Scene(
                root,
                Responsive.px(1000),
                Responsive.py(700));

        CssLoader.cargar(scene);

        VentaRequest venta = new VentaRequest();

        ClientesService clientesService
                = new ClientesService(API_BASE, venta);

        backend = new VentasBackend(
                API_BASE,
                clientesService,
                venta);

        ServiceContainer services
                = new ServiceContainer(API_BASE);

        MenuLateral menu
                = new MenuLateral();

        VBox contenido = new VBox(Responsive.pe(30));
        contenido.getStyleClass().add("content");
        contenido.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Alertas de Stock");
        titulo.getStyleClass().add("titulo-welcome");

        VBox lineas = new VBox(Responsive.pe(20));

        for (int i = 0; i < 4; i++) {

            Region linea = new Region();

            linea.getStyleClass().add("line");
            linea.setPrefHeight(Responsive.py(50));
            linea.setMaxWidth(Responsive.px(600));

            // lineas.getChildren().add(linea);
        }

        contenido.getChildren().addAll(
                titulo,
                lineas
        );

        AlertasStockView alertasView
                = new AlertasStockView(services.stock);

        contenido.getChildren().add(alertasView);

        ScrollPane menuScroll = new ScrollPane(menu);
        menuScroll.setFitToWidth(true);
        menuScroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);
        menuScroll.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED);
        menuScroll.setPannable(true);
        menuScroll.setFocusTraversable(false);

        root.setLeft(menuScroll);
        root.setCenter(contenido);

        vistaResumen = new Resumen(
        backend,
        clientesService,
        new ExcelExportService(
                backend,
                services.pagos,
                services.stock,
                services.gastosFijos,
                services.gastosVariables,
                services.gastosIndividuales),
        services.gastosVariables,
        services.gastosFijos,
        services.gastosIndividuales
);

        vistaVentas
                = new Ventas(vistaResumen);

        vistaGastos
                = new Gastos(
                        services.gastosVariables,
                        services.categorias,
                        services.gastosIndividuales,
                        services.empleados,
                        services.gastosFijos);

        vistaStock
                = new StockView(
                        services.stock,
                        services.categorias,
                        services.gastosVariables,
                        stock -> {
                        });

        vistaPagos
                = new PagosView(
                        services.pagos,
                        clientesService,
                        backend);

        menu.getLogoItem().setOnMouseClicked(e -> {

            SesionPassword.invalidar();

            limpiarActivos(menu);

            root.setCenter(contenido);
        });

        menu.getBtnVentas().setOnAction(e -> {

            SesionPassword.invalidar();

            marcarActivo(
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnAdmin());

            root.setCenter(vistaVentas);

            vistaVentas.recargarDelBackend();
        });

        menu.getBtnResumen().setOnAction(e -> {

            SesionPassword.invalidar();

            marcarActivo(
                    menu.getBtnResumen(),
                    menu.getBtnVentas(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnAdmin());

            root.setCenter(vistaResumen);
        });

        menu.getBtnGastos().setOnAction(e -> {

            SesionPassword.invalidar();

            marcarActivo(
                    menu.getBtnGastos(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnAdmin());

            root.setCenter(
    new Gastos(
        services.gastosVariables,
        services.categorias,
        services.gastosIndividuales,
        services.empleados,
        services.gastosFijos
    )
);
        });

        menu.getBtnStock().setOnAction(e -> {

            SesionPassword.invalidar();

            marcarActivo(
                    menu.getBtnStock(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnPagos(),
                    menu.getBtnAdmin());

            root.setCenter(vistaStock);
        });

        menu.getBtnPagos().setOnAction(e -> {

            SesionPassword.invalidar();

            marcarActivo(
                    menu.getBtnPagos(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnAdmin());

            root.setCenter(vistaPagos);
        });

        menu.getBtnAdmin().setOnAction(e -> {

            SesionPassword.invalidar();

            marcarActivo(
                    menu.getBtnAdmin(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnPagos()
            );

            root.setCenter(
                    new Admin(
                            services.adminService,
                            clientesService,
                            services.categorias,
                            services.empleados,
                            vistaResumen
                    )
            );
        });

        stage.setTitle("Interfaz");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private void configurarEstadoConexion(BorderPane root) {
        estadoConexion = new Label("Verificando conexión con el servidor...");
        estadoConexion.setStyle("-fx-padding: 8 16; -fx-font-size: 12px;"
                + "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;");
        estadoConexion.setVisible(false);
        estadoConexion.setManaged(false);
        root.setBottom(estadoConexion);

        verificarConexion();

        verificadorConexion = new Timeline(new KeyFrame(
                Duration.seconds(10), e -> verificarConexion()));
        verificadorConexion.setCycleCount(Timeline.INDEFINITE);
        verificadorConexion.play();
    }

    private void verificarConexion() {
        String healthUrl = API_BASE.replace("/api", "") + "/actuator/health";

        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(healthUrl))
                        .timeout(java.time.Duration.ofSeconds(3))
                        .GET()
                        .build();

                HttpResponse<Void> response = HttpCompartido.getHttpClient()
                        .send(request, HttpResponse.BodyHandlers.discarding());
                return response.statusCode() == 200;
            } catch (java.io.IOException | InterruptedException ignored) {
                return false;
            }
        }).thenAccept(conectado -> Platform.runLater(() -> {
            if (conectado) {
                estadoConexion.setVisible(false);
                estadoConexion.setManaged(false);
            } else {
                estadoConexion.setText("⚠ No se pudo conectar con el servidor. "
                        + "Iniciá el backend antes de cargar o consultar datos.");
                estadoConexion.setStyle("-fx-padding: 8 16; -fx-font-size: 12px;"
                        + "-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c;");
                estadoConexion.setVisible(true);
                estadoConexion.setManaged(true);
            }
        }));
    }

    private void limpiarActivos(MenuLateral menu) {

        menu.getBtnVentas().getStyleClass().remove("active");
        menu.getBtnResumen().getStyleClass().remove("active");
        menu.getBtnGastos().getStyleClass().remove("active");
        menu.getBtnStock().getStyleClass().remove("active");
        menu.getBtnPagos().getStyleClass().remove("active");
        menu.getBtnAdmin().getStyleClass().remove("active");
    }

    private void marcarActivo(Button activo,
            Button... otros) {

        if (!activo.getStyleClass().contains("active")) {
            activo.getStyleClass().add("active");
        }

        for (Button b : otros) {
            b.getStyleClass().remove("active");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
