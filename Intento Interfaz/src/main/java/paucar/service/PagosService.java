package paucar.service;
import paucar.config.HttpCompartido;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uade.tpo.demo.entity.PagoEmpresa;

/**
 * Cliente HTTP del backend de pagos a empresas.
 *
 * OJO: el controlador PagoEmpresaController del backend está mapeado en
 * {@code /pagos-empresa} (SIN el prefijo /api que usan los demás endpoints),
 * por eso aquí se quita el "/api" del baseUrl para llegar al endpoint real.
 */
public class PagosService {

    private final String BASE_URL;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public PagosService(String apiBase) {

        this.BASE_URL = apiBase.replace("/api", "") + "/pagos-empresa";

        this.http = HttpCompartido.getHttpClient();

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // La entidad expone getters derivados (mes/año) que el backend manda de
        // vuelta y no tienen setter; los ignoramos al leer para no fallar.
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    // OBTENER TODOS
    public List<PagoEmpresa> obtenerTodos() {

        try {

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            var response = http.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                return mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory()
                                .constructCollectionType(
                                        List.class,
                                        PagoEmpresa.class));
            }

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error obteniendo pagos: " + e.getMessage());
        }

        return List.of();
    }

    // CREAR
    public void crear(PagoEmpresa pago) {

        try {

            String json = mapper.writeValueAsString(pago);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error creando pago: " + e.getMessage());
        }
    }

    // MODIFICAR (PUT completo)
    public void modificar(Long id, PagoEmpresa pago) {

        try {

            String json = mapper.writeValueAsString(pago);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error modificando pago: " + e.getMessage());
        }
    }

    // ELIMINAR
    public void eliminar(Long id) {

        try {

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error eliminando pago: " + e.getMessage());
        }
    }
}
