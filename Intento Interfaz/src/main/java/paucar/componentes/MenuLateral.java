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
    private final Button btnCalcula;
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
        btnVentas = crearBotonConIcono("VENTAS", "/img/ventas.png");
        btnResumen = crearBotonConIcono("RESUMEN", "/img/resumen.png");
        btnGastos = crearBotonConIcono("GASTOS", "/img/gastos.png");
        btnStock = crearBotonConIcono("STOCK", "/img/stock.png");
        btnCalcula = crearBotonConIcono("CALCULA", "/img/calcula.png");
        btnAdmin = crearBotonConIcono("ADMIN", "/img/admin.png");

        btnVentas.getStyleClass().add("active");

        Button[] botones = {
            btnVentas,
            btnResumen,
            btnGastos,
            btnStock,
            btnCalcula,
            btnAdmin
        };

        for (Button b : botones) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefWidth(Double.MAX_VALUE);
        }

        Image logoImg
                = new Image(
                        getClass()
                                .getResourceAsStream("/img/logo paucar.png"));

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
                btnCalcula,
                btnAdmin
        );
    }

    private Button crearBotonConIcono(
            String texto,
            String rutaIcono) {

        Image img
                = new Image(
                        getClass()
                                .getResourceAsStream(rutaIcono));

        ImageView icono = new ImageView(img);

        icono.setFitWidth(65);
        icono.setFitHeight(65);
        
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

    public Button getBtnCalcula() {
        return btnCalcula;
    }

    public Button getBtnAdmin() {
        return btnAdmin;
    }

    public StackPane getLogoItem() {
        return logoItem;
    }
}
