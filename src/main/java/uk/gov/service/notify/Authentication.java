package uk.gov.service.notify;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

final class Authentication {

    private Authentication() {}

    static String create(String issuer, String secret) {
        return create(issuer, secret, System.currentTimeMillis());
    }

    static String create(String issuer, String secret, long issuedAtMsFromEpoch) {
        try {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setHeader(HeaderParameterNames.TYPE, "JWT");
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(issuer);
            claims.setIssuedAt(NumericDate.fromMilliseconds(issuedAtMsFromEpoch));
            jws.setPayload(claims.toJson());
            jws.setKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "RAW"));

            return jws.getCompactSerialization();

        } catch (JoseException ex) {
            throw new RuntimeException(ex);
        }
    }

    static void validateBearerToken(String token, String serviceId, String apiKey) throws InvalidJwtException {
        final int allowedSecondsInTheFuture = 60;
        final int allowedSecondsInThePast = 60;
        validateBearerToken(token, serviceId, apiKey, allowedSecondsInTheFuture, allowedSecondsInThePast);
    }

    static void validateBearerToken(String token, String serviceId, String apiKey, int allowedSecondsInTheFuture, int allowedSecondsInThePast) throws InvalidJwtException {
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setExpectedIssuer(serviceId)
                .setIssuedAtRestrictions(allowedSecondsInTheFuture, allowedSecondsInThePast)
                .setVerificationKey(new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "RAW"))
                .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256)
                .build();
        jwtConsumer.process(token);
    }
}
