package paucar.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.demo.entity.CategoriaGastoVariable;

public class CategoriasGastosService {

    private final String BASE_URL;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public CategoriasGastosService(String baseUrl) {
        this.BASE_URL = baseUrl + "/categorias-gasto-variable";
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    // ===== OBTENER TODAS =====
    public List<CategoriaGastoVariable> obtenerCategorias() {
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
                                .constructCollectionType(
                                        List.class,
                                        CategoriaGastoVariable.class
                                )
                );
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error obteniendo categorias: " + e.getMessage());
        }

        return List.of();
    }

    // ===== CREAR =====
    public void crearCategoria(CategoriaGastoVariable categoria) {
        try {
            String json = mapper.writeValueAsString(categoria);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error creando categoria: " + e.getMessage());
        }
    }

    // ===== EDITAR =====
    public void editarCategoria(CategoriaGastoVariable categoria) {
        try {
            String json = mapper.writeValueAsString(categoria);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + categoria.getIdCategoria()))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error editando categoria: " + e.getMessage());
        }
    }

    // ===== ELIMINAR =====
    public void eliminarCategoria(Long id) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            http.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (IOException | InterruptedException e) {
            System.err.println("Error eliminando categoria: " + e.getMessage());
        }
    }
}