package co.com.pragma.api;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.exceptions.ExternalServiceException;
import co.com.pragma.api.exceptions.RepositoryException;
import co.com.pragma.api.helper.ValidationUtil;
import co.com.pragma.model.user.entities.User;
import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.exceptions.UserNotFoundException;
import co.com.pragma.model.user.exceptions.ValidationException;
import co.com.pragma.usecase.user.IUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private IUserUseCase userUseCase;

    @Mock
    private ValidationUtil validator;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserHandler userHandler;

    private UserDTO testUserDTO;
    private User testUser;
    private String traceId;
    private String validJsonBody;

    @BeforeEach
    void setUp() {
        traceId = "test-trace-123";

        testUserDTO = new UserDTO(
                "1234",
                "John",
                "Doe",
                "john@test.com",
                LocalDate.of(1990, 1, 1),
                "calle 12",
                "1234567890",
                (byte) 1,
                BigDecimal.valueOf(5000)
        );

        testUser = new User()
                .setIdUser(1234L)
                .setName("John")
                .setLastName("Doe")
                .setEmail("john@test.com")
                .setBirthDate(LocalDate.of(1990, 1, 1))
                .setAddress("calle 12")
                .setPhone("1234567890")
                .setRoleId((byte) 1)
                .setBaseSalary(BigDecimal.valueOf(5000));

        validJsonBody = """
                {
                    "name": "John Doe",
                    "idNumber": "12345678",
                    "email": "john@test.com",
                    "birthDate": "1990-01-01",
                    "phoneNumber": "1234567890"
                }
                """;
    }

    @Test
    void save_WithValidRequest_ShouldCreateUserSuccessfully() throws Exception {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .body(Mono.just(validJsonBody));

        when(objectMapper.readValue(validJsonBody, UserDTO.class)).thenReturn(testUserDTO);
        when(validator.validate(testUserDTO)).thenReturn(Mono.just(testUserDTO));
        when(userUseCase.save(any(User.class))).thenReturn(Mono.just(testUser));

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.statusCode());
                })
                .verifyComplete();

        verify(objectMapper).readValue(validJsonBody, UserDTO.class);
        verify(validator).validate(testUserDTO);
        verify(userUseCase).save(any(User.class));
    }

    @Test
    void save_WithoutTraceId_ShouldUseDefaultTraceId() throws Exception {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(validJsonBody));

        when(objectMapper.readValue(validJsonBody, UserDTO.class)).thenReturn(testUserDTO);
        when(validator.validate(testUserDTO)).thenReturn(Mono.just(testUserDTO));
        when(userUseCase.save(any(User.class))).thenReturn(Mono.just(testUser));

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CREATED, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void save_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .body(Mono.just(invalidJson));

        when(objectMapper.readValue(invalidJson, UserDTO.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Invalid JSON") {});

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
                })
                .verifyComplete();

        verify(objectMapper).readValue(invalidJson, UserDTO.class);
        verifyNoInteractions(validator);
        verifyNoInteractions(userUseCase);
    }

    @Test
    void save_WithValidationError_ShouldReturnBadRequest() throws Exception {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .body(Mono.just(validJsonBody));

        when(objectMapper.readValue(validJsonBody, UserDTO.class)).thenReturn(testUserDTO);
        when(validator.validate(testUserDTO))
                .thenReturn(Mono.error(new ValidationException("Name is required")));

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
                })
                .verifyComplete();

        verify(objectMapper).readValue(validJsonBody, UserDTO.class);
        verify(validator).validate(testUserDTO);
        verifyNoInteractions(userUseCase);
    }

    @Test
    void save_WithUserAlreadyExistsException_ShouldReturnConflict() throws Exception {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .body(Mono.just(validJsonBody));

        when(objectMapper.readValue(validJsonBody, UserDTO.class)).thenReturn(testUserDTO);
        when(validator.validate(testUserDTO)).thenReturn(Mono.just(testUserDTO));
        when(userUseCase.save(any(User.class)))
                .thenReturn(Mono.error(new UserAlreadyExistsException("User already exists")));

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void save_WithRepositoryException_ShouldReturnInternalServerError() throws Exception {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .body(Mono.just(validJsonBody));

        when(objectMapper.readValue(validJsonBody, UserDTO.class)).thenReturn(testUserDTO);
        when(validator.validate(testUserDTO)).thenReturn(Mono.just(testUserDTO));
        when(userUseCase.save(any(User.class)))
                .thenReturn(Mono.error(new RepositoryException("Database error")));

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void save_WithExternalServiceException_ShouldReturnServiceUnavailable() throws Exception {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .body(Mono.just(validJsonBody));

        when(objectMapper.readValue(validJsonBody, UserDTO.class)).thenReturn(testUserDTO);
        when(validator.validate(testUserDTO)).thenReturn(Mono.just(testUserDTO));
        when(userUseCase.save(any(User.class)))
                .thenReturn(Mono.error(new ExternalServiceException("External service down")));

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void save_WithGenericException_ShouldReturnInternalServerError() throws Exception {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .body(Mono.just(validJsonBody));

        when(objectMapper.readValue(validJsonBody, UserDTO.class)).thenReturn(testUserDTO);
        when(validator.validate(testUserDTO)).thenReturn(Mono.just(testUserDTO));
        when(userUseCase.save(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

        // When
        Mono<ServerResponse> result = userHandler.save(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void getAllUsers_WithValidRequest_ShouldReturnUsersList() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .build();

        List<User> users = List.of(testUser);
        when(userUseCase.findAll()).thenReturn(Flux.fromIterable(users));

        // When
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                    assertNotNull(response.headers().getFirst("Content-Type"));
                    assertTrue(response.headers().getFirst("Content-Type").contains("application/json"));
                })
                .verifyComplete();

        verify(userUseCase).findAll();
    }

    @Test
    void getAllUsers_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .build();

        when(userUseCase.findAll()).thenReturn(Flux.empty());

        // When
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();

        verify(userUseCase).findAll();
    }

    @Test
    void getAllUsers_WithoutTraceId_ShouldUseDefaultTraceId() {
        // Given
        ServerRequest request = MockServerRequest.builder().build();

        List<User> users = List.of(testUser);
        when(userUseCase.findAll()).thenReturn(Flux.fromIterable(users));

        // When
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void getAllUsers_WithRepositoryException_ShouldReturnInternalServerError() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .build();

        when(userUseCase.findAll())
                .thenReturn(Flux.error(new RepositoryException("Database connection error")));

        // When
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void getAllUsers_WithExternalServiceException_ShouldReturnServiceUnavailable() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .build();

        when(userUseCase.findAll())
                .thenReturn(Flux.error(new ExternalServiceException("External service error")));

        // When
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void getAllUsers_WithUserNotFoundException_ShouldReturnNotFound() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .build();

        when(userUseCase.findAll())
                .thenReturn(Flux.error(new UserNotFoundException("No users found")));

        // When
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.NOT_FOUND, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void getAllUsers_WithGenericException_ShouldReturnInternalServerError() {
        // Given
        ServerRequest request = MockServerRequest.builder()
                .header("X-Trace-ID", traceId)
                .build();

        when(userUseCase.findAll())
                .thenReturn(Flux.error(new RuntimeException("Unexpected error")));

        // When
        Mono<ServerResponse> result = userHandler.getAllUsers(request);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void determineHttpStatus_WithDifferentExceptions_ShouldReturnCorrectStatus() {
        // Test IllegalArgumentException
        assertEquals(HttpStatus.BAD_REQUEST,
                UserHandler.determineHttpStatus(new IllegalArgumentException("Invalid argument")));

        // Test ValidationException
        assertEquals(HttpStatus.BAD_REQUEST,
                UserHandler.determineHttpStatus(new ValidationException("Validation failed")));

        // Test UserAlreadyExistsException
        assertEquals(HttpStatus.CONFLICT,
                UserHandler.determineHttpStatus(new UserAlreadyExistsException("User exists")));

        // Test UserNotFoundException
        assertEquals(HttpStatus.NOT_FOUND,
                UserHandler.determineHttpStatus(new UserNotFoundException("User not found")));

        // Test RepositoryException
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                UserHandler.determineHttpStatus(new RepositoryException("Repository error")));

        // Test ExternalServiceException
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                UserHandler.determineHttpStatus(new ExternalServiceException("Service down")));

        // Test Generic Exception
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                UserHandler.determineHttpStatus(new RuntimeException("Unknown error")));
    }
}

class UserHandlerTestSupport extends UserHandler {
    public UserHandlerTestSupport() {
        super(null, null, null);
    }

    public static HttpStatus determineHttpStatus(Throwable ex) {
        return UserHandler.determineHttpStatus(ex);
    }
}