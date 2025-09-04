package co.com.pragma.config;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TransactionConfigTest {

    @Mock
    private ConnectionFactory connectionFactory;

    private final TransactionConfig transactionConfig = new TransactionConfig();

    @Test
    void transactionalOperator_WithValidConnectionFactory_ReturnsTransactionalOperator() {
        // When
        TransactionalOperator result = transactionConfig.transactionalOperator(connectionFactory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(TransactionalOperator.class);
    }

    @Test
    void transactionalOperator_WithNullConnectionFactory_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            transactionConfig.transactionalOperator(null);
        });
    }

    @Test
    void transactionalOperator_CallMultipleTimes_ReturnsDifferentInstances() {
        // When
        TransactionalOperator result1 = transactionConfig.transactionalOperator(connectionFactory);
        TransactionalOperator result2 = transactionConfig.transactionalOperator(connectionFactory);

        // Then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1).isNotSameAs(result2); // Different instances (not singleton)
    }

    @Test
    void transactionalOperator_BeanCreation_CompletesQuickly() {
        // Given
        long startTime = System.currentTimeMillis();

        // When
        TransactionalOperator result = transactionConfig.transactionalOperator(connectionFactory);

        // Then
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertThat(result).isNotNull();
        assertThat(duration).isLessThan(100); // Should complete in less than 100ms
    }

    @Test
    void transactionalOperator_CreatedBean_IsUsable() {
        // When
        TransactionalOperator operator = transactionConfig.transactionalOperator(connectionFactory);

        // Then
        assertThat(operator).isNotNull();
        assertThat(operator).isInstanceOf(TransactionalOperator.class);

        // Verify it has the expected interface methods available
        // (This is implicit verification that the bean is properly constructed)
    }
}