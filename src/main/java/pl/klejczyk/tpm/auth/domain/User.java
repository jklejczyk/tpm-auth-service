package pl.klejczyk.tpm.auth.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read-only inside this service: users are seeded by migration and never created at runtime.
 * A real identity provider would own registration - this one deliberately does not.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;

    private String username;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected User() {
    }

    public String id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }
}
