package uk.gov.service.notify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

class NotifyUtils {

    private static final Properties prop = new Properties();

    static {
        try(InputStream input = NotifyUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            throw new RuntimeException(new NotificationClientException(ex));
        }
    }

    private NotifyUtils() {}

    static String extractServiceId(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 73), Math.max(0, apiKey.length() - 37));
    }

    static String extractApiKey(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 36));
    }

    static String getProperty(String propertyName) {
        final String property = prop.getProperty(propertyName);
        if(Objects.isNull(property)) {
            throw new RuntimeException(new NotificationClientException("Property \""+propertyName+"\" was null"));
        }
        return property;
    }
}
