package paucar.resumen;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import paucar.service.VentasBackend;

public class Mensual extends BorderPane {

    private static final Locale LOCALE_AR = Locale.of("es", "AR");
    private static final NumberFormat MONEDA
            = NumberFormat.getCurrencyInstance(LOCALE_AR);

    private final VentasBackend ventasBackend;

    private final TableView<VentaResumenDiarioDTO> tabla
            = new TableView<>();

    private final ObservableList<VentaResumenDiarioDTO> items
            = FXCollections.observableArrayList();

    public Mensual(VentasBackend backend, int anio, int mes) {
        this.ventasBackend = backend;

        tabla.setItems(items);
        tabla.getColumns().addAll(crearColumnas());
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tabla.setEditable(false);

        setCenter(tabla);

        cargarMes(anio, mes);
    }

    private void cargarMes(int anio, int mes) {

        items.clear();
        LocalDate fecha = LocalDate.of(anio, mes, 1);

        while (fecha.getMonthValue() == mes) {

            VentaResumenDiarioDTO dto = new VentaResumenDiarioDTO(fecha);

            var ventasDelDia = ventasBackend.cargarVentasDelDia(fecha);

            for (var v : ventasDelDia) {
                double monto = ((Number) v.get("monto")).doubleValue();
                TipoDePago tipo = (TipoDePago) v.get("estado");

                switch (tipo) {
                    case EFECTIVO ->
                        dto.setEfectivo(dto.getEfectivo() + monto);
                    case DEBITO ->
                        dto.setDebito(dto.getDebito() + monto);
                    case CREDITO ->
                        dto.setCredito(dto.getCredito() + monto);
                    case TRANSFERENCIA ->
                        dto.setTransferencia(dto.getTransferencia() + monto);
                    case MERCADO_PAGO ->
                        dto.setMercadoPago(dto.getMercadoPago() + monto);
                    case DEBE ->
                        dto.setDebe(dto.getDebe() + monto);
                }

                dto.setVentaTotal(dto.getVentaTotal() + monto);
            }

            items.add(dto);   // ✅ SIEMPRE agrega una fila por día
            fecha = fecha.plusDays(1);
        }
    }

    // =====================
    // Columnas
    // =====================
    private List<TableColumn<VentaResumenDiarioDTO, ?>> crearColumnas() {
        return List.of(
                colFecha(),
                colMonto("V. Total", dto -> dto.getVentaTotal()),
                colMonto("Debe", dto -> dto.getDebe()),
                colMonto("Débito", dto -> dto.getDebito()),
                colMonto("Crédito", dto -> dto.getCredito()),
                colMonto("Transferencia", dto -> dto.getTransferencia()),
                colMonto("MERCADO_PAGO", dto -> dto.getMercadoPago()),
                colMonto("Efectivo", dto -> dto.getEfectivo())
        );
    }

    private TableColumn<VentaResumenDiarioDTO, LocalDate> colFecha() {
        TableColumn<VentaResumenDiarioDTO, LocalDate> col
                = new TableColumn<>("Fecha");

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        c.getValue().getFecha()
                )
        );

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setText(null);
                } else {
                    setText(String.format(
                            "%02d-%s",
                            f.getDayOfMonth(),
                            f.getMonth().getDisplayName(
                                    TextStyle.SHORT, LOCALE_AR
                            )
                    ));
                }
            }
        });
        col.setSortable(false);
        return col;
    }

    private TableColumn<VentaResumenDiarioDTO, Double> colMonto(
            String titulo,
            Function<VentaResumenDiarioDTO, Double> getter) {

        TableColumn<VentaResumenDiarioDTO, Double> col
                = new TableColumn<>(titulo);

        col.setCellValueFactory(c
                -> new javafx.beans.property.SimpleObjectProperty<>(
                        getter.apply(c.getValue())
                )
        );

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" : MONEDA.format(v));
            }
        });

        col.setSortable(false);
        return col;
    }
}
