package co.com.pragma.model.user.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends DomainException {

    private final List<FieldViolation> violations;

    public ValidationException(String message, List<FieldViolation> violations) {
        super(message);
        this.violations = violations;
    }

    public ValidationException(String message) {
        super(message);
        this.violations = null;
    }

    @Getter
    @AllArgsConstructor
    public static class FieldViolation {
        private final String field;
        private final String message;
    }
}
