package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Postage {
    @JsonProperty("first") FIRST("first"),
    @JsonProperty("second") SECOND("second"),
    @JsonProperty("europe") EUROPE("europe"),
    @JsonProperty("rest-of-world") REST_OF_WORLD("rest-of-world");

    private final String postage;

    Postage(String postage) {
        this.postage = postage;
    }

    public String getPostage() {
        return postage;
    }
}