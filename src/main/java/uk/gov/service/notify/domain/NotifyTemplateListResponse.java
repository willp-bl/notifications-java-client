package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class NotifyTemplateListResponse {

    private final List<NotifyTemplate> templates;

    public NotifyTemplateListResponse(@JsonProperty("templates") List<NotifyTemplate> templates) {
        this.templates = templates;
    }

    @JsonProperty("templates")
    public List<NotifyTemplate> getTemplates() {
        return templates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyTemplateListResponse that = (NotifyTemplateListResponse) o;
        return Objects.equals(templates, that.templates);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(templates);
    }

    @Override
    public String toString() {
        return "NotifyTemplateListResponse{" +
                "templates=" + templates +
                '}';
    }
}
