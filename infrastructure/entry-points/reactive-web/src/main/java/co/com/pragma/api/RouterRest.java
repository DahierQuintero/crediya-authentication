package co.com.pragma.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler userHandler) {
        return route(POST("/api/usecase/otherpath"), userHandler::listenPOSTUseCase)
                .and(route(GET("/api/otherusercase/path"), userHandler::listenGETOtherUseCase))
                .and(route(POST("/api/v1/users"), userHandler::save));
    }
}
