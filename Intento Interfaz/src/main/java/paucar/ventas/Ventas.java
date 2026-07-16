package paucar.ventas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.Venta;
import com.uade.tpo.demo.entity.dto.VentaRequest;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import paucar.resumen.Resumen;
import paucar.resumen.empresas.semanal.TablaSemanalDebe;
import paucar.service.ClientesService;
import paucar.service.ProductosService;
import paucar.service.VentasBackend;
import paucar.shared.FechaUtils;
import paucar.shared.LocaleUtils;
import paucar.shared.MonedaUtils;

public final class Ventas extends BorderPane {

    // ====== Constantes y formateadores ======
    private static final String API_BASE = "http://localhost:4002/api";

    private final VentaRequest venta = new VentaRequest();
    // ====== Servicios / backend ======
    private final ProductosService productosService = new ProductosService(API_BASE, venta);
    private final ClientesService clientesService = new ClientesService(API_BASE, venta);
    private final VentasBackend backend = new VentasBackend(API_BASE, clientesService, venta);

    // ====== Estado de la vista ======
    private final ObservableList<Venta> RenglonDeLaTabla = FXCollections.observableArrayList();

    private final ObjectProperty<BigDecimal> total = new SimpleObjectProperty<>(BigDecimal.ZERO);

    private final Button btnAgregar = new Button("+ Agregar");

    // ====== Sugerencias (clientes / productos) ======
    private final ObservableList<String> clientes = FXCollections.observableArrayList();

    private final ObservableList<ProductosService.ProductoItem> productos = FXCollections.observableArrayList();
    private TablaSemanalDebe tablaSemanalDebe;
    private final Resumen resumen;

    // ====== Constructor ======
    public Ventas(Resumen resumen) {
        this.resumen = resumen;
        setPadding(new Insets(16));
        venta.setEstado(TipoDePago.DEBE);
        venta.setObservaciones("");
        initUI();
        initAsync();
        initBindings();
    }

    private void initUI() {
        setTop(crearHeader());/*Pone el encabezado en la parte de arriba de la pantalla */
        setCenter(crearTabla());/*Pone la tabla en la parte central de la pantalla */
        setBottom(crearFooter());/*Pone el pie de página en la parte de abajo de la pantalla */
    }

    private void initAsync() {
        cargarClientesAsync();/*Empieza a cargar la lista de clientes en segundo plano*/
        cargarProductosAsync();/*Empieza a cargar la lista de productos en segundo plano*/
        recargarDelBackend();/*Empieza a cargar la tabla con las ventas del día en segundo plano*/
    }

    private void initBindings() {
        RenglonDeLaTabla.addListener((javafx.collections.ListChangeListener<Venta>) c -> MontoTotalActual());/*Cada vez que cambia la tabla (se agrega, quita o
                                                                                                            modifica una fila), se vuelve a calcular el total */
    }

    private void cargarClientesAsync() {
        CompletableFuture
                .supplyAsync(() -> clientesService.obtenerTodosLosClientesMenosMesas())
                .thenAccept(lista -> Platform.runLater(() -> clientes.setAll(lista)));
    }

    private void cargarProductosAsync() {
        CompletableFuture
                .supplyAsync(productosService::cargarProductos)
                .thenAccept(items -> Platform.runLater(() -> productos.setAll(items)));
    }

    private Node crearTabla() {
        var pane = new Tabla(
                RenglonDeLaTabla,
                LocaleUtils.ES_AR,
                fila -> eliminarVentaDesdeBackend(fila),
                (fila, nuevoEstado) -> actualizarEstadoEnBackend(fila, nuevoEstado)
        );
        return pane; // o pane.asNode() si tu clase expone ese método
    }

    private Node crearHeader() {
        var lblTitulo = new Label(
                FechaUtils.hoyTitulo()
        );
        lblTitulo.getStyleClass().add("titulo-xl");/*Aplicále al Label todos los estilos definidos para
                                                    la clase .titulo-xl de mi css */

        btnAgregar.getStyleClass().add("btn-success");/*ponele los estilos de mi css llamado btn
                                                         success */
        btnAgregar.setOnAction(e -> VentanaAgregarPedido());/*Cuando el usuario haga clic en + Agregar,
                                                           abrí el diálogo para cargar un nuevo pedido */

        var separador = new Region();/*crea separador invisible */
        HBox.setHgrow(separador, Priority.ALWAYS);

        var barra = new HBox(12, lblTitulo, separador, btnAgregar);/*ordena el titulo con la
                                                                            fecha el separador y el boton
                                                                            agregar */
        barra.setAlignment(Pos.CENTER_LEFT);/*centra */
        barra.setPadding(new Insets(0, 0, 10, 0));/*añade 10px abajo del boton
                                                                          + agregar */
        return barra;/*retorna la barra */
    }

