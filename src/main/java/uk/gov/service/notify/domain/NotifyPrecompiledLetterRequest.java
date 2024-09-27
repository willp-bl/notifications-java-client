package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class NotifyPrecompiledLetterRequest {

    @NotEmpty
    private final String reference;
    @NotEmpty
    private final String content;
    @NotNull
    private final Postage postage;

    public NotifyPrecompiledLetterRequest(@JsonProperty("reference") String reference,
                                          @JsonProperty("content") String content,
                                          @JsonProperty("postage") Postage postage) {
        this.reference = reference;
        this.content = content;
        this.postage = postage;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("postage")
    public Postage getPostage() {
        return postage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyPrecompiledLetterRequest that = (NotifyPrecompiledLetterRequest) o;
        return Objects.equals(reference, that.reference) && Objects.equals(content, that.content) && Objects.equals(postage, that.postage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, content, postage);
    }

    @Override
    public String toString() {
        return "NotifyPrecompiledLetterRequest{" +
                "reference='" + reference + '\'' +
                ", content='" + content + '\'' +
                ", postage='" + postage + '\'' +
                '}';
    }
}
