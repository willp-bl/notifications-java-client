package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationStatus {

    private NotificationStatus() {}

    /**
     * See https://docs.notifications.service.gov.uk/rest-api.html#email-status-descriptions
     */
    public enum Email {
        @JsonProperty("created") CREATED("created"),
        @JsonProperty("sending") SENDING("sending"),
        @JsonProperty("delivered") DELIVERED("delivered"),
        @JsonProperty("permanent-failure") PERMANENT_FAILURE("permanent-failure"),
        @JsonProperty("temporary-failure") TEMPORARY_FAILURE("temporary-failure"),
        @JsonProperty("technical-failure") TECHNICAL_FAILURE("technical-failure"),
        @JsonEnumDefaultValue UNKNOWN("unknown");

        private final String status;

        Email(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * See https://docs.notifications.service.gov.uk/rest-api.html#letter-status-descriptions
     * and https://docs.notifications.service.gov.uk/rest-api.html#precompiled-letter-status-descriptions
     */
    public enum Letter {
        @JsonProperty("accepted") ACCEPTED("accepted"),
        @JsonProperty("received") RECEIVED("received"),
        @JsonProperty("cancelled") CANCELLED("cancelled"),
        @JsonProperty("technical-failure") TECHNICAL_FAILURE("technical-failure"),
        @JsonProperty("permanent-failure") PERMANENT_FAILURE("permanent-failure"),
        // pre-compiled letter statuses
        @JsonProperty("pending-virus-check") PENDING_VIRUS_CHECK("pending-virus-check"),
        @JsonProperty("virus-scan-failed") VIRUS_SCAN_FAILED("virus-scan-failed"),
        @JsonProperty("validation-failed") VALIDATION_FAILED("validation-failed"),
        @JsonEnumDefaultValue UNKNOWN("unknown");

        private final String status;

        Letter(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    /**
     * See https://docs.notifications.service.gov.uk/rest-api.html#text-message-status-descriptions
     */
    public enum Sms {
        @JsonProperty("created") CREATED("created"),
        @JsonProperty("sending") SENDING("sending"),
        @JsonProperty("pending") PENDING("pending"),
        @JsonProperty("sent") SENT("sent"),
        @JsonProperty("delivered") DELIVERED("delivered"),
        @JsonProperty("permanent-failure") PERMANENT_FAILURE("permanent-failure"),
        @JsonProperty("temporary-failure") TEMPORARY_FAILURE("temporary-failure"),
        @JsonProperty("technical-failure") TECHNICAL_FAILURE("technical-failure"),
        @JsonEnumDefaultValue UNKNOWN("unknown");

        private final String status;

        Sms(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
