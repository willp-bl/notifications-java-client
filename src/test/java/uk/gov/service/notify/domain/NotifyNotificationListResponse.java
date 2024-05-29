package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public class NotifyNotificationListResponse {

    public static final class Links {
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

    private final List<NotifyNotification> notifications;
    private final Links links;

    public NotifyNotificationListResponse(@JsonProperty("notifications") List<NotifyNotification> notifications,
                                          @JsonProperty("links") Links links) {

        this.notifications = notifications;
        this.links = links;
    }

    @JsonProperty("notifications")
    public List<NotifyNotification> getNotifications() {
        return notifications;
    }

    @JsonProperty("links")
    public Links getLinks() {
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyNotificationListResponse that = (NotifyNotificationListResponse) o;
        return Objects.equals(notifications, that.notifications) && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notifications, links);
    }

    @Override
    public String toString() {
        return "NotifyNotificationListResponse{" +
                "notifications=" + notifications +
                ", links=" + links +
                '}';
    }
}
