package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.FuenteTasa;
import model.TasaCambio;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Entrega la tasa USD -> COP que usan todos los precios en pesos.
 *
 * API externa: ExchangeRate-API, endpoint abierto (sin clave, se actualiza una vez al día):
 * https://open.er-api.com/v6/latest/USD   (datos de https://www.exchangerate-api.com)
 *
 * Si la API no responde a tiempo o devuelve algo inválido, se usa la tasa por defecto
 * (variable de entorno TASA_DEFECTO) y se avisa por consola.
 *
 * Variables de entorno (todas opcionales):
 *   TASA_DEFECTO     tasa de respaldo (por omisión 4000)
 *   TASA_TIMEOUT_MS  tiempo máximo de espera de la API en milisegundos (por omisión 3000)
 *   TASA_API_URL     dirección de la API (por omisión la de arriba; útil para pruebas)
 */
public class TasaService {
    private static final String URL_POR_OMISION = "https://open.er-api.com/v6/latest/USD";
    private static final double TASA_DEFECTO_POR_OMISION = 4000.0;
    private static final long TIMEOUT_POR_OMISION_MS = 3000;
    // La API solo cambia una vez al día y limita el abuso: se reutiliza la última tasa unos minutos.
    private static final Duration VIGENCIA = Duration.ofMinutes(5);
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final HttpClient cliente;
    private final URI url;
    private final long timeoutMs;

    private TasaCambio ultimaTasaApi;
    private Instant ultimaConsulta;

    public TasaService() {
        this(entorno("TASA_API_URL", URL_POR_OMISION), leerTimeout());
    }

    TasaService(String url, long timeoutMs) {
        this.url = URI.create(url);
        this.timeoutMs = timeoutMs;
        this.cliente = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(timeoutMs)).build();
    }

    public synchronized TasaCambio obtenerTasa() {
        if (ultimaTasaApi != null && Duration.between(ultimaConsulta, Instant.now()).compareTo(VIGENCIA) < 0) {
            return ultimaTasaApi;
        }
        try {
            double valor = consultarApi();
            ultimaTasaApi = new TasaCambio(valor, FuenteTasa.API, LocalDateTime.now(ZONA));
            ultimaConsulta = Instant.now();
            return ultimaTasaApi;
        } catch (Exception e) {
            double porDefecto = leerTasaPorDefecto();
            System.out.println("La API no respondió, por lo que se toma un valor por defecto de " + formatear(porDefecto));
            return new TasaCambio(porDefecto, FuenteTasa.DEFECTO, LocalDateTime.now(ZONA));
        }
    }

    /** Devuelve cuántos COP vale 1 USD. Lanza una excepción ante cualquier fallo. */
    private double consultarApi() throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(url)
                .header("Accept", "application/json")
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();

        // get(timeout) limita el tiempo TOTAL (conexión + respuesta completa), no solo el de las cabeceras.
        CompletableFuture<HttpResponse<String>> futuro = cliente.sendAsync(peticion, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> respuesta;
        try {
            respuesta = futuro.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            futuro.cancel(true);
            throw e;
        }

        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("La API respondió con código " + respuesta.statusCode());
        }
        JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
        if (!"success".equals(json.get("result").getAsString())) {
            throw new IllegalStateException("La API no devolvió un resultado exitoso");
        }
        double cop = json.getAsJsonObject("rates").get("COP").getAsDouble();
        if (!Double.isFinite(cop) || cop <= 0) {
            throw new IllegalStateException("La API devolvió una tasa inválida");
        }
        return cop;
    }

    private static double leerTasaPorDefecto() {
        try {
            double valor = Double.parseDouble(entorno("TASA_DEFECTO", String.valueOf(TASA_DEFECTO_POR_OMISION)));
            return (Double.isFinite(valor) && valor > 0) ? valor : TASA_DEFECTO_POR_OMISION;
        } catch (NumberFormatException e) {
            return TASA_DEFECTO_POR_OMISION;
        }
    }

    private static long leerTimeout() {
        try {
            long valor = Long.parseLong(entorno("TASA_TIMEOUT_MS", String.valueOf(TIMEOUT_POR_OMISION_MS)));
            return valor > 0 ? valor : TIMEOUT_POR_OMISION_MS;
        } catch (NumberFormatException e) {
            return TIMEOUT_POR_OMISION_MS;
        }
    }

    private static String entorno(String nombre, String porOmision) {
        String valor = System.getenv(nombre);
        return (valor == null || valor.isBlank()) ? porOmision : valor.trim();
    }

    /** 4000.0 se muestra como 4000; 4123.55 se queda igual. */
    private static String formatear(double valor) {
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
    }
}
