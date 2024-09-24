package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class NotifySmsResponse {

    public static class Content {
        private final String body;
        private final String fromNumber;

        public Content(@JsonProperty("body") String body,
                       @JsonProperty("from_number") String fromNumber) {
            this.body = body;
            this.fromNumber = fromNumber;
        }

        @JsonProperty("body")
        public String getBody() {
            return body;
        }

        @JsonProperty("from_number")
        public String getFromNumber() {
            return fromNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Content content = (Content) o;
            return Objects.equals(body, content.body) && Objects.equals(fromNumber, content.fromNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(body, fromNumber);
        }

        @Override
        public String toString() {
            return "Content{" +
                    "body='" + body + '\'' +
                    ", fromNumber='" + fromNumber + '\'' +
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

    public NotifySmsResponse(@JsonProperty("id") UUID notificationId,
                               @JsonProperty("reference") String reference,
                               @JsonProperty("content") Content content,
                               @JsonProperty("uri") URI uri,
                               @JsonProperty("template") Template template) {
        this.notificationId = notificationId;
        this.reference = reference;
        this.content = content;
        this.uri = uri;
        this.template = template;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifySmsResponse that = (NotifySmsResponse) o;
        return Objects.equals(notificationId, that.notificationId) && Objects.equals(reference, that.reference) && Objects.equals(content, that.content) && Objects.equals(uri, that.uri) && Objects.equals(template, that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, reference, content, uri, template);
    }

    @Override
    public String toString() {
        return "NotifySmsResponse{" +
                "notificationId=" + notificationId +
                ", reference='" + reference + '\'' +
                ", content=" + content +
                ", uri=" + uri +
                ", template=" + template +
                '}';
    }
}
