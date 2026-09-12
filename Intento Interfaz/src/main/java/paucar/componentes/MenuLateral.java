package paucar.componentes;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import paucar.config.Responsive;

public final class MenuLateral extends VBox {

    private final Button btnVentas;
    private final Button btnResumen;
    private final Button btnGastos;
    private final Button btnStock;
    private final Button btnPagos;
    private final Button btnAdmin;

    private final StackPane logoItem;

    public MenuLateral() {

        getStyleClass().add("menu");
        setPadding(Responsive.insets(20));
        setSpacing(Responsive.pe(15));
        setPrefWidth(Responsive.px(200));
        setFillWidth(true);

        btnVentas = crearBotonConIcono("VENTAS", "/img/ventas.png", Responsive.px(65), Responsive.py(65));
        btnResumen = crearBotonConIcono("RESUMEN", "/img/resumen.png", Responsive.px(65), Responsive.py(65));
        btnGastos = crearBotonConIcono("GASTOS", "/img/gastos.png", Responsive.px(65), Responsive.py(65));
        btnStock = crearBotonConIcono("STOCK", "/img/stock.png", Responsive.px(65), Responsive.py(65));
        btnPagos = crearBotonConIcono("PAGOS", "/img/pagos.png", Responsive.px(75), Responsive.py(65));
        btnAdmin = crearBotonConIcono("ADMIN", "/img/admin.png", Responsive.px(65), Responsive.py(65));

        btnVentas.getStyleClass().add("active");

        Button[] botones = {
            btnVentas,
            btnResumen,
            btnGastos,
            btnStock,
            btnPagos,
            btnAdmin
        };

        for (Button b : botones) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefWidth(Double.MAX_VALUE);
        }

        Image logoImg
                = new Image(
                        getClass()
                                .getResource("/img/logo paucar.png")
                                .toExternalForm());

        ImageView logoView = new ImageView(logoImg);

        logoView.setFitWidth(Responsive.px(130));
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        logoView.setCache(true);

        logoItem = new StackPane(logoView);
        logoItem.getStyleClass().add("menu-logo");

        getChildren().addAll(
                logoItem,
                btnVentas,
                btnResumen,
                btnGastos,
                btnStock,
                btnPagos,
                btnAdmin
        );
    }

    private Button crearBotonConIcono(
            String texto,
            String rutaIcono,
            double ancho,
            double alto) {

        Image img = new Image(
                getClass()
                        .getResource(rutaIcono)
                        .toExternalForm());

        ImageView icono = new ImageView(img);

        icono.setFitWidth(ancho);
        icono.setFitHeight(alto);

        Button btn = new Button(texto);
        btn.setGraphic(icono);
        btn.setGraphicTextGap(Responsive.pe(8));
        btn.setContentDisplay(ContentDisplay.TOP);

        return btn;
    }

    public Button getBtnVentas() {
        return btnVentas;
    }

    public Button getBtnResumen() {
        return btnResumen;
    }

    public Button getBtnGastos() {
        return btnGastos;
    }

    public Button getBtnStock() {
        return btnStock;
    }

    public Button getBtnPagos() {
        return btnPagos;
    }

    public Button getBtnAdmin() {
        return btnAdmin;
    }

    public StackPane getLogoItem() {
        return logoItem;
    }
}
