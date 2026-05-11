package paucar.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.demo.entity.dto.VentaResumenDiarioDTO;

public class ResumenBackend {

    private final String BASE_URL;
    private final HttpClient http;
    private final ObjectMapper json;

    public ResumenBackend(String baseUrl) {
        this.BASE_URL = baseUrl;
        this.http = HttpClient.newHttpClient();
        this.json = new ObjectMapper();
    }

    public List<VentaResumenDiarioDTO> cargarResumenDiario(int anio, int mes) {
        try {
            String url = BASE_URL + "/ventas/resumen-diario" + "?anio=" + anio +
                                    "&mes=" + mes;/*Construye la URL para solicitar el resumen diario de
                                                  ventas según año y mes*/

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET()
                    .build();/* construye una request http get para obtener datos */

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers
                    .ofString());/* envia la request y guarda el codigo de la respuesta en response */

            if (response.statusCode() >= 200 && response.statusCode() < 300) {/*si el codigo es mayor a
                                                                               200 y menor a 300*/
                return json.readValue(response.body(),
                        new TypeReference<List<VentaResumenDiarioDTO>>() {
                        });/*Parsea el JSON recibido del backend y lo transforma
                             en una lista de objetos VentaResumenDiarioDTO*/
            }
            System.err.println("Error resumen-diario: HTTP " + response.statusCode());
        } catch (java.io.IOException e) {
            System.err.println("Error de red al cargar resumen diario");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Carga de resumen diario interrumpida");

        } catch (RuntimeException e) {
            System.err.println("Error procesando el resumen diario (JSON)");
        }

        return List.of();/* Retorna una lista vacía cuando no hay datos para devolver */
    }
}