package co.com.pragma.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ApiResponseDTO<T> {
    @Schema(description = "Response status code")
    private String code;

    @Schema(description = "Descriptive message")
    private String message;

    @Schema(description = "Response data")
    private T data;
}
