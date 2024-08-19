package uk.gov.service.notify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import uk.gov.service.notify.domain.NotifyEmailResponse;
import uk.gov.service.notify.domain.NotifyLetterResponse;
import uk.gov.service.notify.domain.NotifyNotification;
import uk.gov.service.notify.domain.NotifyNotificationEmail;
import uk.gov.service.notify.domain.NotifyNotificationLetter;
import uk.gov.service.notify.domain.NotifyNotificationListResponse;
import uk.gov.service.notify.domain.NotifyNotificationSms;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterResponse;
import uk.gov.service.notify.domain.NotifyReceivedTextMessage;
import uk.gov.service.notify.domain.NotifyReceivedTextMessagesResponse;
import uk.gov.service.notify.domain.NotifySmsResponse;
import uk.gov.service.notify.domain.NotifyTemplateLetter;
import uk.gov.service.notify.domain.NotifyTemplateListResponse;
import uk.gov.service.notify.domain.NotifyTemplatePreviewResponse;
import uk.gov.service.notify.domain.NotifyTemplateSms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.service.notify.domain.NotificationStatus.Email;
import static uk.gov.service.notify.domain.NotificationStatus.Letter;
import static uk.gov.service.notify.domain.NotificationStatus.Sms;

@EnabledIfEnvironmentVariable(named = "CLIENT_INTEGRATION_TEST_ENABLED", matches = "true")
public class ClientIntegrationTestIT {

