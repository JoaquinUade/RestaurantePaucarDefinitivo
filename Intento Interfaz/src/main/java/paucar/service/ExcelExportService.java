package paucar.service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.uade.tpo.demo.entity.GastosFijos;
import com.uade.tpo.demo.entity.GastosIndividuales;
import com.uade.tpo.demo.entity.GastosVariables;
import com.uade.tpo.demo.entity.HistorialStock;
import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.Venta;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;

/**
 * Genera un archivo Excel (.xlsx) con toda la información mensual de la
 * aplicación, dividida en hojas (páginas):
 *
 * 1. Resumen Semanal 2. Resumen Mensual 3. Resumen por Empresa 4. Detalle por
 * Empresa 5. Ventas del Mes 6. Pagos del Mes 7. Stock 8. Movimientos de Stock
 * 9. Gastos Fijos 10. Gastos Variables 11. Gastos Individuales
 */
public class ExcelExportService {

    private static final String FORMATO_MONEDA = "$#,##0.00";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ES_AR = new Locale("es", "ES");

    private final VentasBackend ventasBackend;
    private final PagosService pagosService;
    private final StockService stockService;
    private final GastosFijosService gastosFijosService;
    private final GastosVariablesService gastosVariablesService;
    private final GastosIndividualesService gastosIndividualesService;

    public ExcelExportService(
            VentasBackend ventasBackend,
            PagosService pagosService,
            StockService stockService,
            GastosFijosService gastosFijosService,
            GastosVariablesService gastosVariablesService,
            GastosIndividualesService gastosIndividualesService) {
        this.ventasBackend = ventasBackend;
        this.pagosService = pagosService;
        this.stockService = stockService;
        this.gastosFijosService = gastosFijosService;
        this.gastosVariablesService = gastosVariablesService;
        this.gastosIndividualesService = gastosIndividualesService;
    }

