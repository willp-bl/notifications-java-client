package uk.gov.service.notify;

import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class PrepareUploadHelper {

    private PrepareUploadHelper() {}

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
    public static Map<String, ?> prepareUpload(final byte[] documentContents,
                                           String filename,
                                           boolean confirmEmailBeforeDownload,
                                           String retentionPeriod) throws NotificationClientException {
        return internalPrepareUpload(documentContents, filename, confirmEmailBeforeDownload, retentionPeriod);
    }

    private static Map<String, ?> internalPrepareUpload(final byte[] documentContents,
                                                    String filename,
                                                    Boolean confirmEmailBeforeDownload,
                                                    String retentionPeriod) throws NotificationClientException {
        if (documentContents.length > 2 * 1024 * 1024) {
            throw new NotificationClientException(413, "File is larger than 2MB");
        }
        byte[] fileContentAsByte = Base64.encodeBase64(documentContents);
        String fileContent = new String(fileContentAsByte, ISO_8859_1);

        Map<String, Object> jsonFileObject = new HashMap<>();
        jsonFileObject.put("file", fileContent);
        jsonFileObject.put("filename", filename);
        jsonFileObject.put("confirm_email_before_download", confirmEmailBeforeDownload);
        jsonFileObject.put("retention_period", retentionPeriod);
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
    public static Map<String, ?> prepareUpload(final byte[] documentContents,
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
    public static Map<String, ?> prepareUpload(final byte[] documentContents, String filename) throws NotificationClientException {
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
    public static Map<String, ?> prepareUpload(final byte[] documentContents) throws NotificationClientException {
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
    public static Map<String, ?> prepareUpload(final byte[] documentContents,
                                           String filename,
                                           boolean confirmEmailBeforeDownload,
                                           RetentionPeriodDuration retentionPeriod) throws NotificationClientException {
        return internalPrepareUpload(documentContents, filename, confirmEmailBeforeDownload, retentionPeriod.toString());
    }

}
