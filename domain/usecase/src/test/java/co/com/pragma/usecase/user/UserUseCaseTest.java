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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
        testUser.setIdUser("123");
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
        when(userRepositoryPort.existsByIdUser(anyString())).thenReturn(Mono.just(false));
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
        // Arrange
        when(userRepositoryPort.existsByIdUser(testUser.getIdUser())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(userUseCase.save(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("User with idUser " + testUser.getIdUser() + " already exists")
                )
                .verify();

        verify(userRepositoryPort, never()).saveUser(any());
    }

    @Test
    void saveUser_WhenUserWithEmailExists_ShouldThrowException() {
        // Arrange
        when(userRepositoryPort.existsByIdUser(testUser.getIdUser())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByEmail(testUser.getEmail())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(userUseCase.save(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("User with email " + testUser.getEmail() + " already exists")
                )
                .verify();

        verify(userRepositoryPort, never()).saveUser(any());
    }

    @Test
    void saveUser_WhenUserWithIdNumberExists_ShouldThrowException() {
        // Arrange
        when(userRepositoryPort.existsByIdUser(testUser.getIdUser())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByEmail(testUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepositoryPort.existsByIdNumber(testUser.getIdNumber())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(userUseCase.save(testUser))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserAlreadyExistsException &&
                                throwable.getMessage().contains("User with idNumber " + testUser.getIdNumber() + " already exists")
                )
                .verify();

        verify(userRepositoryPort, never()).saveUser(any());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        User user1 = new User(/* ... */);
        User user2 = new User(/* ... */);
        when(userRepositoryPort.findAll()).thenReturn(Flux.just(user1, user2));

        // Act & Assert
        StepVerifier.create(userUseCase.findAll())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(logger).info(anyString(), anyString());
    }
}
