package uk.gov.service.notify;

import org.json.JSONObject;
import org.junit.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PrepareUploadHelperTest {

    @Test
    public void testPrepareUpload() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = PrepareUploadHelper.prepareUpload(documentContent);

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertNull(response.optJSONObject("filename"));
        assertNull(response.optJSONObject("confirm_email_before_download"));
        assertNull(response.optJSONObject("retention_period"));
    }

    @Test
    public void testPrepareUploadWithFilename() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = PrepareUploadHelper.prepareUpload(documentContent, "report.csv");

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertEquals("report.csv", response.getString("filename"));
        assertNull(response.optJSONObject("confirm_email_before_download"));
        assertNull(response.optJSONObject("retention_period"));
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodString() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = PrepareUploadHelper.prepareUpload(
                documentContent,
                "report.csv",
                true,
                "1 weeks");

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertEquals("report.csv", response.getString("filename"));
        assertTrue(response.getBoolean("confirm_email_before_download"));
        assertEquals("1 weeks", response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadWithFilenameAndEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = PrepareUploadHelper.prepareUpload(
                documentContent,
                "report.csv",
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertEquals("report.csv", response.getString("filename"));
        assertTrue(response.getBoolean("confirm_email_before_download"));
        assertEquals("1 weeks", response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        JSONObject response = PrepareUploadHelper.prepareUpload(
                documentContent,
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.getString("file"));
        assertNull(response.optJSONObject("filename"));
        assertTrue(response.getBoolean("confirm_email_before_download"));
        assertEquals("1 weeks", response.getString("retention_period"));
    }

    @Test
    public void testPrepareUploadThrowsExceptionWhenExceeds2MB() {
        char[] data = new char[(2 * 1024 * 1024) + 50];
        byte[] documentContents = new String(data).getBytes();

        NotificationClientException e = assertThrows(NotificationClientException.class,
                () -> PrepareUploadHelper.prepareUpload(documentContents));

        assertEquals(e.getHttpResult(), 413);
        assertEquals(e.getMessage(), "Status code: 413 File is larger than 2MB");
    }

}