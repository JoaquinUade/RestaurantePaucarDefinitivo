package paucar.service;

/*define el contenedor que agrupa clases, interfaces y subpaquetes relacionados */
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.demo.entity.TipoCliente;
import com.uade.tpo.demo.entity.TipoDePago;
import com.uade.tpo.demo.entity.dto.VentaRequest;

public class VentasBackend {

    private final String BASE_URL;/*Variableque contiene la URL base del backend (por ejemplo "http://localhost:8080/api"). */

    private final HttpClient http;/*protocolo de transferencia de hipertexto (Hypertext Transfer Protocol)
                                  para enviar y recibir datos Se utiliza principalmente a través de la API
                                  HttpClient para realizar peticiones GET, POST, PUT y DELETE*/

    private final ObjectMapper TraductorJSON;/*ObjectMapper es una clase de la librería Jackson que sirve para convertir
                                  datos JSON en objetos Java y viceversa*/

    private final ClientesService clientesService;
    private final VentaRequest venta;

    // --- Constructor ---
    public VentasBackend(String BASE_URL, ClientesService clientesService, VentaRequest venta) {/*Recibe un parámetro llamado BASE_URL (un String) que debería ser
                                            la URL base del backend */

        this.BASE_URL = Objects.requireNonNull(BASE_URL);/*si el parámetro es null, lanza un NullPointerException
                                                          (excepción en tiempo de ejecución (RuntimeException)
                                                          que ocurre cuando el programa intenta utilizar una
                                                          referencia de objeto que apunta a null)) inmediatamente.
                                                          Esto evita crear una instancia mal configurada */

        this.http = HttpClient.newHttpClient();/*Crea una instancia por defecto de HttpClient (Java 11+), que
                                               vas a usar para hacer GET/POST al backend */

        this.TraductorJSON = new ObjectMapper();/*El campo traductorJson ahora va a contener un ObjectMapper nuevo */

        this.clientesService = Objects.requireNonNull(clientesService);/*valida que el servicio de clientes no sea
                                                                       null, sino lanza una excepción inmediatamente */

        this.venta = Objects.requireNonNull(venta, "venta (VentaRequest) no puede ser null");

        // defensivo: asegurar listas no nulas
        if (this.venta.getIdProductos() == null) {
            this.venta.setIdProductos(new ArrayList<>());
        }
        if (this.venta.getCantidades() == null) {
            this.venta.setCantidades(new ArrayList<>());
        }

    }

