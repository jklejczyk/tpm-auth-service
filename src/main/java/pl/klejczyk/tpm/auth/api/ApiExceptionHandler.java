package pl.klejczyk.tpm.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.klejczyk.tpm.auth.domain.InvalidCredentials;

import java.util.Map;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(InvalidCredentials.class)
    ResponseEntity<Map<String, String>> unauthorized(InvalidCredentials exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", exception.getMessage()));
    }
}