    private Node crearFooter() {
        var TituloTotal = new Label("Total:");/*texto del total de la suma de precio de productos */
        TituloTotal.getStyleClass().add("total-titulo");/*crea total-titulo para en algun momento
                                                            estilarlo con css */
        var TextoVisualTotal = new Label();/*Creá un Label vacío llamado lblTotal. Después lo voy a llenar
                                   automáticamente con el total formateado */
        TextoVisualTotal.getStyleClass().add("total-monto");/*crea total-monto para en algun momento
                                                               estilarlo con css */
        TextoVisualTotal.textProperty()
                .bind(Bindings.createStringBinding(() -> MonedaUtils.formatearMoneda(total.get()), total));/*Cada vez que total
                                                                                 cambie, actualiza
                                                                              automáticamente el texto del
                                                                             Label con el total formateado */

        var separador = new Region();/*crea una separacion, es como un bloque que no muestra nada pero
                                     ocupa espacio */
        HBox.setHgrow(separador, Priority.ALWAYS);/*hace que el separador ocupe todo el espacio
                                                   horizontal disponible entre el titulo total y
                                                   el texto del total, empujando al texto del total
                                                   hacia la derecha */

        var box = new HBox(10, separador, TituloTotal, TextoVisualTotal);/*crea una caja que
                                                                                  posiciona en orden de 
                                                                                  izquierda a derecha
                                                                                  donde estara el espacio
                                                                                  y el contenido visual */
        box.setAlignment(Pos.CENTER_RIGHT);/*posiciona el contenido de box de forma centrada verticalmente */
        box.setPadding(new Insets(10, 0, 0, 0));/*agrega 10 px arriba del contenido */
        return box;/*retorna la box */
    }

    private void eliminarVentaDesdeBackend(Venta fila) {

        if (fila == null || fila.getIdVenta() == null) {
            return;
        }

        CompletableFuture
                .supplyAsync(() -> backend.eliminarVenta(fila.getIdVenta()))
                .thenAccept(ok -> Platform.runLater(() -> {
            if (ok) {
                recargarDelBackend();
            } else {
                new Alert(
                        Alert.AlertType.ERROR,
                        "No se pudo eliminar la venta"
                ).showAndWait();
            }
        }));
    }

    private void actualizarEstadoEnBackend(Venta fila, TipoDePago nuevoEstado) {

        CompletableFuture
                .supplyAsync(()
                        -> backend.actualizarEstadoVenta(
                        fila.getIdVenta(),
                        nuevoEstado
                )
                )
                .thenAccept(ok -> Platform.runLater(() -> {
            if (!ok) {
                new Alert(
                        Alert.AlertType.ERROR,
                        "No se pudo guardar el estado"
                ).showAndWait();
            } else {

                if (tablaSemanalDebe != null) {
                    tablaSemanalDebe.actualizar();
                }

            }
        }));
    }

    public void recargarDelBackend() {
        CompletableFuture /*CompletableFuture es una herramienta de Java que te permite ejecutar código en
                          segundo plano sin trabar la interfaz gráfica (UI)*/
                .supplyAsync(() -> backend.cargarVentasDelDia(FechaUtils.hoy()))/*Ejecuta en segundo plano el cargarVentasDelDia
                                                                                del backend, pasándole la fecha de hoy
                                                                                (LocalDate.now()), para obtener la lista de
                                                                                ventas del día sin trabar la UI*/
                .thenAccept(lista -> Platform.runLater(() -> {/*cuando termine la operación asíncrona anterior y se
                                                              obtenga la lista, se ejecutará el siguiente bloque de
                                                              código usando Platform.runLater para actualizar la
                                                              interfaz gráfica */

            RenglonDeLaTabla.setAll(lista);
            MontoTotalActual();
        }));
    }

    private void VentanaAgregarPedido() {/*este metodo es el que se ejecuta cuando tocás el botón “+ Agregar”
                                        en la pantalla de Ventas */
        productos.setAll(productosService.cargarProductos());
        var dlg = new Agregar(clientes, productos, clientesService, venta);/*crea un objeto nuevo de la clase Agregar y le
                                                          pasa clientes, productos y venta al constructor */

        var res = dlg.Mostrar(getScene() == null ? null : getScene().getWindow());/*Muestra el diálogo dlg
                                                                             usando como owner la ventana
                                                                             actual (si existe) y guarda en
                                                                          res el Optional con el resultado
                                                                           del usuario */

        res.ifPresent(nombre -> {
            var tipo = dlg.getTipoSeleccionado();
            confirmarPedidoAsync(nombre, tipo);

        });
    }

