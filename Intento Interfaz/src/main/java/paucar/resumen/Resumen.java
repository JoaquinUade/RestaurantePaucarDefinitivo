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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import paucar.config.Responsive;
import paucar.resumen.clientes.MensualClientes;
import paucar.resumen.clientes.semanal.SemanalClientes;
import paucar.resumen.empresas.MensualEmpresas;
import paucar.resumen.empresas.semanal.SemanalEmpresas;
import paucar.resumen.general.MensualGeneral;
import paucar.resumen.general.SemanalGeneral;
import paucar.service.ClientesService;
import paucar.service.ExcelExportService;
import paucar.service.GastosFijosService;
import paucar.service.GastosIndividualesService;
import paucar.service.GastosVariablesService;
import paucar.service.VentasBackend;

public final class Resumen extends BorderPane {

    private final VentasBackend backend;
    private final ClientesService clientesService;
    private final ExcelExportService excelExportService;
    // filtros
    private final ComboBox<String> ResumenTipo = new ComboBox<>();/*
                                                                   * ComboBox es un componente para seleccionar
                                                                   * una opción de una lista desplegable, aqui se
                                                                   * utiliza para que el usuario elija entre
                                                                   * "Mensual" y "Semanal"
     */

    private final ComboBox<String> comboMes = new ComboBox<>();
    private final ComboBox<Integer> comboAnio = new ComboBox<>();
    private final ComboBox<String> comboSemana = new ComboBox<>();

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

    private final GastosVariablesService gastosVariablesService;
    private final GastosFijosService gastosFijosService;
    private final GastosIndividualesService gastosIndividualesService;

    private final ComboBox<String> tipoResumen = new ComboBox<>();

    private final Button btnExcel = new Button("Generar Excel");

    public Resumen(
            VentasBackend backend,
            ClientesService clientesService,
            ExcelExportService excelExportService, GastosVariablesService gastosVariablesService,
            GastosFijosService gastosFijosService, GastosIndividualesService gastosIndividualesService) {

        this.backend = backend;
        this.clientesService = clientesService;
        this.excelExportService = excelExportService;
        this.gastosVariablesService = gastosVariablesService;
        this.gastosFijosService = gastosFijosService;
        this.gastosIndividualesService = gastosIndividualesService;

        initFiltros();
        setTop(crearBarraFiltros());
        setCenter(contenedorResultado);
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

        comboMes.getItems().addAll(
                "Enero",
                "Febrero",
                "Marzo",
                "Abril",
                "Mayo",
                "Junio",
                "Julio",
                "Agosto",
                "Septiembre",
                "Octubre",
                "Noviembre",
                "Diciembre"
        );

        comboMes.setValue(comboMes.getItems().get(LocalDate.now().getMonthValue() - 1));

        for (int anio = 2020; anio <= 2035; anio++) {
            comboAnio.getItems().add(anio);
        }

        comboAnio.setValue(LocalDate.now().getYear());

        comboMes.getStyleClass().add("combo-agregar");
        comboAnio.getStyleClass().add("combo-agregar");

        comboSemana.getItems().addAll(
                "Semana 1",
                "Semana 2",
                "Semana 3",
                "Semana 4",
                "Semana 5"
        );

        comboSemana.setValue("Semana 1");

        comboSemana.getStyleClass().add("combo-agregar");

        /* oculto inicialmente porque arrancamos en mensual */
        comboSemana.setVisible(false);
        comboSemana.setManaged(false);
    }

    private Node crearBarraFiltros() {

        Button btnVer = new Button("Ver");/* crea un botón "Ver" */
        btnVer.setPadding(
                Responsive.insets(8, 16, 8, 16)
        );
        btnVer.setOnAction(e -> aplicarFiltros());/*
                                                   * cuando se hace click en el botón "Ver", se llama al método
                                                   * aplicarFiltros() para mostrar el resumen
                                                   * correspondiente según los filtros seleccionados
         */
        btnExcel.setPadding(
                Responsive.insets(8, 16, 8, 16)
        );

        btnExcel.getStyleClass().add("btn-agregar");
        btnExcel.setOnAction(e -> exportarExcel());

        comboMes.setOnAction(e -> aplicarFiltros());
        comboAnio.setOnAction(e -> aplicarFiltros());
        ResumenTipo.setOnAction(e -> {
            boolean esSemanal = "Semanal".equals(ResumenTipo.getValue());
            comboSemana.setVisible(esSemanal);
            comboSemana.setManaged(esSemanal);
            aplicarFiltros();
        });
        tipoResumen.setOnAction(e -> aplicarFiltros());
        comboSemana.setOnAction(e -> aplicarFiltros());

        HBox barraFiltros = new HBox(Responsive.pe(10),
                ResumenTipo,
                comboSemana,
                comboMes,
                comboAnio,
                tipoResumen,
                btnVer,
                btnExcel);/* crea un contenedor horizontal con los filtros */
        btnVer.setOnAction(e -> aplicarFiltros());

        HBox.setMargin(
                btnVer,
                new Insets(0, 15, 0, 5)
        );

        HBox.setMargin(
                btnExcel,
                new Insets(0, 5, 0, 15)
        );

        barraFiltros.setAlignment(Pos.CENTER_LEFT);/* alinea los elementos a la izquierda */
        barraFiltros.setPadding(Responsive.insets(10, 10, 10, 10));/* agrega un padding de 10 pixeles al fondo */

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
        if (vistaMensualGeneral != null) {
            vistaMensualGeneral.refrescar();
        }
        if (vistaSemanalGeneral != null) {
            vistaSemanalGeneral.refrescar();

        }
    }

