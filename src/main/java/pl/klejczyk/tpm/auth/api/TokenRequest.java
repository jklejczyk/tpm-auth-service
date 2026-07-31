package pl.klejczyk.tpm.auth.api;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
