package co.com.pragma.model.user.exceptions;

public class ValidationException extends DomainException {

    private final String field;
    private final Object value;

    public ValidationException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
