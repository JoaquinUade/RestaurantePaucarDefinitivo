package paucar.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.demo.entity.Producto;

public class AdminService {

    private final String BASE_URL;
    private final HttpClient http;
    private final ObjectMapper TraductorJSON;

    public AdminService(String baseUrl) {
        this.BASE_URL = baseUrl;
        this.http = HttpClient.newHttpClient();
        this.TraductorJSON = new ObjectMapper();
    }

// ===== ADMIN =====
public List<com.uade.tpo.demo.entity.Producto> obtenerProductosAdmin() {
    try {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/productos"))
                .GET()
                .build();

        var response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return TraductorJSON.readValue(
                    response.body(),
                    TraductorJSON.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    com.uade.tpo.demo.entity.Producto.class
                            )
            );
        }

    } catch (IOException | InterruptedException e) {
        System.err.println("Error admin productos: " + e.getMessage());
    }

    return List.of();
}
public void crearProducto(Producto producto) {
    try {
        String json = TraductorJSON.writeValueAsString(producto);

        var request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/productos"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                .build();

        http.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

    } catch (IOException | InterruptedException e) {
        System.err.println("Error creando producto: " + e.getMessage());
    }
}
public void editarProducto(Producto producto) {
    try {
        String json = TraductorJSON.writeValueAsString(producto);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/productos/" +
                        producto.getIdProducto()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        http.send(request, HttpResponse.BodyHandlers.ofString());

    } catch (IOException | InterruptedException e) {
        System.err.println("Error editando producto: " + e.getMessage());
    }
}
public void eliminarProducto(Long idProducto) {
    try {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/productos/" + idProducto))
                .DELETE()
                .build();

        http.send(request, HttpResponse.BodyHandlers.ofString());

    } catch (IOException | InterruptedException e) {
        System.err.println("Error eliminando producto: " + e.getMessage());
    }
}
}
