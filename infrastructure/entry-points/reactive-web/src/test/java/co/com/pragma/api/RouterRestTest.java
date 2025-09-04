package co.com.pragma.api;

import co.com.pragma.api.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

class RouterRestTest {

    private WebTestClient webTestClient;
    private UserHandler userHandler;

    @BeforeEach
    void setUp() {
        userHandler = Mockito.mock(UserHandler.class);
        RouterRest routerRest = new RouterRest();

        webTestClient = WebTestClient.bindToRouterFunction(
                routerRest.routerFunction(userHandler)
        ).build();
    }

    @Test
    void shouldRouteToGetAllUsers() {
        UserDTO user = new UserDTO(
                "123",
                "John",
                "Doe",
                "john.doe@example.com",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                "3001234567",
                (byte) 1,
                BigDecimal.valueOf(5000)
        );

        Mockito.when(userHandler.getAllUsers(Mockito.any()))
                .thenReturn(ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Flux.just(user), UserDTO.class));

        webTestClient.get()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("John");

        Mockito.verify(userHandler).getAllUsers(Mockito.any());
    }

    @Test
    void shouldRouteToSaveUser() {
        UserDTO user = new UserDTO(
                "123",
                "John",
                "Doe",
                "john.doe@example.com",
                LocalDate.of(1990, 1, 1),
                "123 Street",
                "3001234567",
                (byte) 1,
                BigDecimal.valueOf(5000)
        );

        Mockito.when(userHandler.save(Mockito.any()))
                .thenReturn(ServerResponse.created(URI.create("/api/v1/users"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(user), UserDTO.class));

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.email").isEqualTo("john.doe@example.com");

        Mockito.verify(userHandler).save(Mockito.any());
    }
}
