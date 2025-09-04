package co.com.pragma.r2dbc.web.exception;

import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.exceptions.UserNotFoundException;
import co.com.pragma.model.user.exceptions.ValidationException;
import co.com.pragma.r2dbc.exceptions.ExternalServiceException;
import co.com.pragma.r2dbc.exceptions.RepositoryException;
import co.com.pragma.r2dbc.exceptions.ResponseSerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String traceId = extractTraceId(exchange);

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", "application/json");

        ErrorResponse errorResponse = buildErrorResponse(ex, traceId);
        HttpStatusCode status = determineHttpStatus(ex);

        response.setStatusCode(status);

        // Log del error
        logError(ex, traceId, status);

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBuffer buffer = response.bufferFactory().allocateBuffer(1024);
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
                buffer.write(bytes);
                return buffer;
            } catch (Exception e) {
                throw new ResponseSerializationException("Error serializing response", e);
            }
        }));
    }

    private ErrorResponse buildErrorResponse(Throwable ex, String traceId) {
        if (ex instanceof UserAlreadyExistsException) {
            return ErrorResponse.builder()
                    .code(ErrorCode.USER_ALREADY_EXISTS.getCode())
                    .message(ex.getMessage())
                    .traceId(traceId)
                    .timestamp(Instant.now())
                    .build();
        }

        if (ex instanceof UserNotFoundException) {
            return ErrorResponse.builder()
                    .code(ErrorCode.USER_NOT_FOUND.getCode())
                    .message(ex.getMessage())
                    .traceId(traceId)
                    .timestamp(Instant.now())
                    .build();
        }

        if (ex instanceof ValidationException validationEx) {
            return ErrorResponse.builder()
                    .code(ErrorCode.VALIDATION_ERROR.getCode())
                    .message(validationEx.getMessage())
                    .field(validationEx.getField())
                    .value(validationEx.getValue())
                    .traceId(traceId)
                    .timestamp(Instant.now())
                    .build();
        }

        if (ex instanceof RepositoryException) {
            return ErrorResponse.builder()
                    .code(ErrorCode.DATABASE_ERROR.getCode())
                    .message("Database operation failed")
                    .traceId(traceId)
                    .timestamp(Instant.now())
                    .build();
        }

        if (ex instanceof ExternalServiceException) {
            return ErrorResponse.builder()
                    .code(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode())
                    .message("External service unavailable")
                    .traceId(traceId)
                    .timestamp(Instant.now())
                    .build();
        }

        return ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("An unexpected error occurred")
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }

    private HttpStatusCode determineHttpStatus(Throwable ex) {
        if (ex instanceof UserAlreadyExistsException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof UserNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof RepositoryException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (ex instanceof ExternalServiceException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private void logError(Throwable ex, String traceId, HttpStatusCode status) {
        if (status.is5xxServerError()) {
            log.error("TRACE_ID:{} - Server error: {} - {}", traceId, status, ex.getMessage(), ex);
        } else if (status.is4xxClientError()) {
            log.warn("TRACE_ID:{} - Client error: {} - {}", traceId, status, ex.getMessage());
        } else {
            log.info("TRACE_ID:{} - Response: {} - {}", traceId, status, ex.getMessage());
        }
    }

    private String extractTraceId(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-Trace-ID");
    }

}
