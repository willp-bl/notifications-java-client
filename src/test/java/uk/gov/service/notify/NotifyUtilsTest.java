package uk.gov.service.notify;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyUtilsTest {

    private static final String SERVICE_ID = UUID.randomUUID().toString();
    private static final String API_KEY = UUID.randomUUID().toString();
    private static final String COMBINED_API_KEY = "Api_key_name-" + SERVICE_ID + "-" + API_KEY;

    @Test
    void testExtractApiKey() {
        final String apiKey = NotifyUtils.extractApiKey(COMBINED_API_KEY);
        assertThat(apiKey).isEqualTo(API_KEY);
    }

    @Test
    void testExtractServiceId() {
        final String serviceId = NotifyUtils.extractServiceId(COMBINED_API_KEY);
        assertThat(serviceId).isEqualTo(SERVICE_ID);
    }

}