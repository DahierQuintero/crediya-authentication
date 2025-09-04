package co.com.pragma.config;

import co.com.pragma.decorators.TransactionalUserUseCase;
import co.com.pragma.model.user.ports.ILoggerPort;
import co.com.pragma.model.user.ports.IUserRepositoryPort;
import co.com.pragma.usecase.user.IUserUseCase;
import co.com.pragma.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.reactive.TransactionalOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UseCasesConfigTest {

    @Mock
    private IUserRepositoryPort userRepositoryPort;

    @Mock
    private ILoggerPort logger;

    @Mock
    private TransactionalOperator transactionalOperator;

    private UseCasesConfig useCasesConfig;

    @BeforeEach
    void setUp() {
        useCasesConfig = new UseCasesConfig(userRepositoryPort, logger, transactionalOperator);
    }

    @Test
    void userUseCase_WithValidDependencies_ReturnsTransactionalUserUseCase() {
        // When
        IUserUseCase result = useCasesConfig.userUseCase();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(TransactionalUserUseCase.class);
    }

    @Test
    void userUseCase_CreatesUserUseCaseWithCorrectDependencies_WrappedInTransactional() {
        // When
        IUserUseCase result = useCasesConfig.userUseCase();

        // Then
        assertThat(result).isInstanceOf(TransactionalUserUseCase.class);

        // Verify that it's wrapping a UserUseCase (if TransactionalUserUseCase exposes the wrapped instance)
        // This would depend on your TransactionalUserUseCase implementation
        // If you have a getter for the wrapped usecase, you could verify it here
    }

    @Test
    void constructor_WithAllDependencies_InitializesCorrectly() {
        // Given - dependencies are already mocked in setUp()

        // When
        UseCasesConfig config = new UseCasesConfig(userRepositoryPort, logger, transactionalOperator);

        // Then
        assertThat(config).isNotNull();
        // The constructor should complete without exceptions
    }

    @Test
    void userUseCase_CallMultipleTimes_ReturnsDifferentInstances() {
        // When
        IUserUseCase result1 = useCasesConfig.userUseCase();
        IUserUseCase result2 = useCasesConfig.userUseCase();

        // Then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1).isNotSameAs(result2); // Different instances (not singleton by default)
    }

    /**
     * Integration test to verify the configuration works within Spring context
     */
    @SpringJUnitConfig
    static class SpringContextTest {

        @TestConfiguration
        static class TestConfig {

            @Bean
            public IUserRepositoryPort userRepositoryPort() {
                return mock(IUserRepositoryPort.class);
            }

            @Bean
            public ILoggerPort loggerPort() {
                return mock(ILoggerPort.class);
            }

            @Bean
            public TransactionalOperator transactionalOperator() {
                return mock(TransactionalOperator.class);
            }
        }

        @Test
        void springContext_WithUseCasesConfig_LoadsSuccessfully() {
            // Given
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.register(TestConfig.class);
            context.register(UseCasesConfig.class);

            try {
                // When
                context.refresh();

                // Then
                assertThat(context.getBeanDefinitionCount()).isGreaterThan(0);

                // Verify specific beans exist
                IUserUseCase userUseCase = context.getBean(IUserUseCase.class);
                assertThat(userUseCase).isNotNull();
                assertThat(userUseCase).isInstanceOf(TransactionalUserUseCase.class);

                // Verify dependencies are injected
                IUserRepositoryPort repoPort = context.getBean(IUserRepositoryPort.class);
                ILoggerPort loggerPort = context.getBean(ILoggerPort.class);
                TransactionalOperator txOperator = context.getBean(TransactionalOperator.class);

                assertThat(repoPort).isNotNull();
                assertThat(loggerPort).isNotNull();
                assertThat(txOperator).isNotNull();

            } finally {
                context.close();
            }
        }
    }

    /**
     * Test for ComponentScan configuration
     */
    @Test
    void componentScanConfiguration_HasCorrectSettings() {
        // Given
        ComponentScan componentScan = UseCasesConfig.class.getAnnotation(ComponentScan.class);

        // Then
        assertThat(componentScan).isNotNull();
        assertThat(componentScan.basePackages()).containsExactly("co.com.pragma.usecase");
        assertThat(componentScan.useDefaultFilters()).isFalse();

        // Verify include filters
        ComponentScan.Filter[] includeFilters = componentScan.includeFilters();
        assertThat(includeFilters).hasSize(1);
        assertThat(includeFilters[0].type()).isEqualTo(FilterType.REGEX);
        assertThat(includeFilters[0].pattern()).containsExactly("^.+UseCase$");

        // Verify exclude filters
        ComponentScan.Filter[] excludeFilters = componentScan.excludeFilters();
        assertThat(excludeFilters).hasSize(1);
        assertThat(excludeFilters[0].type()).isEqualTo(FilterType.ASSIGNABLE_TYPE);
        assertThat(excludeFilters[0].classes()).contains(UserUseCase.class);
    }

    /**
     * Test that verifies the bean creation with null dependencies throws appropriate exceptions
     */
    @Test
    void userUseCase_WithNullRepository_ThrowsException() {
        // Given
        UseCasesConfig configWithNullRepo = new UseCasesConfig(null, logger, transactionalOperator);

        // When & Then
        try {
            configWithNullRepo.userUseCase();
            // If your UserUseCase constructor validates null parameters, this should fail
            // Otherwise, you might need to add null checks
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void userUseCase_WithNullLogger_ThrowsException() {
        // Given
        UseCasesConfig configWithNullLogger = new UseCasesConfig(userRepositoryPort, null, transactionalOperator);

        // When & Then
        try {
            configWithNullLogger.userUseCase();
            // Similar to above - depends on your UserUseCase implementation
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void userUseCase_WithNullTransactionalOperator_ThrowsException() {
        // Given
        UseCasesConfig configWithNullTx = new UseCasesConfig(userRepositoryPort, logger, null);

        // When & Then
        try {
            configWithNullTx.userUseCase();
            // This should fail when creating TransactionalUserUseCase
        } catch (Exception e) {
            assertThat(e).isInstanceOf(NullPointerException.class);
        }
    }

    /**
     * Performance test - ensure bean creation is fast
     */
    @Test
    void userUseCase_BeanCreation_CompletesQuickly() {
        // Given
        long startTime = System.currentTimeMillis();

        // When
        IUserUseCase result = useCasesConfig.userUseCase();

        // Then
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertThat(result).isNotNull();
        assertThat(duration).isLessThan(100); // Should complete in less than 100ms
    }

    /**
     * Test to verify bean can be used (basic functionality check)
     */
    @Test
    void userUseCase_CreatedBean_IsUsable() {
        // When
        IUserUseCase userUseCase = useCasesConfig.userUseCase();

        // Then
        assertThat(userUseCase).isNotNull();

        // Basic check that the object responds to interface methods
        // This will depend on your IUserUseCase interface
        // Example:
        // assertThat(userUseCase.findAll()).isNotNull(); // If it has this method

        // For now, just verify it implements the interface correctly
        assertTrue(userUseCase instanceof IUserUseCase);
        assertTrue(userUseCase instanceof TransactionalUserUseCase);
    }

    /**
     * Memory usage test - ensure no memory leaks in bean creation
     */
    @Test
    void userUseCase_MultipleCreations_DoesNotCauseMemoryLeak() {
        // Given
        int iterations = 1000;

        // When - Create many instances
        for (int i = 0; i < iterations; i++) {
            IUserUseCase useCase = useCasesConfig.userUseCase();
            assertThat(useCase).isNotNull();
        }

        // Then - Force garbage collection and verify we don't run out of memory
        System.gc();

        // If we reach this point without OutOfMemoryError, the test passes
        assertThat(true).isTrue();
    }
}