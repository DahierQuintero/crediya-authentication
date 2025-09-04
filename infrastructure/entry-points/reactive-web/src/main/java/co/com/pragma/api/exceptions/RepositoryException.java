package co.com.pragma.r2dbc.exceptions;

public class RepositoryException extends RuntimeException {
    public RepositoryException(String message) {
        super(message);
    }

  public RepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
