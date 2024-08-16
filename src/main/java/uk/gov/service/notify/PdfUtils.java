package uk.gov.service.notify;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Scanner;

public class PdfUtils {

    private PdfUtils() {}

    /**
     * A method to determine if a file is a pdf file
     *
     * @param base64String A base64 encoded string containing a PDF file to send via the API
     *
     * @return True if the file is a PDF, otherwise false
     *
     */
    public static boolean isBase64StringPDF(String base64String) {
        final byte[] decoded = Base64.getMimeDecoder().decode(base64String);

        final Scanner input = new Scanner(new ByteArrayInputStream(decoded));

        return isPDF(input);
    }

    private static boolean isPDF(Scanner input) {
        while (input.hasNextLine()) {
            if(input.nextLine().contains("%PDF-")) {
                return true;
            }
        }
        return false;
    }
}
