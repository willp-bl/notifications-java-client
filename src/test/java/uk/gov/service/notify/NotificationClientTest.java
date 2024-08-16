package uk.gov.service.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.apache.commons.io.IOUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.service.notify.domain.NotificationType;
import uk.gov.service.notify.domain.NotifyEmailRequest;
import uk.gov.service.notify.domain.NotifyEmailResponse;
import uk.gov.service.notify.domain.NotifyLetterRequest;
import uk.gov.service.notify.domain.NotifyLetterResponse;
import uk.gov.service.notify.domain.NotifyNotification;
import uk.gov.service.notify.domain.NotifyNotificationEmail;
import uk.gov.service.notify.domain.NotifyNotificationLetter;
import uk.gov.service.notify.domain.NotifyNotificationListResponse;
import uk.gov.service.notify.domain.NotifyNotificationSms;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterRequest;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterResponse;
import uk.gov.service.notify.domain.NotifyReceivedTextMessagesResponse;
import uk.gov.service.notify.domain.NotifySmsRequest;
import uk.gov.service.notify.domain.NotifySmsResponse;
import uk.gov.service.notify.domain.NotifyTemplate;
import uk.gov.service.notify.domain.NotifyTemplateEmail;
import uk.gov.service.notify.domain.NotifyTemplateLetter;
import uk.gov.service.notify.domain.NotifyTemplateListResponse;
import uk.gov.service.notify.domain.NotifyTemplatePreviewRequest;
import uk.gov.service.notify.domain.NotifyTemplatePreviewResponse;

