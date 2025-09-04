package co.com.pragma.api.web.exception;

import co.com.pragma.api.exceptions.ExternalServiceException;
import co.com.pragma.api.exceptions.RepositoryException;
import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.exceptions.UserNotFoundException;
import co.com.pragma.model.user.exceptions.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-ID");
        if (traceId == null) {
            traceId = "NO_TRACE_ID";
        }

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof WebExchangeBindException webEx) {
            return handleValidationException(webEx, response, traceId);
        }

        return handleOtherExceptions(ex, response, traceId);
    }

    private Mono<Void> handleValidationException(WebExchangeBindException ex,
                                                 ServerHttpResponse response, String traceId) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format("'%s' %s",
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(errorMessage)
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();

        return writeResponse(response, HttpStatus.BAD_REQUEST, errorResponse);
    }

    private Mono<Void> handleOtherExceptions(Throwable ex,
                                             ServerHttpResponse response, String traceId) {

        ErrorResponse errorResponse = buildErrorResponse(ex, traceId);
        HttpStatus status = determineHttpStatus(ex);

        logError(ex, traceId, status);
        return writeResponse(response, status, errorResponse);
    }

    private Mono<Void> writeResponse(ServerHttpResponse response,
                                     HttpStatus status, ErrorResponse errorResponse) {

        response.setStatusCode(status);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing error response", e);
            return response.setComplete();
        }
    }

    public static ErrorResponse buildErrorResponse(Throwable ex, String traceId) {
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
            List<ErrorResponse.FieldError> fieldErrors = validationEx.getViolations().stream()
                    .map(v -> new ErrorResponse.FieldError(v.getField(), v.getMessage()))
                    .toList();

            return ErrorResponse.builder()
                    .code(ErrorCode.VALIDATION_ERROR.getCode())
                    .message(validationEx.getMessage())
                    .errors(fieldErrors)
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

        if (ex instanceof WebExchangeBindException) {
            return ErrorResponse.builder()
                    .code(ErrorCode.VALIDATION_ERROR.getCode())
                    .message(ex.getMessage())
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

    private HttpStatus determineHttpStatus(Throwable ex) {
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
        if (ex instanceof WebExchangeBindException) {
            return HttpStatus.BAD_REQUEST;
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
