package paucar.resumen.empresas;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import paucar.service.VentasBackend;

public class SemanalEmpresas extends BorderPane {

    private final VentasBackend backend;

    private LocalDate inicioSemana;
    private LocalDate finSemana;
    private String empresaSeleccionada;

    // UI
    private ComboBox<String> comboEmpresa;
    private FilteredList<String> empresasFiltradas;

    private TablaSemanal tablaSemanal;
    private TablaSemanalDebe tablaDebe;

    private TableView<Map<String, Object>> tablaVentas;
    private TableView<Map<String, Object>> tablaVentasDebe;

    private Label lblTotal;

    public SemanalEmpresas(VentasBackend backend, LocalDate fecha) {
        this.backend = backend;

        this.inicioSemana = fecha.with(DayOfWeek.MONDAY);
        this.finSemana = fecha.with(DayOfWeek.SUNDAY);

        initUI();
    }

    private void initUI() {
        setPadding(new Insets(10));

        CargarComboEmpresas();
        setTop(crearBarraSuperior());
        setCenter(crearVistaResumenSemanal());
    }

    private VBox crearVistaResumenSemanal() {

        VBox contenedor = new VBox(5);/*crea un vertical box  y le indica que deje 15 píxeles de
                                               espacio entre cada elemento hijo que se agregue dentro */

        tablaSemanal = new TablaSemanal(backend);/* Crea un objeto SemanalTablaEmpresas, le pasa
                                                         el backend y lo guarda en la variable tablaSemanal*/

        tablaDebe = new TablaSemanalDebe(backend);/*Crea un objeto SemanalDebeEmpresas, le pasa el
                                                     backend y lo guarda en la variable tablaDebe*/

        tablaVentas = tablaSemanal.getTabla();/*Obtiene la TableView de tablaSemanal y la guarda en la
                                              variable tablaVentas para mostrarla y usarla en la interfaz*/

        tablaVentasDebe = tablaDebe.getTabla();/*Obtiene la TableView de tablaDebe y la guarda en la
                                               variable tablaVentasDebe para mostrarla y usarla en la interfaz*/

        lblTotal = new Label("Total: $ 0");/*Crea una etiqueta para mostrar el total de ventas,
                                                 inicialmente con el texto "Total: $ 0"*/

        lblTotal.getStyleClass().add("lbl-total");/*Agrega la clase de estilo "lbl-total" a la etiqueta*/

        HBox contenedorTotal = new HBox(lblTotal);/*Crea un contenedor horizontal para el total*/

        contenedorTotal.setAlignment(Pos.CENTER_RIGHT);/*Alinea los elementos del contenedor a la derecha*/

        Label lblDebe = new Label("Deudas (cualquier fecha)");/*Crea una etiqueta para mostrar el
                                                                    título de la sección de deudas */
        lblDebe.getStyleClass().add("lbl-debe");/*Agrega la clase de estilo "lbl-debe" a la etiqueta*/

        contenedor.getChildren().addAll(tablaVentas, contenedorTotal, lblDebe,
                tablaVentasDebe);/*Agrega varios componentes visuales al VBox contenedor, en ese
                                                         orden, para que se muestren uno debajo del otro en la interfaz */

        VBox.setVgrow(tablaVentas, Priority.ALWAYS);/* Indica que la tabla tablaVentas debe crecer y
                                                    ocupar todo el espacio vertical disponible dentro del
                                                    VBox*/

        VBox.setVgrow(tablaVentasDebe, Priority.ALWAYS);/* Indica que la tabla tablaVentasDebe debe crecer y
                                                    ocupar todo el espacio vertical disponible dentro del
                                                    VBox*/

        return contenedor;/* Devuelve el VBox que contiene todos los componentes visuales */
    }

    private HBox crearBarraSuperior() {
        comboEmpresa.setPromptText("Seleccionar empresa");/*asigna el texto de ayuda que se muestra
                                                                en el ComboBox cuando no hay ninguna
                                                                empresa seleccionada */

        Label rangoSemana = new Label(
                "Semana: " + inicioSemana + " a " + finSemana);/* Crea una etiqueta que muestra el rango
                                                               de fechas que se van a mostrar, osea la
                                                               fecha que fue elegida, toma como rango el
                                                               lunes de esa semana al domingo*/

        HBox barra = new HBox(15, comboEmpresa, rangoSemana);/*Crea una barra horizontal que
                                                                     contiene el ComboBox para seleccionar
                                                                     la empresa y el rango de fechas que
                                                                     mostrara la tabla de ventas*/

        barra.setAlignment(Pos.CENTER_LEFT);/*Alinea los elementos de la barra a la izquierda*/

        barra.setPadding(new Insets(0, 0, 10, 0));/*Agrega un espacio de 10
                                                                          píxeles para separar el HBox de
                                                                          la tabla de ventas*/
        return barra;/*Devuelve el HBox que se mostrará en la parte superior de la interfaz*/
    }