    /**
     * Genera el Excel y lo guarda en {@code destino}.
     *
     * @return true si se generó y guardó correctamente.
     */
    public boolean exportarExcel(int anio, int mes, LocalDate fechaBase, File destino) {
        Workbook libro = null;
        try {
            libro = new XSSFWorkbook();

            List<Venta> ventasDelMes = ventasBackend.cargarVentasDelMes(mes, anio);
            List<String> empresas = ventasBackend.obtenerClientesPorTipo(TipoCliente.EMPRESA);
            List<PagoEmpresa> pagosDelMes = filtrarPagosDelMes(
                    pagosService.obtenerTodos(), anio, mes);

            List<Stock> stocks = stockService.obtenerTodos();
            LocalDate desde = LocalDate.of(anio, mes, 1);
            LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
            List<HistorialStock> movimientosStock
                    = stockService.obtenerHistorialMes(desde, hasta);

            List<GastosFijos> gastosFijos = filtrarPorMes(
                    gastosFijosService.obtenerTodos(), anio, mes);
            List<GastosVariables> gastosVariables = filtrarPorMes(
                    gastosVariablesService.obtenerTodos(), anio, mes);
            List<GastosIndividuales> gastosIndividuales = filtrarPorMes(
                    gastosIndividualesService.obtenerTodos(), anio, mes);

            if (ventasDelMes.isEmpty()
                    && pagosDelMes.isEmpty()
                    && stocks.isEmpty()
                    && gastosFijos.isEmpty()
                    && gastosVariables.isEmpty()
                    && gastosIndividuales.isEmpty()) {
                System.err.println(
                        "No se obtuvo ninguna información del backend; "
                        + "revisar que esté corriendo.");
                return false;
            }

            Map<LocalDate, VentaResumenDiarioDTO> resumenDiario
                    = resumenPorDia(ventasDelMes);

            hojaResumenSemanal(libro, ventasDelMes);
            hojaResumenMensual(libro, anio, mes, resumenDiario);
            hojaResumenEmpresas(libro, empresas, ventasDelMes);
            hojaVentasDelMes(libro, anio, mes, ventasDelMes);
            hojaPagosDelMes(libro, pagosDelMes);
            hojaStock(libro, stocks);
            hojaMovimientosStock(libro, movimientosStock);
            hojaGastosFijos(libro, gastosFijos);
            hojaGastosVariables(libro, gastosVariables);
            hojaGastosIndividuales(libro, gastosIndividuales);

            try (FileOutputStream salida = new FileOutputStream(destino)) {
                libro.write(salida);
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error generando el Excel: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (libro != null) {
                try {
                    libro.close();
                } catch (Exception ignorado) {
                    // no hacer nada
                }
            }
        }
    }
// ============================================================
    //            AYUDA PARA ARMAR LOS DATOS
    // ============================================================

    private List<PagoEmpresa> filtrarPagosDelMes(
            List<PagoEmpresa> pagos, int anio, int mes) {
        List<PagoEmpresa> resultado = new ArrayList<>();
        for (PagoEmpresa p : pagos) {
            if (p.getFecha() != null
                    && p.getFecha().getYear() == anio
                    && p.getFecha().getMonthValue() == mes) {
                resultado.add(p);
            }
        }
        resultado.sort(Comparator.comparing(PagoEmpresa::getFecha));
        return resultado;
    }

    private <T> List<T> filtrarPorMes(
            List<T> lista, int anio, int mes) {
        List<T> resultado = new ArrayList<>();
        for (T item : lista) {
            if (perteneceAlMes(item, anio, mes)) {
                resultado.add(item);
            }
        }
        return resultado;
    }

    private boolean perteneceAlMes(Object item, int anio, int mes) {
        LocalDate fecha = null;
        if (item instanceof GastosFijos g) {
            fecha = g.getFecha();
        } else if (item instanceof GastosVariables g) {
            fecha = g.getFecha();
        } else if (item instanceof GastosIndividuales g) {
            fecha = g.getFecha();
        }
        return fecha != null
                && fecha.getYear() == anio
                && fecha.getMonthValue() == mes;
    }

    /**
     * Agrupa las ventas por día y acumula los montos por tipo de pago, igual
     * que hace la vista de resumen de la aplicación (el "venta total" no
     * incluye lo que quedó en DEBE).
     */
    private Map<LocalDate, VentaResumenDiarioDTO> resumenPorDia(
            List<Venta> ventas) {
        Map<LocalDate, VentaResumenDiarioDTO> mapa = new TreeMap<>();
        for (Venta v : ventas) {
            if (v.getFecha() == null) {
                continue;
            }
            LocalDate dia = v.getFecha().toLocalDate();
            VentaResumenDiarioDTO resumen
                    = mapa.computeIfAbsent(dia, VentaResumenDiarioDTO::new);
            acumular(resumen, v);
        }
        return mapa;
    }

    private void acumular(VentaResumenDiarioDTO resumen, Venta v) {
        BigDecimal monto = v.getMonto() == null
                ? BigDecimal.ZERO : v.getMonto();
        TipoDePago tipo = v.getEstado();
        if (tipo == null) {
            return;
        }
        switch (tipo) {
            case EFECTIVO ->
                resumen.setEfectivo(
                        resumen.getEfectivo().add(monto));
            case DEBITO ->
                resumen.setDebito(
                        resumen.getDebito().add(monto));
            case CREDITO ->
                resumen.setCredito(
                        resumen.getCredito().add(monto));
            case TRANSFERENCIA ->
                resumen.setTransferencia(
                        resumen.getTransferencia().add(monto));
            case MERCADO_PAGO ->
                resumen.setMercadoPago(
                        resumen.getMercadoPago().add(monto));
            case DEBE ->
                resumen.setDebe(
                        resumen.getDebe().add(monto));
            case DEUDA_PAGADA ->
                resumen.setDeudaPagada(
                        resumen.getDeudaPagada().add(monto));
            default -> {
                // no acumular
            }
        }
        if (tipo != TipoDePago.DEBE) {
            resumen.setVentaTotal(
                    resumen.getVentaTotal().add(monto));
        }
    }

    private void sumarResumen(VentaResumenDiarioDTO destino,
            VentaResumenDiarioDTO parcial) {
        destino.setVentaTotal(destino.getVentaTotal().add(parcial.getVentaTotal()));
        destino.setDebe(destino.getDebe().add(parcial.getDebe()));
        destino.setDeudaPagada(destino.getDeudaPagada().add(parcial.getDeudaPagada()));
        destino.setDebito(destino.getDebito().add(parcial.getDebito()));
        destino.setCredito(destino.getCredito().add(parcial.getCredito()));
        destino.setTransferencia(destino.getTransferencia().add(parcial.getTransferencia()));
        destino.setMercadoPago(destino.getMercadoPago().add(parcial.getMercadoPago()));
        destino.setEfectivo(destino.getEfectivo().add(parcial.getEfectivo()));
    }

    private String diaSemana(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        String nombre = fecha.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, ES_AR);
        return nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
    }