    // ============================================================
    //                    VENTAS
    // ============================================================
    public boolean GuardarPedidos(
            Long idCliente,
            List<Long> idProductos,
            List<Integer> cantidades,
            TipoDePago estado,
            String observaciones) {

        try {
            var estadoEfectivo = (estado == null ? TipoDePago.DEBE : estado);
            var obsSeguras = (observaciones == null ? "" : observaciones);

            // *** USAR el VentaRequest inyectado ***
            venta.setIdCliente(idCliente);
            venta.setEstado(estadoEfectivo);
            venta.setObservaciones(obsSeguras);

            // reemplazar contenidos manteniendo alineación
            venta.getIdProductos().clear();
            venta.getCantidades().clear();
            if (idProductos != null && cantidades != null) {
                int n = Math.min(idProductos.size(), cantidades.size());
                for (int i = 0; i < n; i++) {
                    Long idP = idProductos.get(i);
                    Integer cant = cantidades.get(i);
                    if (idP != null && cant != null && cant > 0) {
                        venta.getIdProductos().add(idP);
                        venta.getCantidades().add(cant);
                    }
                }
            }

            System.out.println("DEBUG idProductos=" + venta.getIdProductos());
            System.out.println("DEBUG cantidades=" + venta.getCantidades());
            System.out.println("DEBUG idCliente=" + venta.getIdCliente());
            System.out.println("DEBUG estado=" + venta.getEstado());
            System.out.println("DEBUG observaciones=" + venta.getObservaciones());

            // Serializar el MISMO VentaRequest compartido
            String body = TraductorJSON.writeValueAsString(venta);

            var solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ventas"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = http.send(solicitud, HttpResponse.BodyHandlers.ofString());

            System.out.println("[POST /ventas] status=" + response.statusCode());
            System.out.println("[POST /ventas] body=" + response.body());

            return response.statusCode() >= 200 && response.statusCode() < 300;

        } catch (java.io.IOException e) {
            System.err.println("guardarVentaCliente (IO): " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("guardarVentaCliente (Interrupted): " + e.getMessage());
            return false;
        }
    }

    public boolean GuardarPedidoMesas(
            String nombreMesa,
            List<Long> idProductos,
            List<Integer> cantidades,
            TipoDePago estado,
            String observaciones) {

        if (idProductos == null || cantidades == null || idProductos.isEmpty() || cantidades.isEmpty()) {
            System.err.println("Pedido (MESA) inválido por carecer de cantidad o de productos válidos");
            return false;
        }
        if (nombreMesa == null || nombreMesa.isBlank()) {
            System.err.println("Pedido (MESA) inválido: nombreMesa vacío");
            return false;
        }

        // Resolver ID de mesa usando el ClientesService INYECTADO
        Long idMesa = clientesService.obtenerClienteIdPorNombre(nombreMesa, TipoCliente.MESA);
        if (idMesa == null) {
            System.err.println("Mesa no encontrada: " + nombreMesa);
            return false;
        }
        return GuardarPedidos(idMesa, idProductos, cantidades, estado, observaciones);
    }

    public List<java.util.Map<String, Object>> cargarVentasDelDia(LocalDate fecha) {
        try {
            var solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(
                            BASE_URL + "/ventas/filtro/anio-mes-dia"
                            + "?anio=" + fecha.getYear()
                            + "&mes=" + fecha.getMonthValue()
                            + "&dia=" + fecha.getDayOfMonth()
                    ))
                    .GET()
                    .build();

            var response = http.send(solicitud, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {/*Verifica que la respuesta
                                                                             del servidor haya sido exitosa
                                                                             (códigos HTTP 200–299)*/

                var array = TraductorJSON.readTree(response.body());/*Toma el texto que vino del servidor
                                                                   (normalmente  string JSON) y lo
                                                                   convierte en un objeto java (un árbol
                                                                   JSON) que se puede leer por campos*/

                var out = new ArrayList<java.util.Map<String, Object>>();/*Lista donde se van a guardar
                                                                         los datos de las ventas obtenidas
                                                                         de forma ordenada */

                if (array.isArray()) {/*Si lo que me devolvió el backend es una lista de elementos,
                                      entonces vamos a recorrerla uno por uno */

                    for (var n : array) {/*Recorre cada elemento del array JSON recibido */
                        String nombre = "";

                        if (n.hasNonNull("cliente") && n.get("cliente").isObject()) {/*Verifica que exista el campo "cliente" y que además sea un objeto JSON válido*/
                            var cli = n.get("cliente");/*guarda en la variable cli el objeto JSON
                                                                  cliente de la venta */

                            if (cli.hasNonNull("nombre") && cli.hasNonNull("tipoCliente")) {/*si cli tiene un campo llamado nombre
                                                                                                                  valido y ademas un campo llamado
                                                                                                                  tipocliente */
                                String base = cli.get("nombre").asText().trim();/*toma el valor del campo nombre del cliente, lo
                                                                                           convierte a texto y le quita los espacios de
                                                                                           adelante y de atrás*/
                                TipoCliente tipo = TipoCliente.valueOf(cli.get("tipoCliente").asText());/*convierte el texto del campo tipoCliente
                                                                                                                  del JSON en un valor del enum TipoCliente*/
                                switch (tipo) {
                                    case EMPRESA ->
                                        nombre = base;
                                    case CLIENTE ->
                                        nombre = base;
                                    case MESA ->
                                        nombre = base;
                                }
                            }
                        }
                        var desc = n.hasNonNull("descripcion") ? n.get("descripcion").asText() : "";/*Obtiene la descripción de la venta desde el JSON
                                                                                                                         si existe, y si no asigna una cadena vacía para
                                                                                                                         evitar valores null */
                        var obs = n.hasNonNull("observaciones") ? n.get("observaciones").asText() : "";/*Obtiene las observaciones de la venta desde el JSON
                                                                                                                         si existe, y si no asigna una cadena vacía para
                                                                                                                         evitar valores null */
                        BigDecimal monto = BigDecimal.ZERO;/*le asigna el valor cero a monto */
                        if (n.hasNonNull("monto")) {/*si el JSON tiene un campo llamado monto */
                            monto = new BigDecimal(n.get("monto").asText())
                                    .setScale(2, RoundingMode.HALF_UP);/*convierte el valor del campo monto del JSON a un BigDecimal
                                                                                 con 2 decimales, redondeando hacia arriba si es necesario */
                        }
                        TipoDePago estado = TipoDePago.EFECTIVO;/*asigna el valor EFECTIVO al campo estado */

                        if (n.hasNonNull("estado")) {/*si el JSON tiene un campo llamado estado */
                            estado = TipoDePago.valueOf(n.get("estado").asText());/*convierte el valor del campo estado del JSON
                                                                                                 en un valor del enum TipoDePago*/
                        }
                        Long idCliente = null;/*inicializa la variable idCliente con null, que se usará para almacenar el ID
                                              del cliente asociado a la venta */


                        if (n.hasNonNull("cliente")/* si el JSON tiene un campo cliente */
                                && n.get("cliente").isObject()/*y ademas ese campo cliente es un objeto JSON valido */
                                && n.get("cliente").hasNonNull("idCliente")) {/*y ademas ese objeto cliente tiene un campo idCliente no nulo entra*/

                            idCliente = n.get("cliente").get("idCliente").asLong();/*convierte el valor del campo idCliente que esta dentro del objeto
                                                                                                        cliente del JSON a un Long y lo asigna a la variable idCliente*/
                        }
                        TipoCliente tipoCli = null;/*inicializa la variable tipoCli con null, que se usará para almacenar el tipo de cliente */

                        if (n.hasNonNull("cliente")/* si el JSON tiene un campo cliente */
                                && n.get("cliente").isObject()/*y ademas ese campo cliente es un objeto JSON valido */
                                && n.get("cliente").hasNonNull("tipoCliente")) {/*y ademas ese objeto cliente tiene un campo tipoCliente no nulo, entra*/

                            tipoCli = TipoCliente.valueOf(n.get("cliente").get("tipoCliente").asText());/*convierte el valor del campo tipoCliente del JSON
                                                                                                     en un valor del enum TipoCliente*/
                        }
                        Long idVenta = null;/*inicializa la variable idVenta con null, que se usará para almacenar el ID de la venta */
                        if (n.hasNonNull("idVenta")) {/* si el JSON tiene un campo llamado idVenta */

                            idVenta = n.get("idVenta").asLong();/*convierte el valor del campo idVenta del JSON a un Long y lo asigna a la variable idVenta*/
                        }

                        var fila = new java.util.HashMap<String, Object>();/*inicializa la variable fila con un nuevo HashMap */
                        fila.put("nombre", nombre);
                        fila.put("descripcion", desc);
                        fila.put("monto", monto);
                        fila.put("estado", estado);
                        fila.put("observaciones", obs);
                        fila.put("idCliente", idCliente);
                        fila.put("tipoCliente", tipoCli);
                        fila.put("idVenta", idVenta);
                        out.add(fila);/*agrega la fila a la lista de ventas */
                    }
                }
                return out;/*devuelve la lista de filas de ventas obtenidas del JSON, donde cada fila es un mapa con los
                            datos de la venta*/
            }

        } catch (java.io.IOException | InterruptedException e) {
            System.err.println("Error recargar ventas: " + e.getMessage());
        }
        return List.of();/*devuelve una lista vacía en caso de error */
    }

    // =====================
// LISTAR CLIENTES POR TIPO (EMPRESA/CLIENTE/MESA)
// =====================
// NUEVO
    public java.util.List<String> obtenerClientesPorTipo(TipoCliente tipoBuscado) {
        try {
            var solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/clientes"))
                    .GET()
                    .build();

            var response = http.send(solicitud, HttpResponse.BodyHandlers.ofString());/*envia la solicitud
                                                                                      y guarda la response */
            if (response.statusCode() >= 200 && response.statusCode() < 300) {/*si el codigo de la response
                                                                              esta entre 200 y 299*/
                var json = TraductorJSON.readTree(response.body());
                var out = new java.util.ArrayList<String>();
                if (json.isArray()) {
                    for (var n : json) {
                        String nombre = n.hasNonNull("nombre") ? n.get("nombre").asText() : null;
                        String tipo = n.hasNonNull("tipoCliente") ? n.get("tipoCliente").asText() : null;
                        if (nombre != null && !nombre.isBlank() && tipo != null) {
                            if (tipo.equalsIgnoreCase(tipoBuscado.name())) {
                                out.add(nombre.trim());
                            }
                        }
                    }
                }
                return out.stream()
                        .distinct()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(java.util.stream.Collectors.toList());
            }
        } catch (java.io.IOException | InterruptedException e) {
            System.err.println("obtenerClientesPorTipo: " + e.getMessage());
        }
        return java.util.List.of();
    }

    public boolean eliminarVenta(Long idVenta) {
        try {
            var solicitud = java.net.http.HttpRequest.newBuilder()/*Crea una nueva solicitud HTTP usando
                                                                el builder*/
                    .uri(java.net.URI.create(BASE_URL + "/ventas/" + idVenta))/*Especifica la URI de
                                                                              destino para la solicitud*/
                    .DELETE()/*Especifica que es una solicitud de eliminación*/
                    .build();

            var response = http.send(solicitud,/*Envía la solicitud HTTP y espera la respuesta del
                                                servidor, especifica que el cuerpo de la respuesta
                                                sea un string*/
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 204;/*Si el código de estado de la respuesta es 204
                                                (No Content), significa que la eliminación fue exitosa,
                                                sino devuelve false*/

        } catch (java.io.IOException e) {
            System.err.println("Error recargar ventas (IO): " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error recargar ventas (Interrupted): " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarEstadoVenta(Long idVenta, TipoDePago estado) {
        try {
            var bodyMap = new java.util.HashMap<String, Object>();/*crea un nuevo HashMap llamado bodyMap que se usará para construir el cuerpo de la solicitud PATCH*/
            bodyMap.put("estado", estado);/*Guarda en el mapa bodyMap el valor de la variable estado usando "estado" como nombre del dato */

            String bodyJson = TraductorJSON.writeValueAsString(bodyMap);/*Convierte el mapa bodyMap a una cadena JSON y lo guarda en la variable bodyJson*/

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/ventas/" + idVenta))
                    .header("Content-Type", "application/json")/*Especifica el tipo de contenido de la solicitud como JSON*/
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(bodyJson))/*Especifica el método de la solicitud como PATCH(osea para
                                                                                       modificar solo una parte de los datos que ya existen) y el
                                                                                       cuerpo de la solicitud como una cadena JSON*/
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());/*Envía la solicitud HTTP y espera la respuesta del servidor,
                                                                                especifica que el cuerpo de la respuesta sea un string*/

            return response.statusCode() >= 200 && response.statusCode() < 300;/*Si el código de estado de la respuesta está en el rango de 200 a 
                                                                            299, significa que la actualización fue exitosa, sino devuelve false*/


        } catch (java.io.IOException | InterruptedException e) {
            System.err.println("Error actualizando estado de venta: " + e.getMessage());
            return false;
        }
    }
}