    private void CargarComboEmpresas() {

        var empresas = backend.obtenerClientesPorTipo(TipoCliente.EMPRESA);/*Obtiene del backend la lista
                                                                           de clientes cuyo tipo es EMPRESA */

        empresasFiltradas = new FilteredList<>(
                javafx.collections.FXCollections
                        .observableArrayList(empresas), s -> true);/*Crea una lista filtrable de empresas a partir
                                                          de la lista original, inicialmente sin aplicar
                                                          filtros */

        comboEmpresa = crearComboEmpresas(empresasFiltradas);/*Crea el ComboBox para seleccionar la
                                                              empresa */

        comboEmpresa.setOnAction(e -> {/*cuando elegimos la empresa */
            empresaSeleccionada = comboEmpresa.getValue();/*guardamos esa empresa en empresa seleccionada */

            cargarVentasSemanalesEmpresas();/*y despues cargamos las ventas semanales de esa empresa para
                                             mostrar en la tabla */
        });
    }

    private void cargarVentasSemanalesEmpresas() {

        String empresa = comboEmpresa.getValue();/*Obtiene el nombre de la empresa seleccionada
                                                 actualmente en el ComboBox*/

        if (empresa == null || empresa.isBlank()) {/*Verifica si no se ha seleccionado ninguna empresa o
                                                si es una cadena vacía o solo espacios en blanco. Si es
                                                así, se muestra un mensaje de error*/

            return;/*Si no se ha seleccionado una empresa válida, se detiene la ejecución de este
                   método para evitar cargar datos incorrectos o vacíos en las tablas de ventas y deudas.*/
        }

        double total = tablaSemanal.cargarSemanaEmpresa(empresa, inicioSemana, finSemana);/* llama a un método que carga
                                                                                          los datos de la semana y devuelve
                                                                                          el total, que se guarda en total*/
        tablaDebe.cargarDeudasEmpresa(empresa,
                inicioSemana.minusMonths(12));
        lblTotal.setText("Total: " + formatoDineroAR(total));
    }

