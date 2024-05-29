package uk.gov.service.notify;


import uk.gov.service.notify.domain.NotificationType;
import uk.gov.service.notify.domain.NotifyEmailResponse;
import uk.gov.service.notify.domain.NotifyLetterResponse;
import uk.gov.service.notify.domain.NotifyNotification;
import uk.gov.service.notify.domain.NotifyNotificationListResponse;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterResponse;
import uk.gov.service.notify.domain.NotifyReceivedTextMessagesResponse;
import uk.gov.service.notify.domain.NotifySmsResponse;
import uk.gov.service.notify.domain.NotifyTemplate;
import uk.gov.service.notify.domain.NotifyTemplateListResponse;
import uk.gov.service.notify.domain.NotifyTemplatePreviewResponse;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

public interface NotificationClientApi {

    /**
     * The sendEmail method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible on the template page in the application.
     * @param emailAddress    The email address
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>NotifyEmailResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-an-email-error-codes
     */
    NotifyEmailResponse sendEmail(UUID templateId, String emailAddress, Map<String, ?> personalisation, String reference) throws NotificationClientException;


    /**
     * The sendEmail method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible on the template page in the application.
     * @param emailAddress    The email address
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @param emailReplyToId  An optional identifier for a reply to email address for the notification, rather than use the service default.
     *                        Service emailReplyToIds can be accessed via the service settings / manage email reply to addresses page.
     *                        Omit this argument to use the default service email reply to address.
     * @return <code>NotifyEmailResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-an-email-error-codes
     */
    NotifyEmailResponse sendEmail(UUID templateId, String emailAddress, Map<String, ?> personalisation, String reference, UUID emailReplyToId) throws NotificationClientException;

    /**
     * The sendEmail method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible on the template page in the application.
     * @param emailAddress    The email address
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @param emailReplyToId  An optional identifier for a reply to email address for the notification, rather than use the service default.
     *                        Service emailReplyToIds can be accessed via the service settings / manage email reply to addresses page.
     *                        Omit this argument to use the default service email reply to address.
     * @param oneClickUnsubscribeURL A link so users can unsubscribe, see https://www.notifications.service.gov.uk/using-notify/unsubscribe-links
     * @return <code>NotifyEmailResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-an-email-error-codes
     */
    NotifyEmailResponse sendEmail(UUID templateId, String emailAddress, Map<String, ?> personalisation, String reference, UUID emailReplyToId, URI oneClickUnsubscribeURL) throws NotificationClientException;

    /**
     * The sendSms method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible from the template page in the application.
     * @param phoneNumber     The mobile phone number
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>NotifySmsResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#error-codes
     */
    NotifySmsResponse sendSms(UUID templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException;

    /**
     * The sendSms method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible from the template page in the application.
     * @param phoneNumber     The mobile phone number
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @param smsSenderId     An optional identifier for the text message sender of the notification, rather than use the service default.
     *                        Service smsSenderIds can be accessed via the service settings / manage text message senders page.
     *                        Omit this argument to use the default service text message sender.
     * @return <code>NotifySmsResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#error-codes
     */
    NotifySmsResponse sendSms(UUID templateId, String phoneNumber, Map<String, ?> personalisation, String reference, UUID smsSenderId) throws NotificationClientException;

    /**
     * The sendLetter method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      Find templateId by clicking API info for the template you want to send
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob.
     *                        Must include the keys "address_line_1", "address_line_2" and "postcode".
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>NotifyLetterResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-a-letter-error-codes
     */
    NotifyLetterResponse sendLetter(UUID templateId, Map<String, ?> personalisation, String reference) throws NotificationClientException;

    /**
     * The sendPrecompiledLetter method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param reference                 A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                                  This reference can be unique or used used to refer to a batch of notifications.
     *                                  Cannot be an empty string or null for precompiled PDF files.
     * @param precompiledPDF            A file containing a PDF conforming to the Notify standards for printing.
     *                                  The file must be a PDF and cannot be null.
     * @return <code>NotifyPrecompiledLetterResponse</code>
     *
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-a-precompiled-letter-error-codes
     */
    NotifyPrecompiledLetterResponse sendPrecompiledLetter(String reference, File precompiledPDF) throws NotificationClientException;

    /**
     * The sendPrecompiledLetter method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param reference                 A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                                  This reference can be unique or used used to refer to a batch of notifications.
     *                                  Cannot be an empty string or null for precompiled PDF files.
     * @param precompiledPDF            A file containing a PDF conforming to the Notify standards for printing.
     *                                  The file must be a PDF and cannot be null.
     * @param postage                   You can choose first or second class postage for your precompiled letter.
     *                                  Set the value to first for first class, or second for second class. If you do not pass in this argument, the postage will default to second class.
     * @return <code>NotifyPrecompiledLetterResponse</code>
     *
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-a-precompiled-letter-error-codes
     */
    NotifyPrecompiledLetterResponse sendPrecompiledLetter(String reference, File precompiledPDF, String postage) throws NotificationClientException;

    /**
     * The sendPrecompiledLetterWithInputStream method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param reference                 A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                                  This reference can be unique or used used to refer to a batch of notifications.
     *                                  Cannot be an empty string or null for precompiled PDF files.
     * @param stream                    An <code>InputStream</code> conforming to the Notify standards for printing.
     *                                  The InputStream cannot be null.
     * @return <code>NotifyPrecompiledLetterResponse</code>
     *
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-a-precompiled-letter-error-codes
     */
    NotifyPrecompiledLetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream) throws NotificationClientException;

