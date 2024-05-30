package uk.gov.service.notify;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.lang.JoseException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

final class Authentication {

    private Authentication() {
    }

    static String create(String issuer, String secret) {
        try {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setHeader(HeaderParameterNames.TYPE, "JWT");
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(issuer);
            claims.setIssuedAtToNow();
            jws.setPayload(claims.toJson());
            jws.setKey(keyFromString(secret));

            return jws.getCompactSerialization();

        } catch (JoseException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static SecretKey keyFromString(String value) throws UnsupportedEncodingException {
        return new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "RAW");
    }
}
