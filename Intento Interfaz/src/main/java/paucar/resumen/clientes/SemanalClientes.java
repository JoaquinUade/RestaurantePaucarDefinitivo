
package paucar.resumen.clientes;

import java.time.LocalDate;

import javafx.scene.layout.BorderPane;
import paucar.service.VentasBackend;

public class SemanalClientes extends BorderPane {

    private final VentasBackend backend;
    private final LocalDate fecha;

    public SemanalClientes(VentasBackend backend, LocalDate fecha) {
        this.backend = backend;
        this.fecha = fecha;
    }
}
