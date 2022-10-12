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
     * Creates an employee with the specified name and
     * @param amount - The amount of units of time. Example: If unit is ChronoUnit.WEEKS, and amount is 52,
     *               then the duration is 52 weeks.
     * @param unit - The unit of time that is being represented as a ChronoUnit. Currently only supports
     *             ChronoUnit.WEEKS until API changes allow for other unit types
     */
    public RetentionPeriodDuration(int amount, ChronoUnit unit) {
        this.amount = amount;
        this.unit = unit;
        validate();
    }

    public int getAmount() {
        return amount;
    }

    public ChronoUnit getUnit() {
        return unit;
    }

    /***
     * Validates the object; this should be run when the object is constructed
     * @throws IllegalArgumentException - This is thrown whenever either amount or unit are considered invalid
     */
    public void validate(){
        if(this.unit == ChronoUnit.WEEKS){
            if(this.amount < 1 || this.amount > 78){
                throw new IllegalArgumentException(
                        "When unit is ChronoUnit.WEEKS, value must be larger than 0 and smaller than 79"
                );
            }
        } else {
            // Currently only Weeks is supported by the API; this can be updated when
            // API changes allows for different values, or specifically when DAYS is added (as then we can
            // convert times to days with ChronoUnit
            throw new IllegalArgumentException(
                    String.format(
                            "%s is not a valid unit. Only ChronoUnit.WEEKS is currently supported.",
                            this.unit.name()
                    )
            );
        }
    }

    /***
     * The toString method converts the object into a string suitable for processing by the Notifications API
     * @return A string, representing a retention period duration for files, that complies with the API's expectations
     */
    @Override
    public String toString(){
        // The unit has to be lower cased for the API to accept it
        return String.format(
                "%d %s",
                getAmount(),
                getUnit().toString().toLowerCase(Locale.ROOT)
        );
    }
}
