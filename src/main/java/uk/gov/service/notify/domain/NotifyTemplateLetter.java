package uk.gov.service.notify.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class NotifyTemplateLetter extends NotifyTemplate {

    private final String letterContactBlock;

    public NotifyTemplateLetter(@JsonProperty("id") UUID id,
                                @JsonProperty("name") String name,
                                @JsonProperty("type") NotificationType type,
                                @JsonProperty("created_at") ZonedDateTime createdAt,
                                @JsonProperty("updated_at") ZonedDateTime updatedAt,
                                @JsonProperty("version") int version,
                                @JsonProperty("created_by") String createdBy,
                                @JsonProperty("body") String body,
                                @JsonProperty("letter_contact_block") String letterContactBlock) {
        super(id, name, type, createdAt, updatedAt, version, createdBy, body);
        this.letterContactBlock = letterContactBlock;
    }

    @JsonProperty("letter_contact_block")
    public String getLetterContactBlock() {
        return letterContactBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NotifyTemplateLetter that = (NotifyTemplateLetter) o;
        return Objects.equals(letterContactBlock, that.letterContactBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), letterContactBlock);
    }

    @Override
    public String toString() {
        return "NotifyTemplateLetter{" +
                "letterContactBlock='" + letterContactBlock + '\'' +
                "} " + super.toString();
    }
}
