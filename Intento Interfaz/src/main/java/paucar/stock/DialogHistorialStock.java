package paucar.stock;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.Stock;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import paucar.config.Responsive;
import paucar.service.StockService;

public class DialogHistorialStock {

    public static void mostrar(
            Stock stock,
            List<HistorialStock> historial,
            List<GastosVariables> gastos,
            StockService stockService,
            LocalDate fechaSeleccionada) {

        Stage ventana = new Stage();

        ventana.initModality(
                Modality.APPLICATION_MODAL);

        ventana.setTitle(
                "Historial - "
                + stock.getNombreProducto());

        VBox root = new VBox(Responsive.pe(15));
        root.setPadding(Responsive.insets(20));
        Runnable[] actualizar = new Runnable[1];
        List<HistorialStock> registros = new java.util.ArrayList<>(historial);
        actualizar[0] = () -> llenarSemanas(
                root,
                registros,
                gastos,
                stockService,
                actualizar[0],
                fechaSeleccionada);
        actualizar[0].run();
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        ventana.setScene(new Scene(scrollPane, Responsive.px(1200), Responsive.py(600)));
        ventana.showAndWait();
    }

    private static void llenarSemanas(
            VBox root,
            List<HistorialStock> historial,
            List<GastosVariables> gastos,
            StockService stockService,
            Runnable actualizar,
            LocalDate fechaSeleccionada) {
        root.getChildren().clear();
        Label tituloMes = new Label(
                fechaSeleccionada.getMonth().toString()
                + " "
                + fechaSeleccionada.getYear());

        tituloMes.setStyle(
                "-fx-font-size: 24px;"
                + "-fx-font-weight: bold;"
        );

        root.getChildren().add(tituloMes);
        List<HistorialStock> historialMes = historial.stream()
                .filter(h
                        -> h.getFecha().getMonth() == fechaSeleccionada.getMonth()
                && h.getFecha().getYear() == fechaSeleccionada.getYear())
                .toList();
        Map<LocalDate, List<HistorialStock>> historialPorSemana
                = historialMes.stream()
                        .collect(Collectors.groupingBy(
                                h -> h.getFecha().with(DayOfWeek.MONDAY),
                                TreeMap::new,
                                Collectors.toList()));

        int semana = 1;

        for (Map.Entry<LocalDate, List<HistorialStock>> entry
                : historialPorSemana.entrySet()) {

            Label titulo = new Label(
                    "SEMANA " + semana);

            titulo.setStyle(
                    "-fx-font-size: 18px;"
                    + "-fx-font-weight: bold;");

            TableView<HistorialStock> tabla
                    = crearTabla(
                            entry.getValue(),
                            gastos, stockService, () -> {
                                List<HistorialStock> nuevos = stockService.obtenerHistorialPorStock(
                                        historial.get(0).getStock().getIdStock());
                                historial.clear();
                                historial.addAll(nuevos);
                                actualizar.run();
                            });

            root.getChildren().addAll(
                    titulo,
                    tabla);

            semana++;
        }

    }

    private static TableView<HistorialStock> crearTabla(
            List<HistorialStock> historial, List<GastosVariables> gastos, StockService stockService, Runnable actualizar) {

        TableView<HistorialStock> tabla
                = new TableView<>();
        paucar.shared.TablaUtils.configurarMensajeSinDatos(tabla);

        TableColumn<HistorialStock, String> colFecha
                = new TableColumn<>("Fecha");

        colFecha.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getFecha()
                                .toString()));

        TableColumn<HistorialStock, String> colMovimiento
                = new TableColumn<>("Movimiento");

        colMovimiento.setCellFactory(col
                -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String item,
                    boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);

                HistorialStock historial
                        = getTableView()
                                .getItems()
                                .get(getIndex());

                if (historial.getMovimiento() != null) {

                    if (historial.getMovimiento()
                            .compareTo(java.math.BigDecimal.ZERO)
                            > 0) {

                        setStyle(
                                "-fx-text-fill: green;"
                                + "-fx-font-weight: bold;");

                    } else if (historial.getMovimiento()
                            .compareTo(java.math.BigDecimal.ZERO)
                            < 0) {

                        setStyle(
                                "-fx-text-fill: red;"
                                + "-fx-font-weight: bold;");
                    }
                }
            }
        });

        colMovimiento.setCellValueFactory(c -> {

            if (c.getValue().getMovimiento() == null) {
                return new SimpleStringProperty("-");
            }

            var mov = c.getValue().getMovimiento();

            String texto
                    = mov.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? "+" + mov.stripTrailingZeros().toPlainString()
                    : mov.stripTrailingZeros().toPlainString();

            return new SimpleStringProperty(texto);
        });

        TableColumn<HistorialStock, String> colStock
                = new TableColumn<>("Stock");

        colStock.setCellValueFactory(c
                -> new SimpleStringProperty(
                        c.getValue()
                                .getCantidad()
                                .stripTrailingZeros()
                                .toPlainString()));

        TableColumn<HistorialStock, String> colGasto
                = new TableColumn<>("Gasto");

        colGasto.setCellValueFactory(c -> {

            if (c.getValue().getGastoVariable() == null) {
                return new SimpleStringProperty("-");
            }

            return new SimpleStringProperty(
                    c.getValue()
                            .getGastoVariable()
                            .getProducto());
        });
        TableColumn<HistorialStock, Void> colAcciones
                = new TableColumn<>("Acciones");

        colAcciones.setCellFactory(param
                -> new TableCell<>() {

            private final Button btnEditar
                    = new Button("Editar");

            private final Button btnEliminar
                    = new Button("Eliminar");

            {
                btnEditar.setOnAction(event -> {

                    HistorialStock registro
                            = getTableView()
                                    .getItems()
                                    .get(getIndex());

                    HistorialStock editado
                            = DialogHistorialEditar
                                    .mostrarEditar(registro, gastos, stockService);

                    if (editado != null) {

                        actualizar.run();
                    }
                });

                btnEliminar.setOnAction(event -> {

                    HistorialStock registro
                            = getTableView()
                                    .getItems()
                                    .get(getIndex());

                    if (DialogHistorialEditar.confirmarEliminacion()) {

                        stockService.eliminarHistorial(
                                registro.getId());

                        getTableView()
                                .getItems()
                                .remove(registro);

                        getTableView().refresh();

                        System.out.println(
                                "Registro eliminado");
                    }
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

                    HBox botones
                            = new HBox(Responsive.pe(5),
                                    btnEditar,
                                    btnEliminar);

                    setGraphic(botones);
                }
            }
        });
        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colMovimiento);
        tabla.getColumns().add(colStock);
        tabla.getColumns().add(colGasto);
        tabla.getColumns().add(colAcciones);

        tabla.setItems(
                FXCollections.observableArrayList(
                        historial));

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        return tabla;
    }
}