    /**
     * The sendPrecompiledLetterWithInputStream method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param reference                 A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                                  This reference can be unique or used used to refer to a batch of notifications.
     *                                  Cannot be an empty string or null for precompiled PDF files.
     * @param stream                    An <code>InputStream</code> conforming to the Notify standards for printing.
     *                                  The InputStream cannot be null.
     * @param postage                   You can choose first or second class postage for your precompiled letter.
     *                                  Set the value to first for first class, or second for second class. If you do not pass in this argument, the postage will default to second class.

     * @return <code>NotifyPrecompiledLetterResponse</code>
     *
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-a-precompiled-letter-error-codes
     */
    NotifyPrecompiledLetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream, String postage) throws NotificationClientException;

    /**
     * The getNotificationById method will return a <code>Notification</code> for a given notification id.
     * The id can be retrieved from the <code>NotificationResponse</code> of a <code>sendEmail</code>, <code>sendLetter</code> or <code>sendSms</code> request.
     *
     * @param notificationId The id of the notification.
     * @return <code>NotifyNotification</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#get-the-status-of-one-message-error-codes
     */
    NotifyNotification getNotificationById(UUID notificationId) throws NotificationClientException;

    /**
     * The getPdfForLetter method will return a <code>byte[]</code> containing the PDF contents of a given letter notification.
     * The id can be retrieved from the <code>NotificationResponse</code> of a <code>sendLetter</code>.
     *
     * @param notificationId The id of the notification.
     * @return <code>byte[]</code> The raw pdf data.
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#get-a-pdf-for-a-letter-notification-error-codes
     */
    byte[] getPdfForLetter(UUID notificationId) throws NotificationClientException;

    /**
     * The getNotifications method will create a GET HTTPS request to retrieve all the notifications.
     *
     * @param status If status is not empty or null notifications will only return notifications for the given status.
     *               Possible statuses are created|sending|delivered|permanent-failure|temporary-failure|technical-failure
     * @param notificationType If notification_type is not empty or null only notifications of the given status will be returned.
     *                          Possible notificationTypes are sms|email
     * @param reference If reference is not empty or null only the notifications with that reference are returned.
     * @param olderThanId If olderThanId is not null only the notifications older than that notification id are returned.
     * @return <code>NotifyNotificationListResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#get-the-status-of-multiple-messages-error-codes
     */
    NotifyNotificationListResponse getNotifications(String status, NotificationType notificationType, String reference, UUID olderThanId) throws NotificationClientException;

    /**
     * The getTemplateById returns a <code>Template</code> given the template id.
     *
     * @param templateId The template id is visible on the template page in the application.
     * @return <code>NotifyTemplate</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#get-a-template-by-id-error-codes
     */
    NotifyTemplate getTemplateById(UUID templateId) throws NotificationClientException;

    /**
     * The getTemplateVersion returns a <code>Template</code> given the template id and version.
     *
     * @param templateId The template id is visible on the template page in the application.
     * @param version The version of the template to return
     * @return <code>NotifyTemplate</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#get-a-template-by-id-and-version-error-codes
     */
    NotifyTemplate getTemplateVersion(UUID templateId, int version) throws NotificationClientException;

    /**
     * Returns all the templates for your service. Filtered by template type if not null.
     *
     * @param templateType If templateType is not empty or null templates will be filtered by type.
     *          Possible template types are email|sms|letter
     * @return <code>NotifyTemplateListResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#get-a-template-by-id-error-codes
     */
    NotifyTemplateListResponse getAllTemplates(NotificationType templateType) throws NotificationClientException;

    /**
     * The generateTemplatePreview returns a template with the placeholders replaced with the given personalisation.
     *
     * @param templateId The template id is visible from the template page in the application.
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @return <code>NotifyTemplatePreviewResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#generate-a-preview-template-error-codes
     */
    NotifyTemplatePreviewResponse generateTemplatePreview(UUID templateId, Map<String, Object> personalisation) throws NotificationClientException;

    /**
     * The getReceivedTextMessages returns a list of <code>ReceivedTextMessage</code>, the list is sorted by createdAt descending.
     * @param olderThanId If olderThanId is not empty or null only the received text messages older than that id are returned.
     * @return <code>NotifyReceivedTextMessagesResponse</code>
     * @throws NotificationClientException
     */
    NotifyReceivedTextMessagesResponse getReceivedTextMessages(UUID olderThanId) throws NotificationClientException;
}
