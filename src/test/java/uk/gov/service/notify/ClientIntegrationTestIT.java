package uk.gov.service.notify;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.gov.service.notify.domain.NotificationStatus;
import uk.gov.service.notify.domain.NotificationType;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.gov.service.notify.domain.NotificationStatus.Letter.ACCEPTED;
import static uk.gov.service.notify.domain.NotificationStatus.Letter.RECEIVED;

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
        assertNotNull(notificationList);
        assertNotNull(notificationList.getNotifications());
        assertFalse(notificationList.getNotifications().isEmpty());
        // Just check the first notification in the list.
        assertNotification(notificationList.getNotifications().get(0));
        String baseUrl = System.getenv("NOTIFY_API_URL");
        assertEquals(URI.create(baseUrl + "/v2/notifications"), notificationList.getLinks().getCurrent());
        if (Objects.nonNull(notificationList.getLinks().getNext())) {
            URI nextUri = notificationList.getLinks().getNext();
            String olderThanId = nextUri.getQuery().substring(nextUri.getQuery().indexOf("older_than=") + "other_than=".length());
            NotifyNotificationListResponse nextList = client.getNotifications(null, null, null, UUID.fromString(olderThanId));
            assertNotNull(notificationList.getLinks().getCurrent());
            assertNotNull(nextList);
            assertNotNull(nextList.getNotifications());
        }
    }

    @Test
    public void testEmailNotificationWithoutPersonalisationReturnsErrorMessageIT() {
        NotificationClient client = getClient();
        try {
            client.sendEmail(getUUIDEnvVar("EMAIL_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_EMAIL"), null, null);
            fail("Expected NotificationClientException: Template missing personalisation: name");
        } catch (NotificationClientException e) {
            assert(e.getMessage().contains("Missing personalisation: name"));
            assert e.getHttpResult() == 400;
            assert(e.getMessage().contains("BadRequestError"));
        }
    }

    private static UUID getUUIDEnvVar(String envVar) {
        return UUID.fromString(System.getenv(envVar));
    }

    @Test
    public void testEmailNotificationWithValidEmailReplyToIdIT() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyEmailResponse emailResponse = sendEmailAndAssertResponse(client);

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

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

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

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
            assertTrue(ex.getMessage().contains("does not exist in database for service id"));
        }

        assertTrue(exceptionThrown);

    }

    @Test
    public void testEmailNotificationWithUploadedDocumentInPersonalisation() throws NotificationClientException, IOException {
        NotificationClient client = getClient();
        HashMap<String, Object> personalisation = new HashMap<>();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("one_page_pdf.pdf").getFile());
        byte [] fileContents = FileUtils.readFileToByteArray(file);

        Map<String, ?> documentFileObject = PrepareUploadHelper.prepareUpload(fileContents);
        personalisation.put("name", documentFileObject);

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
            assert(e.getMessage().contains("Missing personalisation: name"));
            assert(e.getMessage().contains("Status code: 400"));
        }
    }

    @Test
    public void testSmsNotificationWithValidSmsSenderIdIT() throws NotificationClientException {
        NotificationClient client = getClient("API_SENDING_KEY");

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

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

        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);

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
            assertTrue(ex.getMessage().contains("does not exist in database for service id"));
        }

        assertTrue(exceptionThrown);

    }

    @Test
    public void testSendAndGetNotificationWithReference() throws NotificationClientException {
        NotificationClient client = getClient();
        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueString = UUID.randomUUID().toString();
        personalisation.put("name", uniqueString);
        NotifyEmailResponse response = client.sendEmail(getUUIDEnvVar("EMAIL_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_EMAIL"), personalisation, uniqueString);
        assertNotificationEmailResponse(response, uniqueString);
        NotifyNotificationListResponse notifications = client.getNotifications(null, null, uniqueString, null);
        assertEquals(1, notifications.getNotifications().size());
        assertEquals(response.getNotificationId(), notifications.getNotifications().get(0).getId());
    }

    @Test
    public void testGetTemplateById() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyTemplateLetter template = (NotifyTemplateLetter)client.getTemplateById(getUUIDEnvVar("LETTER_TEMPLATE_ID"));
        assertEquals(System.getenv("LETTER_TEMPLATE_ID"), template.getId().toString());
        assertNotNull(template.getCreatedAt());
        assertNotNull(template.getType());
        assertNotNull(template.getBody());
        assertNotNull(template.getName());
        assertNotNull(template.getVersion());
        // FIXME FIXME FIXME - only required for email?
