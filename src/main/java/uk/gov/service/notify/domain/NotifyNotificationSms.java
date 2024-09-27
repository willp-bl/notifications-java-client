package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyNotificationSms extends NotifyNotification {

    public static class CostDetails {
        @NotNull
        @PositiveOrZero
        private final Integer billableSmsFragments;
        private final Double internationalRateMultiplier;
        private final Double smsRate;

        public CostDetails(@JsonProperty("billable_sms_fragments") Integer billableSmsFragments,
                           @JsonProperty("international_rate_multiplier") Double internationalRateMultiplier,
                           @JsonProperty("sms_rate") Double smsRate) {
            this.billableSmsFragments = billableSmsFragments;
            this.internationalRateMultiplier = internationalRateMultiplier;
            this.smsRate = smsRate;
        }

        @JsonProperty("billable_sms_fragments")
        public Integer getBillableSmsFragments() {
            return billableSmsFragments;
        }

        @JsonProperty("international_rate_multiplier")
        public Double getInternationalRateMultiplier() {
            return internationalRateMultiplier;
        }

        @JsonProperty("sms_rate")
        public Double getSmsRate() {
            return smsRate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CostDetails that = (CostDetails) o;
            return Objects.equals(billableSmsFragments, that.billableSmsFragments) && Objects.equals(internationalRateMultiplier, that.internationalRateMultiplier) && Objects.equals(smsRate, that.smsRate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(billableSmsFragments, internationalRateMultiplier, smsRate);
        }

        @Override
        public String toString() {
            return "CostDetails{" +
                    "billableSmsFragments=" + billableSmsFragments +
                    ", internationalRateMultiplier=" + internationalRateMultiplier +
                    ", smsRate=" + smsRate +
                    '}';
        }
    }

    @NotNull
    private final NotificationStatus.Sms status;
    @NotNull
    @Pattern(regexp = "^[0-9\\+\\(\\)]*$")
    private final String phoneNumber;
    private final CostDetails costDetails;

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
        super(id, reference, type, template, body, createdAt, createdByName, sentAt, completedAt, isCostDataReady, costInPounds);
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.costDetails = costDetails;
    }

    @JsonProperty("status")
    public NotificationStatus.Sms getStatus() {
        return status;
    }

    @JsonProperty("phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @JsonProperty("cost_details")
    public CostDetails getCostDetails() {
        return costDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NotifyNotificationSms that = (NotifyNotificationSms) o;
        return status == that.status && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(costDetails, that.costDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, phoneNumber, costDetails);
    }

    @Override
    public String toString() {
        return "NotifyNotificationSms{" +
                "status=" + status +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", costDetails=" + costDetails +
                "} " + super.toString();
    }
}
