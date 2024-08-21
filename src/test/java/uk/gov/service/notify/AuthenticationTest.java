package uk.gov.service.notify;

import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthenticationTest {

    // this tests that the validateBearerToken() method in this class is working
    // so we can be confident that it is checking the output of the main code
    @Test
    public void testFixedJwtString() {
        final String serviceId = UUID.randomUUID().toString();
        final String apiKey = UUID.randomUUID().toString();

        final long msInPastToIssueToken = 120 * 1_000L; // 120 seconds
        final String token = Authentication.create(serviceId, apiKey, System.currentTimeMillis()- msInPastToIssueToken);

        final int allowedSecondsInThePast = 60*3; // 180 seconds
        assertThat(allowedSecondsInThePast*1_000L).isGreaterThan(msInPastToIssueToken);

        try {
            Authentication.validateBearerToken(token, serviceId, apiKey, 60, allowedSecondsInThePast);
        } catch (InvalidJwtException e) {
            fail("unable to validate token: "+e);
        }
    }

    // this tests that the validateBearerToken() method in this class is working
    // so we can be confident that it is checking the output of the main code
    @Test
    public void testFixedJwtStringIsExpired() {
        final String serviceId = UUID.randomUUID().toString();
        final String apiKey = UUID.randomUUID().toString();

        final long msInPastToIssueToken = 120 * 1_000L; // 120 seconds
        final String token = Authentication.create(serviceId, apiKey, System.currentTimeMillis()-msInPastToIssueToken);

        InvalidJwtException e = assertThrows(InvalidJwtException.class,
                () -> Authentication.validateBearerToken(token, serviceId, apiKey));

        assertThat(e).hasMessageContaining("rejected due to invalid claims or other invalid content");
        assertThat(e).hasMessageContaining("is more than 60 second(s) in the past");
    }

    @Test
    public void testJwtCreation() {
        String serviceId = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString();

        String token = Authentication.create(serviceId, apiKey);

        try {
            Authentication.validateBearerToken(token, serviceId, apiKey);
        } catch (InvalidJwtException e) {
            fail("unable to validate token: "+e);
        }
    }

    @Test
    public void testJwtIsInvalidIfWrongIssuerUsed() {
        String serviceId = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString();
        String differentServiceId = UUID.randomUUID().toString();

        String jwt = Authentication.create(differentServiceId, apiKey);
        InvalidJwtException e = assertThrows(InvalidJwtException.class,
                () -> Authentication.validateBearerToken(jwt, serviceId, apiKey));

        assertThat(e).hasMessageContaining("Issuer (iss) claim value ("+differentServiceId+") doesn't match expected value of "+serviceId);
    }

    @Test
    public void testJwtIsInvalidIfWrongKeyUsed() {
        String serviceId = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString();
        String differentApiKey = UUID.randomUUID().toString();

        String jwt = Authentication.create(serviceId, differentApiKey);
        InvalidJwtException e = assertThrows(InvalidJwtException.class,
                () -> Authentication.validateBearerToken(jwt, serviceId, apiKey));

        assertThat(e).hasMessageStartingWith("JWT rejected due to invalid signature");
    }

}