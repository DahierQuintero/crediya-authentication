package co.com.pragma.r2dbc;

import co.com.pragma.model.user.entities.User;
import co.com.pragma.r2dbc.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.reactivecommons.utils.ObjectMapperImp;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IUserEntityRepositoryPortAdapterTest {

    @Mock
    private UserEntityRepository repository;

    @InjectMocks
    private IUserEntityRepositoryPortAdapter adapter;

    private final ObjectMapper mapper = new ObjectMapperImp();
    private User testUser;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        adapter = new IUserEntityRepositoryPortAdapter(repository, mapper);

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

        testUserEntity = new UserEntity();
        testUserEntity.setIdUser(123L);
        testUserEntity.setName("Test");
        testUserEntity.setLastName("User");
        testUserEntity.setEmail("test@example.com");
        testUserEntity.setIdNumber("ID123");
        testUserEntity.setBirthDate(LocalDate.of(2002, 2, 22));
        testUserEntity.setAddress("123 Main St");
        testUserEntity.setPhone("123456789");
        testUserEntity.setRoleId((byte) 1);
        testUserEntity.setBaseSalary(new BigDecimal(5200100));

    }

    @Test
    void saveUser_ShouldReturnSavedUser() {
        // Arrange
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(testUserEntity));

        // Act & Assert
        StepVerifier.create(adapter.saveUser(testUser))
                .expectNextMatches(savedUser ->
                        savedUser.getIdUser().equals(testUser.getIdUser()) &&
                                savedUser.getEmail().equals(testUser.getEmail())
                )
                .verifyComplete();
    }

    @Test
    void existsByIdUser_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        when(repository.existsByIdUser(anyLong())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(adapter.existsByIdUser(123L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByIdUser_WhenUserNotExists_ShouldReturnFalse() {
        // Arrange
        when(repository.existsByIdUser(anyLong())).thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(adapter.existsByIdUser(123L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Arrange
        when(repository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(adapter.existsByEmail("test@example.com"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByIdNumber_WhenIdNumberExists_ShouldReturnTrue() {
        // Arrange
        when(repository.existsByIdNumber(anyString())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(adapter.existsByIdNumber("1234567890"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByIdNumber_WhenIdNumberNotExists_ShouldReturnFalse() {
        // Arrange
        when(repository.existsByIdNumber(anyString())).thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(adapter.existsByIdNumber("non-existent-id"))
                .expectNext(false)
                .verifyComplete();
    }
}
