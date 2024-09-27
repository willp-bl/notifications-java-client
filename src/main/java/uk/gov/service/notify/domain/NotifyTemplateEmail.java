package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyTemplateEmail extends NotifyTemplate {

    @NotEmpty
    private final String subject;

    public NotifyTemplateEmail(@JsonProperty("id") UUID id,
                               @JsonProperty("name") String name,
                               @JsonProperty("type") NotificationType type,
                               @JsonProperty("created_at") ZonedDateTime createdAt,
                               @JsonProperty("updated_at") ZonedDateTime updatedAt,
                               @JsonProperty("version") int version,
                               @JsonProperty("created_by") String createdBy,
                               @JsonProperty("body") String body,
                               @JsonProperty("subject") String subject) {
        super(id, name, type, createdAt, updatedAt, version, createdBy, body);
        this.subject = subject;
    }

    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NotifyTemplateEmail that = (NotifyTemplateEmail) o;
        return Objects.equals(subject, that.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subject);
    }

    @Override
    public String toString() {
        return "NotifyTemplateEmail{" +
                "subject='" + subject + '\'' +
                "} " + super.toString();
    }
}
