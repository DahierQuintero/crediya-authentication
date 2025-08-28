package co.com.pragma.r2dbc;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepositoryPort;
import co.com.pragma.r2dbc.entities.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserEntityRepositoryPortAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        UserEntityRepository
> implements UserRepositoryPort {
    public UserEntityRepositoryPortAdapter(UserEntityRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class));
    }

    @Override
    public Mono<User> saveUser(User user) {
        return save(user);
    }

    @Override
    public Mono<Boolean> existsByIdUserAndEmail(String idUser, String email) {
        return repository.existsByIdUserAndEmail(idUser, email);
    }
}