//        assertNotNull(template.getSubject());
        assertNotNull(template.getLetterContactBlock());
    }

    @Test
    public void testGetTemplateVersion() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyTemplateSms template = (NotifyTemplateSms)client.getTemplateVersion(getUUIDEnvVar("SMS_TEMPLATE_ID"), 1);
        assertEquals(System.getenv("SMS_TEMPLATE_ID"), template.getId().toString());
        assertNotNull(template.getCreatedAt());
        assertNotNull(template.getType());
        assertNotNull(template.getBody());
        assertNotNull(template.getName());
        assertNotNull(template.getVersion());
    }

    @Test
    public void testGetAllTemplates() throws NotificationClientException {
        NotificationClient client = getClient();
        NotifyTemplateListResponse templateList = client.getAllTemplates(null);
        assertTrue(2 <= templateList.getTemplates().size());
    }

    @Test
    public void testGenerateTemplatePreview() throws NotificationClientException {
        NotificationClient client = getClient();
        HashMap<String, Object> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);
        NotifyTemplatePreviewResponse template = client.generateTemplatePreview(getUUIDEnvVar("EMAIL_TEMPLATE_ID"), personalisation);
        assertEquals(getUUIDEnvVar("EMAIL_TEMPLATE_ID"), template.getId());
        assertNotNull(template.getType());
        assertNotNull(template.getBody());
        assertNotNull(template.getSubject());
        assertTrue(template.getBody().contains(uniqueName));
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
        assertFalse(response.getReceivedTextMessages().isEmpty());
        assertNotNull(response.getLinks().getCurrent());
        NotifyReceivedTextMessage receivedTextMessage = response.getReceivedTextMessages().get(0);
        assertNotNull(receivedTextMessage.getId());
        assertNotNull(receivedTextMessage.getNotifyNumber());
        assertNotNull(receivedTextMessage.getUserNumber());
        assertNotNull(receivedTextMessage.getContent());
        assertNotNull(receivedTextMessage.getCreatedAt());
        assertNotNull(receivedTextMessage.getServiceId());
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
        HashMap<String, String> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);
        NotifyEmailResponse response = client.sendEmail(getUUIDEnvVar("EMAIL_TEMPLATE_ID"),
                System.getenv("FUNCTIONAL_TEST_EMAIL"), personalisation, uniqueName);
        assertNotificationEmailResponse(response, uniqueName);
        return response;
    }

    private NotifySmsResponse sendSmsAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        HashMap<String, Object> personalisation = new HashMap<>();
        String uniqueName = UUID.randomUUID().toString();
        personalisation.put("name", uniqueName);
        NotifySmsResponse response = client.sendSms(getUUIDEnvVar("SMS_TEMPLATE_ID"), System.getenv("FUNCTIONAL_TEST_NUMBER"), personalisation, uniqueName);
        assertNotificationSmsResponse(response, uniqueName);
        return response;
    }

    private NotifyLetterResponse sendLetterAndAssertResponse(final NotificationClient client) throws NotificationClientException {
        HashMap<String, String> personalisation = new HashMap<>();
        String addressLine1 = UUID.randomUUID().toString();
        String addressLine2 = UUID.randomUUID().toString();
        String postcode = "SW1 1AA";
        personalisation.put("address_line_1", addressLine1);
        personalisation.put("address_line_2", addressLine2);
        personalisation.put("postcode", postcode);
        NotifyLetterResponse response = client.sendLetter(getUUIDEnvVar("LETTER_TEMPLATE_ID"), personalisation, addressLine1);
        assertNotificationLetterResponse(response, addressLine1);
        return response;
    }

    private void assertNotificationSmsResponse(final NotifySmsResponse response, final String uniqueName){
        assertNotNull(response);
        assertTrue(response.getContent().getBody().contains(uniqueName));
        assertEquals(uniqueName, response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getTemplate().getId());
        assertNotNull(response.getTemplate().getUri());
    }

    private void assertNotificationEmailResponse(final NotifyEmailResponse response, final String uniqueName){
        assertNotNull(response);
        assertTrue(response.getContent().getBody().contains(uniqueName));
        assertEquals(uniqueName, response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getContent().getSubject());
        assertNotNull(response.getContent().getFromEmail());
        assertNotNull(response.getTemplate().getUri());
        assertNotNull(response.getTemplate().getId());
    }

    private void assertNotificationEmailResponseWithDocumentInPersonalisation(final NotifyEmailResponse response, final String uniqueName){
        assertNotNull(response);
        assertTrue(response.getContent().getBody().contains("https://documents."));
        assertEquals(uniqueName, response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getContent().getSubject());
        assertNotNull(response.getContent().getFromEmail());
        assertNotNull(response.getTemplate().getUri());
        assertNotNull(response.getTemplate().getId());
    }

    private void assertNotificationLetterResponse(final NotifyLetterResponse response, final String addressLine1){
        assertNotNull(response);
        assertTrue(response.getContent().getBody().contains(addressLine1));
        assertEquals(addressLine1, response.getReference());
        assertNotNull(response.getNotificationId());
        assertNotNull(response.getTemplate().getId());
        assertNotNull(response.getTemplate().getUri());
    }

    private void assertNotification(NotifyNotification notification) {
        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertNotNull(notification.getTemplate().getId());
        assertNotNull(notification.getTemplate().getUri());
        assertNotNull(notification.getCreatedAt());
        assertNotNull(notification.getType());
        assertNull(notification.getCreatedByName());

        if(notification.getType().equals(NotificationType.sms)) {
            assertNotificationWhenSms((NotifyNotificationSms)notification);
        }
        if(notification.getType().equals(NotificationType.email)){
            assertNotificationWhenEmail((NotifyNotificationEmail)notification);
        }
        if(notification.getType().equals(NotificationType.letter)){
            assertNotificationWhenLetter((NotifyNotificationLetter)notification);
        }
    }

    private void assertNotificationWhenLetter(NotifyNotificationLetter notification) {
        // the other address lines are optional. A precompiled letter will only have address_line_1
        assertNotNull(notification.getLine1());
        // FIXME FIXME FIXME - only for precompiled letters?
//        assertNotNull(notification.getPostage());

        assertNotNull(notification.getStatus());
        assertTrue("expected status to be accepted or received", Arrays.asList(ACCEPTED, RECEIVED).contains(notification.getStatus()));
    }

    private void assertNotificationWhenEmail(NotifyNotificationEmail notification) {
        assertNotNull(notification.getSubject());
        assertNotNull(notification.getEmailAddress());

        assertNotNull(notification.getStatus());
        assertTrue("expected status to be created, sending or delivered", Arrays.asList(NotificationStatus.Email.CREATED, NotificationStatus.Email.SENDING, NotificationStatus.Email.DELIVERED).contains(notification.getStatus()));
    }

    private void assertNotificationWhenSms(NotifyNotificationSms notification) {
        assertNotNull(notification.getPhoneNumber());

        assertNotNull(notification.getStatus());
        assertTrue("expected status to be created, sending or delivered", Arrays.asList(NotificationStatus.Sms.CREATED, NotificationStatus.Sms.SENDING, NotificationStatus.Sms.DELIVERED).contains(notification.getStatus()));
    }

    private void assertPrecompiledLetterResponse(String reference, String postage, NotifyPrecompiledLetterResponse response) {
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(reference, response.getReference());
        assertEquals(postage, response.getPostage());
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

        assertNotEquals(0, pdfData.length);
        // check that we've got a pdf by looking for the magic bytes
        byte[] magicBytes = Arrays.copyOfRange(pdfData, 0, 5);
        String magicString = new String(magicBytes);
        assertEquals("%PDF-", magicString);
        assertTrue(magicString.startsWith("%PDF-"));
    }

}
