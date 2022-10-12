package uk.gov.service.notify;

import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RetentionPeriodDurationTest {

    @Test
    public void testCreatingRetentionPeriodDuration_WithValidData(){
        RetentionPeriodDuration duration = new RetentionPeriodDuration(52, ChronoUnit.WEEKS);

        assertEquals(52, duration.getAmount());
        assertEquals(ChronoUnit.WEEKS, duration.getUnit());

        // We've already checked that the values are correctly inserted; here we check the edge cases that are
        // still valid. As a result, there are no asserts - this test will however fail if the validation during
        // construction fails

        new RetentionPeriodDuration(1, ChronoUnit.WEEKS);
        new RetentionPeriodDuration(78, ChronoUnit.WEEKS);
    }

    @Test
    public void testCreatingRetentionPeriodDuration_WithInvalidUnit(){
        try {
            new RetentionPeriodDuration(52, ChronoUnit.FOREVER);
            fail("Did not get IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals(String.format(
                    "%s is not a valid unit. Only ChronoUnit.WEEKS is currently supported.",
                    ChronoUnit.FOREVER.name()
            ), e.getMessage());
        }
    }

    @Test
    public void testCreatingRetentionPeriodDuration_WithInvalidAmount_Weeks_TooLow(){
        try {
            new RetentionPeriodDuration(0, ChronoUnit.WEEKS);
            fail("Did not get IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals(
                    "When unit is ChronoUnit.WEEKS, value must be larger than 0 and smaller than 79",
                    e.getMessage()
            );
        }
    }

    @Test
    public void testCreatingRetentionPeriodDuration_WithInvalidAmount_Weeks_TooHigh(){
        try {
            new RetentionPeriodDuration(79, ChronoUnit.WEEKS);
            fail("Did not get IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals(
                    "When unit is ChronoUnit.WEEKS, value must be larger than 0 and smaller than 79",
                    e.getMessage()
            );
        }
    }

    @Test
    public void testCreatingStringFromDuration_Weeks(){
        RetentionPeriodDuration duration = new RetentionPeriodDuration(52, ChronoUnit.WEEKS);
        assertEquals("52 weeks", duration.toString());
    }
}
