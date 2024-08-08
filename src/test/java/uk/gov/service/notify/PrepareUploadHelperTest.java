package uk.gov.service.notify;

import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrepareUploadHelperTest {

    @Test
    public void testPrepareUpload() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(documentContent);

        assertThat(response.get("file")).isEqualTo("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==");
        assertThat(response.get("filename")).isNull();
        assertThat(response.get("confirm_email_before_download")).isNull();
        assertThat(response.get("retention_period")).isNull();
    }

    @Test
    public void testPrepareUploadWithFilename() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(documentContent, "report.csv");

        assertThat(response.get("file")).isEqualTo("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==");
        assertThat(response.get("filename")).isEqualTo("report.csv");
        assertThat(response.get("confirm_email_before_download")).isNull();
        assertThat(response.get("retention_period")).isNull();
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodString() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(
                documentContent,
                "report.csv",
                true,
                "1 weeks");

        assertThat(response.get("file")).isEqualTo("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==");
        assertThat(response.get("filename")).isEqualTo("report.csv");
        assertThat(response.get("confirm_email_before_download")).isEqualTo(true);
        assertThat(response.get("retention_period")).isEqualTo("1 weeks");
    }

    @Test
    public void testPrepareUploadWithFilenameAndEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(
                documentContent,
                "report.csv",
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertThat(response.get("file")).isEqualTo("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==");
        assertThat(response.get("filename")).isEqualTo("report.csv");
        assertThat(response.get("confirm_email_before_download")).isEqualTo(true);
        assertThat(response.get("retention_period")).isEqualTo("1 weeks");
    }

    @Test
    public void testPrepareUploadWithEmailConfirmationAndRetentionPeriodDuration() throws NotificationClientException {
        byte[] documentContent = "this is a document to test with".getBytes();

        Map<String, ?> response = PrepareUploadHelper.prepareUpload(
                documentContent,
                true,
                new RetentionPeriodDuration(1, ChronoUnit.WEEKS));

        assertThat(response.get("file")).isEqualTo("dGhpcyBpcyBhIGRvY3VtZW50IHRvIHRlc3Qgd2l0aA==");
        assertThat(response.get("filename")).isNull();
        assertThat(response.get("confirm_email_before_download")).isEqualTo(true);
        assertThat(response.get("retention_period")).isEqualTo("1 weeks");
    }

    @Test
    public void testPrepareUploadThrowsExceptionWhenExceeds2MB() {
        char[] data = new char[(2 * 1024 * 1024) + 50];
        byte[] documentContents = new String(data).getBytes();

        NotificationClientHttpException e = assertThrows(NotificationClientHttpException.class,
                () -> PrepareUploadHelper.prepareUpload(documentContents));

        assertThat(e.getHttpResult()).isEqualTo(413);
        assertThat(e).hasMessage("Status code: 413 File is larger than 2MB");
    }

}