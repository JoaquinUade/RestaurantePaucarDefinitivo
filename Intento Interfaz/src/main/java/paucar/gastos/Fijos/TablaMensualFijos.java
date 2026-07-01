package paucar.gastos.Fijos;

import java.util.List;
import java.util.function.Consumer;

import com.uade.tpo.demo.entity.GastosFijos;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;

public class TablaMensualFijos extends VBox {

    public TablaMensualFijos(List<GastosFijos> gastos,
                         Consumer<GastosFijos> onSelect,
                         boolean esPersonal){

        TableView<GastosFijos> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // ✅ DETALLE (antes nombre)
        TableColumn<GastosFijos, String> colDetalle;

if (esPersonal) {
    colDetalle = new TableColumn<>("Empleado");
} else {
    colDetalle = new TableColumn<>("Detalle");
}

colDetalle.setCellValueFactory(c ->
        new SimpleStringProperty(c.getValue().getDetalle()));

        // ✅ MONTO
        TableColumn<GastosFijos, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(c ->
                new SimpleStringProperty("$" + c.getValue().getMonto()));

        // ✅ ESTADO (Boolean → texto)
        TableColumn<GastosFijos, String> colEstado = new TableColumn<>("Estado");

        colEstado.setCellValueFactory(c -> {
            Boolean estado = c.getValue().getEstado();
            String texto = (estado != null && estado) ? "Pagado" : "Pendiente";
            return new SimpleStringProperty(texto);
        });

        // ✅ ESTADO EDITABLE
        colEstado.setCellFactory(
                ComboBoxTableCell.forTableColumn("Pendiente", "Pagado")
        );

        colEstado.setOnEditCommit(e -> {
            GastosFijos g = e.getRowValue();
            g.setEstado(e.getNewValue().equals("Pagado"));
        });

       TableColumn<GastosFijos, String> colObs = null;

if (!esPersonal) {
    colObs = new TableColumn<>("Observación");

    colObs.setCellValueFactory(c ->
            new SimpleStringProperty(
                    c.getValue().getObservacion() == null ? "" : c.getValue().getObservacion()
            )
    );

    colObs.setCellFactory(TextFieldTableCell.forTableColumn());

    colObs.setOnEditCommit(e -> {
        GastosFijos g = e.getRowValue();
        g.setObservacion(e.getNewValue());
    });
}

        // ✅ TABLA EDITABLE
        tabla.setEditable(true);

        // ✅ AGREGAR COLUMNAS
        tabla.getColumns().add(colDetalle);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colEstado);
        
if (colObs != null) {
    tabla.getColumns().add(colObs);
}

        // ✅ CARGAR DATOS
        tabla.setItems(FXCollections.observableArrayList(gastos));

        // ✅ SELECCIÓN
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                onSelect.accept(newSel);
            }
        });

        // ✅ AGREGAR TABLA AL PANEL
        getChildren().add(tabla);
    }
}