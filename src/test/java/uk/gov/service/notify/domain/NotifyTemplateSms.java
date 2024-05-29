package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.service.notify.NotificationType;

import java.time.ZonedDateTime;
import java.util.UUID;

public class NotifyTemplateSms extends NotifyTemplate {

    public NotifyTemplateSms(@JsonProperty("id") UUID id,
                             @JsonProperty("name") String name,
                             @JsonProperty("type") NotificationType type,
                             @JsonProperty("created_at") ZonedDateTime createdAt,
                             @JsonProperty("updated_at") ZonedDateTime updatedAt,
                             @JsonProperty("version") int version,
                             @JsonProperty("created_by") String createdBy,
                             @JsonProperty("body") String body) {
        super(id, name, type, createdAt, updatedAt, version, createdBy, body);
    }

    @Override
    public String toString() {
        return "NotifyTemplateSms{} " + super.toString();
    }
}
