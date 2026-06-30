package paucar.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uade.tpo.demo.entity.GastosIndividuales;
import com.uade.tpo.demo.entity.dto.GastoIndividualRequest;

public class GastosIndividualesService {

    private final String BASE_URL;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public GastosIndividualesService(String baseUrl) {
        this.BASE_URL = baseUrl + "/gastos-individuales";
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ✅ OBTENER TODOS
    public List<GastosIndividuales> obtenerTodos() {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory()
                                .constructCollectionType(List.class, GastosIndividuales.class));
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error obteniendo gastos individuales: " + e.getMessage());
        }

        return List.of();
    }

    // ✅ CREAR
    public void crear(GastoIndividualRequest gasto) {
        try {
            String json = mapper.writeValueAsString(gasto);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error creando gasto individual: " + e.getMessage());
        }
    }

    // ✅ EDITAR
    public void editar(Long id, GastoIndividualRequest gasto) {
        try {
            String json = mapper.writeValueAsString(gasto);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error editando gasto individual: " + e.getMessage());
        }
    }

    // ✅ ELIMINAR
    public void eliminar(Long id) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error eliminando gasto individual: " + e.getMessage());
        }
    }
}
