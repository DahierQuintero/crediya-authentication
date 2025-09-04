package co.com.pragma.api.exceptions;

public class ResponseSerializationException extends RuntimeException {
    public ResponseSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
