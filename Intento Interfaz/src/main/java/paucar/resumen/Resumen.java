package paucar.resumen;

import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import paucar.resumen.empresas.MensualEmpresas;
import paucar.resumen.empresas.SemanalEmpresas;
import paucar.resumen.general.MensualGeneral;
import paucar.resumen.general.SemanalGeneral;
import paucar.service.VentasBackend;

public class Resumen extends BorderPane {

    private final VentasBackend backend;
    // filtros
    private final ComboBox<String> ResumenTipo = new ComboBox<>();/*ComboBox es un componente para seleccionar
                                                                   una opción de una lista desplegable, aqui se
                                                                   utiliza para que el usuario elija entre
                                                                   "Mensual" y "Semanal"*/

    private final DatePicker pickerFecha = new DatePicker();/*DatePicker es un componente que permite al usuario
                                                            seleccionar una fecha, aqui es para que el usuario
                                                            elija la fecha base para mostrar el resumen mensual
                                                            o semanal*/

    private final BorderPane contenedorResultado = new BorderPane();/*BorderPane es un layout que divide la ventana
                                                                    en: top, bottom, left, right y center. Aqui es
                                                                    para mostrar el resumen mensual o semanal en el
                                                                    centro de la ventana*/
    private final ComboBox<String> tipoResumen = new ComboBox<>();

    public Resumen(VentasBackend backend) {
        this.backend = backend;

        setPadding(new Insets(16));/*agrega un padding de 16 pixeles a todo el borde
                                                       de la pestaña Resumen, arriba abajo y los costados*/

        initFiltros();/* inicializa los filtros */

        setTop(crearBarraFiltros());/* crea la barra de filtros */

        setCenter(contenedorResultado);/*establece el centro del BorderPane como el contenedorResultado,
                                       que es donde se mostrará el resumen mensual o semanal*/
    }

    private void initFiltros() {

        ResumenTipo.getItems().addAll("Mensual", "Semanal");/*agrega las opciones "Mensual" y 
                                                                         "Semanal" a ResumenTipo*/
        ResumenTipo.setValue("Mensual");/* establece el valor por default*/

        tipoResumen.getItems().addAll(
                "General",
                "Empresas"
        // "Clientes" después
        );
        tipoResumen.setValue("General");

        pickerFecha.setValue(LocalDate.now());/* establece la fecha actual por default */
    }

    private Node crearBarraFiltros() {

        Button btnVer = new Button("Ver");/* crea un botón "Ver"*/

        btnVer.setOnAction(e -> aplicarFiltros());/*cuando se hace click en el botón "Ver", se llama al método aplicarFiltros() para mostrar el resumen
                                                  correspondiente según los filtros seleccionados*/

        HBox barraFiltros = new HBox(10,
                ResumenTipo,
                pickerFecha,
                tipoResumen,
                btnVer
        );/*crea un contenedor horizontal con los filtros*/

        barraFiltros.setAlignment(Pos.CENTER_LEFT);/* alinea los elementos a la izquierda */
        barraFiltros.setPadding(new Insets(0, 0, 10, 0));/* agrega un padding de 10 pixeles al fondo*/

        return barraFiltros;/*retorna la barra de filtros*/
    }

private void aplicarFiltros() {

    String periodo = ResumenTipo.getValue();  // Mensual o Semanal
    String tipo = tipoResumen.getValue();     // General o Empresas
    LocalDate fecha = pickerFecha.getValue();

    switch (periodo) {

        case "Mensual" -> {
            int anio = fecha.getYear();
            int mes = fecha.getMonthValue();

            switch (tipo) {
                case "General" -> contenedorResultado.setCenter(
                    new MensualGeneral(backend, anio, mes)
                );
                case "Empresas" -> contenedorResultado.setCenter(
                    new MensualEmpresas(backend, anio, mes)
                );
            }
        }

        case "Semanal" -> {
            switch (tipo) {
                case "General" -> contenedorResultado.setCenter(
                    new SemanalGeneral(backend, fecha)
                );
                case "Empresas" -> contenedorResultado.setCenter(
                    new SemanalEmpresas(backend, fecha)
                );
            }
        }
    }
}
}