    private void aplicarFiltros() {

        String periodo = ResumenTipo.getValue();
        String tipo = tipoResumen.getValue();

        if (comboAnio.getValue() == null
                || comboMes.getValue() == null
                || periodo == null
                || tipo == null) {

            contenedorResultado.setCenter(null);
            return;
        }

        LocalDate fecha;

if ("Semanal".equals(periodo)) {

    fecha = calcularFechaSemanal();

} else {

    fecha = LocalDate.of(
            comboAnio.getValue(),
            comboMes.getSelectionModel().getSelectedIndex() + 1,
            1
    );
}

        switch (periodo) {

            case "Mensual" -> {
                int anio = fecha.getYear();
                int mes = fecha.getMonthValue();

                switch (tipo) {
                    case "General" -> {
                        if (vistaMensualGeneral == null) {
                            vistaMensualGeneral = new MensualGeneral(
                                    backend,
                                    gastosVariablesService,
                                    gastosFijosService,
                                    gastosIndividualesService,
                                    anio,
                                    mes);
                        } else {
                            vistaMensualGeneral.actualizarFecha(fecha);
                        }

                        contenedorResultado.setCenter(vistaMensualGeneral);
                    }

                    case "Empresas" -> {
                        if (vistaMensualEmpresas == null) {
                            vistaMensualEmpresas = new MensualEmpresas(
                                    backend,
                                    anio,
                                    mes);
                        } else {
                            vistaMensualEmpresas.actualizarFecha(fecha);
                        }

                        contenedorResultado.setCenter(vistaMensualEmpresas);
                    }

                    case "Clientes" -> {
                        if (vistaMensualClientes == null) {
                            vistaMensualClientes = new MensualClientes(
                                    backend,
                                    anio,
                                    mes);
                        } else {
                            vistaMensualClientes.actualizarFecha(fecha);
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
                                    = new SemanalGeneral(
                                            backend,
                                            clientesService,
                                            fecha);
                        } else {
                            vistaSemanalGeneral.actualizarFecha(fecha);
                        }

                        contenedorResultado.setCenter(vistaSemanalGeneral);
                    }

                    case "Empresas" -> {
                        if (vistaSemanalEmpresas == null) {
                            vistaSemanalEmpresas
                                    = new SemanalEmpresas(
                                            backend,
                                            fecha);
                        } else {
                            vistaSemanalEmpresas.actualizarFecha(fecha);
                        }

                        contenedorResultado.setCenter(vistaSemanalEmpresas);
                    }

                    case "Clientes" -> {
                        if (vistaSemanalClientes == null) {
                            vistaSemanalClientes
                                    = new SemanalClientes(
                                            backend,
                                            fecha);
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

        if (comboAnio.getValue() == null
                || comboMes.getValue() == null) {

            Alert aviso = new Alert(Alert.AlertType.WARNING);
            aviso.setTitle("Falta seleccionar fecha");
            aviso.setHeaderText(null);
            aviso.setContentText("Seleccioná un mes y un año.");
            aviso.showAndWait();
            return;
        }

        LocalDate fecha = LocalDate.of(
                comboAnio.getValue(),
                comboMes.getSelectionModel().getSelectedIndex() + 1,
                1
        );

        int anio = fecha.getYear();
        int mes = fecha.getMonthValue();

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar resumen en Excel");

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Libro de Excel (*.xlsx)",
                        "*.xlsx"));

        selector.setInitialFileName(
                "Resumen_" + anio + "-"
                + String.format("%02d", mes)
                + ".xlsx");

        File destino = selector.showSaveDialog(getScene().getWindow());

        if (destino == null) {
            return;
        }

        Task<Boolean> tarea = new Task<>() {
            @Override
            protected Boolean call() {
                return excelExportService.exportarExcel(
                        anio,
                        mes,
                        fecha,
                        destino);
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

            System.err.println(
                    "Error exportando Excel: "
                    + ex);

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText(null);
            error.setContentText(
                    "Ocurrió un error al generar el Excel:\n"
                    + ex.getMessage());

            error.showAndWait();
        });

        Thread hilo = new Thread(tarea);
        hilo.setDaemon(true);
        hilo.start();
    }
    private LocalDate calcularFechaSemanal() {

    int anio = comboAnio.getValue();

    int mes =
            comboMes.getSelectionModel().getSelectedIndex() + 1;

    int semana =
            comboSemana.getSelectionModel().getSelectedIndex();

    LocalDate primerDiaMes =
            LocalDate.of(anio, mes, 1);

    return primerDiaMes.plusWeeks(semana);
}

}
