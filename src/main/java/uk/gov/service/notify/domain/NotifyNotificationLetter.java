package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyNotificationLetter extends NotifyNotification {

    public static class CostDetails {
        @NotNull
        @PositiveOrZero
        private final Integer billableSheetsOfPaper;
        @NotNull
        private final Postage postage;

        public CostDetails(@JsonProperty("billable_sheets_of_paper") Integer billableSheetsOfPaper,
                           @JsonProperty("postage") Postage postage) {
            this.billableSheetsOfPaper = billableSheetsOfPaper;
            this.postage = postage;
        }

        @JsonProperty("billable_sheets_of_paper")
        public Integer getBillableSheetsOfPaper() {
            return billableSheetsOfPaper;
        }

        @JsonProperty("postage")
        public Postage getPostage() {
            return postage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CostDetails that = (CostDetails) o;
            return Objects.equals(billableSheetsOfPaper, that.billableSheetsOfPaper) && Objects.equals(postage, that.postage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(billableSheetsOfPaper, postage);
        }

        @Override
        public String toString() {
            return "CostDetails{" +
                    "billableSheetsOfPaper=" + billableSheetsOfPaper +
                    ", postage='" + postage + '\'' +
                    '}';
        }
    }

    @NotNull
    private final NotificationStatus.Letter status;
    // the other address lines are optional. A precompiled letter will only have address_line_1
    @NotEmpty
    private final String line1;
    private final String line2;
    private final String line3;
    private final String line4;
    private final String line5;
    private final String line6;
    private final String line7;
    private final CostDetails costDetails;
    // FIXME FIXME FIXME
    //"estimated_delivery"? - not in API docs

    public NotifyNotificationLetter(@JsonProperty("id") UUID id,
                                    @JsonProperty("reference") String reference,
                                    @JsonProperty("line_1") String line1,
                                    @JsonProperty("line_2") String line2,
                                    @JsonProperty("line_3") String line3,
                                    @JsonProperty("line_4") String line4,
                                    @JsonProperty("line_5") String line5,
                                    @JsonProperty("line_6") String line6,
                                    @JsonProperty("line_7") String line7,
                                    @JsonProperty("type") NotificationType type,
                                    @JsonProperty("status") NotificationStatus.Letter status,
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
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.line4 = line4;
        this.line5 = line5;
        this.line6 = line6;
        this.line7 = line7;
        this.costDetails = costDetails;
    }

    @JsonProperty("status")
    public NotificationStatus.Letter getStatus() {
        return status;
    }

    @JsonProperty("line_1")
    public String getLine1() {
        return line1;
    }

    @JsonProperty("line_2")
    public String getLine2() {
        return line2;
    }

    @JsonProperty("line_3")
    public String getLine3() {
        return line3;
    }

    @JsonProperty("line_4")
    public String getLine4() {
        return line4;
    }

    @JsonProperty("line_5")
    public String getLine5() {
        return line5;
    }

    @JsonProperty("line_6")
    public String getLine6() {
        return line6;
    }

    @JsonProperty("line_7")
    public String getLine7() {
        return line7;
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
        NotifyNotificationLetter that = (NotifyNotificationLetter) o;
        return status == that.status && Objects.equals(line1, that.line1) && Objects.equals(line2, that.line2) && Objects.equals(line3, that.line3) && Objects.equals(line4, that.line4) && Objects.equals(line5, that.line5) && Objects.equals(line6, that.line6) && Objects.equals(line7, that.line7) && Objects.equals(costDetails, that.costDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status, line1, line2, line3, line4, line5, line6, line7, costDetails);
    }

    @Override
    public String toString() {
        return "NotifyNotificationLetter{" +
                "status=" + status +
                ", line1='" + line1 + '\'' +
                ", line2='" + line2 + '\'' +
                ", line3='" + line3 + '\'' +
                ", line4='" + line4 + '\'' +
                ", line5='" + line5 + '\'' +
                ", line6='" + line6 + '\'' +
                ", line7='" + line7 + '\'' +
                ", costDetails=" + costDetails +
                "} " + super.toString();
    }
}
