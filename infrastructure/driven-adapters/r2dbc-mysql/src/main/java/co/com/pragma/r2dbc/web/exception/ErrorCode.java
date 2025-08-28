package co.com.pragma.r2dbc.web.exception;

public enum ErrorCode {

    USER_ALREADY_EXISTS("USER_001", "User already exists"),
    USER_NOT_FOUND("USER_002", "User not found"),
    VALIDATION_ERROR("VALIDATION_001", "Validation failed"),

    BUSINESS_RULE_VIOLATION("BUSINESS_001", "Business rule violation"),

    DATABASE_ERROR("INFRA_001", "Database error"),
    EXTERNAL_SERVICE_ERROR("INFRA_002", "External service error"),

    INTERNAL_SERVER_ERROR("SYSTEM_001", "Internal server error"),
    BAD_REQUEST("SYSTEM_002", "Bad request");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
}
