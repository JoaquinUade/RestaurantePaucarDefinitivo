package paucar.resumen;

import java.io.File;
import java.time.LocalDate;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import paucar.resumen.clientes.MensualClientes;
import paucar.resumen.clientes.semanal.SemanalClientes;
import paucar.resumen.empresas.MensualEmpresas;
import paucar.resumen.empresas.semanal.SemanalEmpresas;
import paucar.resumen.general.MensualGeneral;
import paucar.resumen.general.SemanalGeneral;
import paucar.service.ExcelExportService;
import paucar.service.VentasBackend;

public class Resumen extends BorderPane {

    private final VentasBackend backend;
    private final ExcelExportService excelExportService;
    // filtros
    private final ComboBox<String> ResumenTipo = new ComboBox<>();/*
                                                                   * ComboBox es un componente para seleccionar
                                                                   * una opción de una lista desplegable, aqui se
                                                                   * utiliza para que el usuario elija entre
                                                                   * "Mensual" y "Semanal"
     */

    private final DatePicker pickerFecha = new DatePicker();/*
                                                             * DatePicker es un componente que permite al usuario
                                                             * seleccionar una fecha, aqui es para que el usuario
                                                             * elija la fecha base para mostrar el resumen mensual
                                                             * o semanal
     */

    private final BorderPane contenedorResultado = new BorderPane();/*
                                                                     * BorderPane es un layout que divide la ventana
                                                                     * en: top, bottom, left, right y center. Aqui es
                                                                     * para mostrar el resumen mensual o semanal en el
                                                                     * centro de la ventana
     */
    private SemanalEmpresas vistaSemanalEmpresas;
    private SemanalClientes vistaSemanalClientes;
    private MensualEmpresas vistaMensualEmpresas;
    private MensualClientes vistaMensualClientes;
    private MensualGeneral vistaMensualGeneral;
    private SemanalGeneral vistaSemanalGeneral;

    private final ComboBox<String> tipoResumen = new ComboBox<>();

    private final Button btnExcel = new Button("Generar Excel");

    public Resumen(VentasBackend backend, ExcelExportService excelExportService) {
        this.backend = backend;
        this.excelExportService = excelExportService;

        setPadding(new Insets(16));/*
                                    * agrega un padding de 16 pixeles a todo el borde
                                    * de la pestaña Resumen, arriba abajo y los costados
         */

        initFiltros();/* inicializa los filtros */

        setTop(crearBarraFiltros());/* crea la barra de filtros */

        setCenter(contenedorResultado);/*
                                        * establece el centro del BorderPane como el contenedorResultado,
                                        * que es donde se mostrará el resumen mensual o semanal
         */
    }

    private void initFiltros() {

        ResumenTipo.getItems().addAll("Mensual", "Semanal");/*
                                                             * agrega las opciones "Mensual" y
                                                             * "Semanal" a ResumenTipo
         */
        ResumenTipo.getStyleClass().add("combo-agregar");
        ResumenTipo.setValue("Mensual");/* establece el valor por default */

        tipoResumen.getItems().addAll(
                "General",
                "Empresas", "Clientes"
        // "Clientes" después
        );
        tipoResumen.getStyleClass().add("combo-agregar");
        tipoResumen.setValue("General");

        pickerFecha.setValue(LocalDate.now());/* establece la fecha actual por default */
        pickerFecha.getStyleClass().add("date-agregar");
    }

    private Node crearBarraFiltros() {

        Button btnVer = new Button("Ver");/* crea un botón "Ver" */

        btnVer.setOnAction(e -> aplicarFiltros());/*
                                                   * cuando se hace click en el botón "Ver", se llama al método
                                                   * aplicarFiltros() para mostrar el resumen
                                                   * correspondiente según los filtros seleccionados
         */

        btnExcel.getStyleClass().add("btn-agregar");
        btnExcel.setOnAction(e -> exportarExcel());

        pickerFecha.setOnAction(e -> aplicarFiltros());

        HBox barraFiltros = new HBox(10,
                ResumenTipo,
                pickerFecha,
                tipoResumen,
                btnVer,
                btnExcel);/* crea un contenedor horizontal con los filtros */

        barraFiltros.setAlignment(Pos.CENTER_LEFT);/* alinea los elementos a la izquierda */
        barraFiltros.setPadding(new Insets(0, 0, 10, 0));/* agrega un padding de 10 pixeles al fondo */

        return barraFiltros;/* retorna la barra de filtros */
    }

    public void actualizarDatos() {
System.out.println("ACTUALIZAR DATOS");
        if (vistaMensualClientes != null) {
            
            vistaMensualClientes.refrescar();
        }

        if (vistaMensualEmpresas != null) {
            System.out.println("REFRESCANDO MENSUAL EMPRESAS");
            vistaMensualEmpresas.refrescar();
        }

        if (vistaSemanalClientes != null) {
            vistaSemanalClientes.refrescar();
        }

        if (vistaSemanalEmpresas != null) {
            vistaSemanalEmpresas.refrescar();
        }
        if(vistaMensualGeneral != null){
            vistaMensualGeneral.refrescar();
        }
        if(vistaSemanalGeneral != null){
vistaSemanalGeneral.refrescar();

        }
    }

