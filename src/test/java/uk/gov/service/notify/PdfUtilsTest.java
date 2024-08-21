package uk.gov.service.notify;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.assertj.core.api.Assertions.assertThat;

public class PdfUtilsTest {

    @Test
    public void testIsBase64StringNotValidPdf() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("not_a_pdf.txt")).getFile());

        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));

        String base64encodedString = new String(encoded, US_ASCII);

        assertThat(PdfUtils.isBase64StringPDF(base64encodedString))
                .withFailMessage("test file should not have been detected as a pdf")
                .isFalse();
    }

    @Test
    public void testIsBase64StringValidPdf() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("one_page_pdf.pdf")).getFile());

        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));

        String base64encodedString = new String(encoded, US_ASCII);

        assertThat(PdfUtils.isBase64StringPDF(base64encodedString))
                .withFailMessage("test file should have been detected as a pdf")
                .isTrue();
    }
}
