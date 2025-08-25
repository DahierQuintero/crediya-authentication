package co.com.pragma.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserDTO(
        @NotBlank(message = "Id number is required")
        String idNumber,
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,
        @NotNull(message = "Date of birth is required")
        LocalDate birthDate,
        @NotBlank(message = "Address is required")
        String address,
        @NotBlank(message = "Phone is required")
        String phone,
        @DecimalMin(value = "0", inclusive = false, message = "Base salary must be greater than 0")
        @DecimalMax(value = "15000000.00", inclusive = true, message = "Base salary must be less than 15000000.00")
        BigDecimal baseSalary
) {
}
