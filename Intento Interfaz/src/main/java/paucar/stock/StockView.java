package paucar.stock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.uade.tpo.demo.entity.CategoriaGastoVariable;
import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import paucar.service.CategoriasGastosService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;
import paucar.shared.FechaUtils;

public class StockView extends BorderPane {

    private final StockService service;
    private final CategoriasGastosService categoriasService;
    private Stock stockSeleccionado;
    private final GastosVariablesService gastosVariablesService;
    private boolean modoDiario = false;
    private final HBox contenedorCategorias = new HBox(20);
    private DatePicker filtroFecha;
    private Label lblFecha = new Label();

    public StockView(StockService service, CategoriasGastosService categoriasService,
            GastosVariablesService gastosVariablesService) {

        this.service = service;
        this.categoriasService = categoriasService;
        this.gastosVariablesService = gastosVariablesService;

        Button btnAgregar = new Button("Agregar Producto");
        btnAgregar.getStyleClass().add("btn-agregar");
        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("btn-editar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("btn-eliminar");
        Button btnCambiarVista = new Button("Modo Diario");
        btnCambiarVista.getStyleClass().add("btn-editar");
        filtroFecha = new DatePicker(LocalDate.now());
        filtroFecha.getStyleClass().add("date-agregar");
        filtroFecha.setOnAction(e -> {
            actualizarFecha();
            recargar();
        });

        btnAgregar.setOnAction(e -> {

            List<CategoriaGastoVariable> categorias
                    = this.categoriasService.obtenerCategorias();

            List<GastosVariables> gastos
                    = this.gastosVariablesService.obtenerTodos();
            List<Stock> stocks = service.obtenerTodos();

            StockRequest request = DialogStock.mostrar(categorias, gastos, stocks);

            if (request != null) {

                service.crear(request);

                recargar();
            }
        });
        btnEditar.setOnAction(e -> {

            if (stockSeleccionado == null) {
                return;
            }

            List<CategoriaGastoVariable> categorias
                    = categoriasService.obtenerCategorias();

            StockRequest editado
                    = DialogStock.mostrarEditar(
                            categorias,
                            stockSeleccionado,
                            gastosVariablesService);

            if (editado != null) {

                stockSeleccionado.setNombreProducto(
                        editado.getNombreProducto());

                stockSeleccionado.setCantidad(
                        editado.getCantidad());

                stockSeleccionado.setUnidadCantidad(
                        editado.getUnidadCantidad());

                service.editar(
                        stockSeleccionado.getIdStock(),
                        stockSeleccionado
                );

                recargar();
            }
        });
        btnEliminar.setOnAction(e -> {

            if (stockSeleccionado == null) {
                return;
            }

            boolean confirmado
                    = DialogStock.confirmarEliminacion();

            if (confirmado) {

                service.eliminar(
                        stockSeleccionado.getIdStock()
                );

                recargar();
            }
        });
        Region spacerTop = new Region();
        HBox.setHgrow(spacerTop, Priority.ALWAYS);

        Region spacerBottom = new Region();
        HBox.setHgrow(spacerBottom, Priority.ALWAYS);
        contenedorCategorias.setPadding(new Insets(15));
        ScrollPane scroll = new ScrollPane(contenedorCategorias);
        scroll.getStyleClass().add("scroll-pane");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMinHeight(507);
        // 🔥 claves
        scroll.setFitToWidth(false); // permite scroll horizontal
        scroll.setFitToHeight(false);

        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // horizontal

        VBox fondo = new VBox();
        fondo.getStyleClass().add("fondo-rojo");
        fondo.setPadding(new Insets(15));
        fondo.setSpacing(15);

        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);
        HBox topBar = new HBox(10, filtroFecha, lblFecha, spacerTop, btnAgregar);
        HBox barraBotones = new HBox(10, btnEditar, btnEliminar, spacerBottom, btnCambiarVista);

        topBar.setPadding(new Insets(10));
        fondo.getChildren().addAll(topBar, scroll, barraBotones);
        btnCambiarVista.setOnAction(e -> {

            modoDiario = !modoDiario;

            if (modoDiario) {
                btnCambiarVista.setText("Modo Stock");
                topBar.getChildren().remove(btnAgregar);
            } else {
                btnCambiarVista.setText("Modo Diario");

                if (!topBar.getChildren().contains(btnAgregar)) {
                    topBar.getChildren().add(btnAgregar);
                }
            }

            recargar();
        });
        setCenter(fondo);

        actualizarFecha();
        recargar();
    }

    private void recargar() {

        contenedorCategorias.getChildren().clear();

        List<Stock> stocks = service.obtenerTodos();
        LocalDate fechaSeleccionada = filtroFecha.getValue();

        if (modoDiario) {
            stocks = stocks.stream()
                    .filter(stock -> {

                        List<HistorialStock> historial
                                = service.obtenerHistorialPorStock(
                                        stock.getIdStock());
                        System.out.println("========");
                        System.out.println(stock.getNombreProducto());

                        historial.forEach(h -> System.out.println(
                                h.getFecha() + " -> " + h.getCantidad()
                        )
                        );
                        if (historial.isEmpty()) {
                            return false;
                        }

                        LocalDate fechaNacimiento = historial.stream()
                                .map(HistorialStock::getFecha)
                                .filter(java.util.Objects::nonNull)
                                .min(java.util.Comparator.naturalOrder())
                                .orElse(null);

                        if (fechaNacimiento == null) {
                            return false;
                        }

                        if (fechaSeleccionada.isBefore(fechaNacimiento)) {
                            return false;
                        }

                        HistorialStock ultimoRegistro
                                = historial.stream()
                                        .filter(h
                                                -> h.getFecha() != null
                                        && !h.getFecha().isAfter(fechaSeleccionada))
                                        .max(java.util.Comparator.comparing(
                                                HistorialStock::getFecha))
                                        .orElse(null);

                        if (ultimoRegistro == null) {
                            return false;
                        }
System.out.println(
    "ULTIMO: "
    + ultimoRegistro.getFecha()
    + " -> "
    + ultimoRegistro.getCantidad()
);
                        stock.setCantidad(
                                ultimoRegistro.getCantidad());

                        return true;
                    })
                    .toList();
        } else {

            stocks = stocks.stream()
                    .filter(s -> s.getFecha() != null
                    && s.getFecha().equals(fechaSeleccionada))
                    .toList();
        }
        for (Stock s : stocks) {
            System.out.println(
                    s.getNombreProducto() + " - "
                    + s.getCategoriaGastoVariable().getNombre());
        }

        Map<String, List<Stock>> porCategoria
                = stocks.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getCategoriaGastoVariable()
                                        .getNombre()));

        System.out.println("Categorias: " + porCategoria.size());

        porCategoria.forEach((categoria, lista) -> {

            System.out.println("Categoria: " + categoria);

            contenedorCategorias.getChildren().add(
                    new PanelCategoriaStock(
                            categoria,
                            lista,
                            stock -> stockSeleccionado = stock,
                            modoDiario,
                            service,
                            gastosVariablesService,
                            categoriasService,
                            fechaSeleccionada));
        });
    }

    private void actualizarFecha() {

        lblFecha.setText(
                FechaUtils.formatearTitulo(
                        filtroFecha.getValue()
                )
        );

        lblFecha.getStyleClass().add("titulo-xl-blanco");
    }
}
