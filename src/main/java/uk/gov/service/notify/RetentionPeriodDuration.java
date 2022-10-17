package uk.gov.service.notify;

import java.time.temporal.ChronoUnit;
import java.util.Locale;

/***
 * Represents a retention period duration for files for file upload
 */
public class RetentionPeriodDuration {
    private final int amount;
    private final ChronoUnit unit;

    /***
     * Create a RetentionPeriod instance for a specific period of time.
     *
     * @param amount - The amount of units of time. Example: If unit is
     *               ChronoUnit.WEEKS, and amount is 52,
     *               then the duration is 52 weeks.
     * @param unit   - The unit of time that is being represented as a ChronoUnit.
     *               Currently only supports ChronoUnit.WEEKS.
     */
    public RetentionPeriodDuration(int amount, ChronoUnit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public int getAmount() {
        return amount;
    }

    public ChronoUnit getUnit() {
        return unit;
    }

    /***
     * The toString method converts the object into a string suitable for processing
     * by the Notifications API
     *
     * @return A string, representing a retention period duration for files, that
     *         complies with the API's expectations
     */
    @Override
    public String toString() {
        return String.format(
                "%d %s",
                getAmount(),
                getUnit().toString().toLowerCase(Locale.ROOT));
    }
}
