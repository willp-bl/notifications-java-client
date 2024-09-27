package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.Objects;

public class NotifyTemplatePreviewRequest {

    @NotNull
    private final Map<String, ?> personalisation;

    public NotifyTemplatePreviewRequest(@JsonProperty("personalisation") Map<String, ?> personalisation) {
        this.personalisation = personalisation;
    }

    @JsonProperty("personalisation")
    public Map<String, ?> getPersonalisation() {
        return personalisation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyTemplatePreviewRequest that = (NotifyTemplatePreviewRequest) o;
        return Objects.equals(personalisation, that.personalisation);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(personalisation);
    }

    @Override
    public String toString() {
        return "NotifyTemplatePreviewRequest{" +
                "personalisation=" + personalisation +
                '}';
    }
}
