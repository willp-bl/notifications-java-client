package uk.gov.service.notify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

class NotifyUtils {

    private static final int LENGTH_UUID = 36;
    private static final int LENGTH_DASH = 1;// "-"

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
        return apiKey.substring(Math.max(0, apiKey.length() - LENGTH_UUID - LENGTH_DASH - LENGTH_UUID),
                Math.max(0, apiKey.length() - LENGTH_UUID - LENGTH_DASH));
    }

    static String extractApiKey(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - LENGTH_UUID));
    }

    static String getProperty(NotificationClientOptions overrides, String propertyName) {
        if(overrides.hasOption(propertyName)) {
            return overrides.get(propertyName);
        } else {
            final String property = prop.getProperty(propertyName);
            if (Objects.isNull(property)) {
                throw new RuntimeException(new NotificationClientException("Property \"" + propertyName + "\" was null"));
            }
            return property;
        }
    }
}
