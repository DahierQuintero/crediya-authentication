package co.com.pragma.api.helper;

import co.com.pragma.model.user.exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationUtilTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationUtil validationUtil;

    private TestObject validTestObject;
    private TestObject invalidTestObject;

    @BeforeEach
    void setUp() {
        validTestObject = new TestObject("Valid Name", "valid@email.com");
        invalidTestObject = new TestObject("", "invalid-email");
    }

    @Test
    void validate_WithValidObject_ShouldReturnObjectInMono() {
        // Given
        when(validator.validate(validTestObject)).thenReturn(Set.of());

        // When
        Mono<TestObject> result = validationUtil.validate(validTestObject);

        // Then
        StepVerifier.create(result)
                .expectNext(validTestObject)
                .verifyComplete();

        verify(validator).validate(validTestObject);
    }

    @Test
    void validate_WithNullObject_ShouldReturnError() {
        // Given
        TestObject nullObject = null;

        // When
        Mono<TestObject> result = validationUtil.validate(nullObject);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Body cannot be null"))
                .verify();

        verifyNoInteractions(validator);
    }

    @Test
    void validate_WithSingleValidationError_ShouldReturnValidationException() {
        // Given
        ConstraintViolation<TestObject> violation = createMockViolation("Name cannot be empty");
        Set<ConstraintViolation<TestObject>> violations = Set.of(violation);

        when(validator.validate(invalidTestObject)).thenReturn(violations);

        // When
        Mono<TestObject> result = validationUtil.validate(invalidTestObject);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().equals("Name cannot be empty"))
                .verify();

        verify(validator).validate(invalidTestObject);
    }

    @Test
    void validate_WithMultipleValidationErrors_ShouldReturnConcatenatedMessage() {
        // Given
        ConstraintViolation<TestObject> violation1 = createMockViolation("Name cannot be empty");
        ConstraintViolation<TestObject> violation2 = createMockViolation("Email format is invalid");
        Set<ConstraintViolation<TestObject>> violations = Set.of(violation1, violation2);

        when(validator.validate(invalidTestObject)).thenReturn(violations);

        // When
        Mono<TestObject> result = validationUtil.validate(invalidTestObject);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    if (!(throwable instanceof ValidationException)) {
                        return false;
                    }
                    String message = throwable.getMessage();
                    // El orden puede variar en un Set, así que verificamos que contiene ambos mensajes
                    return (message.contains("Name cannot be empty") && message.contains("Email format is invalid")) &&
                            message.contains(", ");
                })
                .verify();

        verify(validator).validate(invalidTestObject);
    }

    @Test
    void validate_WithEmptyViolationsSet_ShouldReturnObjectInMono() {
        // Given
        Set<ConstraintViolation<TestObject>> emptyViolations = new HashSet<>();
        when(validator.validate(validTestObject)).thenReturn(emptyViolations);

        // When
        Mono<TestObject> result = validationUtil.validate(validTestObject);

        // Then
        StepVerifier.create(result)
                .expectNext(validTestObject)
                .verifyComplete();

        verify(validator).validate(validTestObject);
    }

    @Test
    void validate_WithDifferentObjectType_ShouldWorkGenerically() {
        // Given
        String stringObject = "test string";
        when(validator.validate(stringObject)).thenReturn(Set.of());

        // When
        Mono<String> result = validationUtil.validate(stringObject);

        // Then
        StepVerifier.create(result)
                .expectNext(stringObject)
                .verifyComplete();

        verify(validator).validate(stringObject);
    }

    @Test
    void validate_WithIntegerObject_ShouldWorkGenerically() {
        // Given
        Integer numberObject = 42;
        when(validator.validate(numberObject)).thenReturn(Set.of());

        // When
        Mono<Integer> result = validationUtil.validate(numberObject);

        // Then
        StepVerifier.create(result)
                .expectNext(numberObject)
                .verifyComplete();

        verify(validator).validate(numberObject);
    }

    @Test
    void validate_WithValidationErrorOnDifferentType_ShouldReturnError() {
        // Given
        String stringObject = "";
        ConstraintViolation<String> violation = createMockViolationForType("String cannot be empty");
        Set<ConstraintViolation<String>> violations = Set.of(violation);

        when(validator.validate(stringObject)).thenReturn(violations);

        // When
        Mono<String> result = validationUtil.validate(stringObject);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ValidationException &&
                                throwable.getMessage().equals("String cannot be empty"))
                .verify();
    }

    @Test
    void validate_WithThreeValidationErrors_ShouldConcatenateAllMessages() {
        // Given
        ConstraintViolation<TestObject> violation1 = createMockViolation("Name cannot be empty");
        ConstraintViolation<TestObject> violation2 = createMockViolation("Email format is invalid");
        ConstraintViolation<TestObject> violation3 = createMockViolation("Name must be at least 2 characters");
        Set<ConstraintViolation<TestObject>> violations = Set.of(violation1, violation2, violation3);

        when(validator.validate(invalidTestObject)).thenReturn(violations);

        // When
        Mono<TestObject> result = validationUtil.validate(invalidTestObject);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    if (!(throwable instanceof ValidationException)) {
                        return false;
                    }
                    String message = throwable.getMessage();
                    return message.contains("Name cannot be empty") &&
                            message.contains("Email format is invalid") &&
                            message.contains("Name must be at least 2 characters") &&
                            message.contains(", ");
                })
                .verify();
    }

    @Test
    void validate_ValidationExceptionMessage_ShouldBeProperlyFormatted() {
        // Given
        ConstraintViolation<TestObject> violation1 = createMockViolation("First error");
        ConstraintViolation<TestObject> violation2 = createMockViolation("Second error");
        Set<ConstraintViolation<TestObject>> violations = Set.of(violation1, violation2);

        when(validator.validate(invalidTestObject)).thenReturn(violations);

        // When
        Mono<TestObject> result = validationUtil.validate(invalidTestObject);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    if (!(throwable instanceof ValidationException)) {
                        return false;
                    }
                    String message = throwable.getMessage();
                    // Verificar que no hay espacios extra o formato incorrecto
                    return !message.startsWith(", ") &&
                            !message.endsWith(", ") &&
                            message.contains("First error") &&
                            message.contains("Second error");
                })
                .verify();
    }

    // Método auxiliar para crear mock de ConstraintViolation
    @SuppressWarnings("unchecked")
    private ConstraintViolation<TestObject> createMockViolation(String message) {
        ConstraintViolation<TestObject> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    // Método auxiliar genérico para diferentes tipos
    @SuppressWarnings("unchecked")
    private <T> ConstraintViolation<T> createMockViolationForType(String message) {
        ConstraintViolation<T> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    // Clase de apoyo para testing
    private static class TestObject {
        private String name;
        private String email;

        public TestObject(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return java.util.Objects.equals(name, that.name) &&
                    java.util.Objects.equals(email, that.email);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, email);
        }

        @Override
        public String toString() {
            return "TestObject{name='" + name + "', email='" + email + "'}";
        }
    }
}