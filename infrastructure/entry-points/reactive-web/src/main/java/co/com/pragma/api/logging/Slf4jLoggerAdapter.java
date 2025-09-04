package co.com.pragma.api.logging;

import co.com.pragma.model.user.ports.ILoggerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Slf4j
@Component
public class Slf4jLoggerAdapter implements ILoggerPort {

    private static final String TRACEID = "TRACE_ID:{} - ";

    @Override
    public void info(String traceId, String message, Object... args) {
        log.info(TRACEID + message, Stream.concat(Stream.of(traceId), Stream.of(args)).toArray());
    }

    @Override
    public void debug(String traceId, String message, Object... args) {
        log.debug(TRACEID + message, Stream.concat(Stream.of(traceId), Stream.of(args)).toArray());
    }

    @Override
    public void warn(String traceId, String message, Object... args) {
        log.warn(TRACEID + message, Stream.concat(Stream.of(traceId), Stream.of(args)).toArray());
    }

    @Override
    public void error(String traceId, String message, Throwable throwable, Object... args) {
        log.error(TRACEID + message, Stream.concat(Stream.of(traceId), Stream.of(args)).toArray(), throwable);
    }
}
