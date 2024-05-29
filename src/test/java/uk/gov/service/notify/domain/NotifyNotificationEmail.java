package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.service.notify.NotificationType;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyNotificationEmail extends NotifyNotification {

    private final NotificationStatus.Email status;
    private final String emailAddress;
    private final String subject;

    public NotifyNotificationEmail(@JsonProperty("id") UUID id,
                                   @JsonProperty("reference") String reference,
                                   @JsonProperty("email_address") String emailAddress,
                                   @JsonProperty("type") NotificationType type,
                                   @JsonProperty("status") NotificationStatus.Email status,
                                   @JsonProperty("template") Template template,
                                   @JsonProperty("body") String body,
                                   @JsonProperty("subject") String subject,
                                   @JsonProperty("created_at") ZonedDateTime createdAt,
                                   @JsonProperty("created_by_name") String createdByName,
                                   @JsonProperty("sent_at") ZonedDateTime sentAt,
                                   @JsonProperty("completed_at") ZonedDateTime completedAt,
                                   @JsonProperty("is_cost_data_ready") boolean isCostDataReady,
                                   @JsonProperty("cost_in_pounds") double costInPounds,
                                   @JsonProperty("cost_details") CostDetails costDetails) {
    super(id, reference, type, template, body, createdAt, createdByName, sentAt, completedAt, isCostDataReady, costInPounds, costDetails);
    this.status = status;
        this.emailAddress = emailAddress;
        this.subject = subject;
    }

    @JsonProperty("status")
    public NotificationStatus.Email getStatus() {
        return status;
    }

    @JsonProperty("email_address")
    public String getEmailAddress() {
        return emailAddress;
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
        NotifyNotificationEmail that = (NotifyNotificationEmail) o;
        return status == that.status && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(subject, that.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, emailAddress, subject);
    }

    @Override
    public String toString() {
        return "NotifyNotificationEmail{" +
                "status=" + status +
                ", emailAddress='" + emailAddress + '\'' +
                ", subject='" + subject + '\'' +
                "} " + super.toString();
    }
}
