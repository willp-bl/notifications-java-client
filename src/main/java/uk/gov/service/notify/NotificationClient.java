package uk.gov.service.notify;

import org.apache.http.client.utils.URIBuilder;
import uk.gov.service.notify.domain.NotificationType;
import uk.gov.service.notify.domain.NotifyEmailRequest;
import uk.gov.service.notify.domain.NotifyEmailResponse;
import uk.gov.service.notify.domain.NotifyLetterRequest;
import uk.gov.service.notify.domain.NotifyLetterResponse;
import uk.gov.service.notify.domain.NotifyNotification;
import uk.gov.service.notify.domain.NotifyNotificationListResponse;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterRequest;
import uk.gov.service.notify.domain.NotifyPrecompiledLetterResponse;
import uk.gov.service.notify.domain.NotifyReceivedTextMessagesResponse;
import uk.gov.service.notify.domain.NotifySmsRequest;
import uk.gov.service.notify.domain.NotifySmsResponse;
import uk.gov.service.notify.domain.NotifyTemplate;
import uk.gov.service.notify.domain.NotifyTemplateListResponse;
import uk.gov.service.notify.domain.NotifyTemplatePreviewRequest;
import uk.gov.service.notify.domain.NotifyTemplatePreviewResponse;
import uk.gov.service.notify.domain.Postage;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

public class NotificationClient implements NotificationClientApi {

    private static final Logger LOGGER = Logger.getLogger(NotificationClient.class.toString());
    private static final String LIVE_BASE_URL = "https://api.notifications.service.gov.uk";

    private final String baseUrl;
    private final NotifyHttpClient notifyHttpClient;

    /**
     * This client constructor given the api key.
     *
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     */
    public NotificationClient(String apiKey) {
        this(apiKey, LIVE_BASE_URL, null, NotificationClientOptions.defaultOptions());
    }

    /**
     * This client constructor given the api key.
     *
     * @param apiKey        Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param clientOptions Options for this Notify client
     */
    public NotificationClient(String apiKey, NotificationClientOptions clientOptions) {
        this(apiKey, LIVE_BASE_URL, null, clientOptions);
    }

    /**
     * Use this client constructor if you require a proxy for https requests.
     *
     * @param apiKey        Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param proxySelector Proxy used on the http requests
     */
    public NotificationClient(String apiKey, ProxySelector proxySelector) {
        this(apiKey, LIVE_BASE_URL, proxySelector, NotificationClientOptions.defaultOptions());
    }

    /**
     * Use this client constructor if you require a proxy for https requests.
     *
     * @param apiKey        Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param proxySelector Proxy used on the http requests
     * @param clientOptions Options for this Notify client
     */
    public NotificationClient(String apiKey, ProxySelector proxySelector, NotificationClientOptions clientOptions) {
        this(apiKey, LIVE_BASE_URL, proxySelector, clientOptions);
    }

    /**
     * This client constructor is used for testing on other environments, used by the GOV.UK Notify team.
     *
     * @param apiKey  Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl base URL, defaults to https://api.notifications.service.gov.uk
     */
    public NotificationClient(String apiKey, String baseUrl) {
        this(apiKey, baseUrl, null, NotificationClientOptions.defaultOptions());
    }

    /**
     * This client constructor is used for testing on other environments, used by the GOV.UK Notify team.
     *
     * @param apiKey        Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl       base URL, defaults to https://api.notifications.service.gov.uk
     * @param clientOptions Options for this Notify client
     */
    public NotificationClient(String apiKey, String baseUrl, NotificationClientOptions clientOptions) {
        this(apiKey, baseUrl, null, clientOptions);
    }

    /**
     * @param apiKey        Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl       base URL, defaults to https://api.notifications.service.gov.uk
     * @param proxySelector Proxy used on the http requests
     */
    public NotificationClient(String apiKey,
                              String baseUrl,
                              ProxySelector proxySelector) {
        this(apiKey, baseUrl, proxySelector, null, NotificationClientOptions.defaultOptions());
    }

