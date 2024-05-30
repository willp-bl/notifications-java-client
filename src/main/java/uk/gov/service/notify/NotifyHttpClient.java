package uk.gov.service.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    NotifyHttpClient(String serviceId, String apiKey, String userAgent, Proxy proxy) {
        this.serviceId = serviceId;
        this.apiKey = apiKey;
        this.userAgent = userAgent;
        this.proxy = proxy;
    }

    private String performPostRequest(HttpURLConnection conn, Object requestBody, int expectedStatusCode) throws NotificationClientException {
        try {
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), UTF_8);
            wr.write(objectMapper.writeValueAsString(requestBody));
            wr.flush();

            int httpResult = conn.getResponseCode();
            if (httpResult == expectedStatusCode) {
                return NotifyUtils.readStream(conn.getInputStream());
            } else {
                throw new NotificationClientException(httpResult, NotifyUtils.readStream(conn.getErrorStream()));
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
            int httpResult = conn.getResponseCode();
            if (httpResult == 200) {
                return NotifyUtils.readStream(conn.getInputStream());
            } else {
                throw new NotificationClientException(httpResult, NotifyUtils.readStream(conn.getErrorStream()));
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
        byte[] out;
        try {
            int httpResult = conn.getResponseCode();
            if (httpResult == 200) {
                InputStream is = conn.getInputStream();
                out = IOUtils.toByteArray(is);
            } else {
                throw new NotificationClientException(httpResult, NotifyUtils.readStream(conn.getErrorStream()));
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

    private HttpURLConnection createConnectionAndSetHeaders(String urlString, String method) throws NotificationClientException {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = getConnection(url);
            conn.setRequestMethod(method);
            String token = Authentication.create(serviceId, apiKey);
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

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection conn;

        if (null != proxy) {
            conn = (HttpURLConnection) url.openConnection(proxy);
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        return conn;
    }

    <T> T post(URI uri, Object requestBody, Class<T> responseClass, int expectedResponseCode) throws NotificationClientException {
        HttpURLConnection conn = createConnectionAndSetHeaders(uri.toString(), "POST");
        final String responseBody = performPostRequest(conn, requestBody, expectedResponseCode);
        try {
            return objectMapper.readValue(responseBody, responseClass);
        } catch (JsonProcessingException e) {
            throw new NotificationClientException(e);
        }
    }

    <T> T get(URI uri, Class<T> responseClass) throws NotificationClientException {
        HttpURLConnection conn = createConnectionAndSetHeaders(uri.toString(), "GET");
        String responseBody = performGetRequest(conn);
        try {
            return objectMapper.readValue(responseBody, responseClass);
        } catch (JsonProcessingException e) {
            throw new NotificationClientException(e);
        }
    }

    byte[] get(URI uri) throws NotificationClientException {
        HttpURLConnection conn = createConnectionAndSetHeaders(uri.toString(), "GET");
        return performRawGetRequest(conn);
    }

}
