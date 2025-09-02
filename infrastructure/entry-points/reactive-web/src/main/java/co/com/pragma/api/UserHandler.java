package co.com.pragma.api;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.helper.ValidationUtil;
import co.com.pragma.api.mapper.UserMapper;
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

import java.util.HashMap;
import java.util.Map;
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
                .flatMap(body -> {
                    try {
                        UserDTO userDTO = objectMapper.readValue(body, UserDTO.class);
                        log.debug("[{}] Parsed UserDTO: {}", traceId, userDTO);
                        return Mono.just(userDTO);
                    } catch (JsonProcessingException e) {
                        log.error("[{}] Error parsing request body: {}", traceId, e.getMessage(), e);
                        return Mono.error(new IllegalArgumentException("Invalid request body: " + e.getMessage()));
                    }
                })
                .flatMap(validator::validate)
                .map(UserMapper::toUser)
                .doOnNext(user -> log.debug("[{}] Mapped to User: {}", traceId, user))
                .flatMap(userUseCase::save)
                .map(UserMapper::toUserDTO)
                .flatMap(savedUser -> {
                    log.info("[{}] User created successfully: {}", traceId, savedUser.idNumber());
                    return ServerResponse
                            .status(HttpStatus.CREATED)
                            .bodyValue(savedUser);
                })
                .onErrorResume(e -> {
                    log.error("[{}] Error creating user: {}", traceId, e.getMessage(), e);
                    return ServerResponse
                            .status(getHttpStatus(e))
                            .bodyValue(createErrorResponse(e, traceId));
                })
                .contextWrite(Context.of("traceId", traceId));
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
                .onErrorResume(e -> {
                    log.error("[{}] Error getting all users: {}", traceId, e.getMessage(), e);
                    return ServerResponse
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .bodyValue(createErrorResponse(e, traceId));
                })
                .contextWrite(Context.of("traceId", traceId));
    }

    private String extractTraceId(ServerRequest request) {
        return Objects.requireNonNullElse(
                request.headers().firstHeader("X-Trace-ID"),
                "NO_TRACE_ID"
        );
    }

    private HttpStatus getHttpStatus(Throwable e) {
        if (e instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Map<String, Object> createErrorResponse(Throwable e, String traceId) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", "ERROR");
        errorResponse.put("message", e.getMessage());
        errorResponse.put("traceId", traceId);
        errorResponse.put("timestamp", java.time.Instant.now().toString());
        return errorResponse;
    }
}