    /**
     * @param apiKey        Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl       base URL, defaults to https://api.notifications.service.gov.uk
     * @param proxySelector Proxy used on the http requests
     * @param clientOptions Options for this Notify client
     */
    public NotificationClient(String apiKey,
                              String baseUrl,
                              ProxySelector proxySelector,
                              NotificationClientOptions clientOptions) {
        this(apiKey, baseUrl, proxySelector, null, clientOptions);
    }

    public NotificationClient(String apiKey,
                              String baseUrl,
                              ProxySelector proxySelector,
                              SSLContext sslContext) {
        this(apiKey, baseUrl, proxySelector, sslContext, NotificationClientOptions.defaultOptions());
    }

    public NotificationClient(String apiKey,
                              String baseUrl,
                              ProxySelector proxySelector,
                              SSLContext sslContext,
                              NotificationClientOptions clientOptions) {
        this.baseUrl = baseUrl;
        this.notifyHttpClient = new NotifyHttpClient(NotifyUtils.extractServiceId(apiKey),
                NotifyUtils.extractApiKey(apiKey),
                proxySelector,
                sslContext,
                clientOptions);
    }

    @Override
    public NotifyEmailResponse sendEmail(UUID templateId,
                                         String emailAddress,
                                         Map<String, ?> personalisation,
                                         String reference) throws NotificationClientException {
        return sendEmail(templateId, emailAddress, personalisation, reference, null, null);
    }

    @Override
    public NotifyEmailResponse sendEmail(UUID templateId,
                                         String emailAddress,
                                         Map<String, ?> personalisation,
                                         String reference,
                                         UUID emailReplyToId) throws NotificationClientException {
        return sendEmail(templateId, emailAddress, personalisation, reference, emailReplyToId, null);
    }

    @Override
    public NotifyEmailResponse sendEmail(UUID templateId,
                                         String emailAddress,
                                         Map<String, ?> personalisation,
                                         String reference,
                                         UUID emailReplyToId,
                                         URI oneClickUnsubscribeURL) throws NotificationClientException {

        NotifyEmailRequest requestBody = new NotifyEmailRequest(emailAddress, templateId, personalisation, reference, emailReplyToId, oneClickUnsubscribeURL);

        return notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/email"), requestBody, NotifyEmailResponse.class, HTTP_CREATED);
    }

    @Override
    public NotifySmsResponse sendSms(UUID templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        return sendSms(templateId, phoneNumber, personalisation, reference, null);
    }

    @Override
    public NotifySmsResponse sendSms(UUID templateId,
                                     String phoneNumber,
                                     Map<String, ?> personalisation,
                                     String reference,
                                     UUID smsSenderId) throws NotificationClientException {

        NotifySmsRequest requestBody = new NotifySmsRequest(phoneNumber, templateId, personalisation, reference, smsSenderId);

        return notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/sms"), requestBody, NotifySmsResponse.class, HTTP_CREATED);
    }

    @Override
    public NotifyLetterResponse sendLetter(UUID templateId, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        NotifyLetterRequest requestBody = new NotifyLetterRequest(templateId, personalisation, reference);
        return notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/letter"), requestBody, NotifyLetterResponse.class, HTTP_CREATED);
    }

    @Override
    public NotifyNotification getNotificationById(UUID notificationId) throws NotificationClientException {
        return notifyHttpClient.get(URI.create(baseUrl + "/v2/notifications/" + notificationId), NotifyNotification.class);
    }

    @Override
    public byte[] getPdfForLetter(UUID notificationId) throws NotificationClientException {
        return notifyHttpClient.get(URI.create(baseUrl + "/v2/notifications/" + notificationId + "/pdf"));
    }

    @Override
    public NotifyNotificationListResponse getNotifications(String status, NotificationType notificationType, String reference, UUID olderThanId) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/notifications");
            addQueryParamToURIBuilder("status", status, builder);
            addQueryParamToURIBuilder("template_type", notificationType, builder);
            addQueryParamToURIBuilder("reference", reference, builder);
            addQueryParamToURIBuilder("older_than", olderThanId, builder);

            return notifyHttpClient.get(builder.build(), NotifyNotificationListResponse.class);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    private static void addQueryParamToURIBuilder(String queryParamName, String queryParamValue, URIBuilder builder) {
        if (queryParamValue != null && !queryParamValue.isEmpty()) {
            builder.addParameter(queryParamName, queryParamValue);
        }
    }

