package co.com.pragma.r2dbc;

import co.com.pragma.model.user.entities.User;
import co.com.pragma.model.user.ports.IUserRepositoryPort;
import co.com.pragma.r2dbc.entities.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class IUserEntityRepositoryPortAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        UserEntityRepository
        > implements IUserRepositoryPort {
    public IUserEntityRepositoryPortAdapter(UserEntityRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class));
    }

    @Override
    public Mono<User> saveUser(User user) {
        return save(user);
    }

    @Override
    public Mono<Boolean> existsByIdUser(String idUser) {
        return repository.existsByIdUser(idUser);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Mono<Boolean> existsByIdNumber(String idNumber) {
        return repository.existsByIdNumber(idNumber);
    }
}
