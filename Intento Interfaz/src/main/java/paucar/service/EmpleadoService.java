package paucar.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.demo.entity.Empleado;

public class EmpleadoService {
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String BASE_URL;

    public EmpleadoService(String baseUrl) {
        this.BASE_URL = baseUrl + "/empleados";
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    // ===== OBTENER TODOS LOS EMPLEADOS =====
    public List<Empleado> obtenerTodosLosEmpleados() {
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
                                .constructCollectionType(List.class, Empleado.class)
                );
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error obteniendo empleados: " + e.getMessage());
        }
        return List.of();
    }

    // ===== CREAR EMPLEADO =====
    public Empleado crearEmpleado(Empleado empleado){
    try {
        String json = mapper.writeValueAsString(empleado);

        var requestCrear = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        var response = http.send(requestCrear, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return mapper.readValue(response.body(), Empleado.class);
        } else {
            System.err.println("Error HTTP al crear empleado: " + response.statusCode());
        }

    } catch (IOException | InterruptedException e) {
        System.err.println("Error creando empleado: " + e.getMessage());
    }

    return null;
}

    // ===== EDITAR EMPLEADO =====
    public void editarEmpleado(Long id, Empleado empleado) {
        try {
            String json = mapper.writeValueAsString(empleado);
            var requestMod = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();

            http.send(requestMod, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Error modificando empleado: " + e.getMessage());
        }
    }

    // ===== ELIMINAR EMPLEADO =====
    public void eliminarEmpleado(Long id) {
        try {
            var requestEliminar = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            http.send(requestEliminar, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Error eliminando empleado: " + e.getMessage());
        }
    }

    // ===== OBTENER EMPLEADO POR ID =====
    public Empleado obtenerEmpleadoPorId(Long id) {
        try {
            var requestGet = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET()
                    .build();

            var responseGet = http.send(requestGet, HttpResponse.BodyHandlers.ofString());
            if (responseGet.statusCode() == 200) {
                String jsonBody = responseGet.body();
                return mapper.readValue(jsonBody, Empleado.class);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error obteniendo empleado: " + e.getMessage());
        }
        return null;
    }
}
