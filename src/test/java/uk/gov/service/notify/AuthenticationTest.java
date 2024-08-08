package uk.gov.service.notify;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class AuthenticationTest {

    @Test
    public void testJwtCreation() {
        String serviceId = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString();

        String token = Authentication.create(serviceId, apiKey);

        try {
            validateBearerToken(token, serviceId, apiKey);
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
                () -> validateBearerToken(jwt, serviceId, apiKey));

        assertThat(e.getMessage()).contains("Issuer (iss) claim value ("+differentServiceId+") doesn't match expected value of "+serviceId);
    }

    @Test
    public void testJwtIsInvalidIfWrongKeyUsed() {
        String serviceId = UUID.randomUUID().toString();
        String apiKey = UUID.randomUUID().toString();
        String differentApiKey = UUID.randomUUID().toString();

        String jwt = Authentication.create(serviceId, differentApiKey);
        InvalidJwtException e = assertThrows(InvalidJwtException.class,
                () -> validateBearerToken(jwt, serviceId, apiKey));

        assertThat(e.getMessage()).startsWith("JWT rejected due to invalid signature");;
    }

    private void validateBearerToken(String token, String serviceId, String apiKey) throws InvalidJwtException {
        final int allowedSecondsInTheFuture = 60;
        final int allowedSecondsInThePast = 60;
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setExpectedIssuer(serviceId)
                .setIssuedAtRestrictions(allowedSecondsInTheFuture, allowedSecondsInThePast)
                .setVerificationKey(new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "RAW"))
                .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256)
                .build();
        jwtConsumer.process(token);
    }
}