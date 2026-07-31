package pl.klejczyk.tpm.auth.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.klejczyk.tpm.auth.application.TokenService;

@RestController
class AuthController {

    private final TokenService tokenService;

    AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    TokenResponse token(@Valid @RequestBody TokenRequest request) {
        TokenService.IssuedToken issued = tokenService.issue(request.username(), request.password());
        return new TokenResponse(issued.token(), issued.expiresInSeconds());
    }
}
