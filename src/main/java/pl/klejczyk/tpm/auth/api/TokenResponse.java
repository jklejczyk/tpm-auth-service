package pl.klejczyk.tpm.auth.api;

public record TokenResponse(String token, long expiresIn) {
}
