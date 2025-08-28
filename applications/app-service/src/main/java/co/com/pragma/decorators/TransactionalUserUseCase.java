package co.com.pragma.decorators;

import co.com.pragma.model.user.entities.User;
import co.com.pragma.usecase.user.IUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TransactionalUserUseCase implements IUserUseCase {
    private final IUserUseCase delegate;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Mono<User> save(User user) {
        return transactionalOperator.transactional(
                delegate.save(user)
        );
    }
}