    private static void addQueryParamToURIBuilder(String queryParamName, Object queryParamValue, URIBuilder builder) {
        if (queryParamValue != null) {
            builder.addParameter(queryParamName, queryParamValue.toString());
        }
    }

    @Override
    public NotifyTemplate getTemplateById(UUID templateId) throws NotificationClientException {
        URI uri = URI.create(baseUrl + "/v2/template/" + templateId);
        return notifyHttpClient.get(uri, NotifyTemplate.class);
    }

    @Override
    public NotifyTemplate getTemplateVersion(UUID templateId, int version) throws NotificationClientException {
        URI uri = URI.create(baseUrl + "/v2/template/" + templateId + "/version/" + version);
        return notifyHttpClient.get(uri, NotifyTemplate.class);
    }

    @Override
    public NotifyTemplateListResponse getAllTemplates(NotificationType templateType) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/templates");
            addQueryParamToURIBuilder("type", templateType, builder);
            return notifyHttpClient.get(builder.build(), NotifyTemplateListResponse.class);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    @Override
    public NotifyTemplatePreviewResponse generateTemplatePreview(UUID templateId, Map<String, Object> personalisation) throws NotificationClientException {
        NotifyTemplatePreviewRequest requestBody = new NotifyTemplatePreviewRequest(personalisation);
        return notifyHttpClient.post(URI.create(baseUrl + "/v2/template/" + templateId + "/preview"), requestBody, NotifyTemplatePreviewResponse.class, HTTP_OK);
    }

    @Override
    public NotifyReceivedTextMessagesResponse getReceivedTextMessages(UUID olderThanId) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/received-text-messages");
            addQueryParamToURIBuilder("older_than", olderThanId, builder);
            return notifyHttpClient.get(builder.build(), NotifyReceivedTextMessagesResponse.class);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    private NotifyPrecompiledLetterResponse sendPrecompiledLetter(String reference, String base64EncodedPDFFile, Postage postage) throws NotificationClientException {
        if (Objects.isNull(reference) || reference.isBlank()) {
            throw new NotificationClientException("reference cannot be null or empty");
        }

        if (Objects.isNull(base64EncodedPDFFile) || base64EncodedPDFFile.isBlank()) {
            throw new NotificationClientException("precompiledPDF cannot be null or empty");
        }

        if (!PdfUtils.isBase64StringPDF(base64EncodedPDFFile)) {
            throw new NotificationClientException("base64EncodedPDFFile is not a PDF");
        }

        final NotifyPrecompiledLetterRequest requestBody = new NotifyPrecompiledLetterRequest(reference,
                base64EncodedPDFFile,
                postage);

        return notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/letter"), requestBody, NotifyPrecompiledLetterResponse.class, HTTP_CREATED);
    }

    @Override
    public NotifyPrecompiledLetterResponse sendPrecompiledLetter(String reference, File precompiledPDF) throws NotificationClientException {
        return sendPrecompiledLetter(reference, precompiledPDF, null);
    }

    @Override
    public NotifyPrecompiledLetterResponse sendPrecompiledLetter(String reference, File precompiledPDF, Postage postage) throws NotificationClientException {
        if (precompiledPDF == null) {
            throw new NotificationClientException("File cannot be null");
        }
        final byte[] buf;
        try {
            buf = Files.readAllBytes(precompiledPDF.toPath());
        } catch (IOException e) {
            throw new NotificationClientException("Can't read file");
        }
        return sendPrecompiledLetterWithInputStream(reference, new ByteArrayInputStream(buf), postage);
    }

    @Override
    public NotifyPrecompiledLetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream) throws NotificationClientException {
        return sendPrecompiledLetterWithInputStream(reference, stream, null);
    }

    @Override
    public NotifyPrecompiledLetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream, Postage postage) throws NotificationClientException {
        if (stream == null) {
            throw new NotificationClientException("Input stream cannot be null");
        }
        final String encoded;
        try {
            encoded = Base64.getMimeEncoder().encodeToString(stream.readAllBytes());
        } catch (IOException e) {
            throw new NotificationClientException("Error when turning Base64InputStream into a string");
        }

        return sendPrecompiledLetter(reference, encoded, postage);
    }
}