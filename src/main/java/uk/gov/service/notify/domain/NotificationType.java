package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum NotificationType {
    sms,
    email,
    letter,
    @JsonEnumDefaultValue UNKNOWN
}