    private void aplicarFiltros() {

        String periodo = ResumenTipo.getValue(); // Mensual o Semanal
        String tipo = tipoResumen.getValue(); // General o Empresas
        LocalDate fecha = pickerFecha.getValue();

        switch (periodo) {

            case "Mensual" -> {
                int anio = fecha.getYear();
                int mes = fecha.getMonthValue();

                switch (tipo) {
                    case "General" -> {
                        if (vistaMensualGeneral == null) {
                            vistaMensualGeneral
                                    = new MensualGeneral(backend, anio, mes);
                        }

                        contenedorResultado.setCenter(vistaMensualGeneral);
                    }
                    case "Empresas" -> {
                        if (vistaMensualEmpresas == null) {
                            vistaMensualEmpresas = new MensualEmpresas(backend, anio, mes);
                        }
                        contenedorResultado.setCenter(vistaMensualEmpresas);
                    }
                    case "Clientes" -> {
                        if (vistaMensualClientes == null) {
                            vistaMensualClientes = new MensualClientes(backend, anio, mes);
                        }
                        contenedorResultado.setCenter(vistaMensualClientes);
                    }
                }
            }

            case "Semanal" -> {
                switch (tipo) {
                    case "General" -> {
                        if (vistaSemanalGeneral == null) {
                            vistaSemanalGeneral
                                    = new SemanalGeneral(backend, fecha);
                        }
                        contenedorResultado.setCenter(vistaSemanalGeneral);
                    }
                    case "Empresas" -> {
                        if (vistaSemanalEmpresas == null) {
                            vistaSemanalEmpresas = new SemanalEmpresas(backend, fecha);
                        } else {
                            vistaSemanalEmpresas.actualizarFecha(fecha);
                        }
                        contenedorResultado.setCenter(vistaSemanalEmpresas);
                    }
                    case "Clientes" -> {
                        if (vistaSemanalClientes == null) {
                            vistaSemanalClientes = new SemanalClientes(backend, fecha);
                        } else {
                            vistaSemanalClientes.actualizarFecha(fecha);
                        }
                        contenedorResultado.setCenter(vistaSemanalClientes);
                    }
                }
            }
        }
    }

    private void exportarExcel() {

        LocalDate fecha = pickerFecha.getValue();

        if (fecha == null) {/* si no hay una fecha seleccionada, avisa y corta */
            Alert aviso = new Alert(Alert.AlertType.WARNING);
            aviso.setTitle("Falta seleccionar fecha");
            aviso.setHeaderText(null);
            aviso.setContentText("Seleccioná una fecha para poder generar el Excel.");
            aviso.showAndWait();
            return;
        }

        int anio = fecha.getYear();
        int mes = fecha.getMonthValue();

        FileChooser selector = new FileChooser();/* diálogo para elegir dónde guardar el archivo */
        selector.setTitle("Guardar resumen en Excel");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Libro de Excel (*.xlsx)", "*.xlsx"));
        selector.setInitialFileName(
                "Resumen_" + anio + "-" + String.format("%02d", mes) + ".xlsx");

        File destino = selector.showSaveDialog(getScene().getWindow());

        if (destino == null) {/* si el usuario canceló el diálogo, no hace nada */
            return;
        }

        /* La generación va en un hilo aparte para no congelar la interfaz,
           porque hay que pedir datos al backend por HTTP. */
        Task<Boolean> tarea = new Task<>() {
            @Override
            protected Boolean call() {
                return excelExportService.exportarExcel(
                        anio, mes, fecha, destino);
            }
        };

        btnExcel.disableProperty().bind(tarea.runningProperty());

        tarea.setOnSucceeded(e -> {
            btnExcel.disableProperty().unbind();

            if (Boolean.TRUE.equals(tarea.getValue())) {
                Alert exito = new Alert(Alert.AlertType.INFORMATION);
                exito.setTitle("Exportación exitosa");
                exito.setHeaderText(null);
                exito.setContentText(
                        "El Excel se generó correctamente en:\n"
                                + destino.getAbsolutePath());
                exito.showAndWait();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText(null);
                error.setContentText(
                        "No se pudo generar el Excel. Revisá que el backend esté corriendo.");
                error.showAndWait();
            }
        });

        tarea.setOnFailed(e -> {
            btnExcel.disableProperty().unbind();

            Throwable ex = tarea.getException();
            System.err.println("Error exportando Excel: " + ex);

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText(null);
            error.setContentText(
                    "Ocurrió un error al generar el Excel:\n" + ex.getMessage());
            error.showAndWait();
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }
}
