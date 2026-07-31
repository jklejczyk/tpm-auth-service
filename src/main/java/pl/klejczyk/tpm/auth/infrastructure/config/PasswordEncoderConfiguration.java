package pl.klejczyk.tpm.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class PasswordEncoderConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        // Cost 10 - matches the hashes seeded by migration V2.
        return new BCryptPasswordEncoder(10);
    }
}
