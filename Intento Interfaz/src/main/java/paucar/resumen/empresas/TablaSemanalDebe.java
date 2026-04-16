package paucar.resumen.empresas;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import paucar.service.VentasBackend;

public class TablaSemanalDebe {

    private final VentasBackend backend;
    private final TableView<Map<String, Object>> tabla;

    private static final Locale LOCALE_AR = Locale.of("es", "AR");
    private static final DateTimeFormatter FECHA_FORMATO
            = DateTimeFormatter.ofPattern("d MMMM yyyy", LOCALE_AR);
    private static final NumberFormat MONEDA
            = NumberFormat.getCurrencyInstance(LOCALE_AR);

    public TablaSemanalDebe(VentasBackend backend) {
        this.backend = backend;
        this.tabla = new TableView<>();
        definirColumnas();
    }

    public TableView<Map<String, Object>> getTabla() {
        return tabla;
    }

    // =========================
    // COLUMNAS
    // =========================
    private void definirColumnas() {

        TableColumn<Map<String, Object>, String> colFecha
                = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> {
            LocalDate fecha = (LocalDate) c.getValue().get("fecha");
            return new SimpleObjectProperty<>(
                    fecha == null ? "" : fecha.format(FECHA_FORMATO)
            );
        });

        TableColumn<Map<String, Object>, String> colDescripcion
                = crearColumnaTexto("Descripción", "descripcion", 13);

        TableColumn<Map<String, Object>, String> colMonto
                = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(c -> {
            Number m = (Number) c.getValue().get("monto");
            return new SimpleObjectProperty<>(
                    MONEDA.format(m == null ? 0 : m.doubleValue())
            );
        });

        TableColumn<Map<String, Object>, String> colTipo
                = new TableColumn<>("Tipo de pago");
        colTipo.setCellValueFactory(c
                -> new SimpleObjectProperty<>("DEBE")
        );

        TableColumn<Map<String, Object>, String> colObs
                = crearColumnaTexto("Observaciones", "observaciones", 16);

        colFecha.setSortable(false);
        colDescripcion.setSortable(false);
        colMonto.setSortable(false);
        colTipo.setSortable(false);
        colObs.setSortable(false);

        tabla.getColumns().add(colFecha);
        tabla.getColumns().add(colDescripcion);
        tabla.getColumns().add(colMonto);
        tabla.getColumns().add(colTipo);
        tabla.getColumns().add(colObs);
        
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    // =========================
    // COLUMNA DE TEXTO REUTILIZABLE
    // =========================
    private TableColumn<Map<String, Object>, String> crearColumnaTexto(
            String titulo, String key, int padding) {

        TableColumn<Map<String, Object>, String> col
                = new TableColumn<>(titulo);

        col.setCellValueFactory(c
                -> new SimpleObjectProperty<>((String) c.getValue().get(key))
        );

        col.setCellFactory(tc -> new TableCell<>() {

            private final Text text = new Text();

            {
                text.wrappingWidthProperty()
                        .bind(tc.widthProperty().subtract(padding));
                setGraphic(text);
                setPrefHeight(Region.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    text.setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    setTooltip(new Tooltip(item));
                }
            }
        });

        return col;
    }

    // =========================
    // CARGA DE DEUDAS
    // =========================
    public void cargarDeudasEmpresa(String empresa, LocalDate desde) {

        tabla.getItems().clear();
        LocalDate hoy = LocalDate.now();

        while (!desde.isAfter(hoy)) {

            var ventas = backend.cargarVentasDelDia(desde);

            for (Map<String, Object> v : ventas) {

                if (v.get("tipoCliente") == TipoCliente.EMPRESA
                        && empresa.equals(v.get("nombre"))
                        && v.get("estado") == TipoDePago.DEBE) {

                    v.put("fecha", desde);
                    tabla.getItems().add(v);
                }
            }
            desde = desde.plusDays(1);
        }
    }
}
