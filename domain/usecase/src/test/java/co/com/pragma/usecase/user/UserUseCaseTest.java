package co.com.pragma.usecase.user;

import co.com.pragma.model.user.entities.User;
import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.ports.ILoggerPort;
import co.com.pragma.model.user.ports.IUserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private IUserRepositoryPort userRepositoryPort;

    @Mock
    private ILoggerPort logger;

    @InjectMocks
    private UserUseCase userUseCase;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setIdUser(123L);
        testUser.setName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setIdNumber("ID123");
        testUser.setBirthDate(LocalDate.of(2002, 2, 22));
        testUser.setAddress("123 Main St");
        testUser.setPhone("123456789");
        testUser.setRoleId((byte) 1);
        testUser.setBaseSalary(new BigDecimal(5200100));
    }

    @Test
    void saveUser_Success() {
        // Arrange
        when(userRepositoryPort.existsByIdUser(anyLong())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByIdNumber(anyString())).thenReturn(Mono.just(false));
        when(userRepositoryPort.saveUser(any(User.class))).thenReturn(Mono.just(testUser));

        // Act & Assert
        StepVerifier.create(userUseCase.save(testUser))
                .expectNext(testUser)
                .verifyComplete();

        verify(userRepositoryPort).existsByIdUser(testUser.getIdUser());
        verify(userRepositoryPort).existsByEmail(testUser.getEmail());
        verify(userRepositoryPort).existsByIdNumber(testUser.getIdNumber());
        verify(userRepositoryPort).saveUser(testUser);
    }

    @Test
    void saveUser_WhenUserWithIdExists_ShouldThrowException() {
        when(userRepositoryPort.existsByIdUser(testUser.getIdUser())).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.save(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("idUser " + testUser.getIdUser()))
                .verify();

        verify(userRepositoryPort, never()).saveUser(any());
    }

    @Test
    void saveUser_WhenUserWithEmailExists_ShouldThrowException() {
        when(userRepositoryPort.existsByIdUser(anyLong())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByEmail(testUser.getEmail())).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.save(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("email " + testUser.getEmail()))
                .verify();

        verify(userRepositoryPort, never()).saveUser(any());
    }

    @Test
    void saveUser_WhenUserWithIdNumberExists_ShouldThrowException() {
        when(userRepositoryPort.existsByIdUser(anyLong())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByIdNumber(testUser.getIdNumber())).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.save(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("idNumber " + testUser.getIdNumber()))
                .verify();

        verify(userRepositoryPort, never()).saveUser(any());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user1 = new User("s", "s", "s", "s", LocalDate.now(), "s", "s", (byte) 1, new BigDecimal(1000));
        User user2 = new User("n", "n", "n", "n", LocalDate.now(), "n", "n", (byte) 1, new BigDecimal(1000));
        when(userRepositoryPort.findAll()).thenReturn(Flux.just(user1, user2));

        StepVerifier.create(userUseCase.findAll())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(logger, times(2)).info(anyString(), anyString());
    }
}