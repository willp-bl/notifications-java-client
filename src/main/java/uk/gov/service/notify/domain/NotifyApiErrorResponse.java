package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class NotifyApiErrorResponse {

    public static class NotifyApiError {
        private final String error;
        private final String message;

        public NotifyApiError(@JsonProperty("error") String error,
                              @JsonProperty("message") String message) {
            this.error = error;
            this.message = message;
        }

        @JsonProperty("error")
        public String getError() {
            return error;
        }

        @JsonProperty("message")
        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NotifyApiError that = (NotifyApiError) o;
            return Objects.equals(error, that.error) && Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(error, message);
        }

        @Override
        public String toString() {
            return "NotifyApiError{" +
                    "error='" + error + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    private final int statusCode;
    private final List<NotifyApiError> errors;

    public NotifyApiErrorResponse(@JsonProperty("status_code") int statusCode,
                                  @JsonProperty("errors") List<NotifyApiError> errors) {
        this.statusCode = statusCode;
        this.errors = errors;
    }

    @JsonProperty("status_code")
    public int getStatusCode() {
        return statusCode;
    }

    @JsonProperty("errors")
    public List<NotifyApiError> getErrors() {
        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyApiErrorResponse that = (NotifyApiErrorResponse) o;
        return statusCode == that.statusCode && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, errors);
    }

    @Override
    public String toString() {
        return "NotifyApiException{" +
                "statusCode=" + statusCode +
                ", errors=" + errors +
                '}';
    }
}
