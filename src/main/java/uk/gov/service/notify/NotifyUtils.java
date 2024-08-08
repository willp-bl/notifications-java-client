package uk.gov.service.notify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

class NotifyUtils {

    private NotifyUtils() {}

    static String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        return new String(inputStream.readAllBytes(), UTF_8);
    }

    static String extractServiceId(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 73), Math.max(0, apiKey.length() - 37));
    }

    static String extractApiKey(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 36));
    }

    static String getVersion() {
        final Properties prop = new Properties();
        try(InputStream input = NotifyUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            throw new RuntimeException(new NotificationClientException(ex));
        }
        return prop.getProperty("project.version");
    }

}
