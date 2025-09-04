package co.com.pragma.api;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.exceptions.ExternalServiceException;
import co.com.pragma.api.exceptions.RepositoryException;
import co.com.pragma.api.helper.ValidationUtil;
import co.com.pragma.api.mapper.UserMapper;
import co.com.pragma.api.web.exception.ErrorResponse;
import co.com.pragma.api.web.exception.GlobalExceptionHandler;
import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.exceptions.UserNotFoundException;
import co.com.pragma.model.user.exceptions.ValidationException;
import co.com.pragma.usecase.user.IUserUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHandler {
    private final IUserUseCase userUseCase;
    private final ValidationUtil validator;
    private final ObjectMapper objectMapper;

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        String traceId = extractTraceId(serverRequest);
        log.info("[{}] Received create user request", traceId);

        return serverRequest.bodyToMono(String.class)
                .doOnNext(body -> log.debug("[{}] Request body: {}", traceId, body))
                .flatMap(body -> parseUserDto(body, traceId))
                .flatMap(validator::validate)
                .map(UserMapper::toUser)
                .doOnNext(user -> log.debug("[{}] Mapped to User: {}", traceId, user))
                .flatMap(userUseCase::save)
                .map(UserMapper::toUserDTO)
                .flatMap(savedUser -> buildSuccessResponse(savedUser, traceId))
                .onErrorResume(e -> handleError(e, traceId))
                .contextWrite(Context.of("traceId", traceId));
    }

    private Mono<UserDTO> parseUserDto(String body, String traceId) {
        try {
            UserDTO userDTO = objectMapper.readValue(body, UserDTO.class);
            log.debug("[{}] Parsed UserDTO: {}", traceId, userDTO);
            return Mono.just(userDTO);
        } catch (JsonProcessingException e) {
            log.error("[{}] Error parsing request body: {}", traceId, e.getMessage(), e);
            return Mono.error(new IllegalArgumentException("Invalid request body: " + e.getMessage()));
        }
    }

    private Mono<ServerResponse> buildSuccessResponse(UserDTO savedUser, String traceId) {
        log.info("[{}] User created successfully: {}", traceId, savedUser.idNumber());
        return ServerResponse
                .status(HttpStatus.CREATED)
                .bodyValue(savedUser);
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        String traceId = extractTraceId(request);
        log.info("[{}] Received get all users request", traceId);

        return userUseCase.findAll()
                .map(UserMapper::toUserDTO)
                .collectList()
                .flatMap(users -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(users)
                )
                .onErrorResume(e -> handleError(e, traceId))
                .contextWrite(Context.of("traceId", traceId));
    }

    private String extractTraceId(ServerRequest request) {
        return Objects.requireNonNullElse(
                request.headers().firstHeader("X-Trace-ID"),
                "NO_TRACE_ID"
        );
    }

    private static Mono<ServerResponse> handleError(Throwable e, String traceId) {
        log.error("[{}] Error processing request: {}", traceId, e.getMessage(), e);
        ErrorResponse errorResponse = GlobalExceptionHandler.buildErrorResponse(e, traceId);
        HttpStatus status = determineHttpStatus(e);
        return ServerResponse.status(status).bodyValue(errorResponse);
    }

    public static HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof IllegalArgumentException || ex instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof UserAlreadyExistsException) {
            return HttpStatus.CONFLICT;
        } else if (ex instanceof UserNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof RepositoryException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (ex instanceof ExternalServiceException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
