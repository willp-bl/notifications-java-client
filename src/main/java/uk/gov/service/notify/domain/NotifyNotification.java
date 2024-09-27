package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true,
        property = "type")
@JsonSubTypes({
        // unfortunately can't use the enum
        @JsonSubTypes.Type(name = "email", value = NotifyNotificationEmail.class),
        @JsonSubTypes.Type(name = "sms", value = NotifyNotificationSms.class),
        @JsonSubTypes.Type(name = "letter", value = NotifyNotificationLetter.class)
})
public abstract class NotifyNotification {

    public static class Template {
        private final UUID id;
        private final int version;
        private final URI uri;

        public Template(@JsonProperty("id") UUID id,
                        @JsonProperty("version") int version,
                        @JsonProperty("uri") URI uri) {
            this.id = id;
            this.version = version;
            this.uri = uri;
        }

        @JsonProperty("id")
        public UUID getId() {
            return id;
        }

        @JsonProperty("version")
        public int getVersion() {
            return version;
        }

        @JsonProperty("uri")
        public URI getUri() {
            return uri;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Template template = (Template) o;
            return version == template.version && Objects.equals(id, template.id) && Objects.equals(uri, template.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, version, uri);
        }

        @Override
        public String toString() {
            return "Template{" +
                    "id=" + id +
                    ", version=" + version +
                    ", uri=" + uri +
                    '}';
        }
    }

    private final UUID id;
    private final String reference;
    private final NotificationType type;
    private final Template template;
    private final String body;
    private final ZonedDateTime createdAt;
    private final String createdByName;
    private final ZonedDateTime sentAt;
    private final ZonedDateTime completedAt;
    private final boolean isCostDataReady;
    private final double costInPounds;

    public NotifyNotification(@JsonProperty("id") UUID id,
                              @JsonProperty("reference") String reference,
                              @JsonProperty("type") NotificationType type,
                              @JsonProperty("template") Template template,
                              @JsonProperty("body") String body,
                              @JsonProperty("created_at") ZonedDateTime createdAt,
                              @JsonProperty("created_by_name") String createdByName,
                              @JsonProperty("sent_at") ZonedDateTime sentAt,
                              @JsonProperty("completed_at") ZonedDateTime completedAt,
                              @JsonProperty("is_cost_data_ready") boolean isCostDataReady,
                              @JsonProperty("cost_in_pounds") double costInPounds) {

        this.id = id;
        this.reference = reference;
        this.type = type;
        this.template = template;
        this.body = body;
        this.createdAt = createdAt;
        this.createdByName = createdByName;
        this.sentAt = sentAt;
        this.completedAt = completedAt;
        this.isCostDataReady = isCostDataReady;
        this.costInPounds = costInPounds;
    }

    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("type")
    public NotificationType getType() {
        return type;
    }

    @JsonProperty("template")
    public Template getTemplate() {
        return template;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("created_at")
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_by_name")
    public String getCreatedByName() {
        return createdByName;
    }

    @JsonProperty("sent_at")
    public ZonedDateTime getSentAt() {
        return sentAt;
    }

    @JsonProperty("completed_at")
    public ZonedDateTime getCompletedAt() {
        return completedAt;
    }

    @JsonProperty("is_cost_data_ready")
    public boolean isCostDataReady() {
        return isCostDataReady;
    }

    @JsonProperty("cost_in_pounds")
    public double getCostInPounds() {
        return costInPounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyNotification that = (NotifyNotification) o;
        return isCostDataReady == that.isCostDataReady && Double.compare(costInPounds, that.costInPounds) == 0 && Objects.equals(id, that.id) && Objects.equals(reference, that.reference) && type == that.type && Objects.equals(template, that.template) && Objects.equals(body, that.body) && Objects.equals(createdAt, that.createdAt) && Objects.equals(createdByName, that.createdByName) && Objects.equals(sentAt, that.sentAt) && Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, type, template, body, createdAt, createdByName, sentAt, completedAt, isCostDataReady, costInPounds);
    }

    @Override
    public String toString() {
        return "NotifyNotification{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", type=" + type +
                ", template=" + template +
                ", body='" + body + '\'' +
                ", createdAt=" + createdAt +
                ", createdByName='" + createdByName + '\'' +
                ", sentAt=" + sentAt +
                ", completedAt=" + completedAt +
                ", isCostDataReady=" + isCostDataReady +
                ", costInPounds=" + costInPounds +
                '}';
    }
}