    // =========================================================================================
    // Utilitarios
    // =========================================================================================
    private void MontoTotalActual() {
        BigDecimal t = RenglonDeLaTabla.stream()
                .filter(f -> f != null)
                .filter(f -> f.getEstado() != null)
                .filter(f -> f.getEstado() != TipoDePago.DEBE)
                .map(f -> f.getMonto())
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        total.set(t.setScale(2, RoundingMode.HALF_UP));/*ajusto el total a 2 decimales con
                                                                 redondeo clásico y actualizo la propiedad
                                                                 'total' para refrescar la UI */
    }

    private void confirmarPedidoAsync(String nombreCliente, TipoCliente tipo) {

        java.util.List<Long> ids = new java.util.ArrayList<>(venta.getIdProductos());/*Creá una nueva lista llamada ids(cuyo tipo de dato es long y
                                                                                     Long es un tipo de dato numérico que sirve para guardar números
                                                                                     enteros grandes) y copiá dentro todos los números (IDs) que
                                                                                     vienen de venta.getIdProductos() */

        java.util.List<Integer> cants = new java.util.ArrayList<>(venta.getCantidades());/*Crea una lista nueva que guarda numeros enteros llamada
                                                                                         cants y copia adentro todas las cantidades que vienen de
                                                                                         venta.getCantidades() */

        TipoDePago estado = venta.getEstado();/*Creá una variable llamada estado que sea del tipo TipoDePago y guardá ahí
                                              el valor actual del método de pago de la venta */
        String obs = (venta.getObservaciones() == null ? "" : venta.getObservaciones());/*guarda en obs el resultado de la condicion, si no es null
                                                                                        y tiene contenido guarda el contenido, si esta vacio entonces
                                                                                        guarda vacio*/

        java.util.concurrent.CompletableFuture/*ejecuta en segundo plano */
                .supplyAsync(() -> {/*se asegura de que el cliente exista; si no existe, lo crea */
                    clientesService.crearClienteSiNoExiste(nombreCliente, tipo);

                    return clientesService.obtenerClienteIdPorNombre(nombreCliente, tipo);/*retorna el ID del cliente que
                                                                                          tiene ese nombre y ese tipo */
                })
                .thenCompose(idCliente -> {/*cuando se obtenga el ID del cliente, se ejecutará este bloque de código*/

                    if (idCliente == null) {/*si el id es null */

                        javafx.application.Platform.runLater(() -> {/*Mostrar la alerta desde el hilo de la interfaz gráfica (JavaFX)*/

                            var dlg = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);/*Creá una ventanita que avise al usuario que pasó algo
                                                                                                                   importante, pero no grave, y guardala en dlg */
                            dlg.setTitle("Cliente no encontrado");/*nombra la ventana como cliente no encontrado */

                            dlg.setHeaderText("No se pudo obtener el ID del cliente");/*En la ventanita de advertencia, escribí en grande que no se
                                                                                                  pudo conseguir el ID del cliente*/

                            dlg.setContentText("Verificá el nombre del cliente o volvé a intentar.");/*Decile al usuario qué tiene que revisar para
                                                                                                                  arreglar el problema */

                            dlg.showAndWait();/*Mostrá la ventanita y no sigas con el programa hasta que
                                              el usuario la cierre */
                        });
                        return java.util.concurrent.CompletableFuture.completedFuture(false);/*retorna el CompletableFuture<Boolean> cuyo valor es false */
                    }

                    venta.setIdCliente(idCliente);/*Guardo en venta el ID del cliente */

                    // 3) Guardar el/los pedidos
                    return java.util.concurrent.CompletableFuture.supplyAsync(/*Retorna un CompletableFuture que después va a
                                                                              decir si se guardó bien (true) o mal (false)*/
                            () -> backend.GuardarPedidos(idCliente, ids, cants, estado, obs)
                    );
                })
                .thenAccept(ok -> javafx.application.Platform.runLater(() -> {/*Cuando termine de guardar el pedido y tenga el resultado
                                                                             decile a JavaFX que ejecute esto en la pantalla*/
            if (ok) {/*si todo salio bien, (true) */
                if (!clientes.contains(nombreCliente)) {/*pero el cliente todavia no existe en la lista */
                    clientes.add(nombreCliente);/*agregalo */
                    javafx.collections.FXCollections.sort(clientes, String.CASE_INSENSITIVE_ORDER);/*y dejá la lista ordenada */
                }
                recargarDelBackend();/*recargá la tabla para que aparezca el nuevo pedido que se acaba de
                                     guardar */

                System.out.println("VENTA GUARDADA");

                if (resumen == null) {
                    System.out.println("RESUMEN NULL");
                } else {
                    System.out.println("ACTUALIZANDO RESUMEN");
                    resumen.actualizarDatos();
                }

            }
        }));
    }
}
