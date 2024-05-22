package uk.gov.service.notify;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

class NotifyHttpClient {

    private static final Logger LOGGER = Logger.getLogger(NotifyHttpClient.class.toString());

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

    String performPostRequest(HttpURLConnection conn, JSONObject body, int expectedStatusCode) throws NotificationClientException {
        try{
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), UTF_8);
            wr.write(body.toString());
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

    String performGetRequest(HttpURLConnection conn) throws NotificationClientException {
        try{
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

    byte[] performRawGetRequest(HttpURLConnection conn) throws NotificationClientException {
        byte[] out;
        try{
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

    HttpURLConnection createConnectionAndSetHeaders(String urlString, String method) throws NotificationClientException {
        try
        {
            URL url = new URL(urlString);
            HttpURLConnection conn = getConnection(url);
            conn.setRequestMethod(method);
            Authentication authentication = new Authentication();
            String token = authentication.create(serviceId, apiKey);
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("User-agent", userAgent);
            if (method.equals("POST")) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

            }
            return conn;
        }catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection conn;

        if (null != proxy) {
            conn = (HttpURLConnection) url.openConnection(proxy);
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        return conn;
    }

}
