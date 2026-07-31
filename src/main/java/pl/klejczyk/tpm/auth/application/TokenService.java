package pl.klejczyk.tpm.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.klejczyk.tpm.auth.domain.InvalidCredentials;
import pl.klejczyk.tpm.auth.domain.User;
import pl.klejczyk.tpm.auth.domain.UserRepository;
import pl.klejczyk.tpm.auth.infrastructure.security.JwtIssuer;

@Service
public class TokenService {

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
        User user = users.findByUsername(username).orElseThrow(InvalidCredentials::new);

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new InvalidCredentials();
        }

        return new IssuedToken(jwtIssuer.issue(user), jwtIssuer.ttl().toSeconds());
    }

    public record IssuedToken(String token, long expiresInSeconds) {
    }
}
