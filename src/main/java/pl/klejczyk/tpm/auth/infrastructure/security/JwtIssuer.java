package pl.klejczyk.tpm.auth.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;
import pl.klejczyk.tpm.auth.domain.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Signs tokens with the private key. This is the only place in the whole system that
 * holds it - every other service gets the public key and can therefore only verify.
 */
@Component
public class JwtIssuer {

    private final RSAPrivateKey privateKey;
    private final JwtProperties properties;
    private final Clock clock;

    JwtIssuer(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.privateKey = readPrivateKey(properties);
    }

    public String issue(User user) {
        Instant now = clock.instant();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.id())
                .claim("role", user.role().name())
                .issuer(properties.issuer())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(properties.ttl())))
                .build();

        SignedJWT token = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        try {
            JWSSigner signer = new RSASSASigner(privateKey);
            token.sign(signer);
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to sign the token.", exception);
        }
        return token.serialize();
    }

    public Duration ttl() {
        return properties.ttl();
    }

    private static RSAPrivateKey readPrivateKey(JwtProperties properties) {
        try (var stream = properties.privateKeyLocation().getInputStream()) {
            String pem = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            String base64 = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] der = Base64.getDecoder().decode(base64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException(
                    "Unable to load the private key from " + properties.privateKeyLocation(), exception);
        }
    }
}