import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.unauthorized;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NotificationClientTest {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            // the following two lines are needed to (de)serialize ZonedDateTime in ISO8601 format
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private static final String SERVICE_ID = UUID.randomUUID().toString();
    private static final String API_KEY = UUID.randomUUID().toString();
    private static final String COMBINED_API_KEY = "Api_key_name-" + SERVICE_ID + "-" + API_KEY;
    private String BASE_URL;

    /**
     * A new wireMockRule is created for every test case (as opposed to using @ClassRule) which would use only one
     */
    @RegisterExtension
    private static final WireMockExtension wireMockRule = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    public void beforeEach() {
        this.BASE_URL = "http://localhost:" + wireMockRule.getPort();
    }

    @Test
    public void sendPrecompiledLetterBlankReferenceErrors() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        final File pdfFile = new File(this.getClass().getClassLoader().getResource("one_page_pdf.pdf").getFile());

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter(null, pdfFile));

        assertThat(e).hasMessage("reference cannot be null or empty");
    }

    @Test
    public void sendPrecompiledLetterEmptyFileErrors() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        final File pdfFile = new File(this.getClass().getClassLoader().getResource("empty_file.pdf").getFile());

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter("a reference", pdfFile));

        assertThat(e).hasMessage("precompiledPDF cannot be null or empty");
    }

    @Test
    public void sendPrecompiledLetterBase64EncodedPDFFileIsNull() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetterWithInputStream("reference", null));

        assertThat(e).hasMessage("Input stream cannot be null");
    }

    @Test
    public void sendPrecompiledLetterInputStreamIsNull() {
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter("reference", null));

        assertThat(e).hasMessage("File cannot be null");
    }

    @Test
    public void testSendPrecompiledLetterNotPDF() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("not_a_pdf.txt").getFile());
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendPrecompiledLetter("reference", file));

        assertThat(e).hasMessage("base64EncodedPDFFile is not a PDF");
    }

    @Test
    public void testShouldThrowNotificationExceptionOnErrorResponseCodeAndNoErrorStream() {
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(notFound()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        UUID templateId = UUID.randomUUID();

        NotificationClientHttpException e = assertThrows(NotificationClientHttpException.class,
                () -> client.sendSms(templateId, "aPhoneNumber", emptyMap(), "aReference"));

        assertThat(e.getHttpResult()).isEqualTo(404);
        assertThat(e).hasMessage("Status code: 404 unexpected response code, expected 201");

        validateRequest();
    }

    @Test
    public void testSendEmailHandlesErrors() {
        wireMockRule.stubFor(post("/v2/notifications/email")
                .willReturn(serverError()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        UUID templateId = UUID.randomUUID();
        UUID emailReplyToId = UUID.randomUUID();

        NotificationClientHttpException e = assertThrows(NotificationClientHttpException.class,
                () -> client.sendEmail(templateId, "anEmailAddress", emptyMap(), "aReference", emailReplyToId));

        assertThat(e.getHttpResult()).isEqualTo(500);
        assertThat(e).hasMessage("Status code: 500 unexpected response code, expected 201");

        validateRequest();
    }

    @Test
    public void testSendEmailWithoutUnsubscribeURL() throws NotificationClientException, IOException {
        NotifyEmailResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_email_response.json"), NotifyEmailResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/email")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        Map<String, Object> personalisation = Map.of("application_date", "2018-01-01");
        UUID templateId = UUID.randomUUID();
        UUID emailReplyToId = UUID.randomUUID();

        NotifyEmailResponse actual = client.sendEmail(templateId, "anEmailAddress", personalisation, "aReference", emailReplyToId);

        assertThat(actual.getNotificationId()).isEqualTo(expected.getNotificationId());
        assertThat(actual.getReference()).isEqualTo(expected.getReference());
        assertThat(actual.getContent().getBody()).isEqualTo(expected.getContent().getBody());
        assertThat(actual.getContent().getSubject()).isEqualTo(expected.getContent().getSubject());
        assertThat(actual.getContent().getFromEmail()).isEqualTo(expected.getContent().getFromEmail());
        assertThat(actual.getUri()).isEqualTo(expected.getUri());
        assertThat(actual.getTemplate().getId()).isEqualTo(expected.getTemplate().getId());
        assertThat(actual.getTemplate().getVersion()).isEqualTo(expected.getTemplate().getVersion());
        assertThat(actual.getTemplate().getUri()).isEqualTo(expected.getTemplate().getUri());
        assertThat(actual.getOneClickUnsubscribeURL()).isNull();

        LoggedRequest request = validateRequest();
        NotifyEmailRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyEmailRequest.class);
        assertThat(requestReceivedByNotifyApi.getEmailAddress()).isEqualTo("anEmailAddress");
        assertThat(requestReceivedByNotifyApi.getTemplateId()).isEqualTo(templateId);
        assertThat(requestReceivedByNotifyApi.getPersonalisation()).isEqualTo(personalisation);
        assertThat(requestReceivedByNotifyApi.getReference()).isEqualTo("aReference");
        assertThat(requestReceivedByNotifyApi.getEmailReplyToId()).isEqualTo(emailReplyToId);
        assertThat(requestReceivedByNotifyApi.getOneClickUnsubscribeURL()).isNull();
    }

    @Test
    public void testSendEmailWithUnsubscribeURL() throws NotificationClientException, IOException {
        NotifyEmailResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_email_unsubscribe_response.json"), NotifyEmailResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/email")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        Map<String, Object> personalisation = Map.of("application_date", "2018-01-01");
        URI oneClickUnsubscribeURL = Objects.requireNonNull(expected.getOneClickUnsubscribeURL());
        UUID templateId = Objects.requireNonNull(expected.getTemplate().getId());
        UUID emailReplyToId = UUID.randomUUID();

        NotifyEmailResponse actual = client.sendEmail(templateId, "anEmailAddress", personalisation, "aReference", emailReplyToId, oneClickUnsubscribeURL);

        assertThat(actual.getNotificationId()).isEqualTo(expected.getNotificationId());
        assertThat(actual.getReference()).isEqualTo(expected.getReference());
        assertThat(actual.getContent().getBody()).isEqualTo(expected.getContent().getBody());
        assertThat(actual.getContent().getSubject()).isEqualTo(expected.getContent().getSubject());
        assertThat(actual.getContent().getFromEmail()).isEqualTo(expected.getContent().getFromEmail());
        assertThat(actual.getUri()).isEqualTo(expected.getUri());
        assertThat(actual.getTemplate().getId()).isEqualTo(expected.getTemplate().getId());
        assertThat(actual.getTemplate().getVersion()).isEqualTo(expected.getTemplate().getVersion());
        assertThat(actual.getTemplate().getUri()).isEqualTo(expected.getTemplate().getUri());
        assertThat(actual.getOneClickUnsubscribeURL()).isEqualTo(expected.getOneClickUnsubscribeURL());

        LoggedRequest request = validateRequest();
        NotifyEmailRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyEmailRequest.class);
        assertThat(requestReceivedByNotifyApi.getEmailAddress()).isEqualTo("anEmailAddress");
        assertThat(requestReceivedByNotifyApi.getTemplateId()).isEqualTo(templateId);
        assertThat(requestReceivedByNotifyApi.getPersonalisation()).isEqualTo(personalisation);
        assertThat(requestReceivedByNotifyApi.getReference()).isEqualTo("aReference");
        assertThat(requestReceivedByNotifyApi.getEmailReplyToId()).isEqualTo(emailReplyToId);
        assertThat(requestReceivedByNotifyApi.getOneClickUnsubscribeURL()).isEqualTo(oneClickUnsubscribeURL);
    }

    @Test
    public void testSendSmsHandlesErrors() {
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(serverError()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        UUID templateId = UUID.randomUUID();
        UUID smsSenderId = UUID.randomUUID();

        NotificationClientHttpException e = assertThrows(NotificationClientHttpException.class,
                () -> client.sendSms(templateId, "a phone number", emptyMap(), "aReference", smsSenderId));

        assertThat(e.getHttpResult()).isEqualTo(500);
        assertThat(e).hasMessage("Status code: 500 unexpected response code, expected 201");

        validateRequest();
    }

    @Test
    public void testSendSms() throws IOException, NotificationClientException {
        NotifySmsResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_sms_response.json"), NotifySmsResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        Map<String, Object> personalisation = Map.of("application_date", "2018-01-01");
        UUID templateId = UUID.randomUUID();
        UUID smsSenderId = UUID.randomUUID();

        NotifySmsResponse actual = client.sendSms(templateId, "a phone number", personalisation, "aReference", smsSenderId);

        assertThat(actual.getNotificationId()).isEqualTo(expected.getNotificationId());
        assertThat(actual.getReference()).isEqualTo(expected.getReference());
        assertThat(actual.getContent().getBody()).isEqualTo(expected.getContent().getBody());
        assertThat(actual.getContent().getFromNumber()).isEqualTo(expected.getContent().getFromNumber());
        assertThat(actual.getUri()).isEqualTo(expected.getUri());
        assertThat(actual.getTemplate().getId()).isEqualTo(expected.getTemplate().getId());
        assertThat(actual.getTemplate().getVersion()).isEqualTo(expected.getTemplate().getVersion());
        assertThat(actual.getTemplate().getUri()).isEqualTo(expected.getTemplate().getUri());

        LoggedRequest request = validateRequest();
        NotifySmsRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifySmsRequest.class);
        assertThat(requestReceivedByNotifyApi.getPhoneNumber()).isEqualTo("a phone number");
        assertThat(requestReceivedByNotifyApi.getTemplateId()).isEqualTo(templateId);
        assertThat(requestReceivedByNotifyApi.getPersonalisation()).isEqualTo(personalisation);
        assertThat(requestReceivedByNotifyApi.getReference()).isEqualTo("aReference");
        assertThat(requestReceivedByNotifyApi.getSmsSenderId()).isEqualTo(smsSenderId);
    }

    @Test
    public void testSendLetterHandlesErrors() {
        wireMockRule.stubFor(post("/v2/notifications/letter")
                .willReturn(serverError()));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        Map<String, String> personalisation = Map.of("address_line_1", "a1",
                "address_line_2", "a2",
                "address_line_3", "a3");
        UUID templateId = UUID.randomUUID();

        NotificationClientHttpException e = assertThrows(NotificationClientHttpException.class,
                () -> client.sendLetter(templateId, personalisation, "aReference"));

        assertThat(e.getHttpResult()).isEqualTo(500);
        assertThat(e).hasMessage("Status code: 500 unexpected response code, expected 201");

        validateRequest();
    }

    @Test
    public void testSendLetter() throws IOException, NotificationClientException {
        NotifyLetterResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_letter_response.json"), NotifyLetterResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/letter")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        Map<String, String> personalisation = Map.of("address_line_1", "a1",
                "address_line_2", "a2",
                "address_line_3", "a3");
        UUID templateId = UUID.randomUUID();

        NotifyLetterResponse actual = client.sendLetter(templateId, personalisation, "aReference");

        assertThat(actual.getNotificationId()).isEqualTo(expected.getNotificationId());
        assertThat(actual.getReference()).isEqualTo(expected.getReference());
        assertThat(actual.getContent().getBody()).isEqualTo(expected.getContent().getBody());
        assertThat(actual.getContent().getSubject()).isEqualTo(expected.getContent().getSubject());
        assertThat(actual.getUri()).isEqualTo(expected.getUri());
        assertThat(actual.getTemplate().getId()).isEqualTo(expected.getTemplate().getId());
        assertThat(actual.getTemplate().getVersion()).isEqualTo(expected.getTemplate().getVersion());
        assertThat(actual.getTemplate().getUri()).isEqualTo(expected.getTemplate().getUri());
        assertThat(actual.getScheduledFor()).isEqualTo(expected.getScheduledFor());

        LoggedRequest request = validateRequest();
        NotifyLetterRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyLetterRequest.class);
        assertThat(requestReceivedByNotifyApi.getTemplateId()).isEqualTo(templateId);
        assertThat(requestReceivedByNotifyApi.getPersonalisation()).isEqualTo(personalisation);
        assertThat(requestReceivedByNotifyApi.getReference()).isEqualTo("aReference");
    }

    @Test
    public void testGetNotificationById() throws IOException, NotificationClientException {
        final UUID notificationId = UUID.randomUUID();
        NotifyNotification expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_byid_response.json"), NotifyNotification.class);
        wireMockRule.stubFor(get("/v2/notifications/" + notificationId)
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotifyNotification actual = client.getNotificationById(notificationId);

        assertThat(actual).isInstanceOf(NotifyNotificationSms.class);
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getReference()).isEqualTo(expected.getReference());
        assertThat(((NotifyNotificationSms)actual).getPhoneNumber()).isEqualTo(((NotifyNotificationSms)expected).getPhoneNumber());
        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(((NotifyNotificationSms)actual).getStatus()).isEqualTo(((NotifyNotificationSms)expected).getStatus());
        assertThat(actual.getTemplate().getId()).isEqualTo(expected.getTemplate().getId());
        assertThat(actual.getTemplate().getVersion()).isEqualTo(expected.getTemplate().getVersion());
        assertThat(actual.getTemplate().getUri()).isEqualTo(expected.getTemplate().getUri());
        assertThat(actual.getBody()).isEqualTo(expected.getBody());
        assertThat(actual.getCreatedAt()).isEqualTo(expected.getCreatedAt());
        assertThat(actual.getCreatedByName()).isEqualTo(expected.getCreatedByName());
        assertThat(actual.getSentAt()).isEqualTo(expected.getSentAt());
        assertThat(actual.getCompletedAt()).isEqualTo(expected.getCompletedAt());

        validateRequest();
    }

    @Test
    public void testGetNotifications() throws IOException, NotificationClientException {
        NotifyNotificationListResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_response.json"), NotifyNotificationListResponse.class);
        NotifyNotificationSms expectedNotificationSms = (NotifyNotificationSms)getNotificationOfType(expected, NotificationType.sms);
        NotifyNotificationLetter expectedNotificationLetter = (NotifyNotificationLetter)getNotificationOfType(expected, NotificationType.letter);
        NotifyNotificationEmail expectedNotificationEmail = (NotifyNotificationEmail)getNotificationOfType(expected, NotificationType.email);
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/notifications"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        UUID olderThanId = UUID.randomUUID();

        NotifyNotificationListResponse actual = client.getNotifications("a stat", NotificationType.sms, "a ref", olderThanId);

        assertThat(actual.getNotifications()).hasSize(3);
        assertThat(actual.getLinks().getCurrent()).isEqualTo(expected.getLinks().getCurrent());
        assertThat(actual.getLinks().getNext()).isEqualTo(expected.getLinks().getNext());

        NotifyNotificationSms actualNotificationSms = (NotifyNotificationSms)getNotificationOfType(actual, NotificationType.sms);
        NotifyNotificationLetter actualNotificationLetter = (NotifyNotificationLetter)getNotificationOfType(actual, NotificationType.letter);
        NotifyNotificationEmail actualNotificationEmail = (NotifyNotificationEmail)getNotificationOfType(actual, NotificationType.email);

        // check all the base details match
        checkBaseNotificationDetails(expectedNotificationSms, actualNotificationSms);
        checkBaseNotificationDetails(expectedNotificationLetter, actualNotificationLetter);
        checkBaseNotificationDetails(expectedNotificationEmail, actualNotificationEmail);

        // email specific items
        assertThat(actualNotificationEmail.getSubject()).isEqualTo(expectedNotificationEmail.getSubject());
        assertThat(actualNotificationEmail.getEmailAddress()).isEqualTo(expectedNotificationEmail.getEmailAddress());
        assertThat(actualNotificationEmail.getStatus()).isEqualTo(expectedNotificationEmail.getStatus());

        // phone specific items
        assertThat(actualNotificationSms.getPhoneNumber()).isEqualTo(expectedNotificationSms.getPhoneNumber());
        assertThat(actualNotificationSms.getStatus()).isEqualTo(expectedNotificationSms.getStatus());

        // letter specific items
        assertThat(actualNotificationLetter.getLine1()).isEqualTo(expectedNotificationLetter.getLine1());
        assertThat(actualNotificationLetter.getLine2()).isEqualTo(expectedNotificationLetter.getLine2());
        assertThat(actualNotificationLetter.getLine3()).isEqualTo(expectedNotificationLetter.getLine3());
        assertThat(actualNotificationLetter.getLine4()).isEqualTo(expectedNotificationLetter.getLine4());
        assertThat(actualNotificationLetter.getLine5()).isEqualTo(expectedNotificationLetter.getLine5());
        assertThat(actualNotificationLetter.getLine6()).isEqualTo(expectedNotificationLetter.getLine6());
        assertThat(actualNotificationLetter.getLine7()).isEqualTo(expectedNotificationLetter.getLine7());
        assertThat(actualNotificationLetter.getStatus()).isEqualTo(expectedNotificationLetter.getStatus());

        LoggedRequest request = validateRequest();
        assertThat(request.queryParameter("status").firstValue()).isEqualTo("a stat");
        assertThat(request.queryParameter("template_type").firstValue()).isEqualTo("sms");
        assertThat(request.queryParameter("reference").firstValue()).isEqualTo("a ref");
        assertThat(request.queryParameter("older_than").firstValue()).isEqualTo(olderThanId.toString());
    }

    private static NotifyNotification getNotificationOfType(NotifyNotificationListResponse expected, NotificationType notificationType) {
        return expected.getNotifications()
                .stream()
                .filter(n -> n.getType() == notificationType)
                .findFirst()
                .get();
    }

    private void checkBaseNotificationDetails(NotifyNotification expected, NotifyNotification actual) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getReference()).isEqualTo(expected.getReference());
        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getTemplate().getId()).isEqualTo(expected.getTemplate().getId());
        assertThat(actual.getTemplate().getVersion()).isEqualTo(expected.getTemplate().getVersion());
        assertThat(actual.getTemplate().getUri()).isEqualTo(expected.getTemplate().getUri());
        assertThat(actual.getBody()).isEqualTo(expected.getBody());
        assertThat(actual.getCreatedAt()).isEqualTo(expected.getCreatedAt());
        assertThat(actual.getCreatedByName()).isEqualTo(expected.getCreatedByName());
        assertThat(actual.getSentAt()).isEqualTo(expected.getSentAt());
        assertThat(actual.getCompletedAt()).isEqualTo(expected.getCompletedAt());
    }

    @Test
    public void testGetPdfForLetter() throws NotificationClientException, IOException {
        final UUID notificationId = UUID.randomUUID();
        byte[] pdfFile = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("one_page_pdf.pdf"));
        wireMockRule.stubFor(get("/v2/notifications/" + notificationId + "/pdf")
                .willReturn(ok()
                        .withResponseBody(new Body(pdfFile))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        byte[] responsePdf = client.getPdfForLetter(notificationId);

        assertThat(responsePdf).isEqualTo(pdfFile);

        validateRequest();
    }

    @Test
    public void testSendPrecompiledLetter() throws IOException, NotificationClientException {
        NotifyPrecompiledLetterResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_precompiled_letter_response.json"), NotifyPrecompiledLetterResponse.class);
        wireMockRule.stubFor(post("/v2/notifications/letter")
                .willReturn(created()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        final File pdfFile = new File(this.getClass().getClassLoader().getResource("small.pdf.txt").getFile());

        NotifyPrecompiledLetterResponse actual = client.sendPrecompiledLetter(expected.getReference(), pdfFile, expected.getPostage());

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getReference()).isEqualTo(expected.getReference());
        assertThat(actual.getPostage()).isEqualTo(expected.getPostage());

        LoggedRequest request = validateRequest();
        NotifyPrecompiledLetterRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyPrecompiledLetterRequest.class);
        assertThat(requestReceivedByNotifyApi.getReference()).isEqualTo("your-letter-reference");
        assertThat(requestReceivedByNotifyApi.getContent()).isEqualTo("JVBERi1mb28=");
        assertThat(requestReceivedByNotifyApi.getPostage()).isEqualTo("postage-you-have-set-or-None");
    }

    @Test
    public void testGetTemplateById() throws IOException, NotificationClientException {
        NotifyTemplateLetter expected = (NotifyTemplateLetter)objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_byid_response_letter.json"), NotifyTemplate.class);
        UUID templateId = expected.getId();
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/template/" + templateId))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotifyTemplateLetter actual = (NotifyTemplateLetter)client.getTemplateById(templateId);

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getCreatedAt()).isEqualTo(expected.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualTo(expected.getUpdatedAt());
        assertThat(actual.getVersion()).isEqualTo(expected.getVersion());
        assertThat(actual.getCreatedBy()).isEqualTo(expected.getCreatedBy());
        assertThat(actual.getBody()).isEqualTo(expected.getBody());
        assertThat(actual.getLetterContactBlock()).isEqualTo(expected.getLetterContactBlock());

        validateRequest();
    }

    @Test
    public void testGetTemplateVersion() throws IOException, NotificationClientException {
        NotifyTemplateEmail expected = (NotifyTemplateEmail)objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_byid_response_email.json"), NotifyTemplate.class);
        UUID templateId = expected.getId();
        final int version = 100;
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/template/" + templateId + "/version/" + version))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotifyTemplateEmail actual = (NotifyTemplateEmail)client.getTemplateVersion(templateId, 100);

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getCreatedAt()).isEqualTo(expected.getCreatedAt());
        assertThat(actual.getUpdatedAt()).isEqualTo(expected.getUpdatedAt());
        assertThat(actual.getVersion()).isEqualTo(expected.getVersion());
        assertThat(actual.getCreatedBy()).isEqualTo(expected.getCreatedBy());
        assertThat(actual.getBody()).isEqualTo(expected.getBody());
        assertThat(actual.getSubject()).isEqualTo(expected.getSubject());

        validateRequest();
    }

    @Test
    public void testGetAllTemplates() throws IOException, NotificationClientException {
        NotifyTemplateEmail expectedTemplateEmail = (NotifyTemplateEmail)objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_byid_response_email.json"), NotifyTemplate.class);
        NotifyTemplateListResponse expected = new NotifyTemplateListResponse(Collections.singletonList(expectedTemplateEmail));
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/templates"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotifyTemplateListResponse actual = client.getAllTemplates(NotificationType.email);

        assertThat(actual.getTemplates()).hasSize(1);

        assertThat(actual.getTemplates().get(0).getId()).isEqualTo(expected.getTemplates().get(0).getId());
        assertThat(actual.getTemplates().get(0).getName()).isEqualTo(expected.getTemplates().get(0).getName());
        assertThat(actual.getTemplates().get(0).getType()).isEqualTo(expected.getTemplates().get(0).getType());
        assertThat(actual.getTemplates().get(0).getCreatedAt()).isEqualTo(expected.getTemplates().get(0).getCreatedAt());
        assertThat(actual.getTemplates().get(0).getUpdatedAt()).isEqualTo(expected.getTemplates().get(0).getUpdatedAt());
        assertThat(actual.getTemplates().get(0).getVersion()).isEqualTo(expected.getTemplates().get(0).getVersion());
        assertThat(actual.getTemplates().get(0).getCreatedBy()).isEqualTo(expected.getTemplates().get(0).getCreatedBy());
        assertThat(actual.getTemplates().get(0).getBody()).isEqualTo(expected.getTemplates().get(0).getBody());
        assertThat(((NotifyTemplateEmail)actual.getTemplates().get(0)).getSubject()).isEqualTo(((NotifyTemplateEmail)expected.getTemplates().get(0)).getSubject());

        LoggedRequest request = validateRequest();
        assertThat(request.queryParameter("type").firstValue()).isEqualTo(NotificationType.email.name());
    }

    @Test
    public void testGeneratePreviewTemplate() throws IOException, NotificationClientException {
        NotifyTemplatePreviewResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_template_preview_response.json"), NotifyTemplatePreviewResponse.class);
        UUID templateId = expected.getId();
        Map<String, Object> personalisation = Map.of("application_date", "2018-01-01");
        wireMockRule.stubFor(post(urlPathEqualTo("/v2/template/" + templateId + "/preview"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);

        NotifyTemplatePreviewResponse actual = client.generateTemplatePreview(templateId, personalisation);

        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getType()).isEqualTo(expected.getType());
        assertThat(actual.getVersion()).isEqualTo(expected.getVersion());
        assertThat(actual.getBody()).isEqualTo(expected.getBody());
        assertThat(actual.getSubject()).isEqualTo(expected.getSubject());

        LoggedRequest request = validateRequest();
        NotifyTemplatePreviewRequest requestReceivedByNotifyApi = objectMapper.readValue(request.getBodyAsString(), NotifyTemplatePreviewRequest.class);
        assertThat(requestReceivedByNotifyApi.getPersonalisation()).isEqualTo(personalisation);
    }

    @Test
    public void testGetReceivedTextMessages() throws IOException, NotificationClientException {
        NotifyReceivedTextMessagesResponse expected = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("v2_received-text-messages_response.json"), NotifyReceivedTextMessagesResponse.class);
        wireMockRule.stubFor(get(urlPathEqualTo("/v2/received-text-messages"))
                .willReturn(ok()
                        .withResponseBody(new Body(objectMapper.writeValueAsString(expected)))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        UUID olderThanId = UUID.randomUUID();

        NotifyReceivedTextMessagesResponse actual = client.getReceivedTextMessages(olderThanId);

        assertThat(actual.getReceivedTextMessages().size()).isEqualTo(expected.getReceivedTextMessages().size());
        assertThat(actual.getReceivedTextMessages().get(0).getId()).isEqualTo(expected.getReceivedTextMessages().get(0).getId());
        assertThat(actual.getReceivedTextMessages().get(0).getCreatedAt()).isEqualTo(expected.getReceivedTextMessages().get(0).getCreatedAt());
        assertThat(actual.getReceivedTextMessages().get(0).getServiceId()).isEqualTo(expected.getReceivedTextMessages().get(0).getServiceId());
        assertThat(actual.getReceivedTextMessages().get(0).getNotifyNumber()).isEqualTo(expected.getReceivedTextMessages().get(0).getNotifyNumber());
        assertThat(actual.getReceivedTextMessages().get(0).getUserNumber()).isEqualTo(expected.getReceivedTextMessages().get(0).getUserNumber());
        assertThat(actual.getReceivedTextMessages().get(0).getContent()).isEqualTo(expected.getReceivedTextMessages().get(0).getContent());
        assertThat(actual.getLinks().getCurrent()).isEqualTo(expected.getLinks().getCurrent());
        assertThat(actual.getLinks().getNext()).isEqualTo(expected.getLinks().getNext());

        LoggedRequest request = validateRequest();
        assertThat(request.queryParameter("older_than").firstValue()).isEqualTo(olderThanId.toString());
    }

    @Test
    public void testJsonParsingDoesNotFailWithUnknownValue() throws IOException {
        // NOTE: we do not try and deserialise here, we want the client to do that
        String apiResponse = new String(this.getClass().getClassLoader().getResourceAsStream("v2_notifications_email_unknown_value_response.json").readAllBytes(), StandardCharsets.UTF_8);
        wireMockRule.stubFor(post("/v2/notifications/email")
                .willReturn(created()
                        .withResponseBody(new Body(apiResponse))));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL);
        Map<String, Object> personalisation = Map.of("application_date", "2018-01-01");
        UUID templateId = UUID.randomUUID();
        UUID emailReplyToId = UUID.randomUUID();

        try {
            client.sendEmail(templateId, "anEmailAddress", personalisation, "aReference", emailReplyToId);
        } catch(NotificationClientException e) {
                fail("should have been able to parse a response with an unknown value", e);
        }
    }

    @Test
    public void testBadApiKeyWouldFail() {
        // not the best test as we're effectively performing a check as the server would
        // but it does allow us to check how the client responds
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(unauthorized()));
        final UUID badApiKey = UUID.randomUUID();
        NotificationClient client = new NotificationClient("Api_key_name-" + SERVICE_ID + "-" + badApiKey, BASE_URL);
        UUID templateId = UUID.randomUUID();

        NotificationClientHttpException e = assertThrows(NotificationClientHttpException.class,
                () -> client.sendSms(templateId, "aPhoneNumber", emptyMap(), "aReference"));

        assertThat(e.getHttpResult()).isEqualTo(401);
        assertThat(e).hasMessage("Status code: 401 unexpected response code, expected 201");

        List<LoggedRequest> requests = wireMockRule.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests).hasSize(1);
        final String requestUserAgent = requests.get(0).getHeader("User-Agent");
        assertThat(requestUserAgent).startsWith("NOTIFY-API-JAVA-CLIENT");
        AssertionError invalidJwtException = assertThrows(AssertionError.class,
                () -> validateBearerTokenTest(requests.get(0).header("Authorization").firstValue().substring("Bearer ".length())));
        final String expectedExceptionMessage = "unable to validate jwt: org.jose4j.jwt.consumer.InvalidJwtSignatureException: JWT rejected due to invalid signature";
        assertThat(invalidJwtException).hasMessageStartingWith(expectedExceptionMessage);
    }

    @Test
    public void testConnectTimeoutCodeWorks() {
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(unauthorized().withFixedDelay(100)));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL, null, null, Duration.ofNanos(1), Duration.ofMillis(2000));
        UUID templateId = UUID.randomUUID();

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendSms(templateId, "aPhoneNumber", emptyMap(), "aReference"));

        assertThat(e).hasMessageContaining("HTTP connect timed out");
    }

    @Test
    public void testRequestTimeoutCodeWorks() {
        final int requestTimeout = 50;
        wireMockRule.stubFor(post("/v2/notifications/sms")
                .willReturn(unauthorized().withFixedDelay(requestTimeout*2)));
        NotificationClient client = new NotificationClient(COMBINED_API_KEY, BASE_URL, null, null, Duration.ofMillis(1000), Duration.ofMillis(requestTimeout));
        UUID templateId = UUID.randomUUID();

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> client.sendSms(templateId, "aPhoneNumber", emptyMap(), "aReference"));

        assertThat(e).hasMessageContaining("request timed out");

        validateRequest();
    }

    private void validateBearerToken(String token) throws InvalidJwtException {
        final int allowedSecondsInTheFuture = 60;
        final int allowedSecondsInThePast = 60;
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setExpectedIssuer(SERVICE_ID)
                .setIssuedAtRestrictions(allowedSecondsInTheFuture, allowedSecondsInThePast)
                // NOTE: COMBINED_API_KEY is passed to the API however the API_KEY is later taken from within that by the client
                .setVerificationKey(new SecretKeySpec(API_KEY.getBytes(StandardCharsets.UTF_8), "RAW"))
                .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256)
                .build();
        jwtConsumer.process(token);
    }

    private LoggedRequest validateRequest() {
        List<LoggedRequest> requests = wireMockRule.findRequestsMatching(RequestPattern.everything()).getRequests();
        assertThat(requests).hasSize(1);
        final String requestUserAgent = requests.get(0).getHeader("User-Agent");
        assertThat(requestUserAgent).startsWith("NOTIFY-API-JAVA-CLIENT");
        validateBearerTokenTest(requests.get(0).header("Authorization").firstValue().substring("Bearer ".length()));
        return requests.get(0);
    }

    private void validateBearerTokenTest(String token) {
        try {
            validateBearerToken(token);
        } catch (InvalidJwtException e) {
            fail("unable to validate jwt: " + e);
        }
    }

}
