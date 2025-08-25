package co.com.pragma.model.apiuser.gateways;

import co.com.pragma.model.apiuser.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> saveUser(User user);
    Mono<Boolean> existsByIdUserAndEmail(String idNumber, String email);
}
