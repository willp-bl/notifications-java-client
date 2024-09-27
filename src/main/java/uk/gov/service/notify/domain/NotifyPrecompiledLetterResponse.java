package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;
import java.util.UUID;

public class NotifyPrecompiledLetterResponse {

    @NotNull
    private final UUID id;
    @NotEmpty
    private final String reference;
    @NotNull
    private final Postage postage;

    public NotifyPrecompiledLetterResponse(@JsonProperty("id") UUID id,
                                           @JsonProperty("reference") String reference,
                                           @JsonProperty("postage") Postage postage) {
        this.reference = reference;
        this.id = id;
        this.postage = postage;
    }

    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("postage")
    public Postage getPostage() {
        return postage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotifyPrecompiledLetterResponse that = (NotifyPrecompiledLetterResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(reference, that.reference) && Objects.equals(postage, that.postage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reference, postage);
    }

    @Override
    public String toString() {
        return "NotifyPrecompiledLetterResponse{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", postage='" + postage + '\'' +
                '}';
    }
}
