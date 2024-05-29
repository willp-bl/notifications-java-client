package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyReceivedTextMessage {

    private final UUID id;
    private final ZonedDateTime createdAt;
    private final UUID serviceId;
    private final String notifyNumber;
    private final String userNumber;
    private final String content;

    public NotifyReceivedTextMessage(@JsonProperty("id") UUID id,
                                     @JsonProperty("created_at") ZonedDateTime createdAt,
                                     @JsonProperty("service_id") UUID serviceId,
                                     @JsonProperty("notify_number") String notifyNumber,
                                     @JsonProperty("user_number") String userNumber,
                                     @JsonProperty("content") String content) {

        this.id = id;
        this.createdAt = createdAt;
        this.serviceId = serviceId;
        this.notifyNumber = notifyNumber;
        this.userNumber = userNumber;
        this.content = content;
    }

    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("created_at")
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("service_id")
    public UUID getServiceId() {
        return serviceId;
    }

    @JsonProperty("notify_number")
    public String getNotifyNumber() {
        return notifyNumber;
    }

    @JsonProperty("user_number")
    public String getUserNumber() {
        return userNumber;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyReceivedTextMessage that = (NotifyReceivedTextMessage) o;
        return Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt) && Objects.equals(serviceId, that.serviceId) && Objects.equals(notifyNumber, that.notifyNumber) && Objects.equals(userNumber, that.userNumber) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, serviceId, notifyNumber, userNumber, content);
    }

    @Override
    public String toString() {
        return "NotifyReceivedTextMessage{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", serviceId=" + serviceId +
                ", notifyNumber='" + notifyNumber + '\'' +
                ", userNumber='" + userNumber + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