    private ComboBox<String> crearComboEmpresas(FilteredList<String> empresasFiltradas) {

        ComboBox<String> cbEmpresa = new ComboBox<>(empresasFiltradas);/*Crea el ComboBox para seleccionar
                                                                       la empresa */
        cbEmpresa.setEditable(true);/*Permite que el usuario edite el texto en el ComboBox */

        AtomicBoolean ActualizarTrasAccionUser = new AtomicBoolean(false);/* sirve para que cuando el programa escribe el texto del
                                                                                  ComboBox (por ejemplo al seleccionar una empresa), no se
                                                                                  ejecute el filtrado como si el usuario hubiera escrito */

        cbEmpresa.getEditor().textProperty().addListener((obs, a, TextoNuevo) -> {/*Cada vez que cambia el texto
                                                                         que hay escrito en ComboBox (porque
                                                                         escribe el usuario o porque el
                                                                         programa lo cambia), se ejecuta
                                                                         este código */

            if (ActualizarTrasAccionUser.get()) {/*Si cambio el texto fue y fue por el programa (como al
                                          seleccionar una empresa), no se ejecuta el filtrado para evitar
                                          interferencias o resultados incorrectos*/

                return;/*si es asi se corta el listener inmediatamente*/
            }

            String txt = (TextoNuevo == null ? "" : TextoNuevo.trim().toLowerCase());/*Obtiene el texto nuevo y lo convierte a minúsculas
                                                                                     y le quita los espacios en blanco de delante y atras*/

            empresasFiltradas.setPredicate(empresa -> empresa != null
                    && (txt.isEmpty() || empresa.toLowerCase().contains(txt)));/*Define qué empresas se muestran
                                                                        en el ComboBox según lo que el
                                                                        usuario escribió*/

            if (!cbEmpresa.isShowing() && !txt.isEmpty()) {/*si el combobox no se esta viendo y no esta
                                                           vacío*/
                cbEmpresa.show();/*muestra el ComboBox desplegado*/
            }
        });

        // 2) Selección segura (por ítem, no por índice)
        cbEmpresa.setCellFactory(list -> {/*en el siguiente bloque de codigo personalizamos como se ve y
                                          comporta el cbEmpresa */

            var celda = new javafx.scene.control.ListCell<String>() {/*Crea una celda personalizada que
                                                                    representa una empresa dentro de la
                                                                    lista desplegable del ComboBox */
                @Override
                protected void updateItem(String nameEmpresa, boolean empty) {
                    super.updateItem(nameEmpresa, empty);/*llama al metodo original usando super, que trae la
                                                  clase padre y le avisa a JavaFX que esta celda tiene
                                                  nuevo contenido o está vacía, y limpie lo anterior*/

                    setText(empty || nameEmpresa == null ? "" : nameEmpresa);/*si esta vacia o es null muestra vacio y
                                                               sino muestra el nombre de la empresa */
                }
            };

            celda.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {/*cuando el usuario hace clic con el
                                                                  mouse sobre esta empresa, ejecutá este
                                                                  código antes de que JavaFX haga nada
                                                                  por defecto */

                if (!celda.isEmpty()) {/*si el usuario hizo clic sobre una empresa y no sobre un espacio
                                       vacío del ComboBox*/

                    String item = celda.getItem();/*obtiene el nombre de la empresa dentro de esa celda
                                                  y la guarda en item */

                    ActualizarTrasAccionUser.set(true);/*Marca que el texto del ComboBox va a ser cambiado por el programa y no por el usuario */
                    try {
                        cbEmpresa.getSelectionModel().select(item);/*le informa al ComboBox cuál fue la
                                                                   empresa elegida por el usuario, para
                                                                   que quede registrado*/

                        cbEmpresa.setValue(item);/*guarda en el ComboBox el nombre de la empresa
                                                  seleccionada como un String*/

                        cbEmpresa.getEditor().setText(item);/*pone visualmente en el combobox el nombre
                                                            de la empresa seleccionada */

                        cbEmpresa.getEditor().positionCaret(item.length());/*Coloca el cursor al final
                                                                           del texto */
                    } finally {/*pase lo que pase en try, se vuelve false ActualizarTrasAccionUser*/

                        ActualizarTrasAccionUser.set(false);/*vuelve a indicar que el programa
                                                                     ya terminó de actualizar el texto
                                                                     automáticamente y que los próximos
                                                                     cambios vienen del usuario*/
                    }

                    cbEmpresa.hide();/*oculta el ComboBox */
                    mouseEvent.consume();/*evita que JavaFX haga algo más con ese clic*/
                }
            });
            return celda;/*devuelve la celda personalizada para que el ComboBox la use al mostrar la
                            lista de empresas*/
        });

        cbEmpresa.setButtonCell(new javafx.scene.control.ListCell<>() {/*personaliza la celda del botón del ComboBox */
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);/*llama al metodo original usando super, que trae la
                                                  clase padre y le avisa a JavaFX que esta celda tiene
                                                  nuevo contenido o está vacía, y limpie lo anterior*/
                setText(empty || item == null ? "" : item);/*pone el texto en la celda, o un string vacío
                                                           si está vacía o el item es nulo*/
            }
        });
        return cbEmpresa;/*devuelve el ComboBox con la celda personalizada */
    }

    private String formatoDineroAR(double monto) {

        Locale localeAR = Locale.of("es", "AR");/*Define el formato regional de Argentina
                                                                  para que números y moneda se muestren
                                                                  según las reglas locales*/

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(localeAR);/*Aplica las reglas del locale
                                                                          argentino (separadores decimal y
                                                                          de miles) al formato de números*/

        DecimalFormat df = new DecimalFormat("$ #,##0.00", symbols);/*Define un formato de número
                                                                            que incluye el símbolo de peso,
                                                                            separadores de miles y dos
                                                                            decimales, utilizando los
                                                                            símbolos definidos para
                                                                            Argentina*/
        df.setRoundingMode(RoundingMode.UP);/*Establece el modo de redondeo a redondeo hacia arriba*/

        return df.format(monto);/*Devuelve el monto formateado según el locale argentino*/
    }

    public void actualizarFecha(LocalDate fecha) {
        this.inicioSemana = fecha.with(DayOfWeek.MONDAY);/*Actualiza la fecha de inicio de la semana al
                                                         lunes de la semana que corresponde a la fecha
                                                         dada*/

        this.finSemana = fecha.with(DayOfWeek.SUNDAY);/*Actualiza la fecha de fin de la semana al
                                                      domingo de la semana que corresponde a la fecha
                                                      dada*/

        if (empresaSeleccionada != null && !empresaSeleccionada.isBlank()) {/*Si ya hay una empresa seleccionada, recarga las
                                                                            ventas semanales para esa empresa con las nuevas fechas*/
            cargarVentasSemanalesEmpresas();
        }
    }
}
