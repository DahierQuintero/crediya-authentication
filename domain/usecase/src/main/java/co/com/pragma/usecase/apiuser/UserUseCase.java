package co.com.pragma.usecase.apiuser;

import co.com.pragma.model.apiuser.User;
import co.com.pragma.model.apiuser.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;

    public Mono<User> save(User user) {

        return userRepository.existsByIdUserAndEmail(user.getIdUser(), user.getEmail())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new Exception("User already exists")))
                .then(userRepository.saveUser(user));
    }

}
