package uk.gov.service.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

class NotifyHttpClient {

    private static final Logger LOGGER = Logger.getLogger(NotifyHttpClient.class.toString());

    private static final ObjectMapper objectMapper = new ObjectMapper()
            // the following two lines are needed to (de)serialize ZonedDateTime in ISO8601 format
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private final String serviceId;
    private final String apiKey;
    private final String userAgent;
    private final Proxy proxy;

    NotifyHttpClient(String serviceId, String apiKey, String userAgent, Proxy proxy, SSLContext sslContext) {
        this.serviceId = serviceId;
        this.apiKey = apiKey;
        this.userAgent = userAgent;
        this.proxy = null == proxy ? Proxy.NO_PROXY : proxy;
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

    private String performPostRequest(HttpURLConnection conn, Object requestBody, int expectedStatusCode) throws NotificationClientException {
        try {
            objectMapper.writeValue(conn.getOutputStream(), requestBody);

            final int httpResponseCode = conn.getResponseCode();
            if (httpResponseCode == expectedStatusCode) {
                return NotifyUtils.readStream(conn.getInputStream());
            } else {
                throw new NotificationClientHttpException(httpResponseCode, NotifyUtils.readStream(conn.getErrorStream()));
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String performGetRequest(HttpURLConnection conn) throws NotificationClientException {
        try {
            final int httpResponseCode = conn.getResponseCode();
            if (200 == httpResponseCode) {
                return NotifyUtils.readStream(conn.getInputStream());
            } else {
                throw new NotificationClientHttpException(httpResponseCode, NotifyUtils.readStream(conn.getErrorStream()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private byte[] performRawGetRequest(HttpURLConnection conn) throws NotificationClientException {
        final byte[] out;
        try {
            final int httpResponseCode = conn.getResponseCode();
            if (200 == httpResponseCode) {
                out = conn.getInputStream().readAllBytes();
            } else {
                throw new NotificationClientHttpException(httpResponseCode, NotifyUtils.readStream(conn.getErrorStream()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return out;
    }

    private HttpURLConnection createConnectionAndSetHeaders(URI uri, String method) throws NotificationClientException {
        try {
            final HttpURLConnection conn = (HttpURLConnection)uri.toURL().openConnection(proxy);
            conn.setRequestMethod(method);
            final String token = Authentication.create(serviceId, apiKey);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("User-agent", userAgent);
            if (method.equals("POST")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
            }
            return conn;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    <T> T post(URI uri, Object requestBody, Class<T> responseClass, int expectedResponseCode) throws NotificationClientException {
        final HttpURLConnection conn = createConnectionAndSetHeaders(uri, "POST");
        final String responseBody = performPostRequest(conn, requestBody, expectedResponseCode);
        try {
            return objectMapper.readValue(responseBody, responseClass);
        } catch (JsonProcessingException e) {
            throw new NotificationClientException(e);
        }
    }

    <T> T get(URI uri, Class<T> responseClass) throws NotificationClientException {
        final HttpURLConnection conn = createConnectionAndSetHeaders(uri, "GET");
        final String responseBody = performGetRequest(conn);
        try {
            return objectMapper.readValue(responseBody, responseClass);
        } catch (JsonProcessingException e) {
            throw new NotificationClientException(e);
        }
    }

    byte[] get(URI uri) throws NotificationClientException {
        final HttpURLConnection conn = createConnectionAndSetHeaders(uri, "GET");
        return performRawGetRequest(conn);
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
