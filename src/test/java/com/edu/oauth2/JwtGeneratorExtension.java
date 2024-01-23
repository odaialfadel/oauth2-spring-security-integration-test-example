package com.edu.oauth2;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Date;

/**
 * This class provides functionality to generate JSON Web Tokens (JWT) using RSA256 algorithm.
 * The tokens can be generated with or without an expiry date.
 *
 * <p>The tokens are signed using an RSA private key and can be later verified using the corresponding
 * public key. The generated public key can be accessed as a JSON Web Key Set (JWKS) string.
 *
 * <p>Example usage:
 * <pre>{@code
 * JWTGenerator jwtGenerator = new JWTGenerator();
 * String token = jwtGenerator.token("user123", new Date(System.currentTimeMillis() + 3600000));
 * }</pre>
 */
public class JwtGeneratorExtension implements BeforeAllCallback {

    private final String audience;
    private final String issuer;
    private RSAKey key;
    private String jwks;

    public JwtGeneratorExtension(String audience, String issuer) {
        this.audience = audience;
        this.issuer = issuer;
    }

    public String audience() {
        return audience;
    }

    public String issuer() {
        return issuer;
    }

    public String jwks() {
        return jwks;
    }

    /**
     * Generates a JWT token for the specified subject without an expiry date.
     *
     * @param subject The subject of the JWT.
     * @return The generated JWT token.
     * @throws Exception If an error occurs during token generation.
     */
    public String token(String subject) throws Exception {
        return token(subject, null);
    }

    /**
     * Generates a JWT token for the specified subject with an optional expiry date.
     *
     * @param subject The subject of the JWT.
     * @param expiry The expiry date of the JWT. If null, a default expiry of 60 seconds from the current time is used.
     * @return The generated JWT token.
     * @throws Exception If an error occurs during token generation.
     */
    public String token(String subject, Date expiry) throws Exception {
        final var signer = new RSASSASigner(key);
        final var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(key.getKeyID())
                .build();
        final var jwt = new SignedJWT(header, claims(subject, expiry));

        jwt.sign(signer);

        return jwt.serialize();
    }

    /**
     * Initializes the RSA key and JWKS string before all tests.
     *
     * @param context The ExtensionContext for the test.
     * @throws Exception If an error occurs during initialization.
     */
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        key = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(new Algorithm("RS256"))
                .keyID("... secret key for tests ...")
                .generate();

        jwks = String.format("{\"keys\": [%s]}", key.toPublicJWK().toJSONString());
    }

    /**
     * Generates JWT claims with the specified subject and optional expiry date.
     *
     * @param subject The subject of the JWT.
     * @param expiry The expiry date of the JWT. If null, a default expiry of 60 seconds from the current time is used.
     * @return The JWTClaimsSet representing the claims of the JWT.
     */
    private JWTClaimsSet claims(String subject, Date expiry) {
        if (expiry == null)
            expiry = new Date(new Date().getTime() + 60 * 1000);

        return new JWTClaimsSet.Builder()
                .expirationTime(expiry)
                .audience(audience)
                .issuer(issuer)
                .subject(subject)
                .build();
    }
}
