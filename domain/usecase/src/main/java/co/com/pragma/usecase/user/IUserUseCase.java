package co.com.pragma.usecase.user;

import co.com.pragma.model.user.entities.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserUseCase {
    Mono<User> save(User user);

    Flux<User> findAll();
}
