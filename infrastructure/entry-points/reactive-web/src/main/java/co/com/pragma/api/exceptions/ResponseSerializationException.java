package co.com.pragma.r2dbc.exceptions;

public class ResponseSerializationException extends RuntimeException {
    public ResponseSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
