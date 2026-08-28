package paucar.config;

import java.net.http.HttpClient;

/**
 * Instancia única de {@link HttpClient} compartida por todos los servicios
 * del cliente. HttpClient es seguro para usar desde múltiples hilos y permite
 * reutilizar conexiones (keep-alive), por lo que conviene tener una sola en
 * vez de crear una por servicio.
 */
public final class HttpCompartido {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private HttpCompartido() {
    }

    public static HttpClient getHttpClient() {
        return HTTP;
    }
}