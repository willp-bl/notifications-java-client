package uk.gov.service.notify;

import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RetentionPeriodDurationTest {

    @Test
    public void testCreatingRetentionPeriodDuration_WithValidData(){
        RetentionPeriodDuration duration = new RetentionPeriodDuration(52, ChronoUnit.WEEKS);

        assertThat(duration.getAmount()).isEqualTo(52);
        assertThat(duration.getUnit()).isEqualTo(ChronoUnit.WEEKS);
    }

    @Test
    public void testCreatingStringFromDuration(){
        RetentionPeriodDuration duration = new RetentionPeriodDuration(52, ChronoUnit.WEEKS);

        assertThat(duration).asString().isEqualTo("52 weeks");
    }
}
