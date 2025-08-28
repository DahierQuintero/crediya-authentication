package co.com.pragma.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(
        name = "UserDTO",
        description = "Data User",
        type = "object"
)
public record UserDTO(

        @Schema(description = "Id number", example = "12345678")
        @NotBlank(message = "Id number is required")
        String idNumber,

        @Schema(description = "Name", example = "Dilan")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Last name", example = "Quintero")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Email", example = "dilan@pragmacode.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,

        @Schema(description = "Date of birth", example = "2000-01-01")
        @NotNull(message = "Date of birth is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate birthDate,

        @Schema(description = "Address", example = "Calle 123")
        @NotBlank(message = "Address is required")
        String address,

        @Schema(description = "Phone", example = "3006008080")
        @NotBlank(message = "Phone is required")
        String phone,

        @Schema(description = "Role id", example = "1", type = "integer", format = "int32")
        @NotNull(message = "Role id is required")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        Byte roleId,

        @Schema(description = "Base salary", example = "1000000.00", type = "number", format = "decimal")
        @NotNull(message = "Base salary is required")
        @DecimalMin(value = "0", inclusive = false, message = "Base salary must be greater than 0")
        @DecimalMax(value = "15000000.00", message = "Base salary must be less than 15000000.00")
        BigDecimal baseSalary
) {
}
