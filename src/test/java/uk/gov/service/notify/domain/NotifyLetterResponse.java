package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class NotifyLetterResponse {

    public static class Content {
        private final String body;
        private final String subject;

        public Content(@JsonProperty("body") String body,
                       @JsonProperty("subject") String subject) {
            this.body = body;
            this.subject = subject;
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
            Content content = (Content) o;
            return Objects.equals(body, content.body) && Objects.equals(subject, content.subject);
        }

        @Override
        public int hashCode() {
            return Objects.hash(body, subject);
        }

        @Override
        public String toString() {
            return "Content{" +
                    "body='" + body + '\'' +
                    ", subject='" + subject + '\'' +
                    '}';
        }
    }

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

    private final UUID notificationId;
    private final String reference;
    private final Content content;
    private final URI uri;
    private final Template template;
    private final String scheduledFor;

    public NotifyLetterResponse(@JsonProperty("id") UUID notificationId,
                                @JsonProperty("reference") String reference,
                                @JsonProperty("content") Content content,
                                @JsonProperty("uri") URI uri,
                                @JsonProperty("template") Template template,
                                @JsonProperty("scheduled_for") String scheduledFor) {
        this.notificationId = notificationId;
        this.reference = reference;
        this.content = content;
        this.uri = uri;
        this.template = template;
        this.scheduledFor = scheduledFor;
    }

    @JsonProperty("id")
    public UUID getNotificationId() {
        return notificationId;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("content")
    public Content getContent() {
        return content;
    }

    @JsonProperty("uri")
    public URI getUri() {
        return uri;
    }

    @JsonProperty("template")
    public Template getTemplate() {
        return template;
    }

    @JsonProperty("scheduled_for")
    public String getScheduledFor() {
        return scheduledFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyLetterResponse that = (NotifyLetterResponse) o;
        return Objects.equals(notificationId, that.notificationId) && Objects.equals(reference, that.reference) && Objects.equals(content, that.content) && Objects.equals(uri, that.uri) && Objects.equals(template, that.template) && Objects.equals(scheduledFor, that.scheduledFor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, reference, content, uri, template, scheduledFor);
    }

    @Override
    public String toString() {
        return "NotifyLetterResponse{" +
                "notificationId=" + notificationId +
                ", reference='" + reference + '\'' +
                ", content=" + content +
                ", uri=" + uri +
                ", template=" + template +
                ", scheduledFor='" + scheduledFor + '\'' +
                '}';
    }
}
