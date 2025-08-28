package co.com.pragma.model.user.exceptions;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String email, boolean byEmail) {
        super("User with email " + email + " already exists");
    }
}
