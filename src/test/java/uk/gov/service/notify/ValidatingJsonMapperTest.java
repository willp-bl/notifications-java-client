package uk.gov.service.notify;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatingJsonMapperTest {

    private enum TestEnum {
        KNOWN,
        @JsonEnumDefaultValue UNKNOWN
    }

    private static class TestClassForSerialization {
        @NotNull
        @PastOrPresent
        private final ZonedDateTime time;
        @NotNull
        private final TestEnum testEnum;

        public TestClassForSerialization(@JsonProperty("time") ZonedDateTime time,
                                         @JsonProperty("enum") TestEnum testEnum) {
            this.time = time;
            this.testEnum = testEnum;
        }

        @JsonProperty("time")
        public ZonedDateTime getTime() {
            return time;
        }

        @JsonProperty("enum")
        public TestEnum getTestEnum() {
            return testEnum;
        }
    }

    @Test
    void testShouldSerializeWithValidationOk() throws NotificationClientException, JsonProcessingException {
        NotificationClientOptions options = NotificationClientOptions.defaultOptions()
                .setOption(NotificationClientOptions.Options.VALIDATION_SKIP, "false");
        ValidatingJsonMapper mapper = new ValidatingJsonMapper(options);
        String expected = "2024-05-10T16:40:14Z";
        ZonedDateTime time = ZonedDateTime.parse(expected);
        TestClassForSerialization test = new TestClassForSerialization(time, TestEnum.KNOWN);

        String string = mapper.writeValueAsString(test);

        assertThat(string).isEqualTo("{\"time\":\""+expected+"\",\"enum\":\"KNOWN\"}");
    }

    @Test
    void testShouldSeserializeWithValidationFail() {
        NotificationClientOptions options = NotificationClientOptions.defaultOptions()
                .setOption(NotificationClientOptions.Options.VALIDATION_SKIP, "false");
        ValidatingJsonMapper mapper = new ValidatingJsonMapper(options);
        String expected = "3024-05-10T16:40:14Z";
        ZonedDateTime time = ZonedDateTime.parse(expected);
        TestClassForSerialization test = new TestClassForSerialization(time, TestEnum.KNOWN);

        NotificationClientException exception = assertThrows(NotificationClientException.class,
                () -> mapper.writeValueAsString(test));

        assertThat(exception).hasMessageContaining("TestClassForSerialization.time: must be a date in the past or in the present");
    }

    @Test
    void testShouldSerializeWithValidationFailButValidationOff() throws NotificationClientException, JsonProcessingException {
        NotificationClientOptions options = NotificationClientOptions.defaultOptions()
                .setOption(NotificationClientOptions.Options.VALIDATION_SKIP, "true");
        ValidatingJsonMapper mapper = new ValidatingJsonMapper(options);
        String expected = "3024-05-10T16:40:14Z";
        ZonedDateTime time = ZonedDateTime.parse(expected);
        TestClassForSerialization test = new TestClassForSerialization(time, TestEnum.KNOWN);

        String string = mapper.writeValueAsString(test);

        assertThat(string).isEqualTo("{\"time\":\""+expected+"\",\"enum\":\"KNOWN\"}");
    }

    @Test
    void testShouldDeserializeWithValidationOk() throws NotificationClientException, IOException {
        NotificationClientOptions options = NotificationClientOptions.defaultOptions()
                .setOption(NotificationClientOptions.Options.VALIDATION_SKIP, "false");
        ValidatingJsonMapper mapper = new ValidatingJsonMapper(options);
        String expectedTime = "2024-05-10T16:40:14Z";
        String expected = "{\"time\":\""+expectedTime+"\",\"enum\":\"KNOWN\"}";

        TestClassForSerialization actual = mapper.readValue(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)), TestClassForSerialization.class);

        assertThat(actual.getTime()).isEqualTo(expectedTime);
        assertThat(actual.getTestEnum()).isEqualTo(TestEnum.KNOWN);
    }

    @Test
    void testShouldNotDeserializeWithValidationFail() {
        NotificationClientOptions options = NotificationClientOptions.defaultOptions()
                .setOption(NotificationClientOptions.Options.VALIDATION_SKIP, "false");
        ValidatingJsonMapper mapper = new ValidatingJsonMapper(options);
        String expectedTime = "3024-05-10T16:40:14Z";
        String expected = "{\"time\":\""+expectedTime+"\",\"enum\":\"foo\"}";

        NotificationClientException exception = assertThrows(NotificationClientException.class,
                () -> mapper.readValue(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)), TestClassForSerialization.class));

        assertThat(exception).hasMessageContaining("TestClassForSerialization.time: must be a date in the past or in the present");
    }

    @Test
    void testShouldDeserializeWithValidationFailButValidationOff() throws NotificationClientException, IOException {
        NotificationClientOptions options = NotificationClientOptions.defaultOptions()
                .setOption(NotificationClientOptions.Options.VALIDATION_SKIP, "true");
        ValidatingJsonMapper mapper = new ValidatingJsonMapper(options);
        String expectedTime = "3024-05-10T16:40:14Z";
        String expected = "{\"time\":\""+expectedTime+"\",\"enum\":\"foo\"}";

        TestClassForSerialization actual = mapper.readValue(new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8)), TestClassForSerialization.class);

        assertThat(actual.getTime()).isEqualTo(expectedTime);
        assertThat(actual.getTestEnum()).isEqualTo(TestEnum.UNKNOWN);
    }
}
