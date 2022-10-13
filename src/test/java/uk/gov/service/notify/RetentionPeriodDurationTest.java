package uk.gov.service.notify;

import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;

public class RetentionPeriodDurationTest {

    @Test
    public void testCreatingRetentionPeriodDuration_WithValidData(){
        RetentionPeriodDuration duration = new RetentionPeriodDuration(52, ChronoUnit.WEEKS);

        assertEquals(52, duration.getAmount());
        assertEquals(ChronoUnit.WEEKS, duration.getUnit());
    }

    @Test
    public void testCreatingStringFromDuration(){
        RetentionPeriodDuration duration = new RetentionPeriodDuration(52, ChronoUnit.WEEKS);
        assertEquals("52 weeks", duration.toString());
    }
}
