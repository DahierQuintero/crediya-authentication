package co.com.pragma.api.helper;

import co.com.pragma.api.web.exception.ErrorResponse;
import co.com.pragma.model.user.exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ValidationUtil {

    private final Validator validator;

    public <T> Mono<T> validate(T body) {
        if (body == null) {
            return Mono.error(new ValidationException("Request body cannot be null"));
        }

        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            List<ValidationException.FieldViolation> fieldViolations = violations.stream()
                    .map(v -> new ValidationException.FieldViolation(
                            v.getPropertyPath().toString(),
                            v.getMessage()
                    ))
                    .toList();

            return Mono.error(new ValidationException("Validation failed", fieldViolations));
        }

        return Mono.just(body);
    }
}
