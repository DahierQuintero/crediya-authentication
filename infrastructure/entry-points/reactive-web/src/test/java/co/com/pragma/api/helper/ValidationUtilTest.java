package co.com.pragma.api.helper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationUtilTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationUtil validationUtil;

    @Test
    void validate_WithValidObject_ShouldReturnMonoWithObject() {
        // Given
        TestDto validDto = new TestDto("John", "john@example.com");
        Set<ConstraintViolation<TestDto>> emptyViolations = Set.of();

        when(validator.validate(validDto)).thenReturn(emptyViolations);

        // When
        Mono<TestDto> result = validationUtil.validate(validDto);

        // Then
        StepVerifier.create(result)
                .expectNext(validDto)
                .verifyComplete();

        verify(validator).validate(validDto);
    }

    @Test
    void validate_WithInvalidObject_ShouldReturnMonoError() {
        // Given
        TestDto invalidDto = new TestDto("", "invalid-email");

        // Mock violations
        Set<ConstraintViolation<TestDto>> violations = createMockViolations(invalidDto);
        when(validator.validate(invalidDto)).thenReturn(violations);

        // When
        Mono<TestDto> result = validationUtil.validate(invalidDto);

        // Then
        StepVerifier.create(result)
                .expectError(ConstraintViolationException.class)
                .verify();

        verify(validator).validate(invalidDto);
    }

    @Test
    void validate_WithInvalidObject_ShouldReturnCorrectViolations() {
        // Given
        TestDto invalidDto = new TestDto("", "invalid-email");
        Set<ConstraintViolation<TestDto>> violations = createMockViolations(invalidDto);
        when(validator.validate(invalidDto)).thenReturn(violations);

        // When
        Mono<TestDto> result = validationUtil.validate(invalidDto);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ConstraintViolationException.class, error);
                    ConstraintViolationException ex = (ConstraintViolationException) error;
                    assertEquals(violations, ex.getConstraintViolations());
                    assertEquals(2, ex.getConstraintViolations().size());
                })
                .verify();
    }

    @Test
    void validate_WithNullObject_ShouldHandleGracefully() {
        // Given
        TestDto nullDto = null;
        Set<ConstraintViolation<TestDto>> emptyViolations = Set.of();
        when(validator.validate(nullDto)).thenReturn(emptyViolations);

        // When
        Mono<TestDto> result = validationUtil.validate(nullDto);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(validator).validate(nullDto);
    }

    @Test
    void validate_WithMultipleViolations_ShouldReturnAllViolations() {
        // Given
        TestDto invalidDto = new TestDto("", "");
        Set<ConstraintViolation<TestDto>> multipleViolations = createMultipleViolations(invalidDto);
        when(validator.validate(invalidDto)).thenReturn(multipleViolations);

        // When
        Mono<TestDto> result = validationUtil.validate(invalidDto);

        // Then
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(ConstraintViolationException.class, error);
                    ConstraintViolationException ex = (ConstraintViolationException) error;
                    assertTrue(ex.getConstraintViolations().size() >= 2);
                })
                .verify();
    }

    @Test
    void validate_WithDifferentObjectTypes_ShouldWork() {
        // Given
        String validString = "valid";
        Set<ConstraintViolation<String>> emptyViolations = Set.of();
        when(validator.validate(validString)).thenReturn(emptyViolations);

        // When
        Mono<String> result = validationUtil.validate(validString);

        // Then
        StepVerifier.create(result)
                .expectNext(validString)
                .verifyComplete();
    }

    @Test
    void validate_ShouldPreserveObjectReference() {
        // Given
        TestDto originalDto = new TestDto("John", "john@example.com");
        Set<ConstraintViolation<TestDto>> emptyViolations = Set.of();
        when(validator.validate(originalDto)).thenReturn(emptyViolations);

        // When
        Mono<TestDto> result = validationUtil.validate(originalDto);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto == originalDto) // Same reference
                .verifyComplete();
    }

    // Helper methods para crear mock violations
    @SuppressWarnings("unchecked")
    private Set<ConstraintViolation<TestDto>> createMockViolations(TestDto dto) {
        Set<ConstraintViolation<TestDto>> violations = new HashSet<>();

        ConstraintViolation<TestDto> nameViolation = mock(ConstraintViolation.class);
        when(nameViolation.getMessage()).thenReturn("Name cannot be blank");
        when(nameViolation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));

        ConstraintViolation<TestDto> emailViolation = mock(ConstraintViolation.class);
        when(emailViolation.getMessage()).thenReturn("Email should be valid");
        when(emailViolation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));

        violations.add(nameViolation);
        violations.add(emailViolation);

        return violations;
    }

    @SuppressWarnings("unchecked")
    private Set<ConstraintViolation<TestDto>> createMultipleViolations(TestDto dto) {
        Set<ConstraintViolation<TestDto>> violations = new HashSet<>();

        ConstraintViolation<TestDto> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("Name cannot be blank");

        ConstraintViolation<TestDto> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("Email cannot be blank");

        ConstraintViolation<TestDto> violation3 = mock(ConstraintViolation.class);
        when(violation3.getMessage()).thenReturn("Email should be valid format");

        violations.add(violation1);
        violations.add(violation2);
        violations.add(violation3);

        return violations;
    }

    // Clase de test para usar en las pruebas
    static class TestDto {
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        private String name;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        private String email;

        public TestDto() {}

        public TestDto(String name, String email) {
            this.name = name;
            this.email = email;
        }

        // Getters y setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestDto testDto = (TestDto) o;
            return java.util.Objects.equals(name, testDto.name) &&
                    java.util.Objects.equals(email, testDto.email);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, email);
        }
    }
}

class ValidationUtilIntegrationTest {

    private ValidationUtil validationUtil;

    @Test
    void validate_WithRealValidator_ShouldWorkCorrectly() {
        // Configurar un validador real para este test
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator realValidator = factory.getValidator();
        ValidationUtil realValidationUtil = new ValidationUtil(realValidator);

        // Test con objeto válido
        TestDto validDto = new TestDto("John Doe", "john@example.com");

        StepVerifier.create(realValidationUtil.validate(validDto))
                .expectNext(validDto)
                .verifyComplete();

        // Test con objeto inválido
        TestDto invalidDto = new TestDto("", "invalid-email");

        StepVerifier.create(realValidationUtil.validate(invalidDto))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    // Clase DTO de prueba con anotaciones reales
    @Getter
    @Setter
    static class TestDto {
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        private String name;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        private String email;

        public TestDto() {}

        public TestDto(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}