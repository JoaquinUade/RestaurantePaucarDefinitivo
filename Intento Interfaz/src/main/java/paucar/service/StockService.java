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
import com.uade.tpo.demo.entity.Stock;
import com.uade.tpo.demo.entity.dto.StockRequest;

public class StockService {

    private final String BASE_URL;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public StockService(String baseUrl) {

        this.BASE_URL = baseUrl + "/stock";

        this.http = HttpClient.newHttpClient();

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // OBTENER TODOS

    public List<Stock> obtenerTodos() {

        try {

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            var response =
                    http.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );
System.out.println("URL: " + BASE_URL);
System.out.println("Status: " + response.statusCode());
System.out.println("Body: " + response.body());
            if (response.statusCode() >= 200 &&
                response.statusCode() < 300) {

                return mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory()
                                .constructCollectionType(
                                        List.class,
                                        Stock.class
                                ));
            }

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error obteniendo stock: "
                    + e.getMessage()
            );
        }

        return List.of();
    }

    // OBTENER POR ID

    public Stock obtenerPorId(Long id) {

        try {

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET()
                    .build();

            var response =
                    http.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() >= 200 &&
                response.statusCode() < 300) {

                return mapper.readValue(
                        response.body(),
                        Stock.class
                );
            }

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error obteniendo stock: "
                    + e.getMessage()
            );
        }

        return null;
    }

    // CREAR

    public void crear(StockRequest stock) {

        try {

            String json =
                    mapper.writeValueAsString(stock);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers
                                    .ofString(json)
                    )
                    .build();

            http.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error creando stock: "
                    + e.getMessage()
            );
        }
    }

    // EDITAR

    public void editar(
            Long id,
            Stock stock) {

        try {

            String json =
                    mapper.writeValueAsString(stock);

            var request = HttpRequest.newBuilder()
                    .uri(
                            URI.create(
                                    BASE_URL + "/" + id
                            )
                    )
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .PUT(
                            HttpRequest.BodyPublishers
                                    .ofString(json)
                    )
                    .build();

            http.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error editando stock: "
                    + e.getMessage()
            );
        }
    }

    // ELIMINAR

    public void eliminar(Long id) {

        try {

            var request = HttpRequest.newBuilder()
                    .uri(
                            URI.create(
                                    BASE_URL + "/" + id
                            )
                    )
                    .DELETE()
                    .build();

            http.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error eliminando stock: "
                    + e.getMessage()
            );
        }
    }

    // PRODUCTOS CON FALTA DE STOCK

    public List<Stock> obtenerFaltantes() {

        try {

            var request = HttpRequest.newBuilder()
                    .uri(
                            URI.create(
                                    BASE_URL + "/faltante"
                            )
                    )
                    .GET()
                    .build();

            var response =
                    http.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() >= 200 &&
                response.statusCode() < 300) {

                return mapper.readValue(
                        response.body(),
                        mapper.getTypeFactory()
                                .constructCollectionType(
                                        List.class,
                                        Stock.class
                                ));
            }

        } catch (IOException | InterruptedException e) {

            System.err.println(
                    "Error obteniendo faltantes: "
                    + e.getMessage()
            );
        }

        return List.of();
    }
}