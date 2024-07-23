package uk.gov.service.notify;

import org.json.JSONObject;

import java.util.Optional;

public class CostDetails {
    private Integer billableSmsFragments;
    private Double internationalRateMultiplier;
    private Double smsRate;
    private Integer billableSheetsOfPaper;
    private String postage;

    public CostDetails(String content) {
        JSONObject responseBodyAsJson = new JSONObject(content);
        build(responseBodyAsJson);
    }

    public CostDetails(JSONObject data) {
        build(data);
    }

    private void build(JSONObject data) {
        billableSmsFragments = data.isNull("billable_sms_fragments") ? null : data.getInt("billable_sms_fragments");
        internationalRateMultiplier = data.isNull("international_rate_multiplier") ? null : data.getDouble("international_rate_multiplier");
        smsRate = data.isNull("sms_rate") ? null : data.getDouble("sms_rate");
        billableSheetsOfPaper = data.isNull("billable_sheets_of_paper") ? null : data.getInt("billable_sheets_of_paper");
        postage = data.isNull("postage") ? null : data.getString("postage");
    }

    public Optional<Integer> getBillableSmsFragments() {
        return Optional.ofNullable(billableSmsFragments);
    }

    public void setBillableSmsFragments(Integer billableSmsFragments) {
        this.billableSmsFragments = billableSmsFragments;
    }

    public Optional<Double> getInternationalRateMultiplier() {
        return Optional.ofNullable(internationalRateMultiplier);
    }

    public void setInternationalRateMultiplier(Double internationalRateMultiplier) {
        this.internationalRateMultiplier = internationalRateMultiplier;
    }

    public Optional<Double> getSmsRate() {
        return Optional.ofNullable(smsRate);
    }

    public void setSmsRate(Double smsRate) {
        this.smsRate = smsRate;
    }

    public Optional<Integer> getBillableSheetsOfPaper() {
        return Optional.ofNullable(billableSheetsOfPaper);
    }

    public void setBillableSheetsOfPaper(Integer billableSheetsOfPaper) {
        this.billableSheetsOfPaper = billableSheetsOfPaper;
    }

    public Optional<String> getPostage() {
        return Optional.ofNullable(postage);
    }

    public void setPostage(String postage) {
        this.postage = postage;
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
