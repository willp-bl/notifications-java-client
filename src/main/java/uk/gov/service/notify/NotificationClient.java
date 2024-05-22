package uk.gov.service.notify;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class NotificationClient implements NotificationClientApi {

    private static final Logger LOGGER = Logger.getLogger(NotificationClient.class.toString());
    private static final String LIVE_BASE_URL = "https://api.notifications.service.gov.uk";

    private final String apiKey;
    private final String serviceId;
    private final String baseUrl;
    private final Proxy proxy;
    private final String version;
    private final NotifyHttpClient notifyHttpClient;

    /**
     * This client constructor given the api key.
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     */
    public NotificationClient(final String apiKey) {
        this(
                apiKey,
                LIVE_BASE_URL,
                null
        );
    }

    /**
     * Use this client constructor if you require a proxy for https requests.
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param proxy Proxy used on the http requests
     */
    public NotificationClient(final String apiKey, final Proxy proxy) {
        this(
                apiKey,
                LIVE_BASE_URL,
                proxy
        );
    }

    /**
     * This client constructor is used for testing on other environments, used by the GOV.UK Notify team.
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl base URL, defaults to https://api.notifications.service.gov.uk
     */
    public NotificationClient(final String apiKey, final String baseUrl) {
        this(
                apiKey,
                baseUrl,
                null
        );
    }


    /**
     *
     * @param apiKey Generate an API key by signing in to GOV.UK Notify, https://www.notifications.service.gov.uk, and going to the **API integration** page
     * @param baseUrl base URL, defaults to https://api.notifications.service.gov.uk
     * @param proxy Proxy used on the http requests
     */
    public NotificationClient(final String apiKey, final String baseUrl, final Proxy proxy) {
        this(
                apiKey,
                baseUrl,
                proxy,
                null
        );
        try {
            setDefaultSSLContext();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
        }
    }

    public NotificationClient(final String apiKey,
                              final String baseUrl,
                              final Proxy proxy,
                              final SSLContext sslContext){

        this.apiKey = NotifyUtils.extractApiKey(apiKey);
        this.serviceId = NotifyUtils.extractServiceId(apiKey);
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        if (sslContext != null){
            setCustomSSLContext(sslContext);
        }
        this.version = NotifyUtils.getVersion();
        this.notifyHttpClient = new NotifyHttpClient(this.serviceId, this.apiKey, getUserAgent(), this.proxy);
    }

    String getUserAgent() {
        return "NOTIFY-API-JAVA-CLIENT/" + version;
    }

    String getApiKey() {
        return apiKey;
    }

    String getServiceId() {
        return serviceId;
    }

    String getBaseUrl() {
        return baseUrl;
    }

    Proxy getProxy() {
        return proxy;
    }

    @Override
    public SendEmailResponse sendEmail(UUID templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference) throws NotificationClientException {
        return sendEmail(templateId, emailAddress, personalisation, reference, null, null);
    }

    @Override
    public SendEmailResponse sendEmail(UUID templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference,
                                       UUID emailReplyToId) throws NotificationClientException {
        return sendEmail(templateId, emailAddress, personalisation, reference, emailReplyToId, null);
    }

    @Override
    public SendEmailResponse sendEmail(UUID templateId,
                                       String emailAddress,
                                       Map<String, ?> personalisation,
                                       String reference,
                                       UUID emailReplyToId,
                                       URI oneClickUnsubscribeURL) throws NotificationClientException {

        JSONObject body = createBodyForPostRequest(templateId,
                null,
                emailAddress,
                personalisation,
                reference,
                null,
                null);

        if(emailReplyToId != null)
        {
            body.put("email_reply_to_id", emailReplyToId);
        }

        if(oneClickUnsubscribeURL != null)
        {
            body.put("one_click_unsubscribe_url", oneClickUnsubscribeURL);
        }

        String response = notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/email"), body, HTTP_CREATED);

        return new SendEmailResponse(response);
    }

    @Override
    public SendSmsResponse sendSms(UUID templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        return sendSms(templateId, phoneNumber, personalisation, reference, null);
    }

    @Override
    public SendSmsResponse sendSms(UUID templateId,
                                   String phoneNumber,
                                   Map<String, ?> personalisation,
                                   String reference,
                                   UUID smsSenderId) throws NotificationClientException {

        JSONObject body = createBodyForPostRequest(templateId,
                phoneNumber,
                null,
                personalisation,
                reference,
                null,
                null);

        if( smsSenderId != null){
            body.put("sms_sender_id", smsSenderId);
        }

        String response = notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/sms"), body, HTTP_CREATED);

        return new SendSmsResponse(response);
    }

    @Override
    public SendLetterResponse sendLetter(UUID templateId, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        JSONObject body = createBodyForPostRequest(templateId, null, null, personalisation, reference, null, null);
        String response = notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/letter"), body, HTTP_CREATED);
        return new SendLetterResponse(response);
    }

    @Override
    public Notification getNotificationById(UUID notificationId) throws NotificationClientException {
        String response = notifyHttpClient.getAsString(URI.create(baseUrl + "/v2/notifications/" + notificationId));
        return new Notification(response);
    }

    @Override
    public byte[] getPdfForLetter(UUID notificationId) throws NotificationClientException {
        return notifyHttpClient.getAsByteArray(URI.create(baseUrl + "/v2/notifications/" + notificationId + "/pdf"));
    }

    @Override
    public NotificationList getNotifications(String status, NotificationType notificationType, String reference, UUID olderThanId) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/notifications");
            addQueryParamToURIBuilder("status", status, builder);
            addQueryParamToURIBuilder("template_type", notificationType, builder);
            addQueryParamToURIBuilder("reference", reference, builder);
            addQueryParamToURIBuilder("older_than", olderThanId, builder);

            String response = notifyHttpClient.getAsString(builder.build());
            return new NotificationList(response);
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
    public Template getTemplateById(UUID templateId) throws NotificationClientException{
        URI uri = URI.create(baseUrl + "/v2/template/" + templateId);
        String response = notifyHttpClient.getAsString(uri);
        return new Template(response);
    }

    @Override
    public Template getTemplateVersion(UUID templateId, int version) throws NotificationClientException{
        URI uri = URI.create(baseUrl + "/v2/template/" + templateId + "/version/" + version);
        String response = notifyHttpClient.getAsString(uri);
        return new Template(response);
    }

    @Override
    public TemplateList getAllTemplates(NotificationType templateType) throws NotificationClientException{
        try{
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/templates");
            addQueryParamToURIBuilder("type", templateType, builder);
            String response = notifyHttpClient.getAsString(builder.build());
            return new TemplateList(response);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    @Override
    public TemplatePreview generateTemplatePreview(UUID templateId, Map<String, Object> personalisation) throws NotificationClientException {
        JSONObject body = new JSONObject();
        if (personalisation != null && !personalisation.isEmpty()) {
            body.put("personalisation", new JSONObject(personalisation));
        }
        String response = notifyHttpClient.post(URI.create(baseUrl + "/v2/template/" + templateId + "/preview"), body, HTTP_OK);
        return new TemplatePreview(response);
    }

    @Override
    public ReceivedTextMessageList getReceivedTextMessages(UUID olderThanId) throws NotificationClientException {
        try {
            URIBuilder builder = new URIBuilder(baseUrl + "/v2/received-text-messages");
            addQueryParamToURIBuilder("older_than", olderThanId, builder);
            String response = notifyHttpClient.getAsString(builder.build());
            return new ReceivedTextMessageList(response);
        } catch (URISyntaxException e){
            LOGGER.log(Level.SEVERE, e.toString(), e);
            throw new NotificationClientException(e);
        }
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents           byte[] of the document
     * @param filename                   a string setting the filename of the
     *                                   document upon download
     * @param confirmEmailBeforeDownload boolean True to require the user to enter
     *                                   their email address before accessing the
     *                                   file
     * @param retentionPeriod            a string '[1-78] weeks' to change how long
     *                                   the document should be available to the
     *                                   user
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents,
                                           String filename,
                                           boolean confirmEmailBeforeDownload,
                                           String retentionPeriod) throws NotificationClientException {
        return internalPrepareUpload(documentContents, filename, confirmEmailBeforeDownload, retentionPeriod);
    }

    private static JSONObject internalPrepareUpload(final byte[] documentContents,
                                                    String filename,
                                                    Boolean confirmEmailBeforeDownload,
                                                    String retentionPeriod) throws NotificationClientException {
        if (documentContents.length > 2 * 1024 * 1024) {
            throw new NotificationClientException(413, "File is larger than 2MB");
        }
        byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
        String fileContent = new String(fileContentAsByte, ISO_8859_1);

        Object filenameValue = Objects.nonNull(filename) ? filename : JSONObject.NULL;
        Object confirmEmailBeforeDownloadValue = Objects.nonNull(confirmEmailBeforeDownload) ? confirmEmailBeforeDownload : JSONObject.NULL;
        Object retentionPeriodValue = Objects.nonNull(retentionPeriod) ? retentionPeriod : JSONObject.NULL;

        JSONObject jsonFileObject = new JSONObject();
        jsonFileObject.put("file", fileContent);
        jsonFileObject.put("filename", filenameValue);
        jsonFileObject.put("confirm_email_before_download", confirmEmailBeforeDownloadValue);
        jsonFileObject.put("retention_period", retentionPeriodValue);
        return jsonFileObject;
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents           byte[] of the document
     * @param confirmEmailBeforeDownload boolean True to require the user to enter
     *                                   their email address before accessing the
     *                                   file
     * @param retentionPeriod            a string '[1-78] weeks' to change how long
     *                                   the document should be available to the
     *                                   user
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents,
                                           boolean confirmEmailBeforeDownload,
                                           RetentionPeriodDuration retentionPeriod) throws NotificationClientException {
        return internalPrepareUpload(documentContents, null, confirmEmailBeforeDownload, retentionPeriod.toString());
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents byte[] of the document
     * @param filename         a string setting the filename of the
     *                         document upon download
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents,
                                           String filename) throws NotificationClientException {
        return internalPrepareUpload(documentContents, filename, null, null);
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * @param documentContents byte[] of the document
     * @return <code>JSONObject</code> a json object to be added to the personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents) throws NotificationClientException {
        return internalPrepareUpload(documentContents, null, null, null);
    }

    /**
     * Use the prepareUpload method when uploading a document via sendEmail.
     * The prepareUpload method creates a <code>JSONObject</code> which will need to
     * be added to the personalisation map.
     *
     * This version of the class overloads prepareUpload to allow for the use of the
     * RetentionPeriodDuration class if
     * desired.
     *
     * @see RetentionPeriodDuration
     *
     * @param documentContents           byte[] of the document
     * @param filename                   a string setting the filename of the
     *                                   document upon download
     * @param confirmEmailBeforeDownload boolean True to require the user to enter
     *                                   their email address before accessing the
     *                                   file
     * @param retentionPeriod            a RetentionPeriodDuration that defines how
     *                                   long a file is held for
     * @return <code>JSONObject</code> a json object to be added to the
     *         personalisation is returned
     */
    public static JSONObject prepareUpload(final byte[] documentContents,
                                           String filename,
                                           boolean confirmEmailBeforeDownload,
                                           RetentionPeriodDuration retentionPeriod) throws NotificationClientException {
        return internalPrepareUpload(documentContents, filename, confirmEmailBeforeDownload, retentionPeriod.toString());
    }

    private JSONObject createBodyForPostRequest(final UUID templateId,
                                                final String phoneNumber,
                                                final String emailAddress,
                                                final Map<String, ?> personalisation,
                                                final String reference,
                                                final String encodedFileData,
                                                final String postage) {
        JSONObject body = new JSONObject();

        if(phoneNumber != null && !phoneNumber.isEmpty()) {
            body.put("phone_number", phoneNumber);
        }

        if(emailAddress != null && !emailAddress.isEmpty()) {
            body.put("email_address", emailAddress);
        }

        if(templateId != null) {
            body.put("template_id", templateId);
        }

        if (personalisation != null && !personalisation.isEmpty()) {
            body.put("personalisation", personalisation);
        }

        if(reference != null && !reference.isEmpty()){
            body.put("reference", reference);
        }

        if(encodedFileData != null && !encodedFileData.isEmpty()) {
            body.put("content", encodedFileData);
        }
        if(postage != null && !postage.isEmpty()){
            body.put("postage", postage);
        }
        return body;
    }

    /**
     * Set default SSL context for HTTPS connections.
     * <p/>
     * This is necessary when client has to use keystore
     * (eg provide certification for client authentication).
     * <p/>
     * Use case: enterprise proxy requiring HTTPS client authentication
     */
    private static void setDefaultSSLContext() throws NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
    }

    private static void setCustomSSLContext(final SSLContext sslContext) {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

    private LetterResponse sendPrecompiledLetter(String reference, String base64EncodedPDFFile, String postage) throws NotificationClientException {
        if(NotifyUtils.isBlank(reference)) {
            throw new NotificationClientException("reference cannot be null or empty");
        }

        if(NotifyUtils.isBlank(base64EncodedPDFFile)) {
            throw new NotificationClientException("precompiledPDF cannot be null or empty");
        }

        if(!PdfUtils.isBase64StringPDF(base64EncodedPDFFile)) {
            throw new NotificationClientException("base64EncodedPDFFile is not a PDF");
        }

        JSONObject body = createBodyForPostRequest(null,
                null,
                null,
                null,
                reference,
                base64EncodedPDFFile,
                postage);

        String response = notifyHttpClient.post(URI.create(baseUrl + "/v2/notifications/letter"), body, HTTP_CREATED);
        return new LetterResponse(response);

    }

    @Override
    public LetterResponse sendPrecompiledLetter(String reference, File precompiledPDF) throws NotificationClientException {
        return sendPrecompiledLetter(reference, precompiledPDF, null);
    }

    @Override
    public LetterResponse sendPrecompiledLetter(String reference, File precompiledPDF, String postage) throws NotificationClientException {
        if (precompiledPDF == null)
        {
            throw new NotificationClientException("File cannot be null");
        }
        byte[] buf;
        try {
            buf = FileUtils.readFileToByteArray(precompiledPDF);
        } catch (IOException e) {
            throw new NotificationClientException("Can't read file");
        }
        return sendPrecompiledLetterWithInputStream(reference, new ByteArrayInputStream(buf), postage);
    }

    @Override
    public LetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream) throws NotificationClientException
    {
       return sendPrecompiledLetterWithInputStream(reference, stream, null);
    }

    @Override
    public LetterResponse sendPrecompiledLetterWithInputStream(String reference, InputStream stream, String postage) throws NotificationClientException
    {
        if (stream == null)
        {
            throw new NotificationClientException("Input stream cannot be null");
        }
        Base64InputStream base64InputStream = new Base64InputStream(stream, true, 0, null);
        String encoded;
        try {
            encoded = IOUtils.toString(base64InputStream, ISO_8859_1);
        } catch (IOException e) {
            throw new NotificationClientException("Error when turning Base64InputStream into a string");
        }

        return sendPrecompiledLetter(reference, encoded, postage);
    }

}
