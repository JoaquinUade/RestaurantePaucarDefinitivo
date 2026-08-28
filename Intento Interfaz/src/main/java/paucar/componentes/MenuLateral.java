package paucar.componentes;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MenuLateral extends VBox {

    private final Button btnVentas;
    private final Button btnResumen;
    private final Button btnGastos;
    private final Button btnStock;
    private final Button btnPagos;
    private final Button btnAdmin;

    private final StackPane logoItem;

    public MenuLateral() {

        getStyleClass().add("menu");
        setPadding(new Insets(20));
        setSpacing(15);
        setPrefWidth(200);
        setPadding(new Insets(20));
        setFillWidth(true);
        setMinWidth(200);

        setMaxWidth(200);
        btnVentas = crearBotonConIcono("VENTAS", "/img/ventas.png", 65, 65);
        btnResumen = crearBotonConIcono("RESUMEN", "/img/resumen.png", 65, 65);
        btnGastos = crearBotonConIcono("GASTOS", "/img/gastos.png", 65, 65);
        btnStock = crearBotonConIcono("STOCK", "/img/stock.png", 65, 65);
        btnPagos = crearBotonConIcono("PAGOS", "/img/pagos.png", 75, 65);
        btnAdmin = crearBotonConIcono("ADMIN", "/img/admin.png", 65, 65);

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

        logoView.setFitWidth(130);
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
        btn.setGraphicTextGap(8);
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
