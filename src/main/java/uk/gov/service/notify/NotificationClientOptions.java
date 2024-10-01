package uk.gov.service.notify;

import java.util.HashMap;
import java.util.Map;

public class NotificationClientOptions {

    public enum Options {
        /**
         * Value is in the ISO-8601 duration format
         */
        HTTP_TIMEOUT_CONNECT("http.timeout.connect"),
        /**
         * Value is in the ISO-8601 duration format
         */
        HTTP_TIMEOUT_REQUEST("http.timeout.request");

        private final String propertyKey;

        Options(String propertyKey) {
            this.propertyKey = propertyKey;
        }

        public String getPropertyKey() {
            return propertyKey;
        }
    }

    private final Map<String, String> clientOptions;

    private NotificationClientOptions() {
        this.clientOptions = new HashMap<>();
    }

    public static NotificationClientOptions defaultOptions() {
        return new NotificationClientOptions();
    }

    public NotificationClientOptions setOption(Options key, String value) {
        clientOptions.put(key.getPropertyKey(), value);
        return this;
    }

    boolean hasOption(String key) {
        return clientOptions.containsKey(key);
    }

    String get(String key) {
        return clientOptions.get(key);
    }
}
