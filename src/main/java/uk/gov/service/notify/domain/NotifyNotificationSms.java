package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyNotificationSms extends NotifyNotification {

    private final NotificationStatus.Sms status;
    private final String phoneNumber;

    public NotifyNotificationSms(@JsonProperty("id") UUID id,
                                 @JsonProperty("reference") String reference,
                                 @JsonProperty("phone_number") String phoneNumber,
                                 @JsonProperty("type") NotificationType type,
                                 @JsonProperty("status") NotificationStatus.Sms status,
                                 @JsonProperty("template") Template template,
                                 @JsonProperty("body") String body,
                                 @JsonProperty("created_at") ZonedDateTime createdAt,
                                 @JsonProperty("created_by_name") String createdByName,
                                 @JsonProperty("sent_at") ZonedDateTime sentAt,
                                 @JsonProperty("completed_at") ZonedDateTime completedAt,
                                 @JsonProperty("is_cost_data_ready") boolean isCostDataReady,
                                 @JsonProperty("cost_in_pounds") double costInPounds,
                                 @JsonProperty("cost_details") CostDetails costDetails) {
        super(id, reference, type, template, body, createdAt, createdByName, sentAt, completedAt, isCostDataReady, costInPounds, costDetails);
        this.status = status;
        this.phoneNumber = phoneNumber;
    }

    @JsonProperty("status")
    public NotificationStatus.Sms getStatus() {
        return status;
    }

    @JsonProperty("phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NotifyNotificationSms that = (NotifyNotificationSms) o;
        return status == that.status && Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, phoneNumber);
    }

    @Override
    public String toString() {
        return "NotifyNotificationSms{" +
                "status=" + status +
                ", phoneNumber='" + phoneNumber + '\'' +
                "} " + super.toString();
    }
}
