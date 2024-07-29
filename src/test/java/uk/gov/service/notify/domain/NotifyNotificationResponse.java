package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyNotificationResponse {

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

    public static class CostDetails {
        private final Integer billableSmsFragments;
        private final Double internationalRateMultiplier;
        private final Double smsRate;
        private final Integer billableSheetsOfPaper;
        private final String postage;

        public CostDetails(@JsonProperty("billable_sms_fragments") Integer billableSmsFragments,
                           @JsonProperty("international_rate_multiplier") Double internationalRateMultiplier,
                           @JsonProperty("sms_rate") Double smsRate,
                           @JsonProperty("billable_sheets_of_paper") Integer billableSheetsOfPaper,
                           @JsonProperty("postage") String postage) {
            this.billableSmsFragments = billableSmsFragments;
            this.internationalRateMultiplier = internationalRateMultiplier;
            this.smsRate = smsRate;
            this.billableSheetsOfPaper = billableSheetsOfPaper;
            this.postage = postage;
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

        @JsonProperty("billable_sheets_of_paper")
        public Integer getBillableSheetsOfPaper() {
            return billableSheetsOfPaper;
        }

        @JsonProperty("postage")
        public String getPostage() {
            return postage;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CostDetails that = (CostDetails) o;
            return Objects.equals(billableSmsFragments, that.billableSmsFragments) && Objects.equals(internationalRateMultiplier, that.internationalRateMultiplier) && Objects.equals(smsRate, that.smsRate) && Objects.equals(billableSheetsOfPaper, that.billableSheetsOfPaper) && Objects.equals(postage, that.postage);
        }

        @Override
        public int hashCode() {
            return Objects.hash(billableSmsFragments, internationalRateMultiplier, smsRate, billableSheetsOfPaper, postage);
        }

        @Override
        public String toString() {
            return "CostDetails{" +
                    "billableSmsFragments=" + billableSmsFragments +
                    ", internationalRateMultiplier=" + internationalRateMultiplier +
                    ", smsRate=" + smsRate +
                    ", billableSheetsOfPaper=" + billableSheetsOfPaper +
                    ", postage='" + postage + '\'' +
                    '}';
        }
    }

    private final UUID id;
    private final String reference;
    private final String emailAddress;
    private final String phoneNumber;
    private final String line1;
    private final String line2;
    private final String line3;
    private final String line4;
    private final String line5;
    private final String line6;
    private final String line7;
    private final String type;
    private final String status;
    private final Template template;
    private final String body;
    private final String subject;
    private final ZonedDateTime createdAt;
    private final String createdByName;
    private final ZonedDateTime sentAt;
    private final ZonedDateTime completedAt;

    private final boolean isCostDataReady;
    private final double costInPounds;
    private final CostDetails costDetails;

    public NotifyNotificationResponse(@JsonProperty("id") UUID id,
                                      @JsonProperty("reference") String reference,
                                      @JsonProperty("email_address") String emailAddress,
                                      @JsonProperty("phone_number") String phoneNumber,
                                      @JsonProperty("line_1") String line1,
                                      @JsonProperty("line_2") String line2,
                                      @JsonProperty("line_3") String line3,
                                      @JsonProperty("line_4") String line4,
                                      @JsonProperty("line_5") String line5,
                                      @JsonProperty("line_6") String line6,
                                      @JsonProperty("line_7") String line7,
                                      @JsonProperty("type") String type,
                                      @JsonProperty("status") String status,
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

        this.id = id;
        this.reference = reference;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.line4 = line4;
        this.line5 = line5;
        this.line6 = line6;
        this.line7 = line7;
        this.type = type;
        this.status = status;
        this.template = template;
        this.body = body;
        this.subject = subject;
        this.createdAt = createdAt;
        this.createdByName = createdByName;
        this.sentAt = sentAt;
        this.completedAt = completedAt;
        this.isCostDataReady = isCostDataReady;
        this.costInPounds = costInPounds;
        this.costDetails = costDetails;
    }

    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("email_address")
    public String getEmailAddress() {
        return emailAddress;
    }

    @JsonProperty("phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
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

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("template")
    public Template getTemplate() {
        return template;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    @JsonProperty("subject")
    public String getSubject() {
        return subject;
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

    @JsonProperty("cost_details")
    public CostDetails getCostDetails() {
        return costDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyNotificationResponse that = (NotifyNotificationResponse) o;
        return isCostDataReady == that.isCostDataReady &&
                Double.compare(that.costInPounds, costInPounds) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(reference, that.reference) &&
                Objects.equals(emailAddress, that.emailAddress) &&
                Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(line1, that.line1) &&
                Objects.equals(line2, that.line2) &&
                Objects.equals(line3, that.line3) &&
                Objects.equals(line4, that.line4) &&
                Objects.equals(line5, that.line5) &&
                Objects.equals(line6, that.line6) &&
                Objects.equals(line7, that.line7) &&
                Objects.equals(type, that.type) &&
                Objects.equals(status, that.status) &&
                Objects.equals(template, that.template) &&
                Objects.equals(body, that.body) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(createdByName, that.createdByName) &&
                Objects.equals(sentAt, that.sentAt) &&
                Objects.equals(completedAt, that.completedAt) &&
                Objects.equals(costDetails, that.costDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, emailAddress, phoneNumber, line1, line2, line3, line4, line5, line6, line7, type, status, template, body, subject, createdAt, createdByName, sentAt, completedAt, isCostDataReady, costInPounds, costDetails);
    }

    @Override
    public String toString() {
        return "NotifyNotificationResponse{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", line1='" + line1 + '\'' +
                ", line2='" + line2 + '\'' +
                ", line3='" + line3 + '\'' +
                ", line4='" + line4 + '\'' +
                ", line5='" + line5 + '\'' +
                ", line6='" + line6 + '\'' +
                ", line7='" + line7 + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", template=" + template +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", createdAt=" + createdAt +
                ", createdByName='" + createdByName + '\'' +
                ", sentAt=" + sentAt +
                ", completedAt=" + completedAt +
                ", isCostDataReady=" + isCostDataReady +
                ", costInPounds=" + costInPounds +
                ", costDetails=" + costDetails +
                '}';
    }
}
