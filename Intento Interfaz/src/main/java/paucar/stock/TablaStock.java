package paucar.stock;

import com.uade.tpo.demo.entity.Stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;

public class TablaStock extends BorderPane {

    private TableView<Stock> tabla;

    public TablaStock() {

        tabla = new TableView<>();

        TableColumn<Stock, String> colProducto =
                new TableColumn<>("Producto");

        colProducto.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombreProducto()
                ));

        TableColumn<Stock, String> colCategoria =
                new TableColumn<>("Categoría");

        colCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()
                                .getCategoriaGastoVariable()
                                .getNombre()
                ));

        TableColumn<Stock, String> colCantidad =
                new TableColumn<>("Cantidad");

        colCantidad.setCellValueFactory(data -> {

            Stock stock = data.getValue();

            String valor =
                    stock.getCantidad() +
                    " " +
                    stock.getUnidadMedida();

            return new SimpleStringProperty(valor);
        });

        TableColumn<Stock, String> colMinimo =
                new TableColumn<>("Stock mínimo");

        colMinimo.setCellValueFactory(data -> {

            Stock stock = data.getValue();

            String valor =
                    stock.getStockMinimo() +
                    " " +
                    stock.getUnidadMedida();

            return new SimpleStringProperty(valor);
        });

        TableColumn<Stock, String> colEstado =
                new TableColumn<>("Estado");

        colEstado.setCellValueFactory(data -> {

            Stock stock = data.getValue();

            boolean bajoStock =
                    stock.getCantidad()
                         .compareTo(stock.getStockMinimo()) <= 0;

            return new SimpleStringProperty(
                    bajoStock ? "Bajo Stock" : "OK"
            );
        });

        TableColumn<Stock, Void> colAcciones =
                new TableColumn<>("Acciones");

        colAcciones.setCellFactory(param ->
                new TableCell<>() {

                    private final Button btnEditar =
                            new Button("Editar");

                    private final Button btnEliminar =
                            new Button("Eliminar");

                    {
                        btnEditar.setOnAction(e -> {

    Stock stock =
            getTableView()
            .getItems()
            .get(getIndex());

    System.out.println(
            "Editar: "
            + stock.getNombreProducto()
    );

    cargarDatos();
});

                        btnEliminar.setOnAction(e -> {

                            Stock stock =
                                    getTableView()
                                    .getItems()
                                    .get(getIndex());

                            // stockService.eliminar(stock.getIdStock());

                            cargarDatos();
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {

                            ToolBar toolbar =
                                    new ToolBar(
                                            btnEditar,
                                            btnEliminar
                                    );

                            setGraphic(toolbar);
                        }
                    }
                });

        tabla.getColumns().add(colProducto);
        tabla.getColumns().add(colCategoria);
        tabla.getColumns().add(colCantidad);
        tabla.getColumns().add(colMinimo);
        tabla.getColumns().add(colEstado);
        tabla.getColumns().add(colAcciones);

        setCenter(tabla);
    }

    public void cargarDatos() {

        // List<Stock> stocks =
        // stockService.obtenerTodos();

        tabla.setItems(
                FXCollections.observableArrayList()
        );
    }
    public Stock getSeleccionado() {

    return tabla.getSelectionModel()
            .getSelectedItem();
}
}