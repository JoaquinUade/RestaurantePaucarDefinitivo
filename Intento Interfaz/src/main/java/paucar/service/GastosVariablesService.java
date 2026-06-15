package paucar.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.demo.entity.GastoVariableRequest;
import com.uade.tpo.demo.entity.GastosVariables;

public class GastosVariablesService {

    private final String BASE_URL;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public GastosVariablesService(String baseUrl) {
        this.BASE_URL = baseUrl + "/gastos-variables";
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    // ✅ OBTENER TODOS
    public List<GastosVariables> obtenerTodos() {
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
                                .constructCollectionType(List.class, GastosVariables.class)
                );
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error obteniendo gastos: " + e.getMessage());
        }

        return List.of();
    }

    // ✅ CREAR
    public void crear(GastoVariableRequest gasto) {
        try {
            String json = mapper.writeValueAsString(gasto);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error creando gasto: " + e.getMessage());
        }
    }

    // ✅ ELIMINAR (opcional por ahora)
    public void eliminar(Long id) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error eliminando gasto: " + e.getMessage());
        }
    }
}