package uk.gov.service.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

class NotifyHttpClient {

    private static final Logger LOGGER = Logger.getLogger(NotifyHttpClient.class.toString());

    private static final ObjectMapper objectMapper = new ObjectMapper()
            // the following two lines are needed to (de)serialize ZonedDateTime in ISO8601 format
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private final String bearerToken;
    private final String userAgent;
    private final HttpClient httpClient;

    NotifyHttpClient(String serviceId, String apiKey, String userAgent, Proxy proxy, SSLContext sslContext) {
        this.bearerToken = Authentication.create(serviceId, apiKey);
        this.userAgent = userAgent;
        this.httpClient = HttpClient.newBuilder()
                .proxy(Objects.nonNull(proxy)?ProxySelector.of((InetSocketAddress)proxy.address()):ProxySelector.getDefault())
                .build();
        if (Objects.nonNull(sslContext)) {
            setCustomSSLContext(sslContext);
        } else {
            try {
                setDefaultSSLContext();
            } catch (NoSuchAlgorithmException e) {
                LOGGER.log(Level.SEVERE, e.toString(), e);
            }
        }
    }

    <T> T post(URI uri, Object requestBody, Class<T> responseClass, int expectedResponseCode) throws NotificationClientException {
        final String requestBodyString;
        try {
            requestBodyString = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(1))
                .setHeader("Authorization", "Bearer " + bearerToken)
                .setHeader("User-agent", userAgent)
                .setHeader("Accept", "application/json")
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build();

        try(InputStream responseBody = sendHttpRequest(httpRequest, expectedResponseCode);) {
            return objectMapper.readValue(responseBody, responseClass);
        } catch (IOException e) {
            throw new NotificationClientException(e);
        }
    }

    private InputStream sendHttpRequest(HttpRequest httpRequest, int expectedResponseCode) throws NotificationClientException {
        final HttpResponse<InputStream> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(httpResponse.statusCode() != expectedResponseCode) {
            throw new NotificationClientHttpException(httpResponse.statusCode(), "unexpected response code, expected "+expectedResponseCode);
        }

        return httpResponse.body();
    }

    <T> T get(URI uri, Class<T> responseClass) throws NotificationClientException {
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(1))
                .setHeader("Authorization", "Bearer " + bearerToken)
                .setHeader("User-agent", userAgent)
                .setHeader("Accept", "application/json")
                .GET()
                .build();

        try(InputStream responseBody = sendHttpRequest(httpRequest, 200);) {
            return objectMapper.readValue(responseBody, responseClass);
        } catch (IOException e) {
            throw new NotificationClientException(e);
        }
    }

    byte[] get(URI uri) throws NotificationClientException {
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(1))
                .setHeader("Authorization", "Bearer " + bearerToken)
                .setHeader("User-agent", userAgent)
                .GET()
                .build();

        try (InputStream responseBody = sendHttpRequest(httpRequest, 200)) {
            return responseBody.readAllBytes();
        } catch (IOException e) {
            throw new NotificationClientException(e);
        }
    }

    /**
     * Set default SSL context for HTTPS connections.
     * <p/>
     * This is necessary when client has to use keystore
     * (eg provide certification for client authentication).
     * <p/>
     * Use case: enterprise proxy requiring HTTPS client authentication
     */
    private static void setDefaultSSLContext() throws NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
    }

    private static void setCustomSSLContext(final SSLContext sslContext) {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

}
