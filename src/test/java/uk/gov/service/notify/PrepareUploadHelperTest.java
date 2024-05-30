package uk.gov.service.notify;

import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class PrepareUploadHelperTest {

    @Test
    public void testPrepareUpload() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(documentContent);

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.get("file"));
        assertNull(response.get("filename"));
        assertNull(response.get("confirm_email_before_download"));
        assertNull(response.get("retention_period"));
    }

    @Test
    public void testPrepareUploadWithFilename() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(documentContent, "report.csv");

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.get("file"));
        assertEquals("report.csv", response.get("filename"));
        assertNull(response.get("confirm_email_before_download"));
        assertNull(response.get("retention_period"));
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodString() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(
                documentContent,
                "report.csv",
                true,
                "1 weeks");

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.get("file"));
        assertEquals("report.csv", response.get("filename"));
        assertEquals(true, response.get("confirm_email_before_download"));
        assertEquals("1 weeks", response.get("retention_period"));
    }

    @Test
    public void testPrepareUploadWithFilenameAndEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(
                documentContent,
                "report.csv",
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.get("file"));
        assertEquals("report.csv", response.get("filename"));
        assertEquals(true, response.get("confirm_email_before_download"));
        assertEquals("1 weeks", response.get("retention_period"));
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(
                documentContent,
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertEquals("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==", response.get("file"));
        assertNull(response.get("filename"));
        assertEquals(true, response.get("confirm_email_before_download"));
        assertEquals("1 weeks", response.get("retention_period"));
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