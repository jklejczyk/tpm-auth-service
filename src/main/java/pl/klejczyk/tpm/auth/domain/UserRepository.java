package pl.klejczyk.tpm.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Spring Data derives the query from the method name - no implementation needed.
     */
    Optional<User> findByUsername(String username);
}
