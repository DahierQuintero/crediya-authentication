package co.com.pragma.api;

import co.com.pragma.api.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "User", description = "User management API")
public class RouterRest {
    @Bean
    @RouterOperation(
            path = "/api/v1/users",
            method = {RequestMethod.POST},
            operation = @Operation(
                    operationId = "createUser",
                    summary = "Create a new user",
                    description = "Create a new user in the system",
                    requestBody = @RequestBody(
                            description = "User data to be created",
                            required = true,
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserDTO.class)
                            )
                    ),
                    responses = {
                            @ApiResponse(
                                    responseCode = "201",
                                    description = "User successfully created",
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = ApiResponse.class)
                                    )
                            ),
                            @ApiResponse(
                                    responseCode = "400",
                                    description = "Bad request"
                            ),
                            @ApiResponse(
                                    responseCode = "500",
                                    description = "Internal server error"
                            )
                    }
            )
    )
    public RouterFunction<ServerResponse> routerFunction(UserHandler userHandler) {
        return route(POST("/api/v1/users")
                .and(accept(MediaType.APPLICATION_JSON)), userHandler::save);
    }
}
