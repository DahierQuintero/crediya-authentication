package co.com.pragma.model.user.ports;

import co.com.pragma.model.user.entities.User;
import reactor.core.publisher.Mono;

public interface IUserRepositoryPort {
    Mono<User> saveUser(User user);
    Mono<Boolean> existsByIdUserAndEmail(String idNumber, String email);
}
