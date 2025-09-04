package co.com.pragma.model.user.ports;

import co.com.pragma.model.user.entities.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserRepositoryPort {
    Mono<User> saveUser(User user);

    Mono<Boolean> existsByIdUser(Long idUser);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByIdNumber(String idNumber);

    Flux<User> findAll();
}
