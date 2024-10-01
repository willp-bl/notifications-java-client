package uk.gov.service.notify;

import uk.gov.service.notify.domain.NotificationStatus;

import java.util.List;
import java.util.Map;

public final class TestNotifications {

    /**
     * If you need to smoke test your integration with Notify on a regular basis,
     * you must use the following smoke test phone numbers.
     * https://docs.notifications.service.gov.uk/rest-api.html#smoke-testing
     */
    public static List<String> SMOKE_TEST_SMS_NUMBERS = List.of("07700900000",
            "07700900111",
            "07700900222");

    /**
     * If you need to smoke test your integration with Notify on a regular basis,
     * you must use the following smoke test email addresses.
     * https://docs.notifications.service.gov.uk/rest-api.html#smoke-testing
     */
    public static List<String> SMOKE_TEST_EMAIL_ADDRESSES = List.of("simulate-delivered@notifications.service.gov.uk",
            "simulate-delivered-2@notifications.service.gov.uk",
            "simulate-delivered-3@notifications.service.gov.uk");

    /**
     * Use a test key to test the performance of your service and its integration with GOV.UK Notify.
     *
     * Messages sent using a test key:
     *   * generate realistic responses
     *   * result in a delivered status
     *   * are not actually delivered to a recipient
     *   * do not appear on your dashboard
     *   * do not count against your text message and email allowances
     *
     * To test failure responses with a test key, use the following numbers
     *
     * See also https://docs.notifications.service.gov.uk/rest-api.html#test
     */
    public static Map<NotificationStatus.Sms, String> FIXED_API_RESPONSE_SMS = Map.ofEntries(
            Map.entry(NotificationStatus.Sms.TEMPORARY_FAILURE, "07700900003"),
            Map.entry(NotificationStatus.Sms.PERMANENT_FAILURE, "07700900002")
    );

    /**
     * Use a test key to test the performance of your service and its integration with GOV.UK Notify.
     *
     * Messages sent using a test key:
     *   * generate realistic responses
     *   * result in a delivered status
     *   * are not actually delivered to a recipient
     *   * do not appear on your dashboard
     *   * do not count against your text message and email allowances
     *
     * To test failure responses with a test key, use the following email addresses
     *
     * See also https://docs.notifications.service.gov.uk/rest-api.html#test
     */
    public static Map<NotificationStatus.Email, String> FIXED_API_RESPONSE_EMAIL = Map.ofEntries(
            Map.entry(NotificationStatus.Email.TEMPORARY_FAILURE, "temp-fail@simulator.notify"),
            Map.entry(NotificationStatus.Email.PERMANENT_FAILURE, "perm-fail@simulator.notify")
    );

    private TestNotifications() {}
}
