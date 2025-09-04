package co.com.pragma.model.user.exceptions;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId);
    }
}
