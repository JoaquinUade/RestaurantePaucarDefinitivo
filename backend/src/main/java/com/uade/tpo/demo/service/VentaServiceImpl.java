package com.uade.tpo.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.demo.entity.Cliente;
import com.uade.tpo.demo.entity.EstadoPago;
import com.uade.tpo.demo.entity.PagoEmpresa;
import com.uade.tpo.demo.entity.Producto;
import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.TipoPeriodicidad;
import com.uade.tpo.demo.entity.Venta;
import com.uade.tpo.demo.entity.dto.VentaDTO;
import com.uade.tpo.demo.entity.dto.VentaRequest;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;
import com.uade.tpo.demo.repository.ClienteRepository;
import com.uade.tpo.demo.repository.PagoEmpresaRepository;
import com.uade.tpo.demo.repository.ProductoRepository;
import com.uade.tpo.demo.repository.VentaRepository;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PagoEmpresaRepository pagoEmpresaRepository;

    @Override
    public Venta crearVenta(VentaRequest ventaRequest) {
        // Obtener el cliente
        Cliente cliente = clienteRepository.findById(ventaRequest.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Obtener las listas originales
        List<Long> idProductosOriginal = ventaRequest.getIdProductos();
        List<Integer> cantidadesOriginal = ventaRequest.getCantidades();

        // Procesar duplicados: combinar productos con el mismo ID
        Map<Long, Integer> productosMap = new LinkedHashMap<>(); // Mantiene el orden de primera aparición

        for (int i = 0; i < idProductosOriginal.size(); i++) {
            Long idProducto = idProductosOriginal.get(i);
            Integer cantidad = cantidadesOriginal.get(i);

            // Si el producto ya existe en el mapa, sumamos la cantidad
            if (productosMap.containsKey(idProducto)) {
                productosMap.put(idProducto, productosMap.get(idProducto) + cantidad);
            } else {
                productosMap.put(idProducto, cantidad);
            }
        }

        // Crear nuevas listas sin duplicados
        List<Long> idProductosUnicos = new ArrayList<>(productosMap.keySet());
        List<Integer> cantidadesUnicas = new ArrayList<>(productosMap.values());

        // Calcular monto total y construir descripción con los datos ya procesados
        BigDecimal montoTotal = BigDecimal.ZERO;
        StringBuilder descripcion = new StringBuilder();

        for (int i = 0; i < idProductosUnicos.size(); i++) {
            Long idProducto = idProductosUnicos.get(i);
            Integer cantidad = cantidadesUnicas.get(i);

            // Buscar el producto en la base de datos
            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + idProducto));

            BigDecimal precioProducto = producto.getPrecio();

            // Sumar al monto total
            montoTotal = montoTotal.add(precioProducto.multiply(new BigDecimal(cantidad)));

            // Construir descripción
            if (i > 0) {
                descripcion.append(" + ");
            }
            descripcion.append(cantidad).append(" ").append(producto.getNombre());
        }

        // Crear y guardar la venta
        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setDescripcion(descripcion.toString());
        venta.setMonto(montoTotal);
        venta.setEstado(ventaRequest.getEstado());
        venta.setObservaciones(ventaRequest.getObservaciones());
        venta.setConsumidor(ventaRequest.getConsumidor());
        if (ventaRequest.getFecha() != null) {
            venta.setFecha(ventaRequest.getFecha().atStartOfDay());
        } else {
            venta.setFecha(LocalDateTime.now());
        }
        venta.setDia(LocalDate.now().getDayOfWeek()
                .getDisplayName(TextStyle.FULL,
                        new Locale("es", "ES")));

        Venta ventaGuardada = ventaRepository.save(venta);

        actualizarPagoAutomatico(ventaGuardada);

        return ventaGuardada;
    }

    @Override
    public Venta modificarVenta(Long id, Venta venta) {
        return ventaRepository.findById(id).map(existing -> {
            if (venta.getCliente() != null) {
                existing.setCliente(venta.getCliente());
            }
            if (venta.getDescripcion() != null) {
                existing.setDescripcion(venta.getDescripcion());
            }
            if (venta.getMonto() != null) {
                existing.setMonto(venta.getMonto());
            }
            if (venta.getEstado() != null) {
                existing.setEstado(venta.getEstado());
            }
            if (venta.getObservaciones() != null) {
                existing.setObservaciones(venta.getObservaciones());
            }
            // fecha y id no se actualizan aquí
            return ventaRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Venta no encontrada con id: " + id));
    }

    @Override
public void borrarVenta(Long id) {

    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Venta no encontrada"));

    Cliente cliente = venta.getCliente();

    ventaRepository.delete(venta);

    List<PagoEmpresa> pagos =
            pagoEmpresaRepository.obtenerTodosPorEmpresa(
                    cliente.getIdCliente());

    for (PagoEmpresa pago : pagos) {

        BigDecimal nuevoTotal = calcularTotalPago(
                cliente,
                pago.getTipoPeriodicidad(),
                pago.getFecha());

        pago.setMonto(nuevoTotal);

        pagoEmpresaRepository.save(pago);
    }
}

    @Override
    public List<Venta> filtrarPorMes(int mes, int anio) {
        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getFecha().getMonthValue() == mes && v.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> filtrarPorAnio(int anio) {
        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getFecha().getYear() == anio)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> filtrarPorDia(int dia) {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getFecha().getDayOfMonth() == dia
                && v.getFecha().getMonthValue() == currentMonth
                && v.getFecha().getYear() == currentYear)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> filtrarPorAnioYMes(int anio, int mes) {
        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getFecha().getYear() == anio && v.getFecha().getMonthValue() == mes)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> filtrarPorAnioMesDia(int anio, int mes, int dia) {
        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getFecha().getYear() == anio
                && v.getFecha().getMonthValue() == mes
                && v.getFecha().getDayOfMonth() == dia)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> obtenerTodas() {
        return ventaRepository.findAll();
    }

    @Override
    public List<VentaDTO> obtenerVentasOrdenadas(Integer mes, Integer anio) {
        int yearFilter = anio != null ? anio : java.time.LocalDate.now().getYear();

        return ventaRepository.findAll()
                .stream()
                .filter(v -> {
                    // Filtrar por año
                    if (v.getFecha().getYear() != yearFilter) {
                        return false;
                    }
                    // Filtrar por mes si se proporciona
                    if (mes != null && v.getFecha().getMonthValue() != mes) {
                        return false;
                    }
                    return true;
                })
                .map(v -> new VentaDTO(
                v.getFecha(),
                v.getDia(), // ← Usamos el día de la BD
                v.getCliente().getNombre(),
                v.getDescripcion(),
                v.getMonto()
        ))
                .sorted((v1, v2) -> {
                    int comparaNombre = v1.getNombreCliente().compareTo(v2.getNombreCliente());
                    if (comparaNombre != 0) {
                        return comparaNombre;
                    }
                    return v1.getFecha().compareTo(v2.getFecha());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResumenDiarioDTO> obtenerResumenDiarioPorTipoPago(Integer mes, Integer anio) {
        int yearFilter = anio != null ? anio : java.time.LocalDate.now().getYear();
        int mesFilter = mes != null ? mes : java.time.LocalDate.now().getMonthValue();

        return ventaRepository.findAll()
                .stream()
                .filter(v -> v.getFecha().getYear() == yearFilter
                && v.getFecha().getMonthValue() == mesFilter)
                .collect(Collectors.groupingBy(v -> v.getFecha().toLocalDate()))
                .entrySet()
                .stream()
                .map(entry -> {
                    LocalDate fecha = entry.getKey();
                    List<Venta> ventasDelDia = entry.getValue();

                    // USAMOS EL DÍA DE LA BASE DE DATOS
                    String dia = ventasDelDia.get(0).getDia(); // Todas tienen el mismo día

                    VentaResumenDiarioDTO resumen = new VentaResumenDiarioDTO(fecha);
                    resumen.setDia(dia); // Asignamos el día desde la BD

                    for (Venta venta : ventasDelDia) {
                        BigDecimal monto = venta.getMonto();
                        resumen.setVentaTotal(resumen.getVentaTotal().add(monto));

                        switch (venta.getEstado()) {
                            case TRANSFERENCIA:
                                resumen.setTransferencia(resumen.getTransferencia().add(monto));
                                break;
                            case DEBE:
                                resumen.setDebe(resumen.getDebe().add(monto));
                                break;
                            case DEUDA_PAGADA:
                                resumen.setDeudaPagada(resumen.getDeudaPagada().add(monto));
                                break;
                            case EFECTIVO:
                                resumen.setEfectivo(resumen.getEfectivo().add(monto));
                                break;
                            case MERCADO_PAGO:
                                resumen.setMercadoPago(resumen.getMercadoPago().add(monto));
                                break;
                            case DEBITO:
                                resumen.setDebito(resumen.getDebito().add(monto));
                                break;
                            case CREDITO:
                                resumen.setCredito(resumen.getCredito().add(monto));
                                break;
                        }
                    }

                    return resumen;
                })
                .sorted((r1, r2) -> r1.getFecha().compareTo(r2.getFecha()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> crearMuchasVentas(List<VentaRequest> ventasRequests) {
        List<Venta> ventasCreadas = new ArrayList<>();
        for (VentaRequest ventaRequest : ventasRequests) {
            Venta ventaCreada = crearVenta(ventaRequest);
            ventasCreadas.add(ventaCreada);
        }
        return ventasCreadas;
    }

    private void actualizarPagoAutomatico(Venta venta) {

        if (venta.getEstado() != TipoDePago.DEBE) {
            return;
        }

        Cliente cliente = venta.getCliente();

        if (cliente == null) {
            return;
        }

        // Ahora si empresas y clientes
        if (cliente.getTipoCliente() != TipoCliente.EMPRESA
                && cliente.getTipoCliente() != TipoCliente.CLIENTE) {
            return;
        }

        TipoPeriodicidad periodicidad = cliente.getPeriodicidadPago();
        System.out.println("===========");
        System.out.println("Cliente: " + cliente.getNombre());
        System.out.println("Tipo: " + cliente.getTipoCliente());
        System.out.println("Periodicidad: " + periodicidad);
        System.out.println("===========");

        if (periodicidad == null) {
            return;
        }

        int mes = venta.getFecha().getMonthValue();
        int anio = venta.getFecha().getYear();

        List<PagoEmpresa> pagosExistentes
                = pagoEmpresaRepository.obtenerPagosPorTipoYMes(
                        cliente.getIdCliente(),
                        periodicidad,
                        mes,
                        anio);

        if (!pagosExistentes.isEmpty()) {

            PagoEmpresa pagoExistente = null;

            if (periodicidad == TipoPeriodicidad.SEMANAL) {

                int diaVenta = venta.getFecha().getDayOfMonth();

                for (PagoEmpresa p : pagosExistentes) {

                    int diaPago = p.getFecha().getDayOfMonth();

                    boolean mismaSemana
                            = (diaVenta <= 7 && diaPago <= 7)
                            || (diaVenta >= 8 && diaVenta <= 14
                            && diaPago >= 8 && diaPago <= 14)
                            || (diaVenta >= 15 && diaVenta <= 21
                            && diaPago >= 15 && diaPago <= 21)
                            || (diaVenta >= 22 && diaPago >= 22);

                    if (mismaSemana) {
                        pagoExistente = p;
                        break;
                    }
                }

            } else {

                pagoExistente = pagosExistentes.get(0);
            }

            if (pagoExistente != null) {

                BigDecimal nuevoTotal
                        = calcularTotalPago(
                                cliente,
                                periodicidad,
                                pagoExistente.getFecha());

                pagoExistente.setMonto(nuevoTotal);

                pagoEmpresaRepository.save(pagoExistente);

                return;
            }
        }

        PagoEmpresa nuevoPago = new PagoEmpresa();

        nuevoPago.setEmpresaId(cliente.getIdCliente());
        nuevoPago.setNombre(cliente.getNombre());
        nuevoPago.setTipoPeriodicidad(periodicidad);
        nuevoPago.setFecha(venta.getFecha());
        nuevoPago.setMonto(venta.getMonto());
        nuevoPago.setEstado(EstadoPago.DEBE);

        pagoEmpresaRepository.save(nuevoPago);
        System.out.println("Creando pago automático...");
    }

    private BigDecimal calcularTotalPago(
            Cliente cliente,
            TipoPeriodicidad periodicidad,
            LocalDateTime fecha) {

        BigDecimal total = BigDecimal.ZERO;

        List<Venta> ventas = ventaRepository.findAll();

        for (Venta v : ventas) {

            if (v.getCliente() == null) {
                continue;
            }

            if (!v.getCliente().getIdCliente()
                    .equals(cliente.getIdCliente())) {
                continue;
            }

            if (v.getEstado() != TipoDePago.DEBE
                    && v.getEstado() != TipoDePago.DEUDA_PAGADA) {
                continue;
            }

            if (periodicidad == TipoPeriodicidad.SEMANAL) {

                int diaReferencia = fecha.getDayOfMonth();
                int diaVenta = v.getFecha().getDayOfMonth();

                boolean mismaSemana
                        = (diaReferencia <= 7 && diaVenta <= 7)
                        || (diaReferencia >= 8 && diaReferencia <= 14
                        && diaVenta >= 8 && diaVenta <= 14)
                        || (diaReferencia >= 15 && diaReferencia <= 21
                        && diaVenta >= 15 && diaVenta <= 21)
                        || (diaReferencia >= 22 && diaVenta >= 22);

                if (!mismaSemana) {
                    continue;
                }

            } else {

                if (v.getFecha().getMonthValue()
                        != fecha.getMonthValue()
                        || v.getFecha().getYear()
                        != fecha.getYear()) {
                    continue;
                }
            }

            total = total.add(v.getMonto());
        }

        return total;
    }
}
