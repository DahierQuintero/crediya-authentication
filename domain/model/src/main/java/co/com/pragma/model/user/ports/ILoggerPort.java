package co.com.pragma.model.user.ports;

public interface ILoggerPort {
    void info(String traceId, String message, Object... args);
    void debug(String traceId, String message, Object... args);
    void warn(String traceId, String message, Object... args);
    void error(String traceId, String message, Throwable throwable, Object... args);
}
