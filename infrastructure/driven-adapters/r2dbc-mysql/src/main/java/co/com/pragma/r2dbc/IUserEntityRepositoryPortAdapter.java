package co.com.pragma.r2dbc;

import co.com.pragma.model.user.entities.User;
import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.ports.IUserRepositoryPort;
import co.com.pragma.r2dbc.entities.UserEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
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
        return save(user)
                .onErrorMap(DuplicateKeyException.class, e -> {
                    if (e.getMessage() != null && e.getMessage().contains("user.email")) {
                        return new UserAlreadyExistsException(user.getEmail(), true);
                    }
                    return e;
                });
    }

    @Override
    public Mono<Boolean> existsByIdUserAndEmail(String idUser, String email) {
        return repository.existsByIdUserAndEmail(idUser, email);
    }
}
