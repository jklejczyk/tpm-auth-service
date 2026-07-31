package pl.klejczyk.tpm.auth.domain;

/**
 * Deliberately says nothing about which half was wrong - telling an attacker that a
 * username exists turns login into a user enumeration oracle.
 */
public class InvalidCredentials extends RuntimeException {

    public InvalidCredentials() {
        super("Invalid username or password.");
    }
}