    @Test
    public void testEmailNotificationIT() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyEmailResponse emailResponse = sendEmailAndAssertResponse(client);
        NotifyNotification notification = client.getNotificationById(emailResponse.getNotificationId());
        assertNotification(notification);
    }

    @Test
    public void testSmsNotificationIT() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifySmsResponse response = sendSmsAndAssertResponse(client);
        NotifyNotification notification = client.getNotificationById(response.getNotificationId());
        assertNotification(notification);
    }

    @Test
    public void testLetterNotificationIT() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyLetterResponse letterResponse = sendLetterAndAssertResponse(client);
        UUID notificationId = letterResponse.getNotificationId();
        NotifyNotification notification = client.getNotificationById(notificationId);
        assertNotification(notification);
        assertPdfResponse(client, notificationId);
    }


    @Test
    public void testGetAllNotifications() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyNotificationListResponse notificationList = client.getNotifications(null, null, null, null);
        assertThat(notificationList).isNotNull();
        assertThat(notificationList.getNotifications()).isNotNull();
        assertThat(notificationList.getNotifications()).isNotEmpty();
        // Just check the first notification in the list.
        assertNotification(notificationList.getNotifications().get(0));
        String baseUrl = System.getenv("NOTIFY_API_URL");
        assertThat(notificationList.getLinks().getCurrent()).isEqualTo(URI.create(baseUrl + "/v2/notifications"));
        if (Objects.nonNull(notificationList.getLinks().getNext())) {
            URI nextUri = notificationList.getLinks().getNext();
            String olderThanId = nextUri.getQuery().substring(nextUri.getQuery().indexOf("older_than=") + "other_than=".length());
            NotifyNotificationListResponse nextList = client.getNotifications(null, null, null, UUID.fromString(olderThanId));
            assertThat(notificationList.getLinks().getCurrent()).isNotNull();
            assertThat(nextList).isNotNull();
            assertThat(nextList.getNotifications()).isNotNull();
        }
    }

    @Test
    public void testEmailNotificationWithoutPersonalisationReturnsErrorMessageIT() {
        NotificationClient client = getClient();
        try {
            client.sendEmail(getUUIDEnvVar("EMAIL_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_EMAIL"), null, null);
            fail("Expected NotificationClientException: Template missing personalisation: name");
        } catch (NotificationClientHttpException e) {
            assertThat(e).hasMessageContaining("Missing personalisation: name");
            assertThat(e.getHttpResult()).isEqualTo(400);
            assertThat(e).hasMessageContaining("BadRequestError");
        } catch (NotificationClientException e) {
            fail("unexpected exception: "+e);
        }
    }

    private static UUID getUUIDEnvVar(String envVar) {
        return UUID.fromString(System.getenv(envVar));
    }

    @Test
    public void testEmailNotificationWithValidEmailReplyToIdIT() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyEmailResponse emailResponse = sendEmailAndAssertResponse(client);

        String uniqueName = UUID.randomUUID().toString();
        Map<String, String> personalisation = Map.of("name", uniqueName);

        NotifyEmailResponse response = client.sendEmail(
                getUUIDEnvVar("EMAIL_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_EMAIL"),
                personalisation,
                uniqueName,
                getUUIDEnvVar("EMAIL_REPLY_TO_ID"));

        assertNotificationEmailResponse(response, uniqueName);

        NotifyNotification notification = client.getNotificationById(emailResponse.getNotificationId());
        assertNotification(notification);
    }

    @Test
    public void testEmailNotificationWithInValidEmailReplyToIdIT() throws NotificationClientException {
        NotificationClient client = getClient();
        sendEmailAndAssertResponse(client);

        String uniqueName = UUID.randomUUID().toString();
        Map<String, String> personalisation = Map.of("name", uniqueName);

        UUID fake_uuid = UUID.randomUUID();

        boolean exceptionThrown = false;

        try {
            client.sendEmail(
                    getUUIDEnvVar("EMAIL_TEMPLATE_ID"),
                    System.getenv("FUNCTIONAL_TEST_EMAIL"),
                    personalisation,
                    uniqueName,
                    fake_uuid);
        } catch (final NotificationClientException ex){
            exceptionThrown = true;
            assertThat(ex).hasMessageContaining("does not exist in database for service id");
        }

        assertThat(exceptionThrown).isTrue();

    }

    @Test
    public void testEmailNotificationWithUploadedDocumentInPersonalisation() throws NotificationClientException, IOException {
        NotificationClient client = getClient();

        ClassLoader classLoader = getClass().getClassLoader();
        byte [] fileContents = classLoader.getResourceAsStream("one_page_pdf.pdf").readAllBytes();

        Map<String, ?> documentFileObject = PrepareUploadHelper.prepareUpload(fileContents);
        Map<String, Object> personalisation = Map.of("name", documentFileObject);

        String reference = UUID.randomUUID().toString();
        NotifyEmailResponse emailResponse = client.sendEmail(getUUIDEnvVar("EMAIL_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_EMAIL"),
                personalisation,
                reference
        );

        assertNotificationEmailResponseWithDocumentInPersonalisation(emailResponse, reference);
    }


    @Test
    public void testSmsNotificationWithoutPersonalisationReturnsErrorMessageIT() {
        NotificationClient client = getClient();
        try {
            client.sendSms(getUUIDEnvVar("SMS_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_NUMBER"), null, null);
            fail("Expected NotificationClientException: Template missing personalisation: name");
        } catch (NotificationClientException e) {
            assertThat(e).hasMessageContaining("Missing personalisation: name");
            assertThat(e).hasMessageContaining("Status code: 400");
        }
    }

    @Test
    public void testSmsNotificationWithValidSmsSenderIdIT() throws NotificationClientException {
        NotificationClient client = getClient("API_SENDING_KEY");

        String uniqueName = UUID.randomUUID().toString();
        Map<String, String> personalisation = Map.of("name", uniqueName);

        NotifySmsResponse response = client.sendSms(
                getUUIDEnvVar("SMS_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_NUMBER"),
                personalisation,
                uniqueName,
                getUUIDEnvVar("SMS_SENDER_ID"));

        assertNotificationSmsResponse(response, uniqueName);

        NotifyNotification notification = client.getNotificationById(response.getNotificationId());
        assertNotification(notification);
    }

    @Test
    public void testSmsNotificationWithInValidSmsSenderIdIT() {
        NotificationClient client = getClient();

        String uniqueName = UUID.randomUUID().toString();
        Map<String, String> personalisation = Map.of("name", uniqueName);

        UUID fake_uuid = UUID.randomUUID();

        boolean exceptionThrown = false;

        try {
            client.sendSms(
                    getUUIDEnvVar("SMS_TEMPLATE_ID"),
                    System.getenv("FUNCTIONAL_TEST_NUMBER"),
                    personalisation,
                    uniqueName,
                    fake_uuid);
        } catch (final NotificationClientException ex) {
            exceptionThrown = true;
            assertThat(ex).hasMessageContaining("does not exist in database for service id");
        }

        assertThat(exceptionThrown).isTrue();

    }

    @Test
    public void testSendAndGetNotificationWithReference() throws NotificationClientException {
        NotificationClient client = getClient();
        String uniqueName = UUID.randomUUID().toString();
        Map<String, String> personalisation = Map.of("name", uniqueName);

        NotifyEmailResponse response = client.sendEmail(getUUIDEnvVar("EMAIL_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_EMAIL"), personalisation, uniqueName);
        assertNotificationEmailResponse(response, uniqueName);
        NotifyNotificationListResponse notifications = client.getNotifications(null, null, uniqueName, null);
        assertThat(notifications.getNotifications()).hasSize(1);
        assertThat(notifications.getNotifications().get(0).getId()).isEqualTo(response.getNotificationId());
    }

    @Test
    public void testGetTemplateById() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyTemplateLetter template = (NotifyTemplateLetter)client.getTemplateById(getUUIDEnvVar("LETTER_TEMPLATE_ID"));
        assertThat(template.getId()).isEqualTo(getUUIDEnvVar("LETTER_TEMPLATE_ID"));
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getType()).isNotNull();
        assertThat(template.getBody()).isNotNull();
        assertThat(template.getName()).isNotNull();
        assertThat(template.getVersion()).isNotNull();
        // FIXME FIXME FIXME - only required for email?
