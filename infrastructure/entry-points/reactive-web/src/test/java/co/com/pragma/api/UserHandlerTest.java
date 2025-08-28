package co.com.pragma.api;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.helper.ValidationUtil;
import co.com.pragma.model.user.entities.User;
import co.com.pragma.usecase.user.IUserUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private IUserUseCase userUseCase;

    @Mock
    private ValidationUtil validationUtil;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserHandler userHandler;

    private User testUser;
    private UserDTO testUserDTO;
    private String testJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        testUser = new User()
                .setIdUser("123")
                .setName("John")
                .setLastName("Doe")
                .setEmail("john.doe@example.com")
                .setIdNumber("12345678")
                .setBirthDate(LocalDate.of(1990, 1, 1))
                .setAddress("123 Main St")
                .setPhone("1234567890")
                .setRoleId((byte) 1)
                .setBaseSalary(new BigDecimal("1000000.00"));

        testUserDTO = new UserDTO(
                "12345678",
                "John",
                "Doe",
                "john.doe@example.com",
                LocalDate.of(1990, 1, 1),
                "Calle 11",
                "123 Main St",
                (byte) 1,
                new BigDecimal("1000000.00")
        );

        testJson = "{\"idUser\":\"123\",\"name\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}";
    }

    @Test
    void save_ValidRequest_ShouldReturnCreated() throws JsonProcessingException {
        // Arrange
        ServerRequest request = createServerRequestWithBody(testJson);
        when(objectMapper.readValue(testJson, UserDTO.class)).thenReturn(testUserDTO);
        when(validationUtil.validate(any(UserDTO.class))).thenReturn(Mono.just(testUserDTO));
        when(userUseCase.save(any(User.class))).thenReturn(Mono.just(testUser));

        // Act
        var responseMono = userHandler.save(request);

        // Assert
        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> {
                    assert serverResponse.statusCode() == HttpStatus.CREATED;
                    return true;
                })
                .verifyComplete();

        verify(userUseCase).save(any(User.class));
    }

    @Test
    void save_InvalidJson_ShouldReturnBadRequest() throws JsonProcessingException {
        // Arrange
        String invalidJson = "{invalid-json}";
        ServerRequest request = createServerRequestWithBody(invalidJson);
        when(objectMapper.readValue(invalidJson, UserDTO.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act
        var responseMono = userHandler.save(request);

        // Assert
        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> 
                    serverResponse.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    private ServerRequest createServerRequestWithBody(String body) {
        return ServerRequest.create(
                MockServerWebExchange.from(
                        MockServerHttpRequest.post("/api/users")
                                .header("X-Trace-Id", "test-trace-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body)
                ),
                HandlerStrategies.withDefaults().messageReaders()
        );
    }
}
