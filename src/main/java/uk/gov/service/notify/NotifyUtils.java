package uk.gov.service.notify;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

class NotifyUtils {

    private NotifyUtils() {}

    static boolean isBlank(String string) {
        if(Objects.isNull(string)
                || string.trim().length()==0) {
            return true;
        }
        return false;
    }

    static String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }
        return IOUtils.toString(inputStream, UTF_8);
    }

    static String extractServiceId(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 73), Math.max(0, apiKey.length() - 37));
    }

    static String extractApiKey(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 36));
    }

    static String getVersion() {
        InputStream input = null;
        Properties prop = new Properties();
        try
        {
            input = NotifyUtils.class.getClassLoader().getResourceAsStream("application.properties");

            prop.load(input);

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return prop.getProperty("project.version");
    }

}