    private String formatearFecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    private boolean esDiaHabil(LocalDate fecha) {
        DayOfWeek d = fecha.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
    }

// ============================================================
    //            ESTILOS Y ESCRITURA BÁSICA
    // ============================================================
    private CellStyle estiloTitulo(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font fuente = wb.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 14);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        cs.setFont(fuente);
        cs.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        return cs;
    }

    private CellStyle estiloCabecera(Workbook wb) {
        CellStyle cs = wb.createCellStyle();
        Font fuente = wb.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        cs.setFont(fuente);
        cs.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setBorderBottom(BorderStyle.MEDIUM);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        return cs;
    }

    private CellStyle estiloMoneda(Workbook wb, boolean total) {
        CellStyle cs = wb.createCellStyle();
        cs.setDataFormat(wb.createDataFormat().getFormat(FORMATO_MONEDA));
        cs.setAlignment(HorizontalAlignment.RIGHT);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        if (total) {
            Font fuente = wb.createFont();
            fuente.setBold(true);
            cs.setFont(fuente);
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setBorderTop(BorderStyle.MEDIUM);
        }
        return cs;
    }

    private CellStyle estiloTexto(Workbook wb, boolean total) {
        CellStyle cs = wb.createCellStyle();
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        if (total) {
            Font fuente = wb.createFont();
            fuente.setBold(true);
            cs.setFont(fuente);
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setBorderTop(BorderStyle.MEDIUM);
        }
        return cs;
    }

    private void escribirFilaResumen(Sheet sh, int filaId,
            LocalDate fecha, VentaResumenDiarioDTO r,
            CellStyle estiloTexto, CellStyle estiloMoneda, boolean total) {
        Row fila = sh.createRow(filaId);
        Cell c0 = fila.createCell(0);
        c0.setCellValue(total ? "TOTAL" : formatearFecha(fecha));
        c0.setCellStyle(estiloTexto);

        Cell c1 = fila.createCell(1);
        c1.setCellValue(total ? "" : diaSemana(fecha));
        c1.setCellStyle(estiloTexto);

        celdaMoneda(fila, 2, r.getVentaTotal(), estiloMoneda);
        celdaMoneda(fila, 3, r.getEfectivo(), estiloMoneda);
        celdaMoneda(fila, 4, r.getDebito(), estiloMoneda);
        celdaMoneda(fila, 5, r.getCredito(), estiloMoneda);
        celdaMoneda(fila, 6, r.getTransferencia(), estiloMoneda);
        celdaMoneda(fila, 7, r.getMercadoPago(), estiloMoneda);
        celdaMoneda(fila, 8, r.getDebe(), estiloMoneda);
        celdaMoneda(fila, 9, r.getDeudaPagada(), estiloMoneda);
    }

    private void escribirFilaEmpresa(Sheet sh, int filaId,
            String empresa, VentaResumenDiarioDTO r,
            CellStyle estiloTexto, CellStyle estiloMoneda) {
        Row fila = sh.createRow(filaId);
        Cell c0 = fila.createCell(0);
        c0.setCellValue(empresa);
        c0.setCellStyle(estiloTexto);
        celdaMoneda(fila, 1, r.getVentaTotal(), estiloMoneda);
        celdaMoneda(fila, 2, r.getEfectivo(), estiloMoneda);
        celdaMoneda(fila, 3, r.getDebito(), estiloMoneda);
        celdaMoneda(fila, 4, r.getCredito(), estiloMoneda);
        celdaMoneda(fila, 5, r.getTransferencia(), estiloMoneda);
        celdaMoneda(fila, 6, r.getMercadoPago(), estiloMoneda);
        celdaMoneda(fila, 7, r.getDebe(), estiloMoneda);
        celdaMoneda(fila, 8, r.getDeudaPagada(), estiloMoneda);
    }

    private void celdaMoneda(Row fila, int col, BigDecimal valor,
            CellStyle estilo) {
        Cell c = fila.createCell(col);
        c.setCellValue(valor == null ? 0d : valor.doubleValue());
        c.setCellStyle(estilo);
    }

    /**
     * Escribe el título (fila 0, combinando celdas) y la cabecera (fila 1).
     */
    private void escribirTituloYCabecera(Workbook wb, Sheet sh,
            String titulo, String[] columnas) {
        CellStyle estiloTitulo = estiloTitulo(wb);
        CellStyle estiloCabecera = estiloCabecera(wb);

        Row filaTitulo = sh.createRow(0);
        Cell cTitulo = filaTitulo.createCell(0);
        cTitulo.setCellValue(titulo);
        cTitulo.setCellStyle(estiloTitulo);
        filaTitulo.setHeightInPoints(26);
        if (columnas.length > 1) {
            sh.addMergedRegion(
                    new CellRangeAddress(0, 0, 0, columnas.length - 1));
        }

        Row filaCabecera = sh.createRow(1);
        for (int i = 0; i < columnas.length; i++) {
            Cell c = filaCabecera.createCell(i);
            c.setCellValue(columnas[i]);
            c.setCellStyle(estiloCabecera);
        }
        filaCabecera.setHeightInPoints(20);
    }

    // ============================================================
    //            HOJAS DEL LIBRO
    // ============================================================
    private void hojaResumenSemanal(
            Workbook wb,
            List<Venta> ventas) {

        Sheet sh = wb.createSheet("Resumen Semanal General");

        String[] columnas = {
            "Semana",
            "Fecha",
            "Día",
            "Cliente",
            "Tipo Cliente",
            "Descripción",
            "Método de Pago",
            "Monto",
            "Observación"
        };

        escribirTituloYCabecera(
                wb,
                sh,
                "Resumen Semanal General",
                columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);

        List<Venta> ordenadas = ventas.stream()
                .filter(v -> v.getFecha() != null)
                .sorted(Comparator.comparing(Venta::getFecha))
                .toList();

        int fila = 2;
        BigDecimal total = BigDecimal.ZERO;

        for (Venta v : ordenadas) {

            LocalDate fecha = v.getFecha().toLocalDate();

            Row r = sh.createRow(fila++);

            setTexto(
                    r,
                    0,
                    "Semana " + obtenerSemanaDelMes(fecha),
                    estiloTexto);

            setTexto(
                    r,
                    1,
                    formatearFecha(fecha),
                    estiloTexto);

            setTexto(
                    r,
                    2,
                    diaSemana(fecha),
                    estiloTexto);

            String cliente = v.getCliente() != null
                    && v.getCliente().getNombre() != null
                    ? v.getCliente().getNombre()
                    : "";

            setTexto(r, 3, cliente, estiloTexto);

            String tipoCliente = v.getCliente() != null
                    && v.getCliente().getTipoCliente() != null
                    ? v.getCliente().getTipoCliente().name()
                    : "";

            setTexto(r, 4, tipoCliente, estiloTexto);

            setTexto(
                    r,
                    5,
                    v.getDescripcion(),
                    estiloTexto);

            setTexto(
                    r,
                    6,
                    v.getEstado() != null
                    ? v.getEstado().name()
                    : "",
                    estiloTexto);

            celdaMoneda(
                    r,
                    7,
                    v.getMonto(),
                    estiloMoneda);

            setTexto(
                    r,
                    8,
                    v.getObservaciones(),
                    estiloTexto);

            total = total.add(
                    v.getMonto() != null
                    ? v.getMonto()
                    : BigDecimal.ZERO);
        }

        Row filaTotal = sh.createRow(fila);

        Cell c = filaTotal.createCell(0);
        c.setCellValue("TOTAL");
        c.setCellStyle(estiloTexto(wb, true));

        celdaMoneda(
                filaTotal,
                7,
                total,
                estiloMoneda(wb, true));

        sh.setColumnWidth(0, 12 * 256); // Semana
        sh.setColumnWidth(1, 12 * 256); // Fecha
        sh.setColumnWidth(2, 14 * 256); // Día
        sh.setColumnWidth(3, 25 * 256); // Cliente
        sh.setColumnWidth(4, 15 * 256); // Tipo Cliente
        sh.setColumnWidth(5, 40 * 256); // Descripción
        sh.setColumnWidth(6, 18 * 256); // Método de Pago
        sh.setColumnWidth(7, 13 * 256); // Monto
        sh.setColumnWidth(8, 35 * 256); // Observación

        sh.createFreezePane(0, 2);
    }

    private void hojaResumenMensual(Workbook wb, int anio, int mes,
            Map<LocalDate, VentaResumenDiarioDTO> resumenDiario) {
        Sheet sh = wb.createSheet("Resumen Mensual");
        String[] columnas = {"Fecha", "Día", "Venta Total", "Efectivo",
            "Débito", "Crédito", "Transferencia",
            "Mercado Pago", "Debe", "Deuda Pagada"};
        String mesTexto = LocalDate.of(anio, mes, 1)
                .getMonth().getDisplayName(TextStyle.FULL, ES_AR);
        escribirTituloYCabecera(wb, sh,
                "Resumen Mensual - " + mesTexto + " " + anio, columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);

        LocalDate fecha = LocalDate.of(anio, mes, 1);
        VentaResumenDiarioDTO total = new VentaResumenDiarioDTO(null);
        int fila = 2;

        while (fecha.getMonthValue() == mes) {
            if (esDiaHabil(fecha)) {
                VentaResumenDiarioDTO r = resumenDiario.getOrDefault(
                        fecha, new VentaResumenDiarioDTO(fecha));
                sumarResumen(total, r);
                escribirFilaResumen(sh, fila, fecha, r,
                        estiloTexto, estiloMoneda, false);
                fila++;
            }
            fecha = fecha.plusDays(1);
        }

        escribirFilaResumen(sh, fila, null, total,
                estiloTexto(wb, true), estiloMoneda(wb, true), true);

        sh.setColumnWidth(0, 12 * 256);
        sh.setColumnWidth(1, 14 * 256);
        for (int i = 2; i < columnas.length; i++) {
            sh.setColumnWidth(i, 13 * 256);
        }
        sh.createFreezePane(0, 2);
    }

    private void hojaResumenEmpresas(
            Workbook wb,
            List<String> empresas,
            List<Venta> ventas) {

        Sheet sh = wb.createSheet("Estado Cuenta Empresas");

        String[] columnas = {
            "Fecha",
            "Descripción",
            "Método de Pago",
            "Monto",
            "Observación"
        };

        escribirTituloYCabecera(
                wb,
                sh,
                "Estado de Cuenta Empresas",
                columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);

        int fila = 2;

        for (String empresa : empresas) {

            Row encabezadoEmpresa = sh.createRow(fila++);

            Cell celdaEmpresa = encabezadoEmpresa.createCell(0);
            celdaEmpresa.setCellValue("EMPRESA: " + empresa);
            celdaEmpresa.setCellStyle(estiloTexto(wb, true));

            BigDecimal totalConsumido = BigDecimal.ZERO;
            BigDecimal totalPagado = BigDecimal.ZERO;

            List<Venta> ventasEmpresa = ventas.stream()
                    .filter(v
                            -> v.getCliente() != null
                    && empresa.equalsIgnoreCase(
                            v.getCliente().getNombre()))
                    .sorted(Comparator.comparing(Venta::getFecha))
                    .toList();

            for (Venta v : ventasEmpresa) {

                Row r = sh.createRow(fila++);

                setTexto(
                        r,
                        0,
                        v.getFecha() != null
                        ? formatearFecha(v.getFecha().toLocalDate())
                        : "",
                        estiloTexto);

                setTexto(
                        r,
                        1,
                        v.getDescripcion(),
                        estiloTexto);

                setTexto(
                        r,
                        2,
                        v.getEstado() != null
                        ? v.getEstado().name()
                        : "",
                        estiloTexto);

                celdaMoneda(
                        r,
                        3,
                        v.getMonto(),
                        estiloMoneda);

                setTexto(
                        r,
                        4,
                        v.getObservaciones(),
                        estiloTexto);

                BigDecimal monto = v.getMonto() == null
                        ? BigDecimal.ZERO
                        : v.getMonto();

                totalConsumido = totalConsumido.add(monto);

                if (v.getEstado() == TipoDePago.DEUDA_PAGADA) {
                    totalPagado = totalPagado.add(monto);
                }
            }

            BigDecimal saldoPendiente
                    = totalConsumido.subtract(totalPagado);

            Row resumen1 = sh.createRow(fila++);
            setTexto(
                    resumen1,
                    0,
                    "Total Consumido",
                    estiloTexto(wb, true));
            celdaMoneda(
                    resumen1,
                    3,
                    totalConsumido,
                    estiloMoneda(wb, true));

            Row resumen2 = sh.createRow(fila++);
            setTexto(
                    resumen2,
                    0,
                    "Total Pagado",
                    estiloTexto(wb, true));
            celdaMoneda(
                    resumen2,
                    3,
                    totalPagado,
                    estiloMoneda(wb, true));

            Row resumen3 = sh.createRow(fila++);
            setTexto(
                    resumen3,
                    0,
                    "Saldo Pendiente",
                    estiloTexto(wb, true));
            celdaMoneda(
                    resumen3,
                    3,
                    saldoPendiente,
                    estiloMoneda(wb, true));

            fila++;
        }

        sh.setColumnWidth(0, 18 * 256);
        sh.setColumnWidth(1, 45 * 256);
        sh.setColumnWidth(2, 18 * 256);
        sh.setColumnWidth(3, 15 * 256);
        sh.setColumnWidth(4, 35 * 256);

        sh.createFreezePane(0, 2);
    }

    private void hojaVentasDelMes(Workbook wb, int anio, int mes,
            List<Venta> ventas) {
        Sheet sh = wb.createSheet("Ventas del Mes");
        String[] columnas = {"Fecha", "Cliente", "Tipo", "Descripción",
            "Monto", "Estado", "Observaciones"};
        escribirTituloYCabecera(wb, sh, "Ventas del Mes", columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);

        List<Venta> ordenadas = ventas.stream()
                .filter(v -> v.getFecha() != null)
                .sorted(Comparator.comparing(Venta::getFecha))
                .toList();

        int fila = 2;
        BigDecimal total = BigDecimal.ZERO;
        for (Venta v : ordenadas) {
            Row r = sh.createRow(fila);
            setTexto(r, 0, formatearFecha(v.getFecha().toLocalDate()),
                    estiloTexto);
            String nombreCliente = v.getCliente() != null
                    && v.getCliente().getNombre() != null
                    ? v.getCliente().getNombre() : "";
            String tipoCliente = v.getCliente() != null
                    && v.getCliente().getTipoCliente() != null
                    ? v.getCliente().getTipoCliente().name() : "";
            setTexto(r, 1, nombreCliente, estiloTexto);
            setTexto(r, 2, tipoCliente, estiloTexto);
            setTexto(r, 3, v.getDescripcion(), estiloTexto);
            celdaMoneda(r, 4, v.getMonto(), estiloMoneda);
            setTexto(r, 5, v.getEstado() == null ? "" : v.getEstado().name(),
                    estiloTexto);
            setTexto(r, 6, v.getObservaciones(), estiloTexto);
            total = total.add(v.getMonto() == null ? BigDecimal.ZERO : v.getMonto());
            fila++;
        }

        Row filaTotal = sh.createRow(fila);
        Cell c = filaTotal.createCell(0);
        c.setCellValue("TOTAL");
        c.setCellStyle(estiloTexto(wb, true));
        celdaMoneda(filaTotal, 4, total, estiloMoneda(wb, true));

        sh.setColumnWidth(0, 12 * 256);
        sh.setColumnWidth(1, 24 * 256);
        sh.setColumnWidth(2, 12 * 256);
        sh.setColumnWidth(3, 45 * 256);
        sh.setColumnWidth(4, 13 * 256);
        sh.setColumnWidth(5, 15 * 256);
        sh.setColumnWidth(6, 30 * 256);
        sh.createFreezePane(0, 2);
    }

    private void setTexto(Row fila, int col, String valor,
            CellStyle estilo) {
        Cell c = fila.createCell(col);
        c.setCellValue(valor == null ? "" : valor);
        c.setCellStyle(estilo);
    }

    private void hojaPagosDelMes(Workbook wb, List<PagoEmpresa> pagos) {
        Sheet sh = wb.createSheet("Pagos del Mes");
        String[] columnas = {"Fecha", "Empresa", "Periodicidad", "Nro Pago",
            "CUIT", "Monto", "Monto con IVA", "Factura",
            "Estado", "Observación"};
        escribirTituloYCabecera(wb, sh, "Pagos del Mes", columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);

        int fila = 2;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;
        for (PagoEmpresa p : pagos) {
            Row r = sh.createRow(fila);
            setTexto(r, 0, p.getFecha() == null ? ""
                    : p.getFecha().toLocalDate().format(FORMATO_FECHA),
                    estiloTexto);
            setTexto(r, 1, p.getNombre(), estiloTexto);
            setTexto(r, 2, p.getTipoPeriodicidad() == null ? ""
                    : p.getTipoPeriodicidad().name(), estiloTexto);
            setTexto(r, 3, p.getNumeroPago() == null ? ""
                    : String.valueOf(p.getNumeroPago()), estiloTexto);
            setTexto(r, 4, p.getCuit(), estiloTexto);
            celdaMoneda(r, 5, p.getMonto(), estiloMoneda);
            celdaMoneda(r, 6, p.getMontoConIva(), estiloMoneda);
            setTexto(r, 7, p.getFactura(), estiloTexto);
            setTexto(r, 8, p.getEstado() == null ? "" : p.getEstado().name(),
                    estiloTexto);
            setTexto(r, 9, p.getObservacion(), estiloTexto);
            total = total.add(p.getMonto() == null ? BigDecimal.ZERO : p.getMonto());
            totalIva = totalIva.add(p.getMontoConIva() == null
                    ? BigDecimal.ZERO : p.getMontoConIva());
            fila++;
        }

        Row filaTotal = sh.createRow(fila);
        Cell c = filaTotal.createCell(0);
        c.setCellValue("TOTAL");
        c.setCellStyle(estiloTexto(wb, true));
        celdaMoneda(filaTotal, 5, total, estiloMoneda(wb, true));
        celdaMoneda(filaTotal, 6, totalIva, estiloMoneda(wb, true));

        sh.setColumnWidth(0, 12 * 256);
        sh.setColumnWidth(1, 24 * 256);
        sh.setColumnWidth(2, 14 * 256);
        sh.setColumnWidth(3, 10 * 256);
        sh.setColumnWidth(4, 14 * 256);
        sh.setColumnWidth(5, 13 * 256);
        sh.setColumnWidth(6, 15 * 256);
        sh.setColumnWidth(7, 14 * 256);
        sh.setColumnWidth(8, 12 * 256);
        sh.setColumnWidth(9, 30 * 256);
        sh.createFreezePane(0, 2);
    }

    private void hojaStock(Workbook wb, List<Stock> stocks) {

    Sheet sh = wb.createSheet("Stock");

    String[] columnas = {
        "Producto",
        "Categoría",
        "Cantidad Actual",
        "Unidad",
        "Stock Mínimo",
        "Estado",
        "Última Actualización"
    };

    escribirTituloYCabecera(
            wb,
            sh,
            "Stock Actual",
            columnas);

    CellStyle estiloTexto = estiloTexto(wb, false);

    CellStyle estiloNumero = wb.createCellStyle();
    estiloNumero.setDataFormat(
            wb.createDataFormat().getFormat("#,##0.00"));
    estiloNumero.setAlignment(HorizontalAlignment.RIGHT);
    estiloNumero.setVerticalAlignment(VerticalAlignment.CENTER);
    estiloNumero.setBorderBottom(BorderStyle.THIN);
    estiloNumero.setBorderTop(BorderStyle.THIN);
    estiloNumero.setBorderLeft(BorderStyle.THIN);
    estiloNumero.setBorderRight(BorderStyle.THIN);

    int fila = 2;

    for (Stock s : stocks) {

        Row r = sh.createRow(fila++);

        setTexto(
                r,
                0,
                s.getNombreProducto(),
                estiloTexto);

        String categoria = s.getCategoriaGastoVariable() != null
                ? s.getCategoriaGastoVariable().getNombre()
                : "";

        setTexto(
                r,
                1,
                categoria,
                estiloTexto);

        ponerNumero(
                r,
                2,
                s.getCantidad(),
                estiloNumero);

        setTexto(
                r,
                3,
                s.getUnidadCantidad(),
                estiloTexto);

        ponerNumero(
                r,
                4,
                s.getStockMinimo(),
                estiloNumero);

        BigDecimal cantidad = s.getCantidad() == null
                ? BigDecimal.ZERO
                : s.getCantidad();

        BigDecimal minimo = s.getStockMinimo() == null
                ? BigDecimal.ZERO
                : s.getStockMinimo();

        String estado;

        if (cantidad.compareTo(BigDecimal.ZERO) == 0) {
            estado = "SIN STOCK";
        } else if (cantidad.compareTo(minimo) <= 0) {
            estado = "BAJO STOCK";
        } else {
            estado = "OK";
        }

        setTexto(
                r,
                5,
                estado,
                estiloTexto);

        setTexto(
                r,
                6,
                s.getFecha() == null
                ? ""
                : s.getFecha().format(FORMATO_FECHA),
                estiloTexto);
    }

    sh.setColumnWidth(0, 30 * 256);
    sh.setColumnWidth(1, 20 * 256);
    sh.setColumnWidth(2, 15 * 256);
    sh.setColumnWidth(3, 12 * 256);
    sh.setColumnWidth(4, 15 * 256);
    sh.setColumnWidth(5, 15 * 256);
    sh.setColumnWidth(6, 18 * 256);

    sh.createFreezePane(0, 2);
}

    private void ponerNumero(Row fila, int col, BigDecimal valor,
            CellStyle estilo) {
        Cell c = fila.createCell(col);
        c.setCellValue(valor == null ? 0d : valor.doubleValue());
        c.setCellStyle(estilo);
    }

    private void hojaMovimientosStock(
        Workbook wb,
        List<HistorialStock> movimientos) {

    Sheet sh = wb.createSheet("Movimientos Stock");

    String[] columnas = {
        "Fecha",
        "Movimiento",
        "Cantidad",
        "Unidad",
        "Descripción"
    };

    escribirTituloYCabecera(
            wb,
            sh,
            "Historial de Movimientos por Producto",
            columnas);

    CellStyle estiloTexto = estiloTexto(wb, false);

    CellStyle estiloNumero = wb.createCellStyle();
    estiloNumero.setDataFormat(
            wb.createDataFormat().getFormat("#,##0.00"));
    estiloNumero.setAlignment(HorizontalAlignment.RIGHT);
    estiloNumero.setVerticalAlignment(VerticalAlignment.CENTER);
    estiloNumero.setBorderBottom(BorderStyle.THIN);
    estiloNumero.setBorderTop(BorderStyle.THIN);
    estiloNumero.setBorderLeft(BorderStyle.THIN);
    estiloNumero.setBorderRight(BorderStyle.THIN);

    List<String> productos = movimientos.stream()
            .filter(h -> h.getStock() != null)
            .map(h -> h.getStock().getNombreProducto())
            .distinct()
            .sorted()
            .toList();

    int fila = 2;

    for (String producto : productos) {

        Row encabezado = sh.createRow(fila++);

        Cell c = encabezado.createCell(0);
        c.setCellValue("PRODUCTO: " + producto);
        c.setCellStyle(estiloTexto(wb, true));

        List<HistorialStock> listaProducto = movimientos.stream()
                .filter(h -> h.getStock() != null)
                .filter(h ->
                        producto.equals(
                                h.getStock().getNombreProducto()))
                .sorted(Comparator.comparing(
                        HistorialStock::getFecha))
                .toList();

        for (HistorialStock h : listaProducto) {

            Row r = sh.createRow(fila++);

            setTexto(
                    r,
                    0,
                    h.getFecha() == null
                            ? ""
                            : h.getFecha().format(FORMATO_FECHA),
                    estiloTexto);

            setTexto(
                    r,
                    1,
                    h.getMovimiento() == null
                            ? ""
                            : h.getMovimiento().toPlainString(),
                    estiloTexto);

            ponerNumero(
                    r,
                    2,
                    h.getCantidad(),
                    estiloNumero);

            setTexto(
                    r,
                    3,
                    h.getStock() != null
                            ? h.getStock().getUnidadCantidad()
                            : "",
                    estiloTexto);

            String detalle = h.getGastoVariable() != null
                    ? h.getGastoVariable().getProducto()
                    : "";

            setTexto(
                    r,
                    4,
                    detalle,
                    estiloTexto);
        }

        fila++;
    }

    sh.setColumnWidth(0, 15 * 256); // Fecha
    sh.setColumnWidth(1, 18 * 256); // Movimiento
    sh.setColumnWidth(2, 15 * 256); // Cantidad
    sh.setColumnWidth(3, 12 * 256); // Unidad
    sh.setColumnWidth(4, 45 * 256); // Descripción

    sh.createFreezePane(0, 2);
}

    private void hojaGastosFijos(Workbook wb, List<GastosFijos> gastos) {
        Sheet sh = wb.createSheet("Gastos Fijos");
        String[] columnas = {"Fecha", "Detalle", "Estado", "Es Personal",
            "Monto", "Observación"};
        escribirTituloYCabecera(wb, sh, "Gastos Fijos del Mes", columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);

        List<GastosFijos> ordenados = gastos.stream()
                .sorted(Comparator.comparing(GastosFijos::getFecha))
                .toList();

        int fila = 2;
        BigDecimal total = BigDecimal.ZERO;
        for (GastosFijos g : ordenados) {
            Row r = sh.createRow(fila);
            setTexto(r, 0, g.getFecha() == null ? ""
                    : g.getFecha().format(FORMATO_FECHA), estiloTexto);
            setTexto(r, 1, g.getDetalle(), estiloTexto);
            setTexto(r, 2, Boolean.TRUE.equals(g.getEstado()) ? "PAGADO"
                    : "PENDIENTE", estiloTexto);
            setTexto(r, 3, Boolean.TRUE.equals(g.getEsPersonal())
                    ? "SÍ" : "NO", estiloTexto);
            celdaMoneda(r, 4, g.getMonto(), estiloMoneda);
            setTexto(r, 5, g.getObservacion(), estiloTexto);
            total = total.add(g.getMonto() == null ? BigDecimal.ZERO : g.getMonto());
            fila++;
        }

        Row filaTotal = sh.createRow(fila);
        Cell c = filaTotal.createCell(0);
        c.setCellValue("TOTAL");
        c.setCellStyle(estiloTexto(wb, true));
        celdaMoneda(filaTotal, 4, total, estiloMoneda(wb, true));

        sh.setColumnWidth(0, 12 * 256);
        sh.setColumnWidth(1, 32 * 256);
        sh.setColumnWidth(2, 12 * 256);
        sh.setColumnWidth(3, 13 * 256);
        sh.setColumnWidth(4, 13 * 256);
        sh.setColumnWidth(5, 28 * 256);
        sh.createFreezePane(0, 2);
    }

    private void hojaGastosVariables(Workbook wb, List<GastosVariables> gastos) {
        Sheet sh = wb.createSheet("Gastos Variables");
        String[] columnas = {"Fecha", "Producto", "Categoría",
            "Cant. Comprada", "Medida", "Monto", "Cargado en Stock"};
        escribirTituloYCabecera(wb, sh, "Gastos Variables del Mes", columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);
        CellStyle estiloNumero = wb.createCellStyle();
        estiloNumero.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        estiloNumero.setAlignment(HorizontalAlignment.RIGHT);
        estiloNumero.setVerticalAlignment(VerticalAlignment.CENTER);
        estiloNumero.setBorderBottom(BorderStyle.THIN);
        estiloNumero.setBorderTop(BorderStyle.THIN);
        estiloNumero.setBorderLeft(BorderStyle.THIN);
        estiloNumero.setBorderRight(BorderStyle.THIN);

        List<GastosVariables> ordenados = gastos.stream()
                .sorted(Comparator.comparing(GastosVariables::getFecha))
                .toList();

        int fila = 2;
        BigDecimal total = BigDecimal.ZERO;
        for (GastosVariables g : ordenados) {
            Row r = sh.createRow(fila);
            setTexto(r, 0, g.getFecha() == null ? ""
                    : g.getFecha().format(FORMATO_FECHA), estiloTexto);
            setTexto(r, 1, g.getProducto(), estiloTexto);
            String categoria = g.getCategoria() != null
                    ? g.getCategoria().getNombre() : "";
            setTexto(r, 2, categoria, estiloTexto);
            ponerNumero(r, 3, g.getCantComprada(), estiloNumero);
            setTexto(r, 4, g.getMedida(), estiloTexto);
            celdaMoneda(r, 5, g.getMonto(), estiloMoneda);
            setTexto(r, 6, Boolean.TRUE.equals(g.getCargadoEnStock())
                    ? "SÍ" : "NO", estiloTexto);
            total = total.add(g.getMonto() == null ? BigDecimal.ZERO : g.getMonto());
            fila++;
        }

        Row filaTotal = sh.createRow(fila);
        Cell c = filaTotal.createCell(0);
        c.setCellValue("TOTAL");
        c.setCellStyle(estiloTexto(wb, true));
        celdaMoneda(filaTotal, 5, total, estiloMoneda(wb, true));

        sh.setColumnWidth(0, 12 * 256);
        sh.setColumnWidth(1, 28 * 256);
        sh.setColumnWidth(2, 18 * 256);
        sh.setColumnWidth(3, 14 * 256);
        sh.setColumnWidth(4, 10 * 256);
        sh.setColumnWidth(5, 13 * 256);
        sh.setColumnWidth(6, 16 * 256);
        sh.createFreezePane(0, 2);
    }

    private void hojaGastosIndividuales(Workbook wb,
            List<GastosIndividuales> gastos) {
        Sheet sh = wb.createSheet("Gastos Individuales");
        String[] columnas = {"Fecha", "Empleado", "Detalle", "Monto"};
        escribirTituloYCabecera(wb, sh, "Gastos Individuales del Mes", columnas);

        CellStyle estiloTexto = estiloTexto(wb, false);
        CellStyle estiloMoneda = estiloMoneda(wb, false);

        List<GastosIndividuales> ordenados = gastos.stream()
                .sorted(Comparator.comparing(GastosIndividuales::getFecha))
                .toList();

        int fila = 2;
        BigDecimal total = BigDecimal.ZERO;
        for (GastosIndividuales g : ordenados) {
            Row r = sh.createRow(fila);
            setTexto(r, 0, g.getFecha() == null ? ""
                    : g.getFecha().format(FORMATO_FECHA), estiloTexto);
            String empleado = g.getEmpleado() != null
                    && g.getEmpleado().getNombre() != null
                    ? g.getEmpleado().getNombre() + (g.getEmpleado().getApellido() == null
                    ? "" : " " + g.getEmpleado().getApellido())
                    : "";
            setTexto(r, 1, empleado, estiloTexto);
            setTexto(r, 2, g.getDetalle(), estiloTexto);
            celdaMoneda(r, 3, g.getMonto(), estiloMoneda);
            total = total.add(g.getMonto() == null ? BigDecimal.ZERO : g.getMonto());
            fila++;
        }

        Row filaTotal = sh.createRow(fila);
        Cell c = filaTotal.createCell(0);
        c.setCellValue("TOTAL");
        c.setCellStyle(estiloTexto(wb, true));
        celdaMoneda(filaTotal, 3, total, estiloMoneda(wb, true));

        sh.setColumnWidth(0, 12 * 256);
        sh.setColumnWidth(1, 24 * 256);
        sh.setColumnWidth(2, 40 * 256);
        sh.setColumnWidth(3, 13 * 256);
        sh.createFreezePane(0, 2);
    }

    private int obtenerSemanaDelMes(LocalDate fecha) {
        return ((fecha.getDayOfMonth() - 1) / 7) + 1;
    }
}
