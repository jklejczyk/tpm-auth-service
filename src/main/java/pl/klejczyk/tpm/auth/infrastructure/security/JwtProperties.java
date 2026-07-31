package pl.klejczyk.tpm.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;

@ConfigurationProperties(prefix = "tpm.jwt")
public record JwtProperties(
        Resource privateKeyLocation,
        String issuer,
        Duration ttl) {
}
