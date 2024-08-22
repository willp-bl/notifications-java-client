package uk.gov.service.notify;

import org.json.JSONObject;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class Notification {
    private UUID id;
    private String reference;
    private String emailAddress;
    private String phoneNumber;
    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private String line5;
    private String line6;
    private String postcode;
    private String postage;
    private NotificationType notificationType;
    private String status;
    private String body;
    private String subject;
    private ZonedDateTime createdAt;
    private ZonedDateTime sentAt;
    private ZonedDateTime completedAt;
    private ZonedDateTime estimatedDelivery;
    private String createdByName;
    private boolean isCostDataReady;
    private double costInPounds;

    // Template fields
    private UUID templateId;
    private int templateVersion;
    private String templateUri;

    // CostDetails fields
    private Integer billableSmsFragments;
    private Double internationalRateMultiplier;
    private Double smsRate;
    private Integer billableSheetsOfPaper;
    private String postageType;

    public Notification(String content){
        JSONObject responseBodyAsJson = new JSONObject(content);
        build(responseBodyAsJson);
    }

    public Notification(org.json.JSONObject data){
        build(data);
    }

    private void build(JSONObject data) {
        id = UUID.fromString(data.getString("id"));
        reference = data.isNull("reference") ? null : data.getString("reference");
        emailAddress = data.isNull("email_address") ? null : data.getString("email_address");
        phoneNumber = data.isNull("phone_number") ? null : data.getString("phone_number");
        line1 = data.isNull("line_1") ? null : data.getString("line_1");
        line2 = data.isNull("line_2") ? null : data.getString("line_2");
        line3 = data.isNull("line_3") ? null : data.getString("line_3");
        line4 = data.isNull("line_4") ? null : data.getString("line_4");
        line5 = data.isNull("line_5") ? null : data.getString("line_5");
        line6 = data.isNull("line_6") ? null : data.getString("line_6");
        postcode = data.isNull("postcode") ? null : data.getString("postcode");
        postage = data.isNull("postage") ? null : data.getString("postage");
        notificationType = NotificationType.valueOf(data.getString("type"));
        JSONObject template = data.getJSONObject("template");
        templateId = UUID.fromString(template.getString("id"));
        templateVersion = template.getInt("version");
        templateUri = template.getString("uri");
        body = data.getString("body");
        subject = data.isNull("subject") ? null : data.getString("subject");
        status = data.getString("status");
        createdAt = ZonedDateTime.parse(data.getString("created_at"));
        sentAt =  data.isNull("sent_at") ? null : ZonedDateTime.parse(data.getString("sent_at"));
        completedAt = data.isNull("completed_at") ? null : ZonedDateTime.parse(data.getString("completed_at"));
        estimatedDelivery = data.isNull("estimated_delivery") ? null : ZonedDateTime.parse(data.getString("estimated_delivery"));
        createdByName = data.isNull("created_by_name") ? null : data.getString("created_by_name");

        // Deconstructing CostDetails
        if (!data.isNull("cost_details")) {
            JSONObject costDetails = data.getJSONObject("cost_details");
            billableSmsFragments = costDetails.isNull("billable_sms_fragments") ? null : costDetails.getInt("billable_sms_fragments");
            internationalRateMultiplier = costDetails.isNull("international_rate_multiplier") ? null : costDetails.getDouble("international_rate_multiplier");
            smsRate = costDetails.isNull("sms_rate") ? null : costDetails.getDouble("sms_rate");
            billableSheetsOfPaper = costDetails.isNull("billable_sheets_of_paper") ? null : costDetails.getInt("billable_sheets_of_paper");
            postageType = costDetails.isNull("postage") ? null : costDetails.getString("postage");
        } else {
            billableSmsFragments = null;
            internationalRateMultiplier = null;
            smsRate = null;
            billableSheetsOfPaper = null;
            postageType = null;
        }
    }

    public UUID getId() {
        return id;
    }

    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    public Optional<String> getEmailAddress() {
        return Optional.ofNullable(emailAddress);
    }

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    public Optional<String> getLine1() {
        return Optional.ofNullable(line1);
    }

    public Optional<String> getLine2() {
        return Optional.ofNullable(line2);
    }

    public Optional<String> getLine3() {
        return Optional.ofNullable(line3);
    }

    public Optional<String> getLine4() {
        return Optional.ofNullable(line4);
    }

    public Optional<String> getLine5() {
        return Optional.ofNullable(line5);
    }

    public Optional<String> getLine6() {
        return Optional.ofNullable(line6);
    }

    public Optional<String> getPostcode() {
        return Optional.ofNullable(postcode);
    }

    public Optional<String> getPostage() {
        return Optional.ofNullable(postage);
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public String getStatus() {
        return status;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public int getTemplateVersion() {
        return templateVersion;
    }

    public String getTemplateUri(){
        return templateUri;
    }

    public String getBody() {
        return body;
    }

    public Optional<String> getSubject() {
        return Optional.ofNullable(subject);
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public Optional<ZonedDateTime> getSentAt() {
        return Optional.ofNullable(sentAt);
    }

    public Optional<ZonedDateTime> getCompletedAt() {
        return Optional.ofNullable(completedAt);
    }

    public Optional<String> getCreatedByName() {
        return Optional.ofNullable(createdByName);
    }

    /**
     * estimatedDelivery is only present on letters
     */
    public Optional<ZonedDateTime> getEstimatedDelivery() {
        return Optional.ofNullable(estimatedDelivery);
    }

    public boolean isCostDataReady() {
            return isCostDataReady;
        }

    public double getCostInPounds() {
        return costInPounds;
    }

    // Getters for CostDetails fields
    public Optional<Integer> getBillableSmsFragments() {
        return Optional.ofNullable(billableSmsFragments);
    }

    public Optional<Double> getInternationalRateMultiplier() {
        return Optional.ofNullable(internationalRateMultiplier);
    }

    public Optional<Double> getSmsRate() {
        return Optional.ofNullable(smsRate);
    }

    public Optional<Integer> getBillableSheetsOfPaper() {
        return Optional.ofNullable(billableSheetsOfPaper);
    }

    public Optional<String> getPostageType() {
        return Optional.ofNullable(postageType);
    }

    @Override
    public String toString() {
        return "Notification{" +
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
                ", postcode='" + postcode + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", status='" + status + '\'' +
                ", templateId=" + templateId +
                ", templateVersion=" + templateVersion +
                ", templateUri='" + templateUri + '\'' +
                ", body='" + body + '\'' +
                ", subject='" + subject + '\'' +
                ", createdAt=" + createdAt +
                ", sentAt=" + sentAt +
                ", completedAt=" + completedAt +
                ", estimatedDelivery=" + estimatedDelivery +
                ", createdByName=" + createdByName +
                ", isCostDataReady=" + isCostDataReady +
                ", costInPounds=" + costInPounds +
                ", billableSmsFragments=" + billableSmsFragments +
                ", internationalRateMultiplier=" + internationalRateMultiplier +
                ", smsRate=" + smsRate +
                ", billableSheetsOfPaper=" + billableSheetsOfPaper +
                ", postageType='" + postageType + '\'' +
                '}';
    }
}
