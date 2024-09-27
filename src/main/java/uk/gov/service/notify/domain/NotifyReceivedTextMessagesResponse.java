package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public class NotifyReceivedTextMessagesResponse {

    public static class Links {
        @NotNull
        private final URI current;
        private final URI next;

        public Links(@JsonProperty("current") URI current,
                     @JsonProperty("next") URI next) {

            this.current = current;
            this.next = next;
        }

        @JsonProperty("current")
        public URI getCurrent() {
            return current;
        }

        @JsonProperty("next")
        public URI getNext() {
            return next;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Links links = (Links) o;
            return Objects.equals(current, links.current) && Objects.equals(next, links.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(current, next);
        }

        @Override
        public String toString() {
            return "Links{" +
                    "current=" + current +
                    ", next=" + next +
                    '}';
        }
    }

    private final List<NotifyReceivedTextMessage> receivedTextMessages;
    @NotNull
    private final Links links;

    public NotifyReceivedTextMessagesResponse(@JsonProperty("received_text_messages") List<NotifyReceivedTextMessage> receivedTextMessages,
                                              @JsonProperty("links") Links links) {

        this.receivedTextMessages = receivedTextMessages;
        this.links = links;
    }

    @JsonProperty("received_text_messages")
    public List<NotifyReceivedTextMessage> getReceivedTextMessages() {
        return receivedTextMessages;
    }

    @JsonProperty("links")
    public Links getLinks() {
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyReceivedTextMessagesResponse that = (NotifyReceivedTextMessagesResponse) o;
        return Objects.equals(receivedTextMessages, that.receivedTextMessages) && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receivedTextMessages, links);
    }

    @Override
    public String toString() {
        return "NotifyReceivedTextMessagesResponse{" +
                "receivedTextMessages=" + receivedTextMessages +
                ", links=" + links +
                '}';
    }
}
