package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class NotifyTemplatePreviewResponse {

    private final UUID id;
    private final String type;
    private final int version;
    private final String body;
    private final String subject;

    public NotifyTemplatePreviewResponse(@JsonProperty("id") UUID id,
                                         @JsonProperty("type") String type,
                                         @JsonProperty("version") int version,
                                         @JsonProperty("body") String body,
                                         @JsonProperty("subject") String subject) {
        this.id = id;
        this.type = type;
        this.version = version;
        this.body = body;
        this.subject = subject;
    }

    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("version")
    public int getVersion() {
        return version;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyTemplatePreviewResponse that = (NotifyTemplatePreviewResponse) o;
        return version == that.version && Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(body, that.body) && Objects.equals(subject, that.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, version, body, subject);
    }

    @Override
    public String toString() {
        return "NotifyTemplatePreviewResponse{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", version=" + version +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}
