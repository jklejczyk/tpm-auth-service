package pl.klejczyk.tpm.auth.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.klejczyk.tpm.auth.domain.InvalidCredentials;
import pl.klejczyk.tpm.auth.domain.User;
import pl.klejczyk.tpm.auth.domain.UserRepository;
import pl.klejczyk.tpm.auth.infrastructure.security.JwtIssuer;

@Service
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;

    public TokenService(UserRepository users, PasswordEncoder passwordEncoder, JwtIssuer jwtIssuer) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtIssuer = jwtIssuer;
    }

    @Transactional(readOnly = true)
    public IssuedToken issue(String username, String password) {
        User user = users.findByUsername(username).orElseThrow(() -> rejected(username));

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw rejected(username);
        }

        log.info("Issued token for {} with role {}", user.id(), user.role());
        return new IssuedToken(jwtIssuer.issue(user), jwtIssuer.ttl().toSeconds());
    }

    /**
     * The attempted username is recorded server-side for audit purposes. The response itself
     * stays identical in both cases, so the endpoint cannot be used to discover which
     * usernames exist.
     */
    private InvalidCredentials rejected(String username) {
        log.warn("Rejected token request for username '{}'", username);
        return new InvalidCredentials();
    }

    public record IssuedToken(String token, long expiresInSeconds) {
    }
}
