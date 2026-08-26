package paucar;

import com.uade.tpo.demo.entity.dto.VentaRequest;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import paucar.admin.Admin;
import paucar.componentes.MenuLateral;
import paucar.config.CssLoader;
import paucar.config.ServiceContainer;
import paucar.gastos.Gastos;
import paucar.pagos.PagosView;
import paucar.resumen.Resumen;
import paucar.service.ClientesService;
import paucar.service.VentasBackend;
import paucar.stock.StockView;
import paucar.ventas.Ventas;

public class Aplicacion extends Application {

    private Ventas vistaVentas;
    private Resumen vistaResumen;
    private Gastos vistaGastos;
    private StockView vistaStock;
    private PagosView vistaPagos;

    private static final String API_BASE
            = "http://localhost:4002/api";

    private VentasBackend backend;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, 1000, 700);

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

        VBox contenido = new VBox(30);
        contenido.getStyleClass().add("content");
        contenido.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Alertas de Stock");
        titulo.getStyleClass().add("titulo-welcome");

        VBox lineas = new VBox(20);

        for (int i = 0; i < 4; i++) {

            Region linea = new Region();

            linea.getStyleClass().add("line");
            linea.setPrefHeight(50);
            linea.setMaxWidth(600);

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

        vistaResumen
                = new Resumen(backend);

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

            limpiarActivos(menu);

            root.setCenter(contenido);
        });

        menu.getBtnVentas().setOnAction(e -> {

            marcarActivo(
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnCalcula(),
                    menu.getBtnAdmin());

            root.setCenter(vistaVentas);

            vistaVentas.recargarDelBackend();
        });

        menu.getBtnResumen().setOnAction(e -> {

            marcarActivo(
                    menu.getBtnResumen(),
                    menu.getBtnVentas(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnCalcula(),
                    menu.getBtnAdmin());

            root.setCenter(vistaResumen);
        });

        menu.getBtnGastos().setOnAction(e -> {

            marcarActivo(
                    menu.getBtnGastos(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnCalcula(),
                    menu.getBtnAdmin());

            root.setCenter(vistaGastos);
        });

        menu.getBtnStock().setOnAction(e -> {

            marcarActivo(
                    menu.getBtnStock(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnPagos(),
                    menu.getBtnCalcula(),
                    menu.getBtnAdmin());

            root.setCenter(vistaStock);
        });

        menu.getBtnPagos().setOnAction(e -> {

            marcarActivo(
                    menu.getBtnPagos(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnCalcula(),
                    menu.getBtnAdmin());

            root.setCenter(vistaPagos);
        });

        menu.getBtnCalcula().setOnAction(e -> {

            marcarActivo(
                    menu.getBtnCalcula(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnAdmin());
        });

        menu.getBtnAdmin().setOnAction(e -> {

            marcarActivo(
                    menu.getBtnAdmin(),
                    menu.getBtnVentas(),
                    menu.getBtnResumen(),
                    menu.getBtnGastos(),
                    menu.getBtnStock(),
                    menu.getBtnPagos(),
                    menu.getBtnCalcula());

            root.setCenter(
                    new Admin(
                            services.adminService,
                            clientesService,
                            services.categorias,
                            services.empleados
                    ));
        });

        stage.setTitle("Interfaz");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private void limpiarActivos(MenuLateral menu) {

        menu.getBtnVentas().getStyleClass().remove("active");
        menu.getBtnResumen().getStyleClass().remove("active");
        menu.getBtnGastos().getStyleClass().remove("active");
        menu.getBtnStock().getStyleClass().remove("active");
        menu.getBtnPagos().getStyleClass().remove("active");
        menu.getBtnCalcula().getStyleClass().remove("active");
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