//        assertNotNull(template.getSubject());
        assertThat(template.getLetterContactBlock()).isNotNull();
    }

    @Test
    public void testGetTemplateVersion() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyTemplateSms template = (NotifyTemplateSms)client.getTemplateVersion(getUUIDEnvVar("SMS_TEMPLATE_ID"), 1);
        assertThat(template.getId()).isEqualTo(getUUIDEnvVar("SMS_TEMPLATE_ID"));
        assertThat(template.getCreatedAt()).isNotNull();
        assertThat(template.getType()).isNotNull();
        assertThat(template.getBody()).isNotNull();
        assertThat(template.getName()).isNotNull();
        assertThat(template.getVersion()).isNotNull();
    }

    @Test
    public void testGetAllTemplates() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyTemplateListResponse templateList = client.getAllTemplates(null);
        assertThat(templateList.getTemplates()).hasSizeLessThanOrEqualTo(2);
    }

    @Test
    public void testGenerateTemplatePreview() throws NotificationClientException {
        NotificationClient client = getClient();
        String uniqueName = UUID.randomUUID().toString();
        Map<String, Object> personalisation = Map.of("name", uniqueName);
        NotifyTemplatePreviewResponse template = client.generateTemplatePreview(getUUIDEnvVar("EMAIL_TEMPLATE_ID"), personalisation);
        assertThat(template.getId()).isEqualTo(getUUIDEnvVar("EMAIL_TEMPLATE_ID"));
        assertThat(template.getType()).isNotNull();
        assertThat(template.getBody()).isNotNull();
        assertThat(template.getSubject()).isNotNull();
        assertThat(template.getBody()).contains(uniqueName);
    }

    @Test
    public void testGetReceivedTextMessages() throws NotificationClientException {
        NotificationClient client = getClient("INBOUND_SMS_QUERY_KEY");

        NotifyReceivedTextMessagesResponse response = client.getReceivedTextMessages(null);
        NotifyReceivedTextMessage receivedTextMessage = assertReceivedTextMessageList(response);

        testGetReceivedTextMessagesWithOlderThanId(receivedTextMessage.getId(), client);
    }

    @Test
    public void testSendPrecompiledLetterValidPDFFileIT() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        NotificationClient client = getClient();
        NotifyPrecompiledLetterResponse response =  client.sendPrecompiledLetter(reference, file);

        assertPrecompiledLetterResponse(reference, "second", response);
        assertPdfResponse(client, response.getId());
    }

    @Test
    public void testSendPrecompiledLetterValidPDFFileITWithPostage() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        NotificationClient client = getClient();
        NotifyPrecompiledLetterResponse response =  client.sendPrecompiledLetter(reference, file, "first");

        assertPrecompiledLetterResponse(reference, "first", response);

    }

    @Test
    public void testSendPrecompiledLetterWithInputStream() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        InputStream stream = Files.newInputStream(file.toPath());
        NotificationClient client = getClient();
        NotifyPrecompiledLetterResponse response =  client.sendPrecompiledLetterWithInputStream(reference, stream);

        assertPrecompiledLetterResponse(reference, "second", response);

    }

    @Test
    public void testSendPrecompiledLetterWithInputStreamWithPostage() throws Exception {
        String reference = UUID.randomUUID().toString();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        InputStream stream = Files.newInputStream(file.toPath());
        NotificationClient client = getClient();
        NotifyPrecompiledLetterResponse response =  client.sendPrecompiledLetterWithInputStream(reference, stream, "first");

        assertPrecompiledLetterResponse(reference, "first", response);

    }

    private NotifyReceivedTextMessage assertReceivedTextMessageList(NotifyReceivedTextMessagesResponse response) {
        assertThat(response.getReceivedTextMessages()).isNotEmpty();
        assertThat(response.getLinks().getCurrent()).isNotNull();
        NotifyReceivedTextMessage receivedTextMessage = response.getReceivedTextMessages().get(0);
        assertThat(receivedTextMessage.getId()).isNotNull();
        assertThat(receivedTextMessage.getNotifyNumber()).isNotNull();
        assertThat(receivedTextMessage.getUserNumber()).isNotNull();
        assertThat(receivedTextMessage.getContent()).isNotNull();
        assertThat(receivedTextMessage.getCreatedAt()).isNotNull();
        assertThat(receivedTextMessage.getServiceId()).isNotNull();
        return receivedTextMessage;
    }

    private void testGetReceivedTextMessagesWithOlderThanId(UUID id, NotificationClient client) throws NotificationClientException {
        NotifyReceivedTextMessagesResponse response = client.getReceivedTextMessages(id);
        assertReceivedTextMessageList(response);
    }

    private NotificationClient getClient(){
        String apiKey = System.getenv("API_KEY");
        String baseUrl = System.getenv("NOTIFY_API_URL");
        return new NotificationClient(apiKey, baseUrl);
    }

    private NotificationClient getClient(String api_key){
        String apiKey = System.getenv(api_key);
        String baseUrl = System.getenv("NOTIFY_API_URL");
        return new NotificationClient(apiKey, baseUrl);
    }

    private NotifyEmailResponse sendEmailAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        String uniqueName = UUID.randomUUID().toString();
        Map<String, String> personalisation = Map.of("name", uniqueName);
        NotifyEmailResponse response = client.sendEmail(getUUIDEnvVar("EMAIL_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_EMAIL"), personalisation, uniqueName);
        assertNotificationEmailResponse(response, uniqueName);
        return response;
    }

    private NotifySmsResponse sendSmsAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        String uniqueName = UUID.randomUUID().toString();
        Map<String, Object> personalisation = Map.of("name", uniqueName);
        NotifySmsResponse response = client.sendSms(getUUIDEnvVar("SMS_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_NUMBER"), personalisation, uniqueName);
        assertNotificationSmsResponse(response, uniqueName);
        return response;
    }

    private NotifyLetterResponse sendLetterAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        String addressLine1 = UUID.randomUUID().toString();
        String addressLine2 = UUID.randomUUID().toString();
        String postcode = "SW1 1AA";
        Map<String, String> personalisation = Map.of("address_line_1", addressLine1,
                "address_line_2", addressLine2,
                "postcode", postcode);
        NotifyLetterResponse response = client.sendLetter(getUUIDEnvVar("LETTER_TEMPLATE_ID"), personalisation, addressLine1);
        assertNotificationLetterResponse(response, addressLine1);
        return response;
    }

    private void assertNotificationSmsResponse(final NotifySmsResponse response, final String uniqueName){
        assertThat(response).isNotNull();
        assertThat(response.getContent().getBody()).contains(uniqueName);
        assertThat(response.getReference()).isEqualTo(uniqueName);
        assertThat(response.getNotificationId()).isNotNull();
        assertThat(response.getTemplate().getId()).isNotNull();
        assertThat(response.getTemplate().getUri()).isNotNull();
    }

    private void assertNotificationEmailResponse(final NotifyEmailResponse response, final String uniqueName){
        assertThat(response).isNotNull();
        assertThat(response.getContent().getBody()).contains(uniqueName);
        assertThat(response.getReference()).isEqualTo(uniqueName);
        assertThat(response.getNotificationId()).isNotNull();
        assertThat(response.getContent().getSubject()).isNotNull();
        assertThat(response.getContent().getFromEmail()).isNotNull();
        assertThat(response.getTemplate().getUri()).isNotNull();
        assertThat(response.getTemplate().getId()).isNotNull();
    }

    private void assertNotificationEmailResponseWithDocumentInPersonalisation(final NotifyEmailResponse response, final String uniqueName){
        assertThat(response).isNotNull();
        assertThat(response.getContent().getBody()).contains("https://documents.");
        assertThat(response.getReference()).isEqualTo(uniqueName);
        assertThat(response.getNotificationId()).isNotNull();
        assertThat(response.getContent().getSubject()).isNotNull();
        assertThat(response.getContent().getFromEmail()).isNotNull();
        assertThat(response.getTemplate().getUri()).isNotNull();
        assertThat(response.getTemplate().getId()).isNotNull();
    }

    private void assertNotificationLetterResponse(final NotifyLetterResponse response, final String addressLine1){
        assertThat(response).isNotNull();
        assertThat(response.getContent().getBody()).contains(addressLine1);
        assertThat(response.getReference()).isEqualTo(addressLine1);
        assertThat(response.getNotificationId()).isNotNull();
        assertThat(response.getTemplate().getId()).isNotNull();
        assertThat(response.getTemplate().getUri()).isNotNull();
    }

    private void assertNotification(NotifyNotification notification) {
        assertThat(notification).isNotNull();
        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getTemplate().getId()).isNotNull();
        assertThat(notification.getTemplate().getUri()).isNotNull();
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getType()).isNotNull();
        assertThat(notification.getCreatedByName()).isNull();

        switch(notification.getType()) {
            case sms: {
                assertNotificationWhenSms((NotifyNotificationSms)notification);
                break;
            }
            case email: {
                assertNotificationWhenEmail((NotifyNotificationEmail)notification);
                break;
            }
            case letter: {
                assertNotificationWhenLetter((NotifyNotificationLetter)notification);
                break;
            }
        }
    }

    private void assertNotificationWhenLetter(NotifyNotificationLetter notification) {
        // the other address lines are optional. A precompiled letter will only have address_line_1
        assertThat(notification.getLine1()).isNotNull();
        // FIXME FIXME FIXME - only for precompiled letters?
//        assertNotNull(notification.getPostage());

        assertThat(notification.getStatus()).isNotNull();
        assertThat(notification.getStatus())
                .withFailMessage("expected status to be accepted or received")
                .isIn(List.of(Letter.ACCEPTED, Letter.RECEIVED));
    }

    private void assertNotificationWhenEmail(NotifyNotificationEmail notification) {
        assertThat(notification.getSubject()).isNotNull();
        assertThat(notification.getEmailAddress()).isNotNull();

        assertThat(notification.getStatus()).isNotNull();
        assertThat(notification.getStatus())
                .withFailMessage("expected status to be created, sending or delivered")
                .isIn(List.of(Email.CREATED, Email.SENDING, Email.DELIVERED));
    }

    private void assertNotificationWhenSms(NotifyNotificationSms notification) {
        assertThat(notification.getPhoneNumber()).isNotNull();

        assertThat(notification.getStatus()).isNotNull();
        assertThat(notification.getStatus())
                .withFailMessage("expected status to be created, sending or delivered")
                .isIn(List.of(Sms.CREATED, Sms.SENDING, Sms.DELIVERED));
    }

    private void assertPrecompiledLetterResponse(String reference, String postage, NotifyPrecompiledLetterResponse response) {
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getReference()).isEqualTo(reference);
        assertThat(response.getPostage()).isEqualTo(postage);
    }

    private void assertPdfResponse(NotificationClient client, UUID notificationId) throws NotificationClientException {
        byte[] pdfData;
        int count = 0;
        while (true) {
            try {
                pdfData = client.getPdfForLetter(notificationId);
                break;
            } catch (NotificationClientException e) {
                if (!e.getMessage().contains("PDFNotReadyError")) {
                    throw e;
                }

                count += 1;
                if (count > 10) { // total time slept at this point is 55 seconds
                    throw e;
                } else {
                    try {
                        Thread.sleep(count * 1000);
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        assertThat(pdfData.length).isGreaterThan(0);
        // check that we've got a pdf by looking for the magic bytes
        byte[] magicBytes = Arrays.copyOfRange(pdfData, 0, 5);
        String magicString = new String(magicBytes);
        assertThat(magicString).isEqualTo("%PDF-");
        assertThat(magicString).startsWith("%PDF-");
    }

}